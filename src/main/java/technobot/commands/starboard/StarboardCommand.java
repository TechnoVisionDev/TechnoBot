package technobot.commands.starboard;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
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

import java.util.stream.Collectors;

import static technobot.util.Localization.get;

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
                .addOptions(new OptionData(OptionType.CHANNEL, "channel", "The channel to set as the starboard", true)
                        .setChannelTypes(ChannelType.TEXT, ChannelType.NEWS)));
        this.subCommands.add(new SubcommandData("limit", "Sets the star requirement for messages to post to the starboard.")
                .addOptions(new OptionData(OptionType.INTEGER, "stars", "The star limit").setMinValue(1).setMaxValue(25)));
        this.subCommands.add(new SubcommandData("blacklist", "Blocks a channel from having their messages starred.")
                .addOptions(new OptionData(OptionType.CHANNEL, "channel", "The channel to blacklist")
                        .setChannelTypes(ChannelType.TEXT, ChannelType.NEWS)));
        this.subCommands.add(new SubcommandData("unblacklist", "Removes a channel from the starboard blacklist.")
                .addOptions(new OptionData(OptionType.CHANNEL, "channel", "The channel to unblacklist")
                        .setChannelTypes(ChannelType.TEXT, ChannelType.NEWS)));
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
        switch (event.getSubcommandName()) {
            case "create" -> {
                // Set starboard channel
                long channel = channelOption.getAsGuildChannel().getIdLong();
                starboardHandler.setChannel(channel);
                text = get(s -> s.starboard.create, channel);
            }
            case "limit" -> {
                OptionMapping limitOption = event.getOption("stars");
                if (limitOption != null) {
                    int limit = limitOption.getAsInt();
                    starboardHandler.setStarLimit(limit);
                    text = get(s -> s.starboard.limit.set, limit);
                } else {
                    starboardHandler.setStarLimit(3);
                    text = get(s -> s.starboard.limit.reset);
                }
            }
            case "blacklist" -> {
                if (channelOption != null) {
                    long channel = channelOption.getAsGuildChannel().getIdLong();
                    starboardHandler.blacklistChannel(channel);
                    text = get(s -> s.starboard.blacklist.add, channel);
                } else {
                    starboardHandler.clearBlacklist();
                    text = get(s -> s.starboard.blacklist.reset);
                }
            }
            case "unblacklist" -> {
                if (channelOption != null) {
                    long channel = channelOption.getAsGuildChannel().getIdLong();
                    starboardHandler.unBlacklistChannel(channel);
                    text = get(s -> s.starboard.unblacklist.remove, channel);
                } else {
                    starboardHandler.clearBlacklist();
                    text = get(s -> s.starboard.unblacklist.reset);
                }
            }
            case "lock" -> {
                boolean isLocked = starboardHandler.toggleLock();
                if (isLocked) text = get(s -> s.starboard.lock.lock);
                else text = get(s -> s.starboard.lock.unlock);
            }
            case "jump" -> {
                boolean hasJumpLink = starboardHandler.toggleJump();
                if (hasJumpLink) text = get(s -> s.starboard.jump.enable);
                else text = get(s -> s.starboard.jump.disable);
            }
            case "nsfw" -> {
                boolean isNSFW = starboardHandler.toggleNSFW();
                if (isNSFW) text = get(s -> s.starboard.nsfw.enable);
                else text = get(s -> s.starboard.nsfw.disable);
            }
            case "self" -> {
                boolean canSelfStar = starboardHandler.toggleSelfStar();
                if (canSelfStar) text = get(s -> s.starboard.self.enable);
                else text = get(s -> s.starboard.self.disable);
            }
            case "config" -> {
                text = "";
                text += get(s -> s.starboard.config.threshold, starboardHandler.getStarLimit());
                text += "\n" + get(
                        s -> s.starboard.config.channel,
                        starboardHandler.getChannel() != null
                                ? "<#" + starboardHandler.getChannel() + ">"
                                : "none"
                );
                text += "\n" + get(s -> s.starboard.config.allowNsfw, starboardHandler.isNSFW());
                text += "\n" + get(s -> s.starboard.config.allowSelf, starboardHandler.canSelfStar());
                text += "\n" + get(s -> s.starboard.config.showJumpLinks, starboardHandler.hasJumpLink());
                text += "\n" + get(s -> s.starboard.config.locked, starboardHandler.isLocked());

                text += "\n" + get(
                        s -> s.starboard.config.blacklistedChannels,
                        starboardHandler.getBlacklist().stream()
                                .map(channel -> "<#" + channel + "> ")
                                .collect(Collectors.joining(" "))
                );

                event.getHook().sendMessage(text).queue();
                return;
            }
        }
        event.getHook().sendMessageEmbeds(EmbedUtils.createDefault(text)).queue();
    }
}
