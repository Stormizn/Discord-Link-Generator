package me.stormizn.bot;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.stormizn.bot.commands.UploadCommand;
import me.stormizn.bot.listeners.AutoUploadListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class DiscordBot {

    public static String PASTEBIN_API_KEY;

    public static void main(String[] args) throws Exception {

        // load config from env vars, falling back to config.json
        String token = System.getenv("BOT_TOKEN");
        PASTEBIN_API_KEY = System.getenv("PASTEBIN_API_KEY");
        Set<String> autoChannels = new HashSet<>();

        if (token == null || PASTEBIN_API_KEY == null) {
            Path configPath = Path.of("config.json");
            if (!Files.exists(configPath)) {
                configPath = Path.of("src/main/resources/config.json");
            }
            JsonObject config = JsonParser.parseString(Files.readString(configPath)).getAsJsonObject();
            if (token == null) token = config.get("token").getAsString();
            if (PASTEBIN_API_KEY == null) PASTEBIN_API_KEY = config.get("pastebinApiKey").getAsString();
            config.getAsJsonArray("autoUploadChannels").forEach(
                    e -> autoChannels.add(e.getAsString())
            );
        }

        JDA jda = JDABuilder.createDefault(token)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .addEventListeners(new UploadCommand(), new AutoUploadListener(autoChannels))
                .build();

        // register slash cmds
        jda.updateCommands().addCommands(
                Commands.slash("upload", "Upload a file to mclo.gs or pastebin")
                        .addOption(OptionType.ATTACHMENT, "file",
                                "The file to upload", true)
                        .addOption(OptionType.STRING, "service",
                                "mclogs (default) or pastebin", false)
        ).queue();

        jda.awaitReady();
        System.out.println("Bot is ready!");
    }
}
