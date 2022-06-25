package technobot.listeners;

import com.github.topislavalinkplugins.topissourcemanagers.ISRCAudioTrack;
import com.github.topislavalinkplugins.topissourcemanagers.applemusic.AppleMusicSourceManager;
import com.github.topislavalinkplugins.topissourcemanagers.spotify.SpotifyConfig;
import com.github.topislavalinkplugins.topissourcemanagers.spotify.SpotifySourceManager;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import technobot.data.GuildData;
import technobot.handlers.MusicHandler;
import technobot.util.SecurityUtils;
import technobot.util.embeds.EmbedColor;
import technobot.util.embeds.EmbedUtils;

import java.net.MalformedURLException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

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
    public MusicListener(String spotifyClientId, String spotifyClientSecret) {
        this.playerManager = new DefaultAudioPlayerManager();

        // Add Spotify support
        SpotifyConfig spotifyConfig = new SpotifyConfig();
        spotifyConfig.setClientId(spotifyClientId);
        spotifyConfig.setClientSecret(spotifyClientSecret);
        spotifyConfig.setCountryCode("US");
        this.playerManager.registerSourceManager(new SpotifySourceManager(null, spotifyConfig, playerManager));

        // Add Apple Music support
        playerManager.registerSourceManager(new AppleMusicSourceManager(null, "us", playerManager));

        // Add audio player to source manager
        AudioSourceManagers.registerRemoteSources(playerManager);
    }

    /**
     * Formats track length into a readable string.
     *
     * @param trackLength numerical track length
     * @return string of track length (ex. 2:11)
     */
    public static @NotNull String formatTrackLength(long trackLength) {
        long hours = TimeUnit.MILLISECONDS.toHours(trackLength);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(trackLength) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(trackLength));
        long seconds = TimeUnit.MILLISECONDS.toSeconds(trackLength) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(trackLength));
        String time = "";
        if (hours > 0) time += hours + ":";
        if (minutes < 10 && hours > 0) time += "0";
        time += minutes + ":";
        if (seconds < 10) time += "0";
        time += seconds;
        return time;
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
        if (settings.musicHandler == null || !event.getGuild().getAudioManager().isConnected()) {
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
     * @para guildData    The GuilData instance for this guild.
     * @param channel    The Voice Channel.
     * @param logChannel A log channel to notify users.
     */
    public void joinChannel(@NotNull GuildData guildData, @NotNull AudioChannel channel, TextChannel logChannel) {
        AudioManager manager = channel.getGuild().getAudioManager();
        if (guildData.musicHandler == null) {
            guildData.musicHandler = new MusicHandler(playerManager.createPlayer());
        }
        manager.setSendingHandler(guildData.musicHandler);
        Objects.requireNonNull(guildData.musicHandler).setLogChannel(logChannel);
        guildData.musicHandler.setPlayChannel(channel);
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

        // Check for SSRF vulnerability with whitelist
        try {
            boolean isWhitelisted = SecurityUtils.isUrlWhitelisted(url);
            if(!isWhitelisted) {
                url = "";
            }
        } catch(MalformedURLException ignored) {}
        playerManager.loadItem(url, new AudioLoadResultHandler() {

            @Override
            public void trackLoaded(@NotNull AudioTrack audioTrack) {
                event.getHook().sendMessage(":notes: | Added **"+audioTrack.getInfo().title+"** to the queue.").queue();
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
                event.getHook().sendMessage(":notes: | Added **"+audioPlaylist.getName()+"** with `"+total+"` songs to the queue.").queue();

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

    @Override
    public void onGuildVoiceMove(@NotNull GuildVoiceMoveEvent event) {
        if (event.getJDA().getSelfUser().getIdLong() == event.getMember().getIdLong()) {
            GuildData data = GuildData.get(event.getGuild());
            if (data.musicHandler != null) {
                data.musicHandler.setPlayChannel(event.getChannelJoined());
            }
        }
    }

    @Override
    public void onGuildVoiceLeave(@NotNull GuildVoiceLeaveEvent event) {
        if (event.getJDA().getSelfUser().getIdLong() == event.getMember().getIdLong()) {
            GuildData data = GuildData.get(event.getGuild());
            if (data.musicHandler != null) {
                data.musicHandler.disconnect();
            }
        }
    }
}
