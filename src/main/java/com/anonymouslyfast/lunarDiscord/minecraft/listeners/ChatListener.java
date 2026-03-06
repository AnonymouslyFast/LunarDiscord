package com.anonymouslyfast.lunarDiscord.minecraft.listeners;

import com.anonymouslyfast.lunarDiscord.LunarDiscord;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ChatListener implements Listener {

    FileConfiguration config = LunarDiscord.getInstance().getConfig();

    @EventHandler
    public void onPlayerChat(AsyncChatEvent event) {
        String message = LegacyComponentSerializer.legacySection().serialize(event.message());
        if (event.isCancelled()) return;
        if (event.getPlayer().isOp() && message.startsWith("!")) return;
        if (message.contains("@everyone") || message.contains("@here")) return;

        User user = LuckPermsProvider.get().getUserManager().getUser(event.getPlayer().getUniqueId());
        String group = user.getCachedData().getMetaData().getPrimaryGroup();
        group = group != null ? group : config.getString("default-group");

        String template = config.getString("player-message-template");
        template = template.replace("%PLAYER_USERNAME%", event.getPlayer().getName());
        template = template.replace("%PLAYER_DISPLAY_NAME%", event.getPlayer().displayName().toString());
        template = template.replace("%PLAYER_GROUP%", group != null ? group : "");
        template = template.replace("%EVENT_MESSAGE%", message);

        String textChannelID = config.getString("minecraft-to-discord-channel-id");
        LunarDiscord.getInstance().getJda().getTextChannelById(textChannelID).sendMessage(template).complete();
    }

}
