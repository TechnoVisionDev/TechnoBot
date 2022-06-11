package technobot.commands.greetings;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import technobot.TechnoBot;
import technobot.commands.Category;
import technobot.commands.Command;
import technobot.data.GuildData;
import technobot.handlers.GreetingHandler;
import technobot.util.embeds.EmbedUtils;

/**
 * Command that configures auto join DMs.
 *
 * @author TechnoVision
 */
public class JoinDMCommand extends Command {

    public JoinDMCommand(TechnoBot bot) {
        super(bot);
        this.name = "join-dm";
        this.description = "Set a private message to be sent when a member joins.";
        this.category = Category.GREETINGS;
        this.args.add(new OptionData(OptionType.STRING, "message", "The message to send as a DM"));
        this.permission = Permission.MANAGE_SERVER;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        GreetingHandler greetingHandler = GuildData.get(event.getGuild()).greetingHandler;
        OptionMapping farewellOption = event.getOption("message");

        // Remove farewell message
        if (farewellOption == null) {
            greetingHandler.removeJoinDM();
            String text = EmbedUtils.BLUE_X + " Join DM message successfully removed!";
            event.getHook().sendMessageEmbeds(EmbedUtils.createDefault(text)).queue();
            return;
        }

        // Set greeting message
        greetingHandler.setJoinDM(farewellOption.getAsString());
        String text = EmbedUtils.BLUE_TICK + " Join DM message successfully updated!";
        event.getHook().sendMessageEmbeds(EmbedUtils.createDefault(text)).queue();
    }
}
