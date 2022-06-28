package technobot.commands.suggestions;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import technobot.TechnoBot;
import technobot.commands.Category;
import technobot.commands.Command;
import technobot.data.GuildData;
import technobot.handlers.SuggestionHandler;
import technobot.util.embeds.EmbedColor;
import technobot.util.embeds.EmbedUtils;

import static technobot.util.Localization.get;

/**
 * Command that allows a user to make a suggestion on the suggestion board.
 *
 * @author TechnoVision
 */
public class SuggestCommand extends Command {

    public SuggestCommand(TechnoBot bot) {
        super(bot);
        this.name = "suggest";
        this.description = "Add a suggestion to the suggestion board.";
        this.category = Category.SUGGESTIONS;
        this.args.add(new OptionData(OptionType.STRING, "suggestion", "The content for your suggestion", true));
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.deferReply().queue();

        // Check if suggestion board has been set up
        SuggestionHandler suggestionHandler = GuildData.get(event.getGuild()).suggestionHandler;
        if (!suggestionHandler.isSetup()) {
            String text = get(s -> s.suggestions.suggest.noChannel);
            event.getHook().sendMessageEmbeds(EmbedUtils.createError(text)).queue();
            return;
        }

        // Create suggestion embed
        String content = event.getOption("suggestion").getAsString();
        EmbedBuilder embed = new EmbedBuilder()
                .setColor(EmbedColor.DEFAULT.color)
                .setTitle(get(s -> s.suggestions.suggest.title, suggestionHandler.getNumber()))
                .setDescription(content);

        // Add author to embed if anonymous mode is turned off
        if (suggestionHandler.isAnonymous()) {
            embed.setAuthor(get(s -> s.suggestions.suggest.anonymousAuthor), null, "https://cdn.discordapp.com/embed/avatars/0.png");
        } else {
            embed.setAuthor(event.getUser().getAsTag(), null, event.getUser().getEffectiveAvatarUrl());
        }

        // Make sure channel is valid
        TextChannel channel = event.getGuild().getTextChannelById(suggestionHandler.getChannel());
        if (channel == null) {
            String text = get(s -> s.suggestions.suggest.channelDeleted);
            event.getHook().sendMessageEmbeds(EmbedUtils.createError(text)).queue();
            return;
        }

        // Add suggestion and reaction buttons
        channel.sendMessageEmbeds(embed.build()).queue(suggestion -> {
            suggestionHandler.add(suggestion.getIdLong(), event.getUser().getIdLong());
            try {
                suggestion.addReaction("⬆").queue();
                suggestion.addReaction("⬇").queue();
            } catch (InsufficientPermissionException ignored) {
            }
        });

        // Send a response message
        String text = get(s -> s.suggestions.suggest.message, suggestionHandler.getChannel());
        event.getHook().sendMessageEmbeds(EmbedUtils.createDefault(text)).queue();
    }
}
