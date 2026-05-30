package me.stormizn.bot.commands;

import me.stormizn.bot.DiscordBot;
import me.stormizn.bot.configs.GuildConfig;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

public class FAQCommand extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!event.getName().equals("faq")) return;
        if (!event.isFromGuild()) return;

        event.deferReply().queue();

        String guildId = event.getGuild().getId();
        GuildConfig config = DiscordBot.CONFIG_MANAGER.get(guildId);
        LinkedHashMap<String, String> faq = config.getFaq();
        String sub = event.getSubcommandName();

        // route subcommands
        switch (sub) {
            case "add" -> {
                String key = event.getOption("key").getAsString()
                        .toLowerCase().replaceAll("\\s+", "_");
                String answer = event.getOption("answer").getAsString();
                faq.put(key, answer);
                DiscordBot.CONFIG_MANAGER.save();
                event.getHook().sendMessageEmbeds(new EmbedBuilder()
                        .setColor(Color.GREEN)
                        .setTitle("FAQ Added")
                        .setDescription("**" + key + "** has been added.")
                        .setTimestamp(java.time.Instant.now())
                        .build()
                ).queue();
            }
            // remove an faq entry
            case "remove" -> {
                String key = event.getOption("key").getAsString()
                        .toLowerCase().replaceAll("\\s+", "_");
                if (faq.remove(key) != null) {
                    DiscordBot.CONFIG_MANAGER.save();
                    event.getHook().sendMessageEmbeds(new EmbedBuilder()
                            .setColor(Color.GREEN)
                            .setTitle("FAQ Removed")
                            .setDescription("**" + key + "** has been removed.")
                            .setTimestamp(java.time.Instant.now())
                            .build()
                    ).queue();
                } else {
                    event.getHook().sendMessageEmbeds(new EmbedBuilder()
                            .setColor(Color.RED)
                            .setTitle("FAQ Not Found")
                            .setDescription("No entry for **" + key + "**.")
                            .setTimestamp(java.time.Instant.now())
                            .build()
                    ).queue();
                }
            }
            // list all faq entries
            case "list" -> {
                if (faq.isEmpty()) {
                    event.getHook().sendMessageEmbeds(new EmbedBuilder()
                            .setColor(Color.RED)
                            .setTitle("FAQ List")
                            .setDescription("No FAQ entries configured.")
                            .setTimestamp(java.time.Instant.now())
                            .build()
                    ).queue();
                    return;
                }
                StringBuilder sb = new StringBuilder();
                faq.forEach((k, v) -> {
                    String preview = v.length() > 100 ? v.substring(0, 97) + "..." : v;
                    sb.append("**").append(k).append("** — ").append(preview).append("\n");
                });
                event.getHook().sendMessageEmbeds(new EmbedBuilder()
                        .setColor(new Color(0x5865F2))
                        .setTitle("FAQ List (" + faq.size() + ")")
                        .setDescription(sb.toString())
                        .setTimestamp(java.time.Instant.now())
                        .build()
                ).queue();
            }
            // get a single faq entry
            case "get" -> {
                String key = event.getOption("key").getAsString()
                        .toLowerCase().replaceAll("\\s+", "_");
                String answer = faq.get(key);
                if (answer != null) {
                    event.getHook().sendMessageEmbeds(new EmbedBuilder()
                            .setColor(new Color(0x5865F2))
                            .setTitle("FAQ: " + key)
                            .setDescription(answer)
                            .setTimestamp(java.time.Instant.now())
                            .build()
                    ).queue();
                } else {
                    event.getHook().sendMessageEmbeds(new EmbedBuilder()
                            .setColor(Color.RED)
                            .setTitle("FAQ Not Found")
                            .setDescription("No entry for **" + key + "**. Use `/faq list`.")
                            .setTimestamp(java.time.Instant.now())
                            .build()
                    ).queue();
                }
            }
        }
    }

    // auto-complete faq keys for add/get/remove
    @Override
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event) {
        if (!event.getName().equals("faq")) return;
        if (!event.isFromGuild()) return;
        if (!event.getFocusedOption().getName().equals("key")) return;

        String guildId = event.getGuild().getId();
        GuildConfig config = DiscordBot.CONFIG_MANAGER.get(guildId);
        String typed = event.getFocusedOption().getValue().toLowerCase();

        List<Command.Choice> choices = config.getFaq().keySet().stream()
                .filter(k -> k.startsWith(typed))
                .limit(25)
                .map(k -> new Command.Choice(k, k))
                .collect(Collectors.toList());

        event.replyChoices(choices).queue();
    }
}
