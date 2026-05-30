package me.stormizn.bot.listeners;

import me.stormizn.bot.DiscordBot;
import me.stormizn.bot.configs.GuildConfig;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AntiSpamListener extends ListenerAdapter {

    private final Map<String, ArrayDeque<Long>> messageTimes = new ConcurrentHashMap<>();

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        if (!event.isFromGuild()) return;

        String guildId = event.getGuild().getId();
        GuildConfig config = DiscordBot.CONFIG_MANAGER.get(guildId);
        int threshold = config.getSpamThreshold();
        int windowSeconds = config.getSpamWindowSeconds();
        if (threshold <= 0) return;

        // track messages per user in a sliding time window
        String key = guildId + ":" + event.getAuthor().getId();
        long now = Instant.now().toEpochMilli();
        long windowMs = windowSeconds * 1000L;

        ArrayDeque<Long> timestamps = messageTimes.computeIfAbsent(key, k -> new ArrayDeque<>());

        synchronized (timestamps) {
            // remove old entries outside the window
            while (!timestamps.isEmpty() && now - timestamps.peekFirst() > windowMs) {
                timestamps.pollFirst();
            }
            timestamps.addLast(now);
            // threshold exceeded
            if (timestamps.size() > threshold) {
                event.getMessage().delete().queue(success -> {
                    event.getChannel().sendMessageEmbeds(new EmbedBuilder()
                            .setColor(Color.ORANGE)
                            .setTitle("Spam Detected")
                            .setDescription(event.getAuthor().getAsMention()
                                    + ", please slow down.")
                            .setFooter("Anti-spam")
                            .setTimestamp(Instant.now())
                            .build()
                    ).queue();
                }, failure -> {});
                timestamps.clear();
            }
        }
    }
}
