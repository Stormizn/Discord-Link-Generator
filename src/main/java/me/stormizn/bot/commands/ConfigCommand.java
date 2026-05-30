package me.stormizn.bot.commands;

import me.stormizn.bot.DiscordBot;
import me.stormizn.bot.configs.GuildConfig;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.util.List;
import java.util.Set;

public class ConfigCommand extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!event.getName().equals("config")) return;
        if (!event.isFromGuild()) return;

        // admin-only command
        var member = event.getMember();
        if (member == null || !member.hasPermission(Permission.ADMINISTRATOR)) {
            event.replyEmbeds(new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("No Permission")
                    .setDescription("You need Administrator permission.")
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
            case "help-channel-add" -> {
                Channel c = event.getOption("channel").getAsChannel();
                config.getHelpChannels().add(c.getId());
                DiscordBot.CONFIG_MANAGER.save();
                event.getHook().sendMessageEmbeds(new EmbedBuilder()
                        .setColor(Color.GREEN)
                        .setDescription(c.getAsMention() + " is now a help channel.")
                        .build()
                ).queue();
            }
            // remove a help channel
            case "help-channel-remove" -> {
                Channel c = event.getOption("channel").getAsChannel();
                config.getHelpChannels().remove(c.getId());
                DiscordBot.CONFIG_MANAGER.save();
                event.getHook().sendMessageEmbeds(new EmbedBuilder()
                        .setColor(Color.GREEN)
                        .setDescription(c.getAsMention() + " is no longer a help channel.")
                        .build()
                ).queue();
            }
            // add a core team role
            case "core-role-add" -> {
                Role r = event.getOption("role").getAsRole();
                config.getCoreRoles().add(r.getId());
                DiscordBot.CONFIG_MANAGER.save();
                event.getHook().sendMessageEmbeds(new EmbedBuilder()
                        .setColor(Color.GREEN)
                        .setDescription(r.getAsMention() + " is now a core team role.")
                        .build()
                ).queue();
            }
            // remove a core team role
            case "core-role-remove" -> {
                Role r = event.getOption("role").getAsRole();
                config.getCoreRoles().remove(r.getId());
                DiscordBot.CONFIG_MANAGER.save();
                event.getHook().sendMessageEmbeds(new EmbedBuilder()
                        .setColor(Color.GREEN)
                        .setDescription(r.getAsMention() + " is no longer a core team role.")
                        .build()
                ).queue();
            }
            // anti-spam settings
            case "spam-threshold" -> {
                int v = event.getOption("count").getAsInt();
                config.setSpamThreshold(v);
                DiscordBot.CONFIG_MANAGER.save();
                event.getHook().sendMessageEmbeds(new EmbedBuilder()
                        .setColor(Color.GREEN)
                        .setDescription("Spam threshold set to **" + v + "** messages.")
                        .build()
                ).queue();
            }
            case "spam-window" -> {
                int v = event.getOption("seconds").getAsInt();
                config.setSpamWindowSeconds(v);
                DiscordBot.CONFIG_MANAGER.save();
                event.getHook().sendMessageEmbeds(new EmbedBuilder()
                        .setColor(Color.GREEN)
                        .setDescription("Spam window set to **" + v + "** seconds.")
                        .build()
                ).queue();
            }
            // command config
            case "website" -> {
                String v = event.getOption("url").getAsString();
                config.setWebsiteUrl(v);
                DiscordBot.CONFIG_MANAGER.save();
                event.getHook().sendMessageEmbeds(new EmbedBuilder()
                        .setColor(Color.GREEN)
                        .setDescription("Website URL set to **" + v + "**.")
                        .build()
                ).queue();
            }
            case "outdated-message" -> {
                String v = event.getOption("message").getAsString();
                config.setOutdatedMessage(v);
                DiscordBot.CONFIG_MANAGER.save();
                event.getHook().sendMessageEmbeds(new EmbedBuilder()
                        .setColor(Color.GREEN)
                        .setDescription("Outdated message updated.")
                        .build()
                ).queue();
            }
            // thread tag config
            case "stale-tag" -> {
                String v = event.getOption("name").getAsString();
                config.setStaleTag(v);
                DiscordBot.CONFIG_MANAGER.save();
                event.getHook().sendMessageEmbeds(new EmbedBuilder()
                        .setColor(Color.GREEN)
                        .setDescription("Stale tag set to **" + v + "**.")
                        .build()
                ).queue();
            }
            case "resolved-tag" -> {
                String v = event.getOption("name").getAsString();
                config.setResolvedTag(v);
                DiscordBot.CONFIG_MANAGER.save();
                event.getHook().sendMessageEmbeds(new EmbedBuilder()
                        .setColor(Color.GREEN)
                        .setDescription("Resolved tag set to **" + v + "**.")
                        .build()
                ).queue();
            }
            // show all current settings
            case "show" -> {
                Set<String> help = config.getHelpChannels();
                Set<String> roles = config.getCoreRoles();
                StringBuilder sb = new StringBuilder();
                sb.append("**Help Channels:**\n");
                if (help.isEmpty()) sb.append("*None*\n");
                else help.forEach(id -> sb.append("<#").append(id).append(">\n"));
                sb.append("\n**Core Roles:**\n");
                if (roles.isEmpty()) sb.append("*None*\n");
                else roles.forEach(id -> sb.append("<@&").append(id).append(">\n"));
                sb.append("\n**Spam:** ").append(config.getSpamThreshold())
                        .append(" msgs / ").append(config.getSpamWindowSeconds()).append("s\n");
                sb.append("**Website:** ").append(config.getWebsiteUrl()).append("\n");
                sb.append("**Stale Tag:** ").append(config.getStaleTag()).append("\n");
                sb.append("**Resolved Tag:** ").append(config.getResolvedTag()).append("\n");
                sb.append("**FAQ Entries:** ").append(config.getFaq().size());

                event.getHook().sendMessageEmbeds(new EmbedBuilder()
                        .setColor(new Color(0x5865F2))
                        .setTitle("Bot Configuration")
                        .setDescription(sb.toString())
                        .setTimestamp(java.time.Instant.now())
                        .build()
                ).queue();
            }
        }
    }

    // auto-complete config values with current settings
    @Override
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event) {
        if (!event.getName().equals("config")) return;
        if (!event.isFromGuild()) return;

        String guildId = event.getGuild().getId();
        GuildConfig config = DiscordBot.CONFIG_MANAGER.get(guildId);
        String sub = event.getSubcommandName();
        String opt = event.getFocusedOption().getName();
        String typed = event.getFocusedOption().getValue().toLowerCase();

        String current = switch (sub) {
            case "spam-threshold" -> String.valueOf(config.getSpamThreshold());
            case "spam-window" -> String.valueOf(config.getSpamWindowSeconds());
            case "website" -> config.getWebsiteUrl();
            case "outdated-message" -> config.getOutdatedMessage();
            case "stale-tag" -> config.getStaleTag();
            case "resolved-tag" -> config.getResolvedTag();
            default -> null;
        };

        if (current == null || current.isBlank()) {
            event.replyChoices(List.of()).queue();
            return;
        }

        if (!current.toLowerCase().startsWith(typed)) {
            event.replyChoices(List.of()).queue();
            return;
        }

        event.replyChoices(List.of(new Command.Choice(current, current))).queue();
    }
}
