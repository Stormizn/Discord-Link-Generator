package me.stormizn.bot;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.stormizn.bot.commands.*;
import me.stormizn.bot.configs.ConfigManager;
import me.stormizn.bot.listeners.*;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class DiscordBot {

    public static String PASTEBIN_API_KEY;
    public static ConfigManager CONFIG_MANAGER;

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

        CONFIG_MANAGER = new ConfigManager();

        JDA jda = JDABuilder.createDefault(token)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .addEventListeners(
                        new UploadCommand(),
                        new AutoUploadListener(autoChannels),
                        new FAQCommand(),
                        new ThreadCommand(),
                        new OutdatedCommand(),
                        new WebsiteCommand(),
                        new ConfigCommand(),
                        new AntiPingListener(),
                        new AntiSpamListener()
                )
                .build();

        // register slash cmds
        jda.updateCommands().addCommands(
                Commands.slash("upload", "Upload a file to mclo.gs or pastebin")
                        .addOption(OptionType.ATTACHMENT, "file",
                                "The file to upload", true)
                        .addOption(OptionType.STRING, "service",
                                "mclogs (default) or pastebin", false, true),
                Commands.slash("faq", "Manage FAQ entries")
                        .addSubcommands(
                                new SubcommandData("add", "Add an FAQ entry")
                                        .addOption(OptionType.STRING, "key",
                                                "FAQ key", true, true)
                                        .addOption(OptionType.STRING, "answer",
                                                "FAQ answer", true),
                                new SubcommandData("remove", "Remove an FAQ entry")
                                        .addOption(OptionType.STRING, "key",
                                                "FAQ key", true, true),
                                new SubcommandData("get", "Get an FAQ entry")
                                        .addOption(OptionType.STRING, "key",
                                                "FAQ key", true, true),
                                new SubcommandData("list", "List all FAQ entries")
                        ),
                Commands.slash("thread", "Manage threads")
                        .addSubcommands(
                                new SubcommandData("resolve", "Mark thread as resolved and archive"),
                                new SubcommandData("unresolve", "Reopen a resolved thread"),
                                new SubcommandData("stale", "Mark thread as stale")
                        ),
                Commands.slash("outdated", "Show outdated version message"),
                Commands.slash("website", "Show the project website"),
                Commands.slash("config", "Configure bot settings")
                        .addSubcommands(
                                new SubcommandData("help-channel-add", "Add a help channel")
                                        .addOption(OptionType.CHANNEL, "channel",
                                                "The channel to add", true),
                                new SubcommandData("help-channel-remove", "Remove a help channel")
                                        .addOption(OptionType.CHANNEL, "channel",
                                                "The channel to remove", true),
                                new SubcommandData("core-role-add", "Add a core team role")
                                        .addOption(OptionType.ROLE, "role",
                                                "The role to add", true),
                                new SubcommandData("core-role-remove", "Remove a core team role")
                                        .addOption(OptionType.ROLE, "role",
                                                "The role to remove", true),
                                new SubcommandData("spam-threshold", "Set spam message count threshold")
                                        .addOption(OptionType.INTEGER, "count",
                                                "Max messages in the window", true, true),
                                new SubcommandData("spam-window", "Set spam detection window in seconds")
                                        .addOption(OptionType.INTEGER, "seconds",
                                                "Time window in seconds", true, true),
                                new SubcommandData("website", "Set the website URL")
                                        .addOption(OptionType.STRING, "url",
                                                "The website URL", true, true),
                                new SubcommandData("outdated-message", "Set the outdated version message")
                                        .addOption(OptionType.STRING, "message",
                                                "The message to show", true, true),
                                new SubcommandData("stale-tag", "Set the stale thread tag name")
                                        .addOption(OptionType.STRING, "name",
                                                "Tag name e.g. Stale", true, true),
                                new SubcommandData("resolved-tag", "Set the resolved thread tag name")
                                        .addOption(OptionType.STRING, "name",
                                                "Tag name e.g. Resolved", true, true),
                                new SubcommandData("show", "Show current bot configuration")
                        )
        ).queue();

        jda.awaitReady();
        System.out.println("Bot is ready!");
    }
}
