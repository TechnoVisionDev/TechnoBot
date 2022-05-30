package technobot.commands.suggestions;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import technobot.TechnoBot;
import technobot.commands.Category;
import technobot.commands.Command;
import technobot.data.GuildData;
import technobot.data.cache.Suggestions;
import technobot.util.EmbedColor;
import technobot.util.EmbedUtils;

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

        // Check if suggestion board has been setup
        Suggestions suggestions = GuildData.get(event.getGuild()).suggestions;
        if (!suggestions.isSetup()) {
            String text = "The suggestion channel has not been set!";
            event.getHook().sendMessageEmbeds(EmbedUtils.createError(text)).queue();
            return;
        }

        // Create suggestion embed
        String content = event.getOption("suggestion").getAsString();
        EmbedBuilder embed = new EmbedBuilder()
                .setColor(EmbedColor.DEFAULT.color)
                .setTitle("Suggestion #" + suggestions.getNumber())
                .setDescription(content);

        // Add author to embed if anonymous mode is turned off
        if (suggestions.isAnonymous()) {
            embed.setAuthor("Anonymous", null, "https://cdn.discordapp.com/embed/avatars/0.png");
        } else {
            embed.setAuthor(event.getUser().getAsTag(), null, event.getUser().getEffectiveAvatarUrl());
        }

        // Make sure channel is valid
        TextChannel channel = event.getGuild().getTextChannelById(suggestions.getChannel());
        if (channel == null) {
            String text = "The suggestion channel has been deleted, please set a new one!";
            event.getHook().sendMessageEmbeds(EmbedUtils.createError(text)).queue();
            return;
        }

        // Add suggestion and reaction buttons
        channel.sendMessageEmbeds(embed.build()).queue(suggestion -> {
            suggestions.add(suggestion.getIdLong(), event.getUser().getIdLong());
            suggestion.addReaction("⬆").queue();
            suggestion.addReaction("⬇").queue();
        });

        // Send a response message
        String text = "Your suggestion has been added to <#" + suggestions.getChannel() + ">!";
        event.getHook().sendMessageEmbeds(EmbedUtils.createDefault(text)).queue();
    }
}
