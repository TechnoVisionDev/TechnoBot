package technobot.data.cache;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.entities.AudioChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import technobot.handlers.MusicHandler;
import technobot.util.EmbedColor;
import technobot.util.EmbedUtils;

import java.nio.ByteBuffer;
import java.util.LinkedList;

/**
 * Handles music for each guild with a unique queue and audio player for each.
 *
 * @author TechnoVision, Sparky
 */
public class MusicPlayer implements AudioSendHandler {

    /** LavaPlayer essentials. */
    public final @NotNull AudioPlayer audioPlayer;
    private AudioFrame lastFrame;

    /** Queue of music tacks in FIFO order. */
    private final @NotNull LinkedList<AudioTrack> queue;

    /** The text channel in which the bot is logging music actions. */
    private TextChannel logChannel;

    /** The voice channel in which the bot is playing music. */
    private @Nullable AudioChannel playChannel;

    /** Whether the music player is on loop. */
    private boolean isLoop;

    public MusicPlayer(@NotNull AudioPlayer audioPlayer) {
        this.audioPlayer = audioPlayer;
        this.queue = new LinkedList<>();
        this.isLoop = false;
        TrackScheduler scheduler = new TrackScheduler(this);
        audioPlayer.addListener(scheduler);
    }

    /**
     * Queue a new song to the audio player.
     *
     * @param track audio track to be queued.
     */
    public void enqueue(AudioTrack track) {
        queue.addLast(track);
        if (audioPlayer.getPlayingTrack() == null) {
            audioPlayer.playTrack(queue.getFirst());
        }
    }

    /**
     * Pause audio player.
     */
    public void pause() {
        audioPlayer.setPaused(true);
    }

    /**
     * Resume audio player.
     */
    public void unpause() {
        audioPlayer.setPaused(false);
    }

    /**
     * Check is audio player is paused.
     */
    public boolean isPaused() {
        return audioPlayer.isPaused();
    }

    /**
     * Disconnects from the voice channel and clears queue.
     */
    public void disconnect() {
        playChannel = null;
        stop();
    }

    /**
     * Removes all tracks from the audio player queue and stops playing the current track.
     */
    public void stop() {
        queue.clear();
        audioPlayer.stopTrack();
    }

    /**
     * Sets the volume level of the audio player.
     *
     * @param volume volume level between 0-100
     */
    public void setVolume(int volume) {
        audioPlayer.setVolume(volume);
    }

    /**
     * Skips the current playing track.
     */
    public void skipTrack() {
        isLoop = false;
        audioPlayer.getPlayingTrack().setPosition(audioPlayer.getPlayingTrack().getDuration());
    }

    /**
     * Skips to a specified track in the queue.
     *
     * @param pos Position in the queue to skip to.
     */
    public void skipTo(int pos) {
        if (pos > 1) {
            queue.subList(1, pos).clear();
        }
        skipTrack();
    }

    /**
     * Sets the position of the current track.
     *
     * @param position position in current track in milliseconds.
     */
    public void seek(long position) {
        audioPlayer.getPlayingTrack().setPosition(position);
    }

    /**
     * Get the audio player queue.
     *
     * @return list of tracks in queue. Returns copy to avoid external modification.
     */
    public @NotNull LinkedList<AudioTrack> getQueue() {
        return new LinkedList<>(queue);
    }

    /**
     * Get the voice channel the bot is playing music in.
     *
     * @return voice channel playing music.
     */
    public @Nullable AudioChannel getPlayChannel() {
        return playChannel;
    }

    /**
     * Sets the music play channel.
     *
     * @param channel voice channel to set as play channel.
     */
    public void setPlayChannel(@Nullable AudioChannel channel) {
        playChannel = channel;
    }

    /**
     * Get the text channel that logs music related info and commands.
     *
     * @return text channel logging music info.
     */
    public TextChannel getLogChannel() {
        return logChannel;
    }

    /**
     * Sets the music log channel.
     *
     * @param channel text channel to set as log channel.
     */
    public void setLogChannel(TextChannel channel) {
        logChannel = channel;
    }

    /**
     * Determines whether track is looping.
     *
     * @return true or false based on isLoop
     */
    public boolean isLoop() {
        return isLoop;
    }

    /**
     * Flips loop status like a switch.
     */
    public void loop() {
        isLoop = !isLoop;
    }

    @Override
    public boolean canProvide() {
        lastFrame = audioPlayer.provide();
        return lastFrame != null;
    }

    @Override
    public boolean isOpus() {
        return true;
    }

    @Nullable
    @Override
    public ByteBuffer provide20MsAudio() {
        return ByteBuffer.wrap(lastFrame.getData());
    }

    /**
     * Manages audio events and schedules tracks.
     */
    public static class TrackScheduler extends AudioEventAdapter {

        private final MusicPlayer handler;

        public TrackScheduler(MusicPlayer handler) {
            this.handler = handler;
        }

        /**
         * Creates an embed message when a track starts that displays relevant info.
         *
         * @param player the audio player
         * @param track  the track that is starting.
         */
        @Override
        public void onTrackStart(AudioPlayer player, @NotNull AudioTrack track) {
            //Grab Track Info
            String duration = MusicHandler.formatTrackLength(track.getInfo().length);
            String thumb = String.format("https://img.youtube.com/vi/%s/0.jpg", track.getInfo().uri.substring(32));
            String nextTrack = "Nothing";
            if (handler.queue.size() > 1) {
                AudioTrackInfo info = handler.queue.get(1).getInfo();
                nextTrack = "[" + info.title + "](" + info.uri + ")";
            }
            //Create Embed Message
            handler.logChannel.sendMessageEmbeds(
                    new EmbedBuilder()
                            .setTitle("Now Playing")
                            .setDescription("[" + track.getInfo().title + "](" + track.getInfo().uri + ")")
                            .addField("Song Duration", duration, true)
                            .addField("Up Next", nextTrack, true)
                            .setColor(EmbedColor.DEFAULT.color)
                            .setThumbnail(thumb)
                            .build()
            ).queue();
        }

        @Override
        public void onTrackEnd(@NotNull AudioPlayer player, @NotNull AudioTrack track, @NotNull AudioTrackEndReason endReason) {
            if (handler.isLoop()) {
                // Loop current track
                handler.queue.set(0, track.makeClone());
                player.playTrack(handler.queue.getFirst());
            } else if (!handler.queue.isEmpty()) {
                // Play next track in queue
                handler.queue.removeFirst();
                if (endReason.mayStartNext && handler.queue.size() > 0) {
                    player.playTrack(handler.queue.getFirst());
                }
            }
        }

        @Override
        public void onTrackException(AudioPlayer player, AudioTrack track, @NotNull FriendlyException exception) {
            String msg = "An error occurred! " + exception.getMessage();
            handler.logChannel.sendMessageEmbeds(EmbedUtils.createError(msg)).queue();
            exception.printStackTrace();
        }

        @Override
        public void onTrackStuck(@NotNull AudioPlayer player, AudioTrack track, long thresholdMs) {
            String msg = "Track got stuck, attempting to fix...";
            handler.logChannel.sendMessageEmbeds(EmbedUtils.createError(msg)).queue();
            handler.queue.remove(track);
            player.stopTrack();
            player.playTrack(handler.queue.getFirst());
        }
    }
}
