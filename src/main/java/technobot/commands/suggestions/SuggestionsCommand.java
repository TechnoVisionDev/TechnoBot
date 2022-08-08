package technobot.commands.suggestions;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageAction;
import technobot.TechnoBot;
import technobot.commands.Category;
import technobot.commands.Command;
import technobot.data.GuildData;
import technobot.handlers.SuggestionHandler;
import technobot.listeners.ButtonListener;
import technobot.util.embeds.EmbedUtils;

import java.util.ArrayList;

import static technobot.util.Localization.get;

/**
 * Admin command to set up and modify the suggestion board.
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
        this.subCommands.add(new SubcommandData("create", "Sets a channel to become the suggestion board.")
                .addOptions(new OptionData(OptionType.CHANNEL, "channel", "The channel to set as the suggestion board")
                        .setChannelTypes(ChannelType.TEXT, ChannelType.NEWS)));
        this.subCommands.add(new SubcommandData("dm", "Toggle private messages on suggestion response."));
        this.subCommands.add(new SubcommandData("anonymous", "Toggle anonymous mode for suggestions."));
        this.subCommands.add(new SubcommandData("config", "Display the current suggestions config."));
        this.subCommands.add(new SubcommandData("reset", "Reset all suggestion board data and settings."));
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        Guild guild = event.getGuild();
        SuggestionHandler suggestionHandler = GuildData.get(guild).suggestionHandler;

        String text = null;
        switch (event.getSubcommandName()) {
            case "create" -> {
                // Setup suggestion board
                OptionMapping channelOption = event.getOption("channel");
                if (channelOption == null) {
                    // Create new suggestion channel
                    guild.createTextChannel("suggestions").queue(channel -> {
                        ArrayList<Permission> denyPerms = new ArrayList<>();
                        denyPerms.add(Permission.MESSAGE_ADD_REACTION);
                        denyPerms.add(Permission.MESSAGE_SEND);

                        ArrayList<Permission> allowPerms = new ArrayList<>();
                        allowPerms.add(Permission.VIEW_CHANNEL);
                        allowPerms.add(Permission.MESSAGE_HISTORY);

                        channel.upsertPermissionOverride(guild.getPublicRole()).deny(denyPerms).setAllowed(allowPerms).queue();
                        suggestionHandler.setChannel(channel.getIdLong());
                    });
                    text = get(s -> s.suggestions.suggestions.create.create);
                } else {
                    // Set suggestion board to mentioned channel
                    try {
                        long channel = channelOption.getAsGuildChannel().getIdLong();
                        suggestionHandler.setChannel(channel);
                        text = get(s -> s.suggestions.suggestions.create.set, channel);
                    } catch (NullPointerException e) {
                        text = get(s -> s.suggestions.suggestions.create.failure);
                        event.getHook().sendMessageEmbeds(EmbedUtils.createError(text)).queue();
                        return;
                    }
                }
            }
            case "dm" -> {
                boolean isEnabled = suggestionHandler.toggleResponseDM();
                if (isEnabled) {
                    text = get(s -> s.suggestions.suggestions.dm.enable);
                } else {
                    text = get(s -> s.suggestions.suggestions.dm.disable);
                }
            }
            case "anonymous" -> {
                boolean isEnabled = suggestionHandler.toggleAnonymous();
                if (isEnabled) {
                    text = get(s -> s.suggestions.suggestions.anonymous.enable);
                } else {
                    text = get(s -> s.suggestions.suggestions.dm.disable);
                }
            }
            case "config" -> {
                text = "";
                text += "\n" + get(
                        s -> s.suggestions.suggestions.config.channel,
                        suggestionHandler.getChannel() != null
                                ? "<#" + suggestionHandler.getChannel() + ">"
                                : "none"
                );
                text += "\n" + get(s -> s.suggestions.suggestions.config.dm, suggestionHandler.hasResponseDM());
                text += "\n" + get(s -> s.suggestions.suggestions.config.anonymous, suggestionHandler.isAnonymous());

                event.getHook().sendMessage(text).queue();
                return;
            }
            case "reset" -> {
                text = get(s -> s.suggestions.suggestions.reset);
                WebhookMessageAction<Message> action = event.getHook().sendMessageEmbeds(EmbedUtils.createDefault(text));
                ButtonListener.sendResetMenu(event.getUser().getId(), "Suggestion", action);
                return;
            }
        }
        event.getHook().sendMessageEmbeds(EmbedUtils.createDefault(text)).queue();
    }
}
