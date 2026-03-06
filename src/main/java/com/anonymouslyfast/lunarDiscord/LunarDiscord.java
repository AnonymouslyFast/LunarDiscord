package com.anonymouslyfast.lunarDiscord;

import com.anonymouslyfast.lunarDiscord.discord.listeners.MessageReceivedListener;
import com.anonymouslyfast.lunarDiscord.minecraft.listeners.ChatListener;
import com.anonymouslyfast.lunarDiscord.minecraft.listeners.JoinListener;
import com.anonymouslyfast.lunarDiscord.minecraft.listeners.QuitListener;
import com.anonymouslyfast.lunarDiscord.utils.Colours;
import io.papermc.paper.threadedregions.scheduler.AsyncScheduler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.awt.*;
import java.lang.reflect.Field;
import java.time.Instant;
import java.util.logging.Level;

import static net.dv8tion.jda.api.requests.GatewayIntent.*;

public final class LunarDiscord extends JavaPlugin {

    public boolean botIsDisabled = false;
    public JDA jda;
    private static LunarDiscord instance;


    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        boolean botTokenIsSet =
                getConfig().getString("bot-token") != null && !getConfig().getString("bot-token").isEmpty();
        boolean guildIdIsSet =
                getConfig().getString("guild-id") != null && !getConfig().getString("guild-id").isEmpty();
        boolean minecraftToDiscordChannelIsSet =
                getConfig().getString("minecraft-to-discord-channel-id") != null
                        && !getConfig().getString("minecraft-to-discord-channel-id").isEmpty();

        if (!botTokenIsSet || !guildIdIsSet || !minecraftToDiscordChannelIsSet) {
            getLogger().log(Level.SEVERE, "The bot token, guild id, or minecraft to discord channel id is not set! Please set it in LunarDiscord/config.yml to continue.");
            botIsDisabled = true;
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        try {
            jda = JDABuilder.createDefault(getConfig().getString("bot-token"))
                    .enableIntents(MESSAGE_CONTENT, GUILD_MEMBERS, GUILD_MESSAGES)
                    .build()
                    .awaitReady();
        } catch (InterruptedException e) {
            botIsDisabled = true;
            throw new RuntimeException(e);
        }

        if (getConfig().getBoolean("rich-presence-enabled")) {
            String presence = Bukkit.getOnlinePlayers().size() + "/" + Bukkit.getMaxPlayers() + " players";
            jda.getPresence().setActivity(Activity.watching(presence));
        }

        registerDiscordListeners();
        registerMinecraftListeners(getServer().getPluginManager());

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setColor(Colours.getColourFromName(getConfig().getString("server-enabled-embed-colour"), Color.green))
                .setDescription(getConfig().getString("server-enabled-embed-contents"))
                .setTimestamp(Instant.now());
        jda.getTextChannelById(getConfig().getString("minecraft-to-discord-channel-id")).sendMessageEmbeds(embedBuilder.build()).complete();


    }

    @Override
    public void onDisable() {
        if (botIsDisabled) return;
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setColor(Colours.getColourFromName(getConfig().getString("server-disabled-embed-colour"), Color.red))
                .setDescription(getConfig().getString("server-disabled-embed-contents"))
                .setTimestamp(Instant.now());
        jda.getTextChannelById(getConfig().getString("minecraft-to-discord-channel-id")).sendMessageEmbeds(embedBuilder.build()).complete();
    }

    public void registerMinecraftListeners(PluginManager pluginManager) {
        pluginManager.registerEvents(new JoinListener(), instance);
        pluginManager.registerEvents(new QuitListener(), instance);
        pluginManager.registerEvents(new ChatListener(), instance);
    }

    public void registerDiscordListeners() {
        jda.addEventListener(new MessageReceivedListener());
    }


    public static LunarDiscord getInstance() {
        return instance;
    }

    public JDA getJda() {
        return jda;
    }

}
