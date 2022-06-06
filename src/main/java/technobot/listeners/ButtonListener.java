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
    public static final Map<Long, List<MessageEmbed>> menus = new HashMap<>();
    public static final Map<Long, List<Button>> buttons = new HashMap<>();

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        // Check that these are 'help' buttons
        String[] args = event.getComponentId().split(":");

        // Check if user owns this menu
        long userID = Long.parseLong(args[2]);
        if (userID != event.getUser().getIdLong()) return;
        List<Button> components = buttons.get(userID);
        if (components == null) return;

        if (args[0].equals("suggestions")) {
            if (args[1].equals("yes")) {
                event.deferEdit().queue();
                GuildData.get(event.getGuild()).suggestionHandler.reset();
                MessageEmbed embed = EmbedUtils.createSuccess("Suggestion system was successfully reset!");
                event.getHook().editOriginalComponents(new ArrayList<>()).setEmbeds(embed).queue();
            } else if (args[1].equals("no")) {
                event.deferEdit().queue();
                MessageEmbed embed = EmbedUtils.createError("Suggestion system was **NOT** reset!");
                event.getHook().editOriginalComponents(new ArrayList<>()).setEmbeds(embed).queue();
            }
        }

        if (args[0].equals("help")) {
            if (args[1].equals("next")) {
                // Move to next embed
                int page = Integer.parseInt(components.get(1).getId().split(":")[2]) + 1;
                List<MessageEmbed> embeds = menus.get(userID);
                if (page < embeds.size()) {
                    // Update buttons
                    components.set(1, components.get(1).withId("help:page:" + page).withLabel((page + 1) + "/" + embeds.size()));
                    components.set(0, components.get(0).asEnabled());
                    if (page == embeds.size() - 1) {
                        components.set(2, components.get(2).asDisabled());
                    }
                    buttons.put(userID, components);
                    event.editComponents(ActionRow.of(components)).setEmbeds(embeds.get(page)).queue();
                }
            } else if (args[1].equals("prev")) {
                // Move to previous embed
                int page = Integer.parseInt(components.get(1).getId().split(":")[2]) - 1;
                List<MessageEmbed> embeds = menus.get(userID);
                if (page >= 0) {
                    // Update buttons
                    components.set(1, components.get(1).withId("help:page:" + page).withLabel((page + 1) + "/" + embeds.size()));
                    components.set(2, components.get(2).asEnabled());
                    if (page == 0) {
                        components.set(0, components.get(0).asDisabled());
                    }
                    buttons.put(userID, components);
                    event.editComponents(ActionRow.of(components)).setEmbeds(embeds.get(page)).queue();
                }
            }
        }
    }
}
