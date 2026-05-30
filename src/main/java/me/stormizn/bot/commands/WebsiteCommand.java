package me.stormizn.bot.commands;

import me.stormizn.bot.DiscordBot;
import me.stormizn.bot.configs.GuildConfig;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;

public class WebsiteCommand extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!event.getName().equals("website")) return;

        String url = DiscordBot.CONFIG_MANAGER.get(
                event.isFromGuild() ? event.getGuild().getId() : "0"
        ).getWebsiteUrl();

        event.replyEmbeds(new EmbedBuilder()
                .setColor(new Color(0x5865F2))
                .setTitle("Website")
                .setDescription(url)
                .setTimestamp(java.time.Instant.now())
                .build()
        ).queue();
    }
}
