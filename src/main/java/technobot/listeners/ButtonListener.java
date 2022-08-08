package technobot.listeners;

import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageAction;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import technobot.commands.casino.BlackjackCommand;
import technobot.commands.casino.CrashCommand;
import technobot.data.GuildData;
import technobot.util.embeds.EmbedUtils;
import technobot.util.enums.Cards;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static technobot.util.Localization.get;

/**
 * Listens for button input and handles all button backend.
 *
 * @author TechnoVision
 */
public class ButtonListener extends ListenerAdapter {

    public static final int MINUTES_TO_DISABLE = 3;

    public static final ScheduledExecutorService executor = Executors.newScheduledThreadPool(20);
    public static final Map<String, List<MessageEmbed>> menus = new HashMap<>();
    public static final Map<String, List<Button>> buttons = new HashMap<>();

    /**
     * Adds pagination buttons to a message action.
     *
     * @param userID the ID of the user who is accessing this menu.
     * @param action the ReplyCallbackAction to add components to.
     * @param embeds the embed pages.
     */
    public static void sendPaginatedMenu(String userID, ReplyCallbackAction action, List<MessageEmbed> embeds) {
        String uuid = userID + ":" + UUID.randomUUID();
        List<Button> components = getPaginationButtons(uuid, embeds.size());
        buttons.put(uuid, components);
        menus.put(uuid, embeds);
        action.addActionRow(components).queue(interactionHook -> ButtonListener.disableButtons(uuid, interactionHook));
    }

    /**
     * Adds pagination buttons to a deferred reply message action.
     *
     * @param userID the ID of the user who is accessing this menu.
     * @param action the WebhookMessageAction<Message> to add components to.
     * @param embeds the embed pages.
     */
    public static void sendPaginatedMenu(String userID, WebhookMessageAction<Message> action, List<MessageEmbed> embeds) {
        String uuid = userID + ":" + UUID.randomUUID();
        List<Button> components = getPaginationButtons(uuid, embeds.size());
        buttons.put(uuid, components);
        menus.put(uuid, embeds);
        action.addActionRow(components).queue(interactionHook -> ButtonListener.disableButtons(uuid, interactionHook));
    }

    /**
     * Adds reset buttons to a deferred reply message action.
     *
     * @param userID the ID of the user who is accessing this menu.
     * @param systemName the name of the system to reset.
     * @param action the WebhookMessageAction<Message> to add components to.
     */
    public static void sendResetMenu(String userID, String systemName, WebhookMessageAction<Message> action) {
        String uuid = userID + ":" + UUID.randomUUID();
        List<Button> components = getResetButtons(uuid, systemName);
        buttons.put(uuid, components);
        action.addActionRow(components).queue(interactionHook -> ButtonListener.disableButtons(uuid, interactionHook));
    }

    /**
     * Get a list of buttons for paginated embeds.
     *
     * @param uuid the unique ID generated for these buttons.
     * @param maxPages the total number of embed pages.
     * @return A list of components to use on a paginated embed.
     */
    private static List<Button> getPaginationButtons(String uuid, int maxPages) {
        return Arrays.asList(
                Button.primary(
                        "pagination:prev:" + uuid,
                        get(s -> s.general.buttons.previous) + ""
                ).asDisabled(),
                Button.of(ButtonStyle.SECONDARY, "pagination:page:0", "1/" + maxPages).asDisabled(),
                Button.primary(
                        "pagination:next:" + uuid,
                        get(s -> s.general.buttons.next) + ""
                )
        );
    }

    /**
     * Get a list of buttons for reset embeds (selectable yes and no).
     *
     * @param uuid the unique ID generated for these buttons.
     * @param systemName the name of the system being reset.
     * @return A list of components to use on a reset embed.
     */
    private static List<Button> getResetButtons(String uuid, String systemName) {
        return Arrays.asList(
                Button.success("reset:yes:" + uuid + ":" + systemName, Emoji.fromMarkdown("✔")),
                Button.danger("reset:no:" + uuid + ":" + systemName, Emoji.fromUnicode("✖"))
        );
    }

    /**
     * Schedules a timer task to disable buttons and clear cache after a set time.
     *
     * @param uuid the uuid of the components to disable.
     * @param hook a interaction hook pointing to original message.
     */
    public static void disableButtons(String uuid, InteractionHook hook) {
        Runnable task = () -> {
            List<Button> actionRow = ButtonListener.buttons.get(uuid);
            List<Button> newActionRow = new ArrayList<>();
            for (Button button : actionRow) {
                newActionRow.add(button.asDisabled());
            }
            hook.editOriginalComponents(ActionRow.of(newActionRow)).queue(null, new ErrorHandler().ignore(ErrorResponse.UNKNOWN_MESSAGE));
            ButtonListener.buttons.remove(uuid);
            ButtonListener.menus.remove(uuid);
        };
        ButtonListener.executor.schedule(task, MINUTES_TO_DISABLE, TimeUnit.MINUTES);
    }

    /**
     * Schedules a timer task to disable buttons and clear cache after a set time.
     *
     * @param uuid the uuid of the components to disable.
     * @param hook a message hook pointing to original message.
     */
    public static void disableButtons(String uuid, Message hook) {
        Runnable task = () -> {
            List<Button> actionRow = ButtonListener.buttons.get(uuid);
            List<Button> newActionRow = new ArrayList<>();
            for (Button button : actionRow) {
                newActionRow.add(button.asDisabled());
            }
            hook.editMessageComponents(ActionRow.of(newActionRow)).queue(null, new ErrorHandler().ignore(ErrorResponse.UNKNOWN_MESSAGE));
            ButtonListener.buttons.remove(uuid);
            ButtonListener.menus.remove(uuid);
        };
        ButtonListener.executor.schedule(task, MINUTES_TO_DISABLE, TimeUnit.MINUTES);
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        // Check that these are 'help' buttons
        String[] pressedArgs = event.getComponentId().split(":");

        // Check if user owns this menu
        long userID = Long.parseLong(pressedArgs[2]);
        if (userID != event.getUser().getIdLong()) return;

        // Get other buttons
        String uuid = userID+":"+pressedArgs[3];
        List<Button> components = buttons.get(uuid);
        if (components == null) return;
        String[] storedArgs = components.get(0).getId().split(":");

        if (pressedArgs[0].equals("pagination") && storedArgs[0].equals("pagination")) {
            if (pressedArgs[1].equals("next")) {
                // Move to next embed
                int page = Integer.parseInt(components.get(1).getId().split(":")[2]) + 1;
                List<MessageEmbed> embeds = menus.get(uuid);
                if (page < embeds.size()) {
                    // Update buttons
                    components.set(1, components.get(1).withId("pagination:page:" + page).withLabel((page + 1) + "/" + embeds.size()));
                    components.set(0, components.get(0).asEnabled());
                    if (page == embeds.size() - 1) {
                        components.set(2, components.get(2).asDisabled());
                    }
                    buttons.put(uuid, components);
                    event.editComponents(ActionRow.of(components)).setEmbeds(embeds.get(page)).queue();
                }
            } else if (pressedArgs[1].equals("prev")) {
                // Move to previous embed
                int page = Integer.parseInt(components.get(1).getId().split(":")[2]) - 1;
                List<MessageEmbed> embeds = menus.get(uuid);
                if (page >= 0) {
                    // Update buttons
                    components.set(1, components.get(1).withId("pagination:page:" + page).withLabel((page + 1) + "/" + embeds.size()));
                    components.set(2, components.get(2).asEnabled());
                    if (page == 0) {
                        components.set(0, components.get(0).asDisabled());
                    }
                    buttons.put(uuid, components);
                    event.editComponents(ActionRow.of(components)).setEmbeds(embeds.get(page)).queue();
                }
            }
        }
        else if (pressedArgs[0].equals("reset") && storedArgs[0].equals("reset")) {
            String systemName = pressedArgs[4];
            if (pressedArgs[1].equals("yes")) {
                event.deferEdit().queue();
                GuildData data = GuildData.get(event.getGuild());
                if (systemName.equalsIgnoreCase("Suggestion")) data.greetingHandler.reset();
                else if (systemName.equalsIgnoreCase("Greeting")) data.greetingHandler.reset();
                else if (systemName.equalsIgnoreCase("Leveling")) data.levelingHandler.resetAll();
                MessageEmbed embed = EmbedUtils.createSuccess(
                        get(s -> s.general.reset.success, systemName)
                );
                event.getHook().editOriginalComponents(new ArrayList<>()).setEmbeds(embed).queue();
            } else if (pressedArgs[1].equals("no")) {
                event.deferEdit().queue();
                MessageEmbed embed = EmbedUtils.createError(
                        get(s -> s.general.reset.failure, systemName)
                );
                event.getHook().editOriginalComponents(new ArrayList<>()).setEmbeds(embed).queue();
            }
        }
        else if (pressedArgs[0].equals("blackjack") && storedArgs[0].equals("blackjack")) {
            long bet = Long.parseLong(pressedArgs[4]);
            MessageEmbed embed = null;
            if (pressedArgs[1].equals("hit")) {
                embed = BlackjackCommand.hit(event.getGuild(), event.getUser(), bet, uuid);
            } else if (pressedArgs[1].equals("stand")) {
                embed = BlackjackCommand.stand(event.getGuild(), event.getUser(), bet, uuid);
            }
            event.editComponents(ActionRow.of(buttons.get(uuid))).setEmbeds(embed).queue();
        }
        else if (pressedArgs[0].equals("crash") && storedArgs[0].equals("crash")) {
            MessageEmbed embed = CrashCommand.cashout(event.getGuild(), event.getUser());
            Button disabledButton = buttons.remove(uuid).get(0).asDisabled();
            event.editComponents(ActionRow.of(disabledButton)).setEmbeds(embed).queue();
        }
    }
}
