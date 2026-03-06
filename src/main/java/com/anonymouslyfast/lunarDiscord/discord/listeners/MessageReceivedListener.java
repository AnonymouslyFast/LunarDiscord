package com.anonymouslyfast.lunarDiscord.discord.listeners;

import com.anonymouslyfast.lunarDiscord.LunarDiscord;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

public class MessageReceivedListener extends ListenerAdapter {

    private FileConfiguration config = LunarDiscord.getInstance().getConfig();

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (!event.getChannel().getId().equals(config.getString("minecraft-to-discord-channel-id"))) return;
        if (event.getAuthor().isBot()) return;

        boolean isReply = false;
        String templateMessage = config.getString("to-minecraft-message-template");
        if (event.getMessage().getReferencedMessage() != null && config.getBoolean("show_replies_in_messages")) {
            isReply = true;
            templateMessage = config.getString("to-minecraft-message-with-reply-template");
        }

        // Filling out template
        String authorRole = event.getMember().getRoles().getFirst().getName();
        templateMessage = templateMessage.replace("%USER%", event.getAuthor().getName());
        templateMessage = templateMessage.replace("%NICKNAME%", event.getAuthor().getEffectiveName());
        templateMessage = templateMessage.replace("%USER_ROLE%", authorRole);
        // TODO: Complete CONNECTED_MINECRAFT_USERNAME once verification system is done.
        //templateMessage = templateMessage.replaceAll("%CONNECTED_MINECRAFT_USERNAME%", );
        templateMessage = templateMessage.replace("%MESSAGE%", event.getMessage().getContentDisplay());
        if (isReply) {
            String replyAuthorRole = event.getMessage().getReferencedMessage().getMember().getRoles().getFirst().getName();
            templateMessage = templateMessage.replace("%REPLY_USER%", event.getMessage().getReferencedMessage().getAuthor().getName());
            templateMessage = templateMessage.replace("%REPLY_NICKNAME%", event.getMessage().getReferencedMessage().getAuthor().getEffectiveName());
            templateMessage = templateMessage.replace("%REPLY_USER_ROLE%", replyAuthorRole);
            // TODO: Complete REPLY_CONNECTED_MINECRAFT_USERNAME once verification system is done.
            //templateMessage = templateMessage.replaceAll("%REPLY_CONNECTED_MINECRAFT_USERNAME%", );
        }
        Component message = LegacyComponentSerializer.legacy('&').deserialize(templateMessage);
        Bukkit.broadcast(message);
    }

}
