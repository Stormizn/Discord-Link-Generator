package me.stormizn.bot.configs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConfigManager {

    // file where guild configs are
    private static final Path CONFIG_PATH = Path.of("guild_configs.json");
    private final Map<String, GuildConfig> configs = new ConcurrentHashMap<>();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public ConfigManager() {
        load();
    }

    public GuildConfig get(String guildID) {
        return configs.computeIfAbsent(guildID, k -> new GuildConfig());
    }

    // persists all guild configs to disk
    public synchronized void save() {
        try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
            gson.toJson(configs, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // loads guild configs from disk
    private void load() {
        if (!Files.exists(CONFIG_PATH)) return;
        try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
            Type type = new TypeToken<Map<String, GuildConfig>>() {}.getType();
            Map<String, GuildConfig> loaded = gson.fromJson(reader, type);
            if (loaded != null) configs.putAll(loaded);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
