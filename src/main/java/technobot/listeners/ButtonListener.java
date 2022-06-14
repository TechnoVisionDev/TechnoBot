package technobot.listeners;

import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import technobot.data.GuildData;
import technobot.util.embeds.EmbedUtils;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class ButtonListener extends ListenerAdapter {

    public static final ScheduledExecutorService executor = Executors.newScheduledThreadPool(10);
    public static final Map<String, List<MessageEmbed>> menus = new HashMap<>();
    public static final Map<String, List<Button>> buttons = new HashMap<>();

    /**
     * Get a list of buttons for reset embeds (selectable yes and no).
     *
     * @param uuid the unique ID generated for these buttons.
     * @param systemName the name of the system being reset.
     * @return A list of components to use on a reset embed.
     */
    public static List<Button> getResetButtons(String uuid, String systemName) {
        return Arrays.asList(
            Button.success("reset:yes:"+uuid+":"+systemName, Emoji.fromMarkdown("\u2714")),
            Button.danger("reset:no:"+uuid+":"+systemName, Emoji.fromUnicode("\u2716"))
        );
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
                MessageEmbed embed = EmbedUtils.createSuccess(systemName+" system was successfully reset!");
                event.getHook().editOriginalComponents(new ArrayList<>()).setEmbeds(embed).queue();
            } else if (pressedArgs[1].equals("no")) {
                event.deferEdit().queue();
                MessageEmbed embed = EmbedUtils.createError(systemName+" system was **NOT** reset!");
                event.getHook().editOriginalComponents(new ArrayList<>()).setEmbeds(embed).queue();
            }
        }
    }
}
