package technobot.commands.levels;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.ErrorResponse;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

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
        Config config = data.config;

        String text = "";
        Bson update = null;
        switch(event.getSubcommandName()) {
            case "channel" -> {
                OptionMapping channelOption = event.getOption("channel");
                if (channelOption != null) {
                    long channel = channelOption.getAsGuildChannel().getIdLong();
                    config.setLevelingChannel(channel);
                    update = Updates.set("leveling_channel", channel);
                    text = EmbedUtils.BLUE_TICK + " Leveling messages will now only display in <#" + channel + ">.";
                } else {
                    config.setLevelingChannel(null);
                    update = Updates.unset("leveling_channel");
                    text = EmbedUtils.BLUE_TICK + " Leveling messages will now display in the channel the user levels up in.";
                }
            }
            case "message" -> {
                OptionMapping messageOption = event.getOption("text");
                if (messageOption != null) {
                    String msg = messageOption.getAsString();
                    config.setLevelingMessage(msg);
                    update = Updates.set("leveling_message", msg);
                    text = EmbedUtils.BLUE_TICK + " Successfully set a custom level-up message.";
                } else {
                    config.setLevelingMessage(null);
                    update = Updates.unset("leveling_message");
                    text = EmbedUtils.BLUE_TICK + " Reset level-up message to default.";
                }
            }
            case "dm" -> {
                boolean isDM = !config.isLevelingDM();
                config.setLevelingDM(isDM);
                update = Updates.set("leveling_dm", isDM);
                if (isDM) {
                    text = EmbedUtils.BLUE_TICK + " Level-up messages will now be sent through DMs.";
                } else {
                    text = EmbedUtils.BLUE_X + " Level-up messages will no longer be sent through DMs.";
                }
            }
            case "mod" -> {
                OptionMapping modOption = event.getOption("mod");
                int mod = 1;
                if (modOption != null) {
                    mod = modOption.getAsInt();
                    text = EmbedUtils.BLUE_TICK + " Leveling messages will now only display every **" + mod + "** levels.";
                } else {
                    text = EmbedUtils.BLUE_TICK + " Leveling messages have been reset to display every level.";
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
                        text = EmbedUtils.BLUE_TICK + " Successfully updated the server rankcard background!";
                    } else {
                        config.setLevelingBackground(null);
                        update = Updates.unset("leveling_background");
                        text = EmbedUtils.BLUE_TICK + " Reset the server rankcard background to default image!";
                    }
                } catch (IOException | NullPointerException | OutOfMemoryError e2) {
                    event.getHook().sendMessageEmbeds(EmbedUtils.createError("Unable to set that URL as the server rankcard background.")).queue();
                    return;
                }
            }
            case "mute" -> {
                boolean isMute = !config.isLevelingMute();
                config.setLevelingMute(isMute);
                update = Updates.set("leveling_mute", isMute);
                if (isMute) {
                    text = EmbedUtils.BLUE_X + " Leveling messages have been muted and will not be displayed!";
                } else {
                    text = EmbedUtils.BLUE_TICK + " Leveling messages will now be displayed!";
                }
            }
            case "reward" -> {
                int level = event.getOption("level").getAsInt();
                String reward = event.getOption("role").getAsRole().getId();

                // Check if role can be given by TechnoBot
                Role role = event.getGuild().getRoleById(reward);
                int botPos = event.getGuild().getBotRole().getPosition();
                if (role == null || role.getPosition() >= botPos || role.isManaged()) {
                    event.getHook().sendMessageEmbeds(EmbedUtils.createError("I cannot reward that role! Please check my permissions and role position.")).queue();
                    return;
                }
                Integer x = config.getRewards().get(reward);
                if (x != null && x == level) {
                    config.removeReward(reward);
                    update = Updates.unset("rewards."+reward);
                    text = EmbedUtils.BLUE_TICK + " Successfully removed the <@&"+reward+"> reward role.";
                } else {
                    config.addReward(level, reward);
                    update = Updates.set("rewards."+reward, level);
                    text = EmbedUtils.BLUE_TICK + " Users will now receive the <@&"+reward+"> role at level **"+level+"**.";
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
                text = EmbedUtils.BLUE_TICK + " All leveling data was reset for **" + user.getName() + "**.";
                event.getHook().sendMessageEmbeds(EmbedUtils.createDefault(text)).queue();
                return;
            }
            case "reset-all" -> {
                long userID = event.getUser().getIdLong();
                String uuid = userID + ":" + UUID.randomUUID();
                List<Button> components = ButtonListener.getResetButtons(uuid, "Leveling");
                ButtonListener.buttons.put(uuid, components);
                text = "Would you like to reset the leveling system?\nThis will delete **ALL** data!";
                event.getHook().sendMessageEmbeds(EmbedUtils.createDefault(text)).addActionRow(components).queue(interactionHook -> {
                    // Timer task to disable buttons and clear cache after 3 minutes
                    Runnable task = () -> {
                        List<Button> actionRow = ButtonListener.buttons.get(uuid);
                        for (int i = 0; i < actionRow.size(); i++) {
                            actionRow.set(i, actionRow.get(i).asDisabled());
                        }
                        interactionHook.editMessageComponents(ActionRow.of(actionRow)).queue(null, new ErrorHandler().ignore(ErrorResponse.UNKNOWN_MESSAGE));
                        ButtonListener.buttons.remove(uuid);
                    };
                    ButtonListener.executor.schedule(task, 3, TimeUnit.MINUTES);
                });
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
        if (config.getLevelingChannel() == null) {
            text += "**Level-Up Channel:** none\n";
        } else {
            text += "**Level-Up Channel:** <#" + config.getLevelingChannel() + ">\n";
        }
        text += "**Leveling Modulus:** " + config.getLevelingMod() + "\n";
        text += "**Is Muted:** " + config.isLevelingMute() + "\n";
        text += "**Level-Up DMs:** " + config.isLevelingDM() + "\n";
        if (config.getLevelingMessage() == null) {
            text += "**Custom Message:** none\n";
        } else {
            text += "**Custom Message:** '" + config.getLevelingMessage() + "'\n";
        }
        if (config.getLevelingBackground() == null) {
            text += "**Custom Background:** none\n";
        } else {
            text += "**Custom Background:** " + config.getLevelingBackground() + "\n";
        }
        return text;
    }
}
