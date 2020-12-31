package com.technovision.technobot.listeners.managers;

import com.technovision.technobot.TechnoBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.commons.io.IOUtils;
import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.GistFile;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.GistService;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.concurrent.ExecutionException;

public class AutoPastebinManager extends ListenerAdapter {
    private final String GITHUB_TOKEN;

    public AutoPastebinManager(final TechnoBot bot) {
        GITHUB_TOKEN = bot.getBotConfig().getJson().getString("github-token");
    }

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        if (!event.getAuthor().isBot()) {
            if (!event.getMessage().getAttachments().isEmpty()) {
                Message.Attachment logOrJavaFile = event.getMessage().getAttachments().stream()
                        .filter(attachment -> attachment.getFileExtension().equals("log") ||
                                              attachment.getFileExtension().equals("java") ||
                                              attachment.getFileExtension().equals("txt") ||
                                              attachment.getFileExtension().equals("kt") ||
                                              attachment.getFileExtension().equals("json") ||
                                              attachment.getFileExtension().equals("gradle")
                        )
                        .findFirst()
                        .orElseThrow(() ->
                                new NullPointerException("Couldn't find a recognizable code or log file."));

                GitHubClient gitHubClient = new GitHubClient();
                gitHubClient.setOAuth2Token(GITHUB_TOKEN);

                Gist gist = new Gist();

                try {
                    GistFile gistFile = new GistFile();
                    String stringifiedFile = IOUtils.toString(logOrJavaFile.retrieveInputStream().get(), Charset.defaultCharset());
                    gistFile.setContent(!stringifiedFile.isEmpty() ? stringifiedFile : "idk why this file is empty.");

                    gist.setFiles(Collections.singletonMap(logOrJavaFile.getFileName(), gistFile));
                    gist.setDescription("My stuff");

                    GistService gistService = new GistService();
                    gistService.getClient().setOAuth2Token(GITHUB_TOKEN);

                    gist = gistService.createGist(gist);

                } catch (InterruptedException | ExecutionException | IOException e) {
                    e.printStackTrace();
                }

                MessageEmbed embed = new EmbedBuilder()
                        .setTitle("Send in GH Gist!")
                        .addField("We prefer not to download files, so we've made a Gist for you!", gist.getHtmlUrl(), false)
                        .addField("WARNING", "Please refrain from sending files in the future, and use Pastebin or GH Gists instead.", false)
                        .build();

                event.getChannel().sendMessage(embed).queue();
            }
        }
    }
}
