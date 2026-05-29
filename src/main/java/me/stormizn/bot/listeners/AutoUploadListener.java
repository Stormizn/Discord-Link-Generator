package me.stormizn.bot.listeners;

import me.stormizn.bot.services.MclogsService;
import me.stormizn.bot.services.PasteService;
import me.stormizn.bot.services.PastebinService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.Color;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Set;

public class AutoUploadListener extends ListenerAdapter {

    // can add more acc to need
    private static final Set<String> TEXT_EXTENSIONS = Set.of(
            "txt", "log", "yml", "yaml", "json", "xml", "cfg", "conf",
            "properties", "md", "java", "py", "js", "ts", "kt", "gradle",
            "toml", "ini", "sh", "bat", "env", "gitignore", "csv"
    );

    // only process listener in these channels
    private final Set<String> allowedChannels;

    public AutoUploadListener(Set<String> allowedChannels) {
        this.allowedChannels = allowedChannels;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        if (!event.isFromGuild()) return;
        if (!allowedChannels.contains(event.getChannel().getId())) return;

        List<Message.Attachment> attachments = event.getMessage().getAttachments();
        if (attachments.isEmpty()) return;

        Message.Attachment file = attachments.get(0);
        String fileName = file.getFileName();
        String ext = fileName.contains(".")
                ? fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase()
                : "";
        if (!TEXT_EXTENSIONS.contains(ext)) return;
        if (file.getSize() > 10_000_000) return; // skips files > 10mb

        event.getChannel().sendTyping().queue();

        file.getProxy().downloadToPath()
                .thenApplyAsync(path -> {
                    try {
                        byte[] bytes = Files.readAllBytes(path);
                        Files.deleteIfExists(path);
                        return new String(bytes, StandardCharsets.UTF_8);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .thenApplyAsync(content -> {
                    try {
                        return uploadWithFallback(content, fileName,
                                new MclogsService(), new PastebinService());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .thenAccept(url -> {
                    EmbedBuilder embed = new EmbedBuilder()
                            .setColor(new Color(0x5865F2))
                            .setAuthor(event.getAuthor().getName(), null, event.getAuthor().getEffectiveAvatarUrl())
                            .setTitle("📎 " + fileName)
                            .setDescription(url)
                            .addField("Size", formatSize(file.getSize()), true)
                            .setFooter("Auto-uploaded")
                            .setTimestamp(java.time.Instant.now());

                    event.getMessage().replyEmbeds(embed.build())
                            .mentionRepliedUser(false).queue();
                })
                .exceptionally(error -> {
                    Throwable cause = error.getCause() instanceof RuntimeException ? error.getCause().getCause() : error.getCause();

                    EmbedBuilder embed = new EmbedBuilder()
                            .setColor(Color.RED)
                            .setTitle("Upload Failed")
                            .setDescription(cause != null ? cause.getMessage() : error.getMessage())
                            .setFooter("Auto-upload error")
                            .setTimestamp(java.time.Instant.now());

                    event.getChannel().sendMessageEmbeds(embed.build()).queue();
                    return null;
                });
    }


    // fallback system
    private String uploadWithFallback(String content, String filename,
                                      PasteService primary, PasteService fallback) throws Exception {
        try {
            return primary.upload(content, filename);
        } catch (Exception e) {
            if (fallback != null) {
                return fallback.upload(content, filename);
            }
            throw e;
        }
    }

    // formats file size
    private String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
    }

}
