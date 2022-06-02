package technobot.commands.starboard;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import technobot.TechnoBot;
import technobot.commands.Category;
import technobot.commands.Command;
import technobot.data.GuildData;
import technobot.handlers.StarboardHandler;
import technobot.util.EmbedColor;
import technobot.util.EmbedUtils;

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
        this.args.add(new OptionData(OptionType.CHANNEL, "channel", "Set this channel to become the starboard."));
        this.args.add(new OptionData(OptionType.STRING, "option", "Toggle a setting on/off.")
                .addChoice("lock", "lock")
                .addChoice("jump", "jump")
                .addChoice("nsfw", "nsfw")
                .addChoice("self", "self"));
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        OptionMapping channelOption = event.getOption("channel");
        OptionMapping settingsOption = event.getOption("option");
        StarboardHandler starboardHandler = GuildData.get(event.getGuild()).starboardHandler;

        if (channelOption != null) {
            // Set starboard channel
            long channel = channelOption.getAsGuildChannel().getIdLong();
            starboardHandler.setChannel(channel);
            String text = EmbedUtils.BLUE_TICK + " Set the starboard channel to <#" + channel + ">";
            event.getHook().sendMessageEmbeds(EmbedUtils.createDefault(text)).queue();

        } else if (settingsOption != null) {
            // Toggle options
            String text = null;
            switch (settingsOption.getAsString().toLowerCase()) {
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
            }
            event.getHook().sendMessageEmbeds(EmbedUtils.createDefault(text)).queue();
        } else {
            String cmd = "`/" + this.name + " ";
            EmbedBuilder embed = new EmbedBuilder()
                    .setColor(EmbedColor.DEFAULT.color)
                    .setTitle(":dizzy: Starboard Settings")
                    .addField(cmd + "<#channel>`", "Sets a channel to become the starboard.", false)
                    .addField(cmd + "limit <stars>`", "Sets the star requirement for messages to post to the starboard.", false)
                    .addField(cmd + "blacklist <#channel>`", "Blocks a channel from having their messages starred.", false)
                    .addField(cmd + "unblacklist <#channel>`", "Removes a channel from the starboard blacklist.", false)
                    .addField(cmd + "lock`", "Toggles a temporary lock on the entire starboard.", false)
                    .addField(cmd + "jump`", "Toggles a link to the source message for each starboard entry.", false)
                    .addField(cmd + "nsfw`", "Toggles the ability to star messages in NSFW channels.", false)
                    .addField(cmd + "self`", "Toggles the ability to star your own messages.", false);
            event.getHook().sendMessageEmbeds(embed.build()).queue();
        }
    }
}
