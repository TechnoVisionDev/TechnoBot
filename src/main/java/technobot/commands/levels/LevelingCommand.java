package technobot.commands.levels;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageAction;
import org.bson.conversions.Bson;
import technobot.TechnoBot;
import technobot.commands.Category;
import technobot.commands.Command;
import technobot.data.GuildData;
import technobot.data.cache.Config;
import technobot.listeners.ButtonListener;
import technobot.util.embeds.EmbedUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import static technobot.util.localization.Localization.get;

/**
 * Command that displays and modifies leveling config.
 *
 * @author TechnoVision
 */
public class LevelingCommand extends Command {

    public LevelingCommand(TechnoBot bot) {
        super(bot);
        this.name = "leveling";
        this.description = "Modify this server's leveling config.";
        this.category = Category.LEVELS;
        this.permission = Permission.MANAGE_SERVER;
        this.subCommands.add(new SubcommandData("channel", "Sets a channel to send level-up messages to.")
                .addOptions(new OptionData(OptionType.CHANNEL, "channel", "The channel to send level-up messages to")
                        .setChannelTypes(ChannelType.TEXT, ChannelType.NEWS)));
        this.subCommands.add(new SubcommandData("message", "Sets a custom level-up message.")
                .addOption(OptionType.STRING, "text", "Custom level-up message. Use {user} for their name and {lvl} for the level"));
        this.subCommands.add(new SubcommandData("dm", "Makes the bot dm the member their level-up message."));
        this.subCommands.add(new SubcommandData("mod", "Sets what a level has to be evenly divisible by to be announced.")
                .addOptions(new OptionData(OptionType.INTEGER, "mod", "The level mod value. For example, mod 5 would announce at every 5th level").setMinValue(0).setMaxValue(100)));
        this.subCommands.add(new SubcommandData("server-background", "Sets the default rankcard background for this server.")
                .addOption(OptionType.STRING, "url", "URL to image to set as rankcard background"));
        this.subCommands.add(new SubcommandData("mute", "Removes the level-up message entirely."));
        this.subCommands.add(new SubcommandData("reward", "Adds a role reward for reaching a certain level.")
                .addOptions(new OptionData(OptionType.INTEGER, "level", "The level required to gain this reward", true).setMinValue(1).setMaxValue(1000))
                .addOption(OptionType.ROLE, "role", "The role to add as a reward", true));
        this.subCommands.add(new SubcommandData("config", "Displays all current leveling settings for this server."));
        this.subCommands.add(new SubcommandData("reset", "Resets leveling data for a user.")
                .addOption(OptionType.USER, "user", "The user to reset progress for", true));
        this.subCommands.add(new SubcommandData("reset-all", "Resets all leveling data for this server."));
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        Bson filter = Filters.eq("guild", event.getGuild().getIdLong());
        GuildData data = GuildData.get(event.getGuild());
        Config config = data.configHandler.getConfig();

        String text = "";
        Bson update = null;
        switch (event.getSubcommandName()) {
            case "channel" -> {
                OptionMapping channelOption = event.getOption("channel");
                if (channelOption != null) {
                    long channel = channelOption.getAsGuildChannel().getIdLong();
                    config.setLevelingChannel(channel);
                    update = Updates.set("leveling_channel", channel);
                    text = get(s -> s.levels().leveling().channel().specific(), channel);
                } else {
                    config.setLevelingChannel(null);
                    update = Updates.unset("leveling_channel");
                    text = get(s -> s.levels().leveling().channel().specific());
                }
            }
            case "message" -> {
                OptionMapping messageOption = event.getOption("text");
                if (messageOption != null) {
                    String msg = messageOption.getAsString();
                    config.setLevelingMessage(msg);
                    update = Updates.set("leveling_message", msg);
                    text = get(s -> s.levels().leveling().message().set());
                } else {
                    config.setLevelingMessage(null);
                    update = Updates.unset("leveling_message");
                    text = get(s -> s.levels().leveling().message().reset());
                }
            }
            case "dm" -> {
                boolean isDM = !config.isLevelingDM();
                config.setLevelingDM(isDM);
                update = Updates.set("leveling_dm", isDM);
                if (isDM) {
                    text = get(s -> s.levels().leveling().dm().enable());
                } else {
                    text = get(s -> s.levels().leveling().dm().disable());
                }
            }
            case "mod" -> {
                OptionMapping modOption = event.getOption("mod");
                int mod = 1;
                if (modOption != null) {
                    mod = modOption.getAsInt();
                    text = get(s -> s.levels().leveling().mod().set(), mod);
                } else {
                    text = get(s -> s.levels().leveling().mod().reset());
                }
                config.setLevelingMod(mod);
                update = Updates.set("leveling_mod", mod);
            }
            case "server-background" -> {
                try {
                    OptionMapping option = event.getOption("url");
                    if (option != null) {
                        String urlOption = option.getAsString();
                        URL url = new URL(urlOption);
                        BufferedImage test = ImageIO.read(url);
                        test.getWidth();
                        config.setLevelingBackground(urlOption);
                        update = Updates.set("leveling_background", urlOption);
                        text = get(s -> s.levels().leveling().serverBackground().set());
                    } else {
                        config.setLevelingBackground(null);
                        update = Updates.unset("leveling_background");
                        text = get(s -> s.levels().leveling().serverBackground().reset());
                    }
                } catch (IOException | NullPointerException | OutOfMemoryError e2) {
                    event.getHook().sendMessageEmbeds(EmbedUtils.createError(get(s -> s.levels().leveling().serverBackground().failure()))).queue();
                    return;
                }
            }
            case "mute" -> {
                boolean isMute = !config.isLevelingMute();
                config.setLevelingMute(isMute);
                update = Updates.set("leveling_mute", isMute);
                if (isMute) {
                    text = get(s -> s.levels().leveling().mute().disable());
                } else {
                    text = get(s -> s.levels().leveling().mute().enable());
                }
            }
            case "reward" -> {
                int level = event.getOption("level").getAsInt();
                String reward = event.getOption("role").getAsRole().getId();

                // Check if role can be given by TechnoBot
                Role role = event.getGuild().getRoleById(reward);
                int botPos = event.getGuild().getBotRole().getPosition();
                if (role == null || role.getPosition() >= botPos || role.isManaged()) {
                    event.getHook().sendMessageEmbeds(EmbedUtils.createError(get(s -> s.levels().leveling().reward().failure()))).queue();
                    return;
                }
                Integer x = config.getRewards().get(reward);
                if (x != null && x == level) {
                    config.removeReward(reward);
                    update = Updates.unset("rewards." + reward);
                    text = get(s -> s.levels().leveling().reward().remove(), role);
                } else {
                    config.addReward(level, reward);
                    update = Updates.set("rewards." + reward, level);
                    text = get(s -> s.levels().leveling().reward().add(), role);
                }
            }
            case "config" -> {
                text = configToString(config);
                event.getHook().sendMessage(text).queue();
                return;
            }
            case "reset" -> {
                User user = event.getOption("user").getAsUser();
                data.levelingHandler.resetProfile(user.getIdLong());
                text = get(s -> s.levels().leveling().reset(), user.getName());
                event.getHook().sendMessageEmbeds(EmbedUtils.createDefault(text)).queue();
                return;
            }
            case "reset-all" -> {
                text = get(s -> s.levels().leveling().resetAll());
                WebhookMessageAction<Message> action = event.getHook().sendMessageEmbeds(EmbedUtils.createDefault(text));
                ButtonListener.sendResetMenu(event.getUser().getId(), "Leveling", action);
                return;
            }
        }
        bot.database.config.updateOne(filter, update);
        event.getHook().sendMessageEmbeds(EmbedUtils.createDefault(text)).queue();
    }

    /**
     * Converts the leveling config into a readable string
     *
     * @param config an instance of the guild config.
     * @return Stringified config (leveling only).
     */
    private String configToString(Config config) {
        String text = "";
        text += get(
                s -> s.levels().leveling().config().channel(),
                config.getLevelingChannel() == null ? "none" : config.getLevelingChannel()
        ) + "\n";

        text += get(
                s -> s.levels().leveling().config().modulus(),
                config.getLevelingMod()
        ) + "\n";


        text += get(
                s -> s.levels().leveling().config().muted(),
                config.isLevelingMute()
        ) + "\n";

        text += get(
                s -> s.levels().leveling().config().dms(),
                config.isLevelingDM()
        ) + "\n";

        text += get(
                s -> s.levels().leveling().config().message(),
                config.getLevelingMessage() == null ? "none" : config.getLevelingMessage()
        ) + "\n";

        text += get(
                s -> s.levels().leveling().config().background(),
                config.getLevelingBackground() == null ? "none" : config.getLevelingBackground()
        ) + "\n";

        return text;
    }
}
