package technobot.commands.utility;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import technobot.TechnoBot;
import technobot.commands.Category;
import technobot.commands.Command;
import technobot.commands.CommandRegistry;
import technobot.util.embeds.EmbedColor;
import technobot.util.embeds.EmbedUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HelpCommand extends Command {

    private static final int COMMANDS_PER_PAGE = 6;

    private static final Map<Long, List<MessageEmbed>> menus = new HashMap<>();
    private static final Map<Long, List<Button>> buttons = new HashMap<>();

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
            List<MessageEmbed> embeds = buildCategoryMenu(category, categories.get(category));

            ReplyCallbackAction action = event.replyEmbeds(embeds.get(0));
            if (embeds.size() > 1) {
                long userID = event.getUser().getIdLong();
                menus.put(userID, embeds);
                List<Button> components = new ArrayList<>();
                components.add(Button.primary("help:prev:"+userID, "PREVIOUS").asDisabled());
                components.add(Button.of(ButtonStyle.SECONDARY, "help:page:0", "1/"+embeds.size()).asDisabled());
                components.add(Button.primary("help:next:"+userID, "NEXT"));
                buttons.put(userID, components);
                action = action.addActionRow(components);
            }
            action.queue();
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
                event.replyEmbeds(builder.build()).queue();
            } else {
                // Command specified doesn't exist.
                event.replyEmbeds(EmbedUtils.createError("No command called \"" + option2.getAsString() + "\" found.")).queue();
            }
        } else {
            // Display default menu
            builder.setTitle("TechnoBot Commands");
            categories.forEach((category, commands) -> {
                String categoryName = category.name().toLowerCase();
                String value = "`/help " + categoryName + "`";
                builder.addField(category.emoji + " " + category.name, value, true);
            });
            event.replyEmbeds(builder.build()).queue();
        }
    }

    /**
     * Builds a menu with all the commands in a specified category.
     *
     * @param category the category to build a menu for.
     * @param commands a list of the commands in this category.
     * @return a list of MessageEmbed objects for pagination.
     */
    public List<MessageEmbed> buildCategoryMenu(Category category, List<Command> commands) {
        List<MessageEmbed> embeds = new ArrayList<>();
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle(category.emoji + "  **%s Commands**".formatted(category.name));
        embed.setColor(EmbedColor.DEFAULT.color);

        int counter = 0;
        for (Command cmd : commands) {
            if (cmd.subCommands.isEmpty()) {
                embed.appendDescription("`" + getUsage(cmd) + "`\n" + cmd.description + "\n\n");
                counter++;
                if (counter % COMMANDS_PER_PAGE == 0) {
                    embeds.add(embed.build());
                    embed.setDescription("");
                }
            } else {
                for (SubcommandData sub : cmd.subCommands) {
                    embed.appendDescription("`" + getUsage(sub, cmd.name) + "`\n" + sub.getDescription() + "\n\n");
                    counter++;
                    if (counter % COMMANDS_PER_PAGE == 0) {
                        embeds.add(embed.build());
                        embed.setDescription("");
                    }
                }
            }
        }
        return embeds;
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

    /**
     * Button events for help menu pagination.
     */
    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        // Check that these are 'help' buttons
        String[] args = event.getComponentId().split(":");
        if (!args[0].equals("help")) return;

        // Check if user owns this menu
        long userID = Long.parseLong(args[2]);
        if (userID != event.getUser().getIdLong()) return;
        List<Button> components = buttons.get(userID);

        if (args[1].equals("next")) {
            // Move to next embed
            int page = Integer.parseInt(components.get(1).getId().split(":")[2]) + 1;
            List<MessageEmbed> embeds = menus.get(userID);
            if (page < embeds.size()) {
                // Update buttons
                components.set(1, components.get(1).withId("help:page:"+page).withLabel((page+1)+"/"+embeds.size()));
                if (page == embeds.size()-1) {
                    components.set(2, components.get(2).asDisabled());
                } else {
                    components.set(0, components.get(0).asEnabled());
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
                components.set(1, components.get(1).withId("help:page:"+page).withLabel((page+1)+"/"+embeds.size()));
                if (page == 0) {
                    components.set(0, components.get(0).asDisabled());
                } else {
                    components.set(2, components.get(2).asEnabled());
                }
                buttons.put(userID, components);
                event.editComponents(ActionRow.of(components)).setEmbeds(embeds.get(page)).queue();
            }
        }
    }
}