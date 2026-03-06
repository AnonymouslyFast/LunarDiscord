package com.anonymouslyfast.lunarDiscord.minecraft.listeners;

import com.anonymouslyfast.lunarDiscord.LunarDiscord;
import com.anonymouslyfast.lunarDiscord.utils.Colours;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.awt.*;
import java.time.Instant;

public class QuitListener implements Listener {

    FileConfiguration config = LunarDiscord.getInstance().getConfig();

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        String author = replacePlaceholder(config.getString("leave-embed-contents"), player);

        if (config.getBoolean("rich-presence-enabled")) { // Updating bot presence
            String presence = Bukkit.getOnlinePlayers().size()-1 + "/" + Bukkit.getMaxPlayers() + " players";
            LunarDiscord.getInstance().getJda().getPresence().setActivity(Activity.watching(presence));
        }

        if (config.getBoolean("leave-embed-enabled")) {
            EmbedBuilder builder = new EmbedBuilder()
                    .setColor(Colours.getColourFromName(config.getString("leave-embed-colour"), Color.red))
                    .setAuthor(author, null, "https://minotar.net/avatar/" + player.getUniqueId());
            builder.setTimestamp(Instant.now());

            String textChannelID = config.getString("minecraft-to-discord-channel-id");
            LunarDiscord.getInstance().getJda().getTextChannelById(textChannelID).sendMessageEmbeds(builder.build()).complete();
        }
    }

    private String replacePlaceholder(String string, Player player) {
        string = string.replace("%PLAYER_USERNAME%", player.getName());
        string = string.replace("%PLAYER_DISPLAY_NAME%", player.displayName().toString());
        User user = LuckPermsProvider.get().getUserManager().getUser(player.getUniqueId());
        String group = user.getCachedData().getMetaData().getPrimaryGroup();
        group = group != null ? group : config.getString("default-group");
        string = string.replace("%PLAYER_GROUP%", group != null ? group : "");
        return string;
    }

}
