package technobot.commands.levels;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.bson.conversions.Bson;
import technobot.TechnoBot;
import technobot.commands.Category;
import technobot.commands.Command;
import technobot.data.GuildData;
import technobot.data.cache.Leveling;
import technobot.handlers.LevelingHandler;
import technobot.util.embeds.EmbedUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Command that allows you to customize your rank card.
 *
 * @author TechnoVision
 */
public class RankcardCommand extends Command {

    public RankcardCommand(TechnoBot bot) {
        super(bot);
        this.name = "rankcard";
        this.description = "Customize your rank card.";
        this.category = Category.LEVELS;
        this.subCommands.add(new SubcommandData("background", "Sets the background of your rank card.")
                .addOption(OptionType.STRING, "url", "URL to a valid image file", true));
        this.subCommands.add(new SubcommandData("color", "Sets the base color of your rank card.")
                .addOption(OptionType.STRING, "color", "A hex color code", true));
        this.subCommands.add(new SubcommandData("accent", "Sets the accent color of your rank card.")
                .addOption(OptionType.STRING, "color", "A hex color code", true));
        this.subCommands.add(new SubcommandData("opacity", "Sets the base color of your rank card.")
                .addOptions(new OptionData(OptionType.INTEGER, "percent", "the percentage of opacity from 0% - 100%", true).setMinValue(0).setMaxValue(100)));
        this.subCommands.add(new SubcommandData("reset", "Resets customization to default settings."));
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        Guild guild = event.getGuild();
        User user = event.getUser();
        LevelingHandler levelingHandler = GuildData.get(guild).levelingHandler;

        // Get leveling profile
        Leveling profile = levelingHandler.getProfile(event.getUser().getIdLong());
        if (profile == null) {
            String text = "You do not have a rank yet! Send some messages first.";
            event.getHook().sendMessageEmbeds(EmbedUtils.createError(text)).queue();
            return;
        }

        Bson update = null;
        String text = null;
        Bson filter = Filters.and(Filters.eq("guild", guild.getIdLong()), Filters.eq("user", user.getIdLong()));
        switch(event.getSubcommandName()) {
            case "background" -> {
                try {
                    String urlOption = event.getOption("url").getAsString();
                    URL url = new URL(urlOption);
                    BufferedImage test = ImageIO.read(url);
                    test.getWidth();
                    update = Updates.set("background", urlOption);
                    text = "Successfully updated your background!";
                } catch (IOException | NullPointerException | OutOfMemoryError e2) {
                    event.getHook().sendMessageEmbeds(EmbedUtils.createError("Unable to set that URL as your background.")).queue();
                    return;
                }
            }
            case "color" -> {
                try {
                    String color = event.getOption("color").getAsString();
                    if (!color.startsWith("#")) {
                        color = "#" + color;
                    }
                    Color.decode(color);
                    update = Updates.set("color", color);
                    text = "Successfully updated your color to **" + color + "**";
                } catch (NumberFormatException e) {
                    event.getHook().sendMessageEmbeds(EmbedUtils.createError("That is not a valid hex code, please use a valid color.")).queue();
                    return;
                }
            }
            case "accent" -> {
                try {
                    String accent = event.getOption("color").getAsString();
                    if (!accent.startsWith("#")) {
                        accent = "#" + accent;
                    }
                    Color.decode(accent);
                    update = Updates.set("accent", accent);
                    text = "Successfully updated your accent color to **" + accent + "**";
                } catch (NumberFormatException e) {
                    event.getHook().sendMessageEmbeds(EmbedUtils.createError("That is not a valid hex code, please use a valid color.")).queue();
                    return;
                }
            }
            case "opacity" -> {
                int opacity = event.getOption("percent").getAsInt();
                update = Updates.set("opacity", opacity);
                text = "Successfully updated your opacity to **" + opacity + "%**";
            }
            case "reset" -> {
                List<Bson> updates = new ArrayList<>();
                updates.add(Updates.set("opacity", 50));
                updates.add(Updates.set("color", "#8394eb"));
                updates.add(Updates.set("accent", "#FFFFFF"));
                updates.add(Updates.set("background", ""));
                bot.database.leveling.updateOne(filter, updates);
                event.getHook().sendMessageEmbeds(EmbedUtils.createDefault("Successfully reset your rank card to default settings!")).queue();
                return;
            }
        }
        bot.database.leveling.updateOne(filter, update);
        event.getHook().sendMessageEmbeds(EmbedUtils.createDefault(EmbedUtils.GREEN_TICK + " " + text)).queue();
    }
}
