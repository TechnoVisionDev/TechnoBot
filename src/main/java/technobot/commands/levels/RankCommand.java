package technobot.commands.levels;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import technobot.TechnoBot;
import technobot.commands.Category;
import technobot.commands.Command;
import technobot.data.GuildData;
import technobot.data.cache.Leveling;
import technobot.handlers.LevelingHandler;
import technobot.util.embeds.EmbedUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * Command that renders and displays a user's rank card.
 *
 * @author TechnoVision
 */
public class RankCommand extends Command {

    /** Path to image resources for rankcard. */
    public static final String PATH = "assets/rankcard/";

    /** Suffix types for XP formatting */
    private final NavigableMap<Long, String> suffixes = new TreeMap<>();

    public RankCommand(TechnoBot bot) {
        super(bot);
        this.name = "rank";
        this.description = "Displays your level and server rank.";
        this.category = Category.LEVELS;
        this.args.add(new OptionData(OptionType.USER, "user", "Display this user's server rank"));

        suffixes.put(1_000L, "k");
        suffixes.put(1_000_000L, "m");
        suffixes.put(1_000_000_000L, "b");
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        OptionMapping userOption = event.getOption("user");
        User user = userOption != null ? userOption.getAsUser() : event.getUser();

        // Check if profile exists
        LevelingHandler levelingHandler = GuildData.get(event.getGuild()).levelingHandler;
        Leveling profile = levelingHandler.getProfile(user.getIdLong());
        if (profile == null) {
            String text = "You do not have a rank yet! Send some messages first.";
            event.getHook().sendMessageEmbeds(EmbedUtils.createError(text)).queue();
            return;
        }

        // Display rank card
        int level = profile.getLevel();
        String xp = format(profile.getXp());
        String max = format(levelingHandler.calculateLevelGoal(level));
        float percent = ((float) (profile.getXp() * 100) / (float) (levelingHandler.calculateLevelGoal(level)));
        String percentStr = String.valueOf((int) percent);

        try {
            // Get Graphics
            ClassLoader cl = getClass().getClassLoader();
            BufferedImage base = ImageIO.read(cl.getResource(PATH + "base.png"));
            BufferedImage outline = ImageIO.read(cl.getResource(PATH + "outline.png"));
            Graphics2D g = (Graphics2D) base.getGraphics();
            g.addRenderingHints(new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY));
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT);

            // Add Background
            BufferedImage background;
            if (profile.getBackground().isEmpty()) {
                background = ImageIO.read(cl.getResource(PATH + "background.png"));
            } else {
                background = ImageIO.read(new URL(profile.getBackground()));
            }
            BufferedImage rectBuffer = new BufferedImage(base.getWidth(), base.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = rectBuffer.createGraphics();
            g2.setClip(new Rectangle2D.Float(0, 0, base.getWidth(), base.getHeight()));
            int x = base.getWidth() - background.getWidth();
            int y = base.getHeight() - background.getHeight();
            if (background.getWidth() >= 934 && background.getHeight() >= 282) {
                g2.drawImage(background, x / 2, y / 2, null);
            } else {
                g2.drawImage(background, 0, 0, base.getWidth(), base.getHeight(), null);
            }
            g2.dispose();
            g.drawImage(rectBuffer, 0, 0, base.getWidth(), base.getHeight(), null);

            // Add Outline
            double opacity = profile.getOpacity();
            AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) opacity);
            g.setComposite(ac);
            g.drawImage(outline, 0, 0, null);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));

            // Text
            g.setStroke(new BasicStroke(3));
            g.setColor(Color.decode(profile.getAccent()));
            g.setFont(new Font("Helvetica", Font.PLAIN, 52));
            g.drawLine(300, 140, 870, 140);
            g.drawString(user.getName(), 300, 110);
            g.setFont(new Font("Arial", Font.PLAIN, 35));

            int rank = levelingHandler.getRank(user.getIdLong());
            int xModifier = 0;
            int temp = Math.min(rank, 10000);
            while (temp >= 10) {
                temp /= 10;
                xModifier += 15;
            }

            if (rank == 0) { rank = event.getGuild().getMemberCount(); }
            g.drawString("Rank #" + rank, 740 - xModifier, 110);

            g.drawString("Level " + level, 300, 180);
            g.setFont(new Font("Arial", Font.PLAIN, 25));
            xModifier = 0;
            if (xp.length() > 2) { xModifier += 10; }
            if (xp.length() > 3 || max.length() > 3) { xModifier += 10; }
            if (xp.length() > 4 || max.length() > 4) { xModifier += 10; }
            g.drawString(xp + " / " + max, 775 - xModifier, 180);

            // XP Bar
            g.drawRoundRect(300, 200, 570, 40, 20, 20);
            g.setColor(Color.decode("#101636"));
            g.fillRoundRect(300, 200, 570, 40, 20, 20);
            g.setColor(Color.decode(profile.getColor()));
            g.fillRoundRect(300, 200, (int) (570 * (percent * 0.01)), 40, 20, 20);
            g.setColor(Color.decode(profile.getAccent()));
            g.setFont(new Font("Arial", Font.PLAIN, 30));
            g.drawString(percentStr + "%", 560, 230);

            //Add Avatar
            BufferedImage avatar;
            avatar = getAvatar(user);
            g.setStroke(new BasicStroke(4));
            int width = avatar.getWidth();
            BufferedImage circleBuffer = new BufferedImage(width, width, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g3 = circleBuffer.createGraphics();
            g3.setClip(new Ellipse2D.Float(0, 0, width, width));
            g3.drawImage(avatar, 0, 0, width, width, null);
            g3.dispose();
            g.drawImage(circleBuffer, 55, 38, null);
            g.setColor(Color.decode(profile.getColor()));
            g.drawOval(55, 38, width, width);
            g.dispose();

            //Write and send file
            File rankCard = new File("card.png");
            ImageIO.write(base, "png", rankCard);

            // TODO: TEMPORARY! Minn fix in commit 4f7b413a9c7b96a541eaf269dea95290f529f503
            byte[] bytes = new byte[(int) rankCard.length()];
            FileInputStream fis = new FileInputStream(rankCard);
            fis.read(bytes);
            // END TEMPORARY (normally put 'rankCard' into sendFile())

            event.getHook().sendFile(bytes, "card.png").queue();
        } catch (IOException e) {
            String text = "An error occurred while trying to access that rankcard!";
            event.getHook().sendMessageEmbeds(EmbedUtils.createError(text)).queue();
            e.printStackTrace();
        }
    }

    /**
     * Formats XP values into truncated shorthand.
     *
     * @param value the un-formatted XP value.
     * @return The formatted XP string for rankcard.
     */
    private String format(long value) {
        if (value < 1000) return Long.toString(value);
        Map.Entry<Long, String> e = suffixes.floorEntry(value);
        Long divideBy = e.getKey();
        String suffix = e.getValue();
        long truncated = value / (divideBy / 10);
        boolean hasDecimal = truncated < 100 && (truncated / 10d) != (truncated / 10);
        return hasDecimal ? (truncated / 10d) + suffix : (truncated / 10) + suffix;
    }

    /**
     * Retrieves and edits user avatar into an icon for rankcard.
     *
     * @param user The user whose avatar to use.
     * @return image of edited avatar.
     * @throws IOException avatar image url is invalid.
     */
    private BufferedImage getAvatar(User user) throws IOException {
        try {
            URL url = new URL(user.getAvatarUrl());
            BufferedImage addon = ImageIO.read(url);

            int w = addon.getWidth() + 80;
            int h = addon.getHeight() + 80;

            BufferedImage after = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            AffineTransform at = new AffineTransform();
            at.scale(1.62, 1.62);
            AffineTransformOp scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);

            return scaleOp.filter(addon, after);

        } catch (MalformedURLException e) {
            URL url = new URL(user.getEffectiveAvatarUrl());
            BufferedImage addon = ImageIO.read(url);

            int w = addon.getWidth() - 45;
            int h = addon.getHeight() - 45;

            BufferedImage after = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            AffineTransform at = new AffineTransform();
            at.scale(0.82, 0.82);
            AffineTransformOp scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);

            return scaleOp.filter(addon, after);
        }
    }
}
