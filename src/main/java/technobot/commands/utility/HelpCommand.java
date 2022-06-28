package technobot.commands.utility;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import technobot.TechnoBot;
import technobot.commands.Category;
import technobot.commands.Command;
import technobot.commands.CommandRegistry;
import technobot.listeners.ButtonListener;
import technobot.util.embeds.EmbedColor;
import technobot.util.embeds.EmbedUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import static technobot.util.Localization.get;

public class HelpCommand extends Command {

    private static final int COMMANDS_PER_PAGE = 6;

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
        HashMap<Category, List<Command>> categories = new LinkedHashMap<>();
        EmbedBuilder builder = new EmbedBuilder().setColor(EmbedColor.DEFAULT.color);
        for (Category category : Category.values()) {
            categories.put(category, new ArrayList<>());
        }
        for (Command cmd : CommandRegistry.commands) {
            categories.get(cmd.category).add(cmd);
        }

        OptionMapping option = event.getOption("category");
        OptionMapping commandOption = event.getOption("command");
        if (option != null && commandOption != null) {
            event.replyEmbeds(EmbedUtils.createError("Please only give one optional argument and try again.")).queue();
        } else if (option != null) {
            // Display category commands menu
            Category category = Category.valueOf(option.getAsString().toUpperCase());
            List<MessageEmbed> embeds = buildCategoryMenu(category, categories.get(category));
            if (embeds.isEmpty()) {
                // No commands for this category
                EmbedBuilder embed = new EmbedBuilder()
                        .setTitle(get(
                                s -> s.utility.help.categoryTitle,
                                category.emoji, category.name
                        ))
                        .setDescription(get(s -> s.utility.help.comingSoon))
                        .setColor(EmbedColor.DEFAULT.color);
                event.replyEmbeds(embed.build()).queue();
                return;
            }
            // Send paginated help menu
            ReplyCallbackAction action = event.replyEmbeds(embeds.get(0));
            if (embeds.size() > 1) {
                ButtonListener.sendPaginatedMenu(event.getUser().getId(), action, embeds);
                return;
            }
            action.queue();
        } else if (commandOption != null) {
            // Display command details menu
            Command cmd = CommandRegistry.commandsMap.get(commandOption.getAsString());
            if (cmd != null) {
                builder.setTitle(get(s -> s.utility.help.commandTitle, cmd.name));
                builder.setDescription(cmd.description);
                StringBuilder usages = new StringBuilder();
                if (cmd.subCommands.isEmpty()) {
                    usages.append("`").append(getUsage(cmd)).append("`");
                } else {
                    for (SubcommandData sub : cmd.subCommands) {
                        usages.append("`").append(getUsage(sub, cmd.name)).append("`\n");
                    }
                }
                builder.addField(get(s -> s.utility.help.commandUsage), usages.toString(), false);
                builder.addField(get(s -> s.utility.help.commandPerms), getPermissions(cmd), false);
                event.replyEmbeds(builder.build()).queue();
            } else {
                // Command specified doesn't exist.
                event.replyEmbeds(EmbedUtils.createError(get(s -> s.utility.help.noCommand, commandOption.getAsString()))).queue();
            }
        } else {
            // Display default menu
            builder.setTitle(get(s -> s.utility.help.defaultTitle));
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

        embed.setTitle(get(
                s -> s.utility.help.categoryTitle,
                category.emoji, category.name
        ));
        embed.setColor(EmbedColor.DEFAULT.color);

        int counter = 0;
        for (Command cmd : commands) {
            if (cmd.subCommands.isEmpty()) {
                embed.appendDescription("`" + getUsage(cmd) + "`\n" + cmd.description + "\n\n");
                counter++;
                if (counter % COMMANDS_PER_PAGE == 0) {
                    embeds.add(embed.build());
                    embed.setDescription("");
                    counter = 0;
                }
            } else {
                for (SubcommandData sub : cmd.subCommands) {
                    embed.appendDescription("`" + getUsage(sub, cmd.name) + "`\n" + sub.getDescription() + "\n\n");
                    counter++;
                    if (counter % COMMANDS_PER_PAGE == 0) {
                        embeds.add(embed.build());
                        embed.setDescription("");
                        counter = 0;
                    }
                }
            }
        }
        if (counter != 0) embeds.add(embed.build());
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
            usage.append(isRequired ? " <" : " [");
            usage.append(cmd.args.get(i).getName());
            usage.append(isRequired ? ">" : "]");
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
            usage.append(isRequired ? " <" : " [");
            usage.append(arg.getName());
            usage.append(isRequired ? ">" : "]");
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
            return get(s -> s.utility.help.noPermissions);
        }
        return cmd.permission.getName();
    }
}