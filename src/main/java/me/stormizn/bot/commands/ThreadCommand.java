package me.stormizn.bot.commands;

import me.stormizn.bot.DiscordBot;
import me.stormizn.bot.configs.GuildConfig;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;

public class ThreadCommand extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!event.getName().equals("thread")) return;
        if (!event.isFromGuild()) return;

        // must be used inside a thread channel
        if (!(event.getChannel() instanceof ThreadChannel thread)) {
            event.replyEmbeds(new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Not a Thread")
                    .setDescription("This command can only be used inside a thread.")
                    .setTimestamp(java.time.Instant.now())
                    .build()
            ).setEphemeral(true).queue();
            return;
        }

        event.deferReply().queue();

        String guildId = event.getGuild().getId();
        GuildConfig config = DiscordBot.CONFIG_MANAGER.get(guildId);
        String sub = event.getSubcommandName();

        // route subcommands
        switch (sub) {
            case "resolve" -> {
                String tag = config.getResolvedTag();
                thread.getManager()
                        .setName("[" + tag + "] " + stripTags(thread.getName()))
                        .setArchived(true)
                        .queue();
                event.getHook().sendMessageEmbeds(new EmbedBuilder()
                        .setColor(Color.GREEN)
                        .setTitle("Thread Resolved")
                        .setDescription("Marked as resolved and archived.")
                        .setTimestamp(java.time.Instant.now())
                        .build()
                ).queue();
            }
            // reopen and strip tags
            case "unresolve" -> {
                String cleaned = thread.getName()
                        .replace("[" + config.getResolvedTag() + "] ", "")
                        .replace("[" + config.getStaleTag() + "] ", "")
                        .trim();
                thread.getManager()
                        .setName(cleaned)
                        .setArchived(false)
                        .queue();
                event.getHook().sendMessageEmbeds(new EmbedBuilder()
                        .setColor(new Color(0x5865F2))
                        .setTitle("Thread Reopened")
                        .setDescription("Reopened and tags removed.")
                        .setTimestamp(java.time.Instant.now())
                        .build()
                ).queue();
            }
            // mark as stale
            case "stale" -> {
                String tag = config.getStaleTag();
                thread.getManager()
                        .setName("[" + tag + "] " + stripTags(thread.getName()))
                        .queue();
                event.getHook().sendMessageEmbeds(new EmbedBuilder()
                        .setColor(Color.ORANGE)
                        .setTitle("Thread Marked Stale")
                        .setDescription("This thread has been marked as stale.")
                        .setTimestamp(java.time.Instant.now())
                        .build()
                ).queue();
            }
        }
    }

    // removes leading [Tag] prefixes from thread names
    private String stripTags(String name) {
        return name.replaceAll("^\\[[^\\]]+\\]\\s*", "").trim();
    }
}
