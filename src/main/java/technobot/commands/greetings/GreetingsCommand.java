package technobot.commands.greetings;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.ErrorResponse;
import technobot.TechnoBot;
import technobot.commands.Category;
import technobot.commands.Command;
import technobot.data.GuildData;
import technobot.data.cache.Greetings;
import technobot.handlers.GreetingHandler;
import technobot.listeners.ButtonListener;
import technobot.util.embeds.EmbedUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

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
        switch(event.getSubcommandName()) {
            case "channel" -> {
                OptionMapping channelOption = event.getOption("channel");
                if (channelOption == null) {
                    // Remove welcome channel if not specified
                    greetingHandler.removeChannel();
                    text = EmbedUtils.BLUE_X + " Welcome channel successfully removed!";
                } else {
                    // Set welcome channel
                    Long channelID = channelOption.getAsGuildChannel().getIdLong();
                    greetingHandler.setChannel(channelID);
                    text = EmbedUtils.BLUE_X + " Welcome channel set to <#" + channelID + ">";
                }
            }
            case "config" -> {
                text = configToString(greetingHandler.getConfig());
                event.getHook().sendMessage(text).queue();
                return;
            }
            case "reset" -> {
                long userID = event.getUser().getIdLong();
                String uuid = userID + ":" + UUID.randomUUID();
                text = "Would you like to reset the greeting system?\nThis will delete **ALL** data!";
                List<Button> components = new ArrayList<>();
                components.add(Button.success("greetings:yes:"+uuid, Emoji.fromMarkdown("\u2714")));
                components.add(Button.danger("greetings:no:"+uuid, Emoji.fromUnicode("\u2716")));
                ButtonListener.buttons.put(uuid, components);
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
        if (greetings.getWelcomeChannel() == null) {
            text += "**Welcome Channel:** none\n";
        } else {
            text += "**Welcome Channel:** <#" + greetings.getWelcomeChannel() + ">\n";
        }
        if (greetings.getGreeting() == null) {
            text += "**Greeting:** none\n";
        } else {
            text += "**Greeting:** " + greetings.getGreeting() + "\n";
        }
        if (greetings.getFarewell() == null) {
            text += "**Farewell:** none\n";
        } else {
            text += "**Farewell:** " + greetings.getFarewell() + "\n";
        }
        if (greetings.getJoinDM() == null) {
            text += "**Join DM:** none\n";
        } else {
            text += "**Join DM:** " + greetings.getJoinDM() + "\n";
        }
        return text;
    }
}
