package technobot.commands.music;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import technobot.TechnoBot;
import technobot.commands.Category;
import technobot.commands.Command;
import technobot.data.GuildData;

public class TestCommand extends Command {

    public TestCommand(TechnoBot bot) {
        super(bot);
        this.name = "test";
        this.description = "Roll a dice.";
        this.category = Category.MUSIC;
    }

    public void execute(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        GuildData data = GuildData.get(event.getGuild());
        System.out.println(data.music.audioPlayer.getPlayingTrack());
        event.getHook().sendMessage("lol").queue();
    }
}
