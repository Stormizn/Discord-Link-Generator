package me.stormizn.bot.commands;

import me.stormizn.bot.services.MclogsService;
import me.stormizn.bot.services.PasteService;
import me.stormizn.bot.services.PastebinService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Set;

public class UploadCommand extends ListenerAdapter {

    private static final Set<String> TEXT_EXTENSIONS = Set.of(
            "txt", "log", "yml", "yaml", "json", "xml", "cfg", "conf",
            "properties", "md", "java", "py", "js", "ts", "kt", "gradle",
            "toml", "ini", "sh", "bat", "env", "gitignore", "csv"
    );

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!event.getName().equals("upload")) return;

        event.deferReply().queue();

        var file = event.getOption("file").getAsAttachment();
        String fileName = file.getFileName();
        String ext = fileName.contains(".")
                ? fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase()
                : "";
        if (!TEXT_EXTENSIONS.contains(ext)) {
            EmbedBuilder embed = new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Unsupported File Type")
                    .setDescription("`." + ext + "` files are not supported. Please upload a text or log file.")
                    .setFooter("Upload error")
                    .setTimestamp(java.time.Instant.now());
            event.getHook().sendMessageEmbeds(embed.build()).queue();
            return;
        }

        boolean usePastebin = event.getOption("service") != null
                && event.getOption("service").getAsString().equalsIgnoreCase("pastebin");

        // download content
        file.getProxy().downloadToPath()
                // read content
                .thenApplyAsync(path -> {
                    try {
                        byte[] bytes = Files.readAllBytes(path);
                        Files.deleteIfExists(path);
                        return new String(bytes, StandardCharsets.UTF_8);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                // upload content
                .thenApplyAsync(content -> {
                    try {
                        if (usePastebin) {
                            return uploadWithFallback(content, file.getFileName(),
                                    new PastebinService(), null);
                        }
                        return uploadWithFallback(content, file.getFileName(),
                                new MclogsService(), new PastebinService());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                // send url
                .thenAccept(url -> {
                    EmbedBuilder embed = new EmbedBuilder()
                            .setColor(new Color(0x5865F2))
                            .setAuthor(event.getUser().getName(), null, event.getUser().getEffectiveAvatarUrl())
                            .setTitle("📎 " + file.getFileName())
                            .setDescription(url)
                            .addField("Size", formatSize(file.getSize()), true)
                            .addField("Service", usePastebin ? "Pastebin" : "mclo.gs", true)
                            .setFooter("Uploaded")
                            .setTimestamp(java.time.Instant.now());

                    event.getHook().sendMessageEmbeds(embed.build()).queue();
                })
                // error handling
                .exceptionally(error -> {
                    Throwable cause = error.getCause() instanceof RuntimeException
                            ? error.getCause().getCause() : error.getCause();

                    EmbedBuilder embed = new EmbedBuilder()
                            .setColor(Color.RED)
                            .setTitle("Upload Failed")
                            .setDescription(cause != null ? cause.getMessage() : error.getMessage())
                            .setFooter("Upload error")
                            .setTimestamp(java.time.Instant.now());

                    event.getHook().sendMessageEmbeds(embed.build()).queue();
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

    // auto-complete service option
    @Override
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event) {
        if (!event.getName().equals("upload")) return;
        if (!event.getFocusedOption().getName().equals("service")) return;

        String typed = event.getFocusedOption().getValue().toLowerCase();
        List<Command.Choice> choices = List.of("mclogs", "pastebin").stream()
                .filter(s -> s.startsWith(typed))
                .map(s -> new Command.Choice(s, s))
                .toList();

        event.replyChoices(choices).queue();
    }

    // formats file size
    private String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
    }

}
