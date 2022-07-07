package technobot.commands.casino;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import technobot.TechnoBot;
import technobot.commands.Category;
import technobot.commands.Command;
import technobot.data.GuildData;
import technobot.handlers.economy.EconomyHandler;
import technobot.util.embeds.EmbedColor;
import technobot.util.embeds.EmbedUtils;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Command that plays slot machine game.
 *
 * @author TechnoVision
 */
public class SlotsCommand extends Command {

    private final Map<Long, String> USER_THEMES;
    private final Map<String, List<String>> THEMES;

    public SlotsCommand(TechnoBot bot) {
        super(bot);
        this.name = "slots";
        this.description = "Spin the slot machine.";
        this.category = Category.CASINO;
        this.args.add(new OptionData(OptionType.INTEGER, "bet", "The amount you want to wager", true).setMinValue(1));
        this.args.add(new OptionData(OptionType.STRING, "theme", "The theme of the slot machine")
                .addChoice("fruity", "fruity")
                .addChoice("luxury", "luxury")
                .addChoice("space", "space")
                .addChoice("desert", "desert")
                .addChoice("enchanted", "enchanted")
                .addChoice("sexy", "sexy")
                .addChoice("holiday", "holiday")
                .addChoice("drunk", "drunk")
                .addChoice("apocalypse", "apocalypse")
                .addChoice("spooky", "spooky"));

        // Store themed slot emojis
        USER_THEMES = new HashMap<>();
        THEMES = new HashMap<>();
        THEMES.put("fruity", List.of("\uD83C\uDF4E", "\uD83C\uDF47", "\uD83C\uDF4C"));
        THEMES.put("luxury", List.of("\uD83D\uDCB8", "\uD83D\uDCB0", "\uD83D\uDC8E"));
        THEMES.put("space", List.of("\uD83E\uDE90", "\uD83D\uDE80", "\uD83D\uDC7D"));
        THEMES.put("desert", List.of("\uD83C\uDF35", "\uD83D\uDC2A", "☀"));
        THEMES.put("enchanted", List.of("✨", "\uD83C\uDF44", "\uD83E\uDDDA"));
        THEMES.put("sexy", List.of("\uD83D\uDCA6", "\uD83C\uDF51", "\uD83C\uDF46"));
        THEMES.put("holiday", List.of("❄", "\uD83C\uDF85", "\uD83C\uDF84"));
        THEMES.put("drunk", List.of("\uD83C\uDF78", "\uD83C\uDF7A", "\uD83C\uDF7E"));
        THEMES.put("apocalypse", List.of("\uD83D\uDD25", "\uD83C\uDF2A", "\uD83C\uDF0B"));
        THEMES.put("spooky", List.of("\uD83D\uDD78", "\uD83C\uDF83", "☠"));
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        // Get command data and theme
        User user = event.getUser();
        long bet = event.getOption("bet").getAsLong();
        OptionMapping themeOption = event.getOption("theme");
        List<String> emojis;
        if (themeOption != null) {
            String themeName = themeOption.getAsString();
            emojis = THEMES.get(themeName);
            USER_THEMES.put(user.getIdLong(), themeName);
        } else {
            String themeName = USER_THEMES.get(user.getIdLong());
            if (themeName == null) themeName = "fruity";
            emojis = THEMES.get(themeName);
        }

        // Charge player for bet
        EconomyHandler economyHandler = GuildData.get(event.getGuild()).economyHandler;
        long balance = economyHandler.getBalance(user.getIdLong());
        if (balance < bet) {
            String currency = economyHandler.getCurrency() + " **" + balance + "**";
            String text = "You don't have enough money for this bet. You currently have " + currency + " in cash.";
            event.replyEmbeds(EmbedUtils.createError(text)).setEphemeral(true).queue();
            return;
        }
        economyHandler.removeMoney(user.getIdLong(), bet);

        // Spin slot machine
        int[] slot = new int[3];
        StringBuilder slotMachine = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                int index = ThreadLocalRandom.current().nextInt(3);
                slotMachine.append(emojis.get(index));
                if (j != 2) slotMachine.append(" | ");
                if (i == 1) slot[j] = index;
            }
            if (i == 1) slotMachine.append(" ⬅");
            slotMachine.append("\n");
        }

        // Calculate earnings
        boolean isWinner = slot[0] == slot[1] && slot[0] == slot[2];
        long earnings = bet;
        if (isWinner) {
            switch (slot[0]) {
                case 0 -> earnings = bet * 9;
                case 1 -> earnings = bet * 9;
                case 2 -> earnings = bet * 9;
            }
        }

        // Send embed message
        EmbedBuilder embed = new EmbedBuilder().setAuthor(user.getAsTag(), null, user.getEffectiveAvatarUrl());
        if (isWinner) {
            embed.setColor(EmbedColor.SUCCESS.color);
            embed.setDescription("You won " + economyHandler.getCurrency() + " " + earnings + "\n\n");
            economyHandler.addMoney(user.getIdLong(), earnings);
        } else {
            embed.setColor(EmbedColor.ERROR.color);
            embed.setDescription("You lost " + economyHandler.getCurrency() + " " + earnings + "\n\n");
        }
        embed.appendDescription(slotMachine);
        event.replyEmbeds(embed.build()).queue();
    }
}
