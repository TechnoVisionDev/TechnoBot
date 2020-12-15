package com.technovision.technobot.commands.other;

import com.google.common.collect.Sets;
import com.technovision.technobot.TechnoBot;
import com.technovision.technobot.commands.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class CommandHelp extends Command {
    public CommandHelp() {
        super("help", "Displays a list of available commands", "{prefix}help [category|command]", Command.Category.OTHER);
    }

    @Override
    public boolean execute(MessageReceivedEvent event, String[] args) {
        Map<Category, List<Command>> categories = new HashMap<Category, List<Command>>();

        for (Category c : Category.values()) {
            categories.put(c, new ArrayList<>());
        }
        for (Command c : TechnoBot.getInstance().getRegistry().getCommands()) {
            categories.get(c.category).add(c);
        }
        if (args.length == 0) {
            event.getChannel().sendMessage(new EmbedBuilder() {{
                setTitle("TechnoBot Commands");
                setColor(EMBED_COLOR);
                setThumbnail(TechnoBot.getInstance().getJDA().getSelfUser().getEffectiveAvatarUrl());
                categories.forEach((category, commands) -> {
                    addField((category.name().charAt(0) + "").toUpperCase() + category.name().substring(1).toLowerCase(), commands.size() + " commands in category | `" + PREFIX + "help " + category.name().toLowerCase() + "`", false);
                });
            }}.build()).queue();
        } else {
            try {
                Category c = Category.valueOf(args[0].toUpperCase());
                String categoryName = (c.name().charAt(0) + "").toUpperCase() + c.name().substring(1).toLowerCase();
                EmbedBuilder builder = new EmbedBuilder()
                        .setTitle(categoryName + " Commands")
                        .setColor(EMBED_COLOR);
                String description = "";
                for (Command cmd : categories.get(c)) {
                    String usage = cmd.usage.replace("{prefix}", PREFIX);
                    description += "`" + usage + "`\n" + cmd.description + "\n\n";
                }
                builder.setDescription(description);
                event.getChannel().sendMessage(builder.build()).queue();
            } catch (IllegalArgumentException e) {
                for (Command cmd : TechnoBot.getInstance().getRegistry().getCommands()) {
                    if (args[0].equalsIgnoreCase(cmd.name)) {
                        EmbedBuilder builder = new EmbedBuilder()
                                .setTitle((cmd.name.charAt(0) + "").toUpperCase() + cmd.name.substring(1))
                                .setColor(EMBED_COLOR)
                                .setDescription(cmd.description)
                                .addField("Category", ("" + cmd.category.name().charAt(0)).toUpperCase() + cmd.category.name().substring(1).toLowerCase(), true)
                                .addField("Usage", "`" + cmd.usage.replace("{prefix}", PREFIX) + "`", true);
                        event.getChannel().sendMessage(builder.build()).queue();
                        return true;
                    }
                }
                event.getChannel().sendMessage("No command called \"" + args[0] + "\" found.").queue();
            }
        }
        return true;
    }

    @Override
    public @NotNull Set<String> getAliases() {
        return Sets.newHashSet("commands", "cmds");
    }
}
