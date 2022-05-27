package technobot.commands.utility;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import technobot.TechnoBot;
import technobot.commands.Category;
import technobot.commands.Command;
import technobot.commands.CommandRegistry;
import technobot.util.EmbedColor;

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
        OptionData data2 = new OptionData(OptionType.STRING, "command", "See details for this command");
        for (Command cmd : CommandRegistry.commands) {
            data2.addChoice(cmd.name, cmd.name);
        }
        this.args.add(data2);
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
            List<Command> commands = categories.get(category);
            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle(category.emoji + "  **%s Commands**".formatted(category.name));
            embed.setColor(EmbedColor.DEFAULT.color);
            for (Command cmd : commands) {
                embed.appendDescription("`" + getUsage(cmd) + "`\n" + cmd.description + "\n\n");
            }
            event.getHook().sendMessageEmbeds(embed.build()).queue();
        } else if (option2 != null) {
            // Display command details menu
            Command cmd = null;
            for (Command c : CommandRegistry.commands) {
                if (c.name.equals(option2.getAsString())) {
                    cmd = c;
                }
            }
            assert cmd != null;
            builder.setTitle("Command: " + cmd.name);
            builder.setDescription(cmd.description);
            builder.addField("Usage:", "`" + getUsage(cmd) + "`", false);
            event.getHook().sendMessageEmbeds(builder.build()).queue();
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
        String usage = "/" + cmd.name;
        if (cmd.args.isEmpty()) return usage;
        boolean isRequired = cmd.args.get(0).isRequired();
        if (isRequired) { usage += " <"; }
        else { usage += " ["; }
        for (int i = 0; i < cmd.args.size(); i++) {
            usage += args.get(i).getName();
            if (i+1 != cmd.args.size()) {
                usage += " | ";
            }
        }
        if (isRequired) { usage += ">"; }
        else { usage += "]"; }
        return usage;
    }

    /**
     * Builds a string of permissions from command.
     *
     * @param cmd the command to draw perms from.
     * @return A string of command perms.
     */
    private String getPermissions(Command cmd) {
        // TODO: Implement
        return "";
    }
}
