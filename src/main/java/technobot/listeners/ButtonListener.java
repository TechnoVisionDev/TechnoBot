package technobot.listeners;

import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import technobot.data.GuildData;
import technobot.util.embeds.EmbedUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class ButtonListener extends ListenerAdapter {

    public static final ScheduledExecutorService executor = Executors.newScheduledThreadPool(10);
    public static final Map<String, List<MessageEmbed>> menus = new HashMap<>();
    public static final Map<String, List<Button>> buttons = new HashMap<>();

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

        if (pressedArgs[0].equals("help") && storedArgs[0].equals("help")) {
            if (pressedArgs[1].equals("next")) {
                // Move to next embed
                int page = Integer.parseInt(components.get(1).getId().split(":")[2]) + 1;
                List<MessageEmbed> embeds = menus.get(uuid);
                if (page < embeds.size()) {
                    // Update buttons
                    components.set(1, components.get(1).withId("help:page:" + page).withLabel((page + 1) + "/" + embeds.size()));
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
                    components.set(1, components.get(1).withId("help:page:" + page).withLabel((page + 1) + "/" + embeds.size()));
                    components.set(2, components.get(2).asEnabled());
                    if (page == 0) {
                        components.set(0, components.get(0).asDisabled());
                    }
                    buttons.put(uuid, components);
                    event.editComponents(ActionRow.of(components)).setEmbeds(embeds.get(page)).queue();
                }
            }
        }
        else if (pressedArgs[0].equals("suggestions") && storedArgs[0].equals("suggestions")) {
            if (pressedArgs[1].equals("yes")) {
                event.deferEdit().queue();
                GuildData.get(event.getGuild()).suggestionHandler.reset();
                MessageEmbed embed = EmbedUtils.createSuccess("Suggestion system was successfully reset!");
                event.getHook().editOriginalComponents(new ArrayList<>()).setEmbeds(embed).queue();
            } else if (pressedArgs[1].equals("no")) {
                event.deferEdit().queue();
                MessageEmbed embed = EmbedUtils.createError("Suggestion system was **NOT** reset!");
                event.getHook().editOriginalComponents(new ArrayList<>()).setEmbeds(embed).queue();
            }
        }
        else if (pressedArgs[0].equals("greetings") && storedArgs[0].equals("greetings")) {
            if (pressedArgs[1].equals("yes")) {
                event.deferEdit().queue();
                GuildData.get(event.getGuild()).greetingHandler.reset();
                MessageEmbed embed = EmbedUtils.createSuccess("Greeting system was successfully reset!");
                event.getHook().editOriginalComponents(new ArrayList<>()).setEmbeds(embed).queue();
            } else if (pressedArgs[1].equals("no")) {
                event.deferEdit().queue();
                MessageEmbed embed = EmbedUtils.createError("Greeting system was **NOT** reset!");
                event.getHook().editOriginalComponents(new ArrayList<>()).setEmbeds(embed).queue();
            }
        }
    }
}
