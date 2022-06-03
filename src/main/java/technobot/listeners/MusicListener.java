package technobot.listeners;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import technobot.data.GuildData;
import technobot.handlers.MusicHandler;
import technobot.util.embeds.EmbedColor;
import technobot.util.embeds.EmbedUtils;

import java.util.Objects;

/**
 * Module for music player backend and voice channel events.
 *
 * @author TechnoVision, Sparky
 */
public class MusicListener extends ListenerAdapter {

    private final @NotNull AudioPlayerManager playerManager;

    /**
     * Setup audio player manager.
     */
    public MusicListener() {
        this.playerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(playerManager);
    }

    /**
     * Formats track length into a readable string.
     *
     * @param trackLength numerical track length
     * @return string of track length (ex. 2:11)
     */
    public static @NotNull String formatTrackLength(long trackLength) {
        long msPos = trackLength;
        long minPos = msPos / 60000;
        msPos = msPos % 60000;
        int secPos = (int) Math.floor((float) msPos / 1000f);
        return minPos + ":" + ((secPos < 10) ? "0" + secPos : secPos);
    }

    /**
     * Runs a number of validity checks to make sure music player
     * instance is valid before retrieval.
     *
     * @param event The slash command event containing command data.
     * @return Null if invalid status, otherwise music player instance.
     */
    @Nullable
    public MusicHandler getMusic(@NotNull SlashCommandInteractionEvent event, boolean skipQueueCheck) {
        GuildData settings = GuildData.get(event.getGuild());
        // Check if user is in voice channel
        if (!inChannel(Objects.requireNonNull(event.getMember()))) {
            String text = "Please connect to a voice channel first!";
            event.getHook().sendMessageEmbeds(EmbedUtils.createError(text)).queue();
            return null;
        }
        // Bot should join voice channel if not already in one.
        AudioChannel channel = Objects.requireNonNull(event.getMember().getVoiceState()).getChannel();
        if (settings.musicHandler == null || settings.musicHandler.getPlayChannel() == null) {
            assert channel != null;
            joinChannel(settings, channel, event.getTextChannel());
        }
        // Check if music is playing in this guild
        if (!skipQueueCheck) {
            if (settings.musicHandler == null || settings.musicHandler.getQueue().isEmpty()) {
                String text = ":sound: There are no songs in the queue!";
                event.getHook().sendMessageEmbeds(EmbedUtils.createDefault(text)).queue();
                return null;
            }
            // Check if member is in the right voice channel
            if (settings.musicHandler.getPlayChannel() != channel) {
                String text = "You are not in the same voice channel as TechnoBot!";
                event.getHook().sendMessageEmbeds(EmbedUtils.createError(text)).queue();
                return null;
            }
        }
        return settings.musicHandler;
    }

    /**
     * Joins a voice channel.
     *
     * @param channel    The Voice Channel
     * @param logChannel A log channel to notify users.
     */
    public void joinChannel(@NotNull GuildData settings, @NotNull AudioChannel channel, TextChannel logChannel) {
        AudioManager manager = channel.getGuild().getAudioManager();
        if (!manager.isConnected()) {
            settings.musicHandler = new MusicHandler(playerManager.createPlayer());
            manager.setSendingHandler(settings.musicHandler);
        }
        Objects.requireNonNull(settings.musicHandler).setLogChannel(logChannel);
        settings.musicHandler.setPlayChannel(channel);
        manager.openAudioConnection(channel);
    }

    /**
     * Checks whether the specified member is in a voice channel.
     *
     * @param member The specified Member
     * @return True if this member is in a voice channel, otherwise false.
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean inChannel(@NotNull Member member) {
        return member.getVoiceState() != null && member.getVoiceState().inAudioChannel();
    }

    /**
     * Add a track to the specified guild.
     *
     * @param event  A slash command event.
     * @param url    The track URL.
     */
    public void addTrack(SlashCommandInteractionEvent event, String url) {
        MusicHandler music = GuildData.get(event.getGuild()).musicHandler;
        if (music == null) return;

        playerManager.loadItem(url, new AudioLoadResultHandler() {

            @Override
            public void trackLoaded(@NotNull AudioTrack audioTrack) {
                // Create embed message
                if (!music.getQueue().isEmpty()) {
                    String duration = formatTrackLength(audioTrack.getInfo().length);
                    String thumb = String.format("https://img.youtube.com/vi/%s/0.jpg", audioTrack.getInfo().uri.substring(32));

                    MessageEmbed embed = new EmbedBuilder()
                            .setColor(EmbedColor.DEFAULT.color)
                            .setTitle(audioTrack.getInfo().title, audioTrack.getInfo().uri)
                            .addField("Song Duration", duration, true)
                            .addField("Position in Queue", String.valueOf(music.getQueue().size()), true)
                            .setFooter("Added by " + event.getUser().getAsTag(), event.getUser().getEffectiveAvatarUrl())
                            .setThumbnail(thumb)
                            .build();
                    event.getHook().sendMessage(EmbedUtils.BLUE_TICK + " **" + audioTrack.getInfo().title + "** successfully added!").addEmbeds(embed).queue();
                } else {
                    event.getHook().sendMessage(EmbedUtils.BLUE_TICK + " **" + audioTrack.getInfo().title + "** successfully added!").queue();
                }
                music.enqueue(audioTrack);
            }

            @Override
            public void playlistLoaded(@NotNull AudioPlaylist audioPlaylist) {
                // Queue first result if youtube search
                if (audioPlaylist.isSearchResult()) {
                    trackLoaded(audioPlaylist.getTracks().get(0));
                    return;
                }

                // Otherwise load first 100 tracks from playlist
                int total = audioPlaylist.getTracks().size();
                if (total > 100) total = 100;
                String msg = ":ballot_box_with_check: Added " + total + " tracks from playlist `" + audioPlaylist.getName() + "`";
                event.getHook().sendMessage(msg).queue();

                total = music.getQueue().size();
                for (AudioTrack track : audioPlaylist.getTracks()) {
                    if (total < 100) {
                        music.enqueue(track);
                    }
                    total++;
                }
            }

            @Override
            public void noMatches() {
                String msg = "That is not a valid song!";
                event.getHook().sendMessageEmbeds(EmbedUtils.createError(msg)).queue();
            }

            @Override
            public void loadFailed(FriendlyException e) {
                String msg = "That is not a valid link!";
                event.getHook().sendMessageEmbeds(EmbedUtils.createError(msg)).queue();
            }
        });
    }

    /**
     * Pause music player if bot is alone in voice channel.
     * If the bot leaves the voice channel, delete data cache.
     *
     * @param event executes when someone leaves a voice channel.
     */
    @Override
    public void onGuildVoiceLeave(@NotNull GuildVoiceLeaveEvent event) {
        if (event.getChannelLeft().getMembers().size() == 1) {
            MusicHandler music = GuildData.get(event.getGuild()).musicHandler;
            if (music != null && event.getChannelLeft() == music.getPlayChannel()) {
                music.pause();
            }
        }
    }

    /**
     * Unpause music player if bot is no longer alone in voice channel.
     *
     * @param event executes when someone joins a voice channel.
     */
    @Override
    public void onGuildVoiceJoin(@NotNull GuildVoiceJoinEvent event) {
        if (event.getChannelJoined().getMembers().size() == 2) {
            MusicHandler music = GuildData.get(event.getGuild()).musicHandler;
            if (music != null && event.getChannelJoined() == music.getPlayChannel()) {
                music.unpause();
            }
        }
    }
}
