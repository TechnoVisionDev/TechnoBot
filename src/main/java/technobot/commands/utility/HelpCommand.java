package technobot.commands.utility;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import technobot.TechnoBot;
import technobot.commands.Category;
import technobot.commands.Command;
import technobot.commands.CommandRegistry;
import technobot.util.EmbedColor;
import technobot.util.EmbedUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HelpCommand extends Command {

    public HelpCommand(TechnoBot bot) {
        super(bot);
        this.name = "help";
        this.description = "Display a list of all commands and categories.";
        this.category = Category.UTILITY;
        OptionData data = new OptionData(OptionType.STRING, "category", "See commands under this category");
        for (Category c : Category.values()) {
            String name = c.name.toLowerCase();
            data.addChoice(name, name);
        }
        this.args.add(data);
        this.args.add(new OptionData(OptionType.STRING, "command", "See details for this command"));
    }

    public void execute(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        // Create a hashmap that groups commands by categories.
        HashMap<Category, List<Command>> categories = new HashMap<>();
        EmbedBuilder builder = new EmbedBuilder().setColor(EmbedColor.DEFAULT.color);
        for (Category category : Category.values()) {
            categories.put(category, new ArrayList<>());
        }
        for (Command cmd : CommandRegistry.commands) {
            categories.get(cmd.category).add(cmd);
        }

        OptionMapping option = event.getOption("category");
        OptionMapping option2 = event.getOption("command");
        if (option != null) {
            // Display category commands menu
            Category category = Category.valueOf(option.getAsString().toUpperCase());
            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle(category.emoji + "  **%s Commands**".formatted(category.name));
            embed.setColor(EmbedColor.DEFAULT.color);
            for (Command cmd : categories.get(category)) {
                if (cmd.subCommands.isEmpty()) {
                    embed.appendDescription("`" + getUsage(cmd) + "`\n" + cmd.description + "\n\n");
                } else {
                    for (SubcommandData sub : cmd.subCommands) {
                        embed.appendDescription("`" + getUsage(sub, cmd.name) + "`\n" + sub.getDescription() + "\n\n");
                    }
                }
            }
            event.getHook().sendMessageEmbeds(embed.build()).queue();
        } else if (option2 != null) {
            // Display command details menu
            Command cmd = CommandRegistry.commandsMap.get(option2.getAsString());
            if (cmd != null) {
                builder.setTitle("Command: " + cmd.name);
                builder.setDescription(cmd.description);
                StringBuilder usages = new StringBuilder();
                if (cmd.subCommands.isEmpty()) {
                    usages.append("`").append(getUsage(cmd)).append("`");
                } else {
                    for (SubcommandData sub : cmd.subCommands) {
                        usages.append("`").append(getUsage(sub, cmd.name)).append("`\n");
                    }
                }
                builder.addField("Usage:", usages.toString(), false);
                builder.addField("Permission:", getPermissions(cmd), false);
                event.getHook().sendMessageEmbeds(builder.build()).queue();
            } else {
                // Command specified doesn't exist.
                event.getHook().sendMessageEmbeds(EmbedUtils.createError("No command called \"" + option2.getAsString() + "\" found.")).queue();
            }
        } else {
            // Display default menu
            builder.setTitle("TechnoBot Commands");
            categories.forEach((category, commands) -> {
                String categoryName = category.name().toLowerCase();
                String value = "`/help " + categoryName + "`";
                builder.addField(category.emoji + " " + category.name, value, true);
            });
            event.getHook().sendMessageEmbeds(builder.build()).queue();
        }
    }

    /**
     * Creates a string of command usage.
     *
     * @param cmd Command to build usage for.
     * @return String with name and args stitched together.
     */
    public String getUsage(Command cmd) {
        StringBuilder usage = new StringBuilder("/" + cmd.name);
        if (cmd.args.isEmpty()) return usage.toString();
        for (int i = 0; i < cmd.args.size(); i++) {
            boolean isRequired = cmd.args.get(i).isRequired();
            if (isRequired) { usage.append(" <"); }
            else { usage.append(" ["); }
            usage.append(cmd.args.get(i).getName());
            if (isRequired) { usage.append(">"); }
            else { usage.append("]"); }
        }
        return usage.toString();
    }

    /**
     * Creates a string of subcommand usage.
     *
     * @param cmd sub command data from a command.
     * @return String with name and args stitched together.
     */
    public String getUsage(SubcommandData cmd, String commandName) {
        StringBuilder usage = new StringBuilder("/" + commandName + " " + cmd.getName());
        if (cmd.getOptions().isEmpty()) return usage.toString();
        for (OptionData arg : cmd.getOptions()) {
            boolean isRequired = arg.isRequired();
            if (isRequired) {
                usage.append(" <");
            } else {
                usage.append(" [");
            }
            usage.append(arg.getName());
            if (isRequired) {
                usage.append(">");
            } else {
                usage.append("]");
            }
        }
        return usage.toString();
    }

    /**
     * Builds a string of permissions from command.
     *
     * @param cmd the command to draw perms from.
     * @return A string of command perms.
     */
    private String getPermissions(Command cmd) {
        if (cmd.permission == null) {
            return "None";
        }
        return cmd.permission.getName();
    }
}
