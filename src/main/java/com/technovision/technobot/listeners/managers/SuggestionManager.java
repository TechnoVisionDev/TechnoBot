package com.technovision.technobot.listeners.managers;

import com.technovision.technobot.TechnoBot;
import com.technovision.technobot.commands.Command;
import com.technovision.technobot.data.Configuration;
import com.technovision.technobot.util.enums.SuggestionResponse;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.json.JSONObject;

public class SuggestionManager {

    public static final String CHANNEL = "SUGGESTIONS";
    public static final String LOG = "SUGGESTION-LOG";

    private final Configuration suggestions;

    public SuggestionManager() {
        suggestions = new Configuration("data/", "suggestions.json") {
            @Override
            public void load() {
                super.load();
                if (!getJson().has("amount")) getJson().put("amount", 0);
                if (!getJson().has("suggestions")) getJson().put("suggestions", new JSONObject());
            }
        };
        suggestions.save();
    }

    public void addSuggestion(String id) {
        int amount = suggestions.getJson().getInt("amount") + 1;
        suggestions.getJson().getJSONObject("suggestions").put(String.valueOf(amount), id);
        suggestions.getJson().put("amount", amount);
        suggestions.save();
    }

    public int getAmount() {
        return suggestions.getJson().getInt("amount");
    }

    public String getSuggestion(int num) {
        return suggestions.getJson().getJSONObject("suggestions").getString(String.valueOf(num));
    }

    private void editEmbed(TextChannel channel, String staffName, String[] args, SuggestionResponse response) {
        int num = Integer.parseInt(args[0]);
        String reason = "No reason given";
        if (args.length > 1) {
            reason = "";
            for (int i = 1; i < args.length; i++) {
                reason += args[i] + " ";
            }
        }
        String id = TechnoBot.getInstance().getSuggestionManager().getSuggestion(num);
        Message msg = channel.retrieveMessageById(id).complete();
        MessageEmbed embed = msg.getEmbeds().get(0);
        EmbedBuilder editedEmbed = new EmbedBuilder();
        editedEmbed.setAuthor(embed.getAuthor().getName(), embed.getUrl(), embed.getAuthor().getIconUrl());
        editedEmbed.setTitle("Suggestions #" + num + " " + response.getResponse());
        editedEmbed.setDescription(embed.getDescription());
        editedEmbed.addField("Reason from " + staffName, reason, false);
        editedEmbed.setColor(response.getColor());
        msg.editMessage(editedEmbed.build()).queue();
    }

    public void respond(MessageReceivedEvent event, String[] args, SuggestionResponse response) {
        if (!event.getMember().hasPermission(Permission.MANAGE_SERVER)) {
            EmbedBuilder embed = new EmbedBuilder()
                    .setColor(Command.ERROR_EMBED_COLOR)
                    .setDescription(":x: You do not have permission to do that!");
            event.getChannel().sendMessage(embed.build()).queue();
            return;
        }
        if (args.length > 0) {
            try {
                int num = Integer.parseInt(args[0]);
                if (num < 1 || num > getAmount()) {
                    event.getChannel().sendMessage("Could not find a suggestion with that ID.").queue();
                    return;
                }
                String staffName = event.getAuthor().getAsTag();
                TextChannel channel = event.getGuild().getTextChannelsByName(SuggestionManager.CHANNEL, true).get(0);
                TechnoBot.getInstance().getSuggestionManager().editEmbed(channel, staffName, args, response);

                EmbedBuilder embed = new EmbedBuilder()
                        .setColor(Command.EMBED_COLOR)
                        .setDescription("Suggestion #" + args[0] + " has been " + response.getResponse().toLowerCase() + "!");
                event.getChannel().sendMessage(embed.build()).queue();
                return;
            } catch (NumberFormatException e) {
                EmbedBuilder embed = new EmbedBuilder()
                        .setColor(Command.ERROR_EMBED_COLOR)
                        .setDescription(":x: " + "\"" + args[0] + "\" is not a valid suggestion ID.");
                event.getChannel().sendMessage(embed.build()).queue();
                return;
            }
        }
        event.getChannel().sendMessage("`" + response.getCommand() + " <id> [reason]`").queue();
    }
}
