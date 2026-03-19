package com.anonymouslyfast.lunarDiscord.minecraft.listeners;

import ch.njol.skript.variables.Variables;
import com.anonymouslyfast.lunarDiscord.LunarDiscord;
import com.anonymouslyfast.lunarDiscord.utils.Colours;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.awt.*;
import java.time.Instant;

public class JoinListener implements Listener {

    FileConfiguration config = LunarDiscord.getInstance().getConfig();

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        EmbedBuilder builder = new EmbedBuilder();
        if (!player.hasPlayedBefore()) { // New Player
            String author = replacePlaceholder(config.getString("new-join-embed-contents"), player);
            builder.setColor(Colours.getColourFromName(config.getString("new-join-embed-colour"), Color.yellow));
            builder.setAuthor(author, null, "https://minotar.net/avatar/" + player.getUniqueId());
        } else { // old player
            String author = replacePlaceholder(config.getString("join-embed-contents"), player);
            builder.setColor(Colours.getColourFromName(config.getString("join-embed-colour"), Color.green));
            builder.setAuthor(author, null, "https://minotar.net/avatar/" + player.getUniqueId());
        }
        builder.setTimestamp(Instant.now());
        String textChannelID = config.getString("minecraft-to-discord-channel-id");
        LunarDiscord.getInstance().getJda().getTextChannelById(textChannelID).sendMessageEmbeds(builder.build()).complete();

        if (config.getBoolean("rich-presence-enabled")) { // Updating bot presence
            String presence = Bukkit.getOnlinePlayers().size() + "/" + Bukkit.getMaxPlayers() + " players";
            LunarDiscord.getInstance().getJda().getPresence().setActivity(Activity.watching(presence));
        }

        // Discord stuff
        if (!LunarDiscord.getInstance().getStorageProvider().isPlayerLinked(player.getUniqueId())) return;
        String userID = LunarDiscord.getInstance().getStorageProvider().getLinkedDiscordID(player.getUniqueId());
        if (userID == null || LunarDiscord.getInstance().getJda().getUserById(userID) == null) return;
        String primaryGuildID = LunarDiscord.getInstance().getJda().getUserById(userID).getPrimaryGuild().getId();
        if (primaryGuildID == null) return;

        String tagVarName = config.getString("skript-discord-tag-reward-variable-name");
        if (tagVarName == null) tagVarName = "hasDiscordTag";

        boolean hasDiscordTag = primaryGuildID.equals(config.getString("guild-id"));
        Variables.setVariable(tagVarName + "::" + player.getUniqueId(), hasDiscordTag, null, false);
    }

    private String replacePlaceholder(String string, Player player) {
        string = string.replace("%PLAYER_USERNAME%", player.getName());
        string = string.replace("%PLAYER_DISPLAY_NAME%", player.displayName().toString());
        string = string.replace("%PLAYER_JOIN_NUMBER%", String.valueOf(Bukkit.getOfflinePlayers().length));
        User user = LuckPermsProvider.get().getUserManager().getUser(player.getUniqueId());
        String group = user.getCachedData().getMetaData().getPrimaryGroup();
        group = group != null ? group : config.getString("default-group");
        string = string.replace("%PLAYER_GROUP%", group != null ? group : "");
        return string;
    }

}
