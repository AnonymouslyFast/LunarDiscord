package com.anonymouslyfast.lunarDiscord.minecraft.listeners;

import com.anonymouslyfast.lunarDiscord.LunarDiscord;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class DeathListener implements Listener {

    FileConfiguration config = LunarDiscord.getInstance().getConfig();

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Entity attacker = event.getEntity();
        if (event.getEntity().getKiller() == null && !config.getBoolean("send-deaths-by-non-players")) return;

        String message = config.getString("death-message");
        message = message.replace("%VICTIM%", victim.getName());
        message = message.replace("%ATTACKER%", attacker.getName());
        String deathMessage = PlainTextComponentSerializer.plainText().serialize(event.deathMessage());
        message = message.replace("%DEATH_MESSAGE%", deathMessage);
        String channelID = config.getString("minecraft-to-discord-channel-id");
        assert channelID != null;
        LunarDiscord.getInstance().getJda().getTextChannelById(channelID).sendMessage(message).complete();


    }

}
