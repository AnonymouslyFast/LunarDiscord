package com.anonymouslyfast.lunarDiscord.discord.listeners;

import com.anonymouslyfast.lunarDiscord.LunarDiscord;
import com.anonymouslyfast.lunarDiscord.utils.Colours;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

public class DiscordChatListener extends ListenerAdapter {

    private final FileConfiguration config = LunarDiscord.getInstance().getConfig();

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        if (event.getChannel().getId().equals(config.getString("minecraft-logs-channel-id"))) {
            Bukkit.getScheduler().runTask(LunarDiscord.getInstance(), () -> {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), event.getMessage().getContentDisplay());
            });
        }

        if (!event.getChannel().getId().equals(config.getString("minecraft-to-discord-channel-id"))) return;

        boolean isReply = false;
        String templateMessage = config.getString("to-minecraft-message-template");
        if (event.getMessage().getReferencedMessage() != null && config.getBoolean("show-replies-in-messages")) {
            isReply = true;
            templateMessage = config.getString("to-minecraft-message-with-reply-template");
        }

        // Filling out template
        String authorRole = event.getMember().getRoles().getFirst().getName();
        templateMessage = templateMessage.replace("%USER%", event.getAuthor().getName());
        templateMessage = templateMessage.replace("%NICKNAME%", event.getAuthor().getEffectiveName());
        templateMessage = templateMessage.replace("%USER_ROLE%" , authorRole);
        templateMessage = templateMessage.replace("%MESSAGE%", event.getMessage().getContentDisplay());
        if (isReply && event.getMessage().getReferencedMessage().getMember() != null) {
            String replyAuthorRole = event.getMessage().getReferencedMessage().getMember().getRoles().getFirst().getName();
            templateMessage = templateMessage.replace("%REPLY_USER%", event.getMessage().getReferencedMessage().getAuthor().getName());
            templateMessage = templateMessage.replace("%REPLY_NICKNAME%", event.getMessage().getReferencedMessage().getAuthor().getEffectiveName());
            templateMessage = templateMessage.replace("%REPLY_USER_ROLE%", replyAuthorRole);
        }
        Bukkit.broadcast(Colours.translateLegacyColours(templateMessage));
    }

}
