package me.stormizn.bot.listeners;

import me.stormizn.bot.DiscordBot;
import me.stormizn.bot.configs.GuildConfig;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.util.Set;

public class AntiPingListener extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        if (!event.isFromGuild()) return;

        String guildId = event.getGuild().getId();
        GuildConfig config = DiscordBot.CONFIG_MANAGER.get(guildId);
        Set<String> coreRoles = config.getCoreRoles();
        Set<String> helpChannels = config.getHelpChannels();

        // nothing to guard against
        if (coreRoles.isEmpty()) return;
        // allow pings in help channels
        if (helpChannels.contains(event.getChannel().getId())) return;

        // core members can ping each other
        Member member = event.getMember();
        if (member != null) {
            boolean isCoreMember = member.getRoles().stream()
                    .map(Role::getId)
                    .anyMatch(coreRoles::contains);
            if (isCoreMember) return;
        }

        // check if message contains core role pings
        boolean pingedCore = event.getMessage().getMentions().getRoles().stream()
                .map(Role::getId)
                .anyMatch(coreRoles::contains);

        if (!pingedCore) return;

        // delete and warn
        event.getMessage().delete().queue();
        event.getChannel().sendMessageEmbeds(new EmbedBuilder()
                .setColor(Color.RED)
                .setTitle("Improper Ping")
                .setDescription(event.getAuthor().getAsMention()
                        + ", please do not ping core team members outside of help channels.")
                .setFooter("Anti-ping")
                .setTimestamp(java.time.Instant.now())
                .build()
        ).queue();

    }
}
