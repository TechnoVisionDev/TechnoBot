package technobot.commands.suggestions;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import technobot.TechnoBot;
import technobot.commands.Category;
import technobot.commands.Command;
import technobot.data.GuildData;
import technobot.data.cache.Suggestions;
import technobot.util.EmbedColor;
import technobot.util.EmbedUtils;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.ArrayList;

/**
 * Admin command to setup and modify the suggestion board.
 *
 * @author TechnoVision
 */
public class SuggestionsCommand extends Command {

    public SuggestionsCommand(TechnoBot bot) {
        super(bot);
        this.name = "suggestions";
        this.description = "Setup and modify the suggestions config.";
        this.category = Category.SUGGESTIONS;
        this.permission = Permission.MANAGE_SERVER;
        this.args.add(new OptionData(OptionType.CHANNEL, "channel", "Set this channel to become the suggestion board."));
        this.args.add(new OptionData(OptionType.STRING, "option", "Toggle a setting on/off.")
                .addChoice("setup", "setup")
                .addChoice("dm", "dm")
                .addChoice("anonymous", "anonymous")
                .addChoice("reset", "reset"));
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        Guild guild = event.getGuild();
        GuildData data = GuildData.get(guild);

        // Set suggestion board channel
        OptionMapping channelOption = event.getOption("channel");
        OptionMapping settingsOption = event.getOption("option");
        if (channelOption != null) {
            // Set suggestion board to mentioned channel
            long channel = channelOption.getAsGuildChannel().getIdLong();
            String channelMention = "<#" + channel + ">";
            if (data.suggestions == null)  data.suggestions = new Suggestions(bot, guild.getIdLong(), channel);
            else data.suggestions.setChannel(channel);
            String response = EmbedUtils.BLUE_TICK + " Set the suggestion channel to " + channelMention;
            EmbedBuilder embed = new EmbedBuilder().setColor(EmbedColor.DEFAULT.color).setDescription(response);
            event.getHook().sendMessageEmbeds(embed.build()).queue();
        } else if (settingsOption != null) {
            // Toggle options
            switch (settingsOption.getAsString().toLowerCase()) {
                case "dm" -> {
                    boolean isEnabled = data.suggestions.toggleResponseDM();
                    String text;
                    if (isEnabled) {
                        text = EmbedUtils.BLUE_TICK + " Response DMs have been **enabled** for suggestions!";
                    } else {
                        text = EmbedUtils.BLUE_X + " Response DMs have been **disabled** for suggestions!";
                    }
                    event.getHook().sendMessageEmbeds(EmbedUtils.createDefault(text)).queue();
                }
                case "anonymous" -> {
                    boolean isEnabled = data.suggestions.toggleAnonymous();
                    String text;
                    if (isEnabled) {
                        text = EmbedUtils.BLUE_TICK + " Anonymous mode has been **enabled** for suggestions!";
                    } else {
                        text = EmbedUtils.BLUE_X + " Anonymous mode has been **disabled** for suggestions!";
                    }
                    event.getHook().sendMessageEmbeds(EmbedUtils.createDefault(text)).queue();
                }
                case "setup" -> {
                    guild.createTextChannel("suggestions").queue(channel -> {
                        ArrayList<Permission> denyPerms = new ArrayList<>();
                        denyPerms.add(Permission.MESSAGE_ADD_REACTION);
                        denyPerms.add(Permission.MESSAGE_SEND);

                        ArrayList<Permission> allowPerms = new ArrayList<>();
                        allowPerms.add(Permission.VIEW_CHANNEL);
                        allowPerms.add(Permission.MESSAGE_HISTORY);

                        channel.upsertPermissionOverride(guild.getPublicRole()).deny(denyPerms).setAllowed(allowPerms).queue();
                        if (data.suggestions == null)  data.suggestions = new Suggestions(bot, guild.getIdLong(), channel.getIdLong());
                        else data.suggestions.setChannel(channel.getIdLong());
                    });
                    String response = EmbedUtils.BLUE_TICK + " Created a new suggestion channel!";
                    EmbedBuilder embed = new EmbedBuilder().setColor(EmbedColor.DEFAULT.color).setDescription(response);
                    event.getHook().sendMessageEmbeds(embed.build()).queue();
                }
                case "reset" -> {
                    String text = "Would you like to reset the suggestions system?\nThis will delete **ALL** data!";
                    event.getHook().sendMessageEmbeds(EmbedUtils.createDefault(text))
                            .addActionRow(
                                    Button.success("yes", Emoji.fromMarkdown("\u2714")),
                                    Button.danger("no", Emoji.fromUnicode("\u2716")))
                            .queue();
                }
            }
        } else {
            String channel = (data.suggestions.getChannel() != null) ? "<#" + data.suggestions.getChannel() + ">" : "`Not set`";
            String config = "Suggestion channel: " + channel + " \n"
                    + "DM on response: `" + data.suggestions.hasResponseDM() + "`\n"
                    + "Anonymous suggestions: `" + data.suggestions.isAnonymous() + "`";
            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("Suggestions Config")
                    .setDescription(config)
                    .setColor(EmbedColor.DEFAULT.color);
            event.getHook().sendMessageEmbeds(embed.build()).queue();
        }
    }

    /**
     * Button events for 'reset' option
     */
    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        event.deferEdit().queue();
        event.getHook().editOriginalComponents(new ArrayList<>()).queue(x -> {
            if (event.getComponentId().equals("yes")) {
                GuildData.get(event.getGuild()).suggestions.reset(event.getGuild());
                x.editMessageEmbeds(EmbedUtils.createSuccess("Suggestion system was successfully reset!")).queue();
            } else if (event.getComponentId().equals("no")) {
                x.editMessageEmbeds(EmbedUtils.createError("Suggestion system was **NOT** reset!")).queue();
            }
        });
    }
}
