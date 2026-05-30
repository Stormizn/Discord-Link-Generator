package me.stormizn.bot.commands;

import me.stormizn.bot.DiscordBot;
import me.stormizn.bot.configs.GuildConfig;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;

public class OutdatedCommand extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!event.getName().equals("outdated")) return;

        String message = DiscordBot.CONFIG_MANAGER.get(
                event.isFromGuild() ? event.getGuild().getId() : "0"
        ).getOutdatedMessage();

        event.replyEmbeds(new EmbedBuilder()
                .setColor(Color.ORANGE)
                .setTitle("Outdated Version Detected")
                .setDescription(message)
                .setFooter("Please update")
                .setTimestamp(java.time.Instant.now())
                .build()
        ).queue();
    }
}
