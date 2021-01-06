package com.technovision.technobot.commands.fun;

import com.technovision.technobot.TechnoBot;
import com.technovision.technobot.commands.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.util.concurrent.ThreadLocalRandom;

/**
 * RockPaperScissors command
 * Created by hirshi001
 */
public class CommandRockPaperScissors extends Command {

    private static final String[] CHOICES = new String[]{"rock", "paper", "scissors"};

    public CommandRockPaperScissors(@NotNull TechnoBot bot) {
        super(bot, "rps", "a rock paper scissors game", "{prefix}rps <move>", Category.FUN);
    }

    @Override
    public boolean execute(final MessageReceivedEvent event, String[] args) {

        if(args.length>0){
            String playerChoice = args[0].toLowerCase();
            String msg;
            int systemChoice = ThreadLocalRandom.current().nextInt(0,3);

            switch(playerChoice){
                case "rock":
                    msg = output(test(1, systemChoice));
                    break;
                case "paper":
                    msg = output(test(2, systemChoice));
                    break;
                case "scissors":
                    msg = output(test(0, systemChoice));
                    break;
                default: //Invalid Arguments
                    event.getChannel().sendMessage(defaultErrorMessage(event)).queue();
                    return true;
            }
            EmbedBuilder eb = new EmbedBuilder();
            eb.setColor(Color.BLACK);
            eb.setAuthor(event.getAuthor().getAsTag(), null, event.getAuthor().getEffectiveAvatarUrl());
            eb.setDescription(msg + "\nYou picked " + playerChoice +"!\nThe computer picked " + getChoice(systemChoice) + ".");

        }
        //No Arguments Provided
        else{
            event.getChannel().sendMessage(defaultErrorMessage(event)).queue();
        }
        return true;
    }

    private static String getChoice(int choice){
        return CHOICES[choice];
    }


    private MessageEmbed defaultErrorMessage(MessageReceivedEvent event){
        return new EmbedBuilder()
                .setColor(Command.ERROR_EMBED_COLOR)
                .setAuthor(event.getAuthor().getAsTag(), null, event.getAuthor().getEffectiveAvatarUrl())
                .setDescription(":x: Invalid argument provided.\n\nUsage:\n`"+Command.PREFIX+this.name+" <move>`")
                .addField("Note","`<move>` should be `rock`, `paper`, or `scissors`", true)
                .addField("Example", Command.PREFIX+this.name+" paper", true)
                .build();
    }

    /**
     * returns 0 if tie, 1 if player wins, -1 if player loses
     * @param playerChoice
     * @param systemChoice
     * @return
     */
    private int test(int playerChoice, int systemChoice){
        return playerChoice-systemChoice;
    }

    private String output(int val){
        switch(val){
            case -1:
                return "You lost!";
            case 0:
                return "You tied!";
            case 1:
                return "You won!";
            default:
                return "Something went wrong";
        }
    }

}
