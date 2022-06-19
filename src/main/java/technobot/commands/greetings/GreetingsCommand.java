package technobot.commands.greetings;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
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
import technobot.data.cache.Greetings;
import technobot.handlers.GreetingHandler;
import technobot.listeners.ButtonListener;
import technobot.util.embeds.EmbedUtils;

import static technobot.util.localization.Localization.get;

/**
 * Command that displays and modifies greetings config.
 *
 * @author TechnoVision
 */
public class GreetingsCommand extends Command {

    public GreetingsCommand(TechnoBot bot) {
        super(bot);
        this.name = "greetings";
        this.description = "Modify this server's greetings config.";
        this.category = Category.GREETINGS;
        this.permission = Permission.MANAGE_SERVER;
        this.subCommands.add(new SubcommandData("channel", "Set a channel to send welcome messages to.")
                .addOptions(new OptionData(OptionType.CHANNEL, "channel", "The channel to send welcome messages to")
                        .setChannelTypes(ChannelType.TEXT, ChannelType.NEWS)));
        this.subCommands.add(new SubcommandData("config", "Display the greetings config for this server."));
        this.subCommands.add(new SubcommandData("reset", "Reset all greetings data and settings."));
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        GreetingHandler greetingHandler = GuildData.get(event.getGuild()).greetingHandler;

        String text = "";
        switch (event.getSubcommandName()) {
            case "channel" -> {
                OptionMapping channelOption = event.getOption("channel");
                if (channelOption == null) {
                    // Remove welcome channel if not specified
                    greetingHandler.removeChannel();
                    text = get(s -> s.greeting().greetings().removed());
                } else {
                    // Set welcome channel
                    Long channelID = channelOption.getAsGuildChannel().getIdLong();
                    greetingHandler.setChannel(channelID);
                    text = get(s -> s.greeting().greetings().set(), channelID);
                }
            }
            case "config" -> {
                text = configToString(greetingHandler.getConfig());
                event.getHook().sendMessage(text).queue();
                return;
            }
            case "reset" -> {
                text = get(s -> s.greeting().greetings().reset());
                WebhookMessageAction<Message> action = event.getHook().sendMessageEmbeds(EmbedUtils.createDefault(text));
                ButtonListener.sendResetMenu(event.getUser().getId(), "Greeting", action);
                return;
            }
        }
        event.getHook().sendMessageEmbeds(EmbedUtils.createDefault(text)).queue();
    }

    /**
     * Converts the greetings config into a readable string
     *
     * @param greetings an instance of the guild greetings config.
     * @return Stringified config (greetings only).
     */
    private String configToString(Greetings greetings) {
        String text = "";
        text += get(s -> s.greeting().greetings().welcomeConfig(),
                greetings.getWelcomeChannel() == null ? "none" : "<#" + greetings.getWelcomeChannel() + ">") + "\n";

        text += get(s -> s.greeting().greetings().greetingConfig(),
                greetings.getGreeting() == null ? "none" : greetings.getGreeting()) + "\n";

        text += get(s -> s.greeting().greetings().farewellConfig(),
                greetings.getFarewell() == null ? "none" : greetings.getFarewell()) + "\n";

        text += get(s -> s.greeting().greetings().joinDmConfig(),
                greetings.getJoinDM() == null ? "none" : greetings.getJoinDM()) + "\n";

        return text;
    }
}
