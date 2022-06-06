package technobot.commands.starboard;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import technobot.TechnoBot;
import technobot.commands.Category;
import technobot.commands.Command;
import technobot.data.GuildData;
import technobot.handlers.StarboardHandler;
import technobot.util.embeds.EmbedUtils;

/**
 * Admin command that sets up and modifies the starboard.
 *
 * @author TechnoVision
 */
public class StarboardCommand extends Command {

    public StarboardCommand(TechnoBot bot) {
        super(bot);
        this.name = "starboard";
        this.description = "Setup and modify the starboard config.";
        this.category = Category.STARBOARD;
        this.permission = Permission.MANAGE_SERVER;
        this.subCommands.add(new SubcommandData("create", "Sets a channel to become the starboard.")
                .addOption(OptionType.CHANNEL, "channel", "The channel to set as the starboard", true));
        this.subCommands.add(new SubcommandData("limit", "Sets the star requirement for messages to post to the starboard.")
                .addOptions(new OptionData(OptionType.INTEGER, "stars", "The star limit").setMinValue(1).setMaxValue(25)));
        this.subCommands.add(new SubcommandData("blacklist", "Blocks a channel from having their messages starred.")
                .addOption(OptionType.CHANNEL, "channel", "The channel to blacklist"));
        this.subCommands.add(new SubcommandData("unblacklist", "Removes a channel from the starboard blacklist.")
                .addOption(OptionType.CHANNEL, "channel", "The channel to unblacklist"));
        this.subCommands.add(new SubcommandData("lock", "Toggles a temporary lock on the entire starboard."));
        this.subCommands.add(new SubcommandData("jump", "Toggles a link to the source message for each starboard entry."));
        this.subCommands.add(new SubcommandData("nsfw", "Toggles the ability to star messages in NSFW channels."));
        this.subCommands.add(new SubcommandData("self", "Toggles the ability to star your own messages."));
        this.subCommands.add(new SubcommandData("config", "Display the current starboard config."));
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        OptionMapping channelOption = event.getOption("channel");
        StarboardHandler starboardHandler = GuildData.get(event.getGuild()).starboardHandler;

        String text = null;
        switch(event.getSubcommandName()) {
            case "create" -> {
                // Set starboard channel
                try {
                    long channel = channelOption.getAsTextChannel().getIdLong();
                    starboardHandler.setChannel(channel);
                    text = EmbedUtils.BLUE_TICK + " Set the starboard channel to <#" + channel + ">";
                } catch (NullPointerException e) {
                    text = "You can only set a text channel as the starboard!";
                    event.getHook().sendMessageEmbeds(EmbedUtils.createError(text)).queue();
                    return;
                }
            }
            case "limit" -> {
                OptionMapping limitOption = event.getOption("stars");
                if (limitOption != null) {
                    int limit = limitOption.getAsInt();
                    starboardHandler.setStarLimit(limit);
                    text = EmbedUtils.BLUE_TICK + " Messages now require " + limit + " stars to show up on the starboard!";
                } else {
                    starboardHandler.setStarLimit(3);
                    text = EmbedUtils.BLUE_TICK + " Reset the star limit to default!";
                }
            }
            case "blacklist" -> {
                if (channelOption != null) {
                    try {
                        long channel = channelOption.getAsTextChannel().getIdLong();
                        starboardHandler.blacklistChannel(channel);
                        text = EmbedUtils.BLUE_TICK + " Starboard will now ignore reactions from <#" + channel + ">";
                    } catch (NullPointerException e) {
                        text = "You can only blacklist a text channel!";
                        event.getHook().sendMessageEmbeds(EmbedUtils.createError(text)).queue();
                        return;
                    }
                } else {
                    starboardHandler.clearBlacklist();
                    text = EmbedUtils.BLUE_TICK + " Reset the starboard blacklist!";
                }
            }
            case "unblacklist" -> {
                if (channelOption != null) {
                    try {
                        long channel = channelOption.getAsTextChannel().getIdLong();
                        starboardHandler.unBlacklistChannel(channel);
                        text = EmbedUtils.BLUE_X + " Removed <#" + channel + "> from the Starboard blacklist!";
                    } catch (NullPointerException e) {
                        text = "You can only unblacklist a text channel!";
                        event.getHook().sendMessageEmbeds(EmbedUtils.createError(text)).queue();
                        return;
                    }
                } else {
                    starboardHandler.clearBlacklist();
                    text = EmbedUtils.BLUE_TICK + " Reset the starboard blacklist!";
                }
            }
            case "lock" -> {
                boolean isLocked = starboardHandler.toggleLock();
                if (isLocked) text = EmbedUtils.BLUE_TICK + " Locked the starboard!";
                else text = EmbedUtils.BLUE_X + " Unlocked the starboard!";
            }
            case "jump" -> {
                boolean hasJumpLink = starboardHandler.toggleJump();
                if (hasJumpLink) text = EmbedUtils.BLUE_TICK + " Enabled jump links on Starboard posts!";
                else text = EmbedUtils.BLUE_X + " Disabled jump links on Starboard posts!";
            }
            case "nsfw" -> {
                boolean isNSFW = starboardHandler.toggleNSFW();
                if (isNSFW) text = EmbedUtils.BLUE_TICK + " Users can now star messages in NSFW channels!";
                else text = EmbedUtils.BLUE_X + " Users can no longer star messages in NSFW channels!";
            }
            case "self" -> {
                boolean canSelfStar = starboardHandler.toggleSelfStar();
                if (canSelfStar) text = EmbedUtils.BLUE_TICK + " Users can now star their own messages!";
                else text = EmbedUtils.BLUE_X + " Users can no longer star their own messages!";
            }
            case "config" -> {
                text = "";
                text += "**Threshold:** " + starboardHandler.getStarLimit();
                if (starboardHandler.getChannel() != null) {
                    text += "\n**Channel:** <#" + starboardHandler.getChannel() + ">";
                } else {
                    text += "\n**Channel:** none";
                }
                text += "\n**Allow NSFW:** " + starboardHandler.isNSFW();
                text += "\n**Count Self Stars:** " + starboardHandler.canSelfStar();
                text += "\n**Show Jump URL:** " + starboardHandler.hasJumpLink();
                text += "\n**Locked:** " + starboardHandler.isLocked();
                text += "\n**Blacklisted Channels:** ";
                for (Long channel : starboardHandler.getBlacklist()) {
                    text += "<#" + channel + "> ";
                }
                event.getHook().sendMessage(text).queue();
                return;
            }
        }
        event.getHook().sendMessageEmbeds(EmbedUtils.createDefault(text)).queue();
    }
}
