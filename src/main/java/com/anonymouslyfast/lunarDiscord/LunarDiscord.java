package com.anonymouslyfast.lunarDiscord;

import com.anonymouslyfast.lunarDiscord.discord.listeners.ButtonInteractionListener;
import com.anonymouslyfast.lunarDiscord.discord.listeners.DiscordChatListener;
import com.anonymouslyfast.lunarDiscord.discord.listeners.ModalSubmitListener;
import com.anonymouslyfast.lunarDiscord.minecraft.commands.LinkMinecraftCommand;
import com.anonymouslyfast.lunarDiscord.minecraft.commands.UnLinkCommand;
import com.anonymouslyfast.lunarDiscord.minecraft.listeners.ChatListener;
import com.anonymouslyfast.lunarDiscord.minecraft.listeners.DeathListener;
import com.anonymouslyfast.lunarDiscord.minecraft.listeners.JoinListener;
import com.anonymouslyfast.lunarDiscord.minecraft.listeners.QuitListener;
import com.anonymouslyfast.lunarDiscord.storage.SQLiteStorageProvider;
import com.anonymouslyfast.lunarDiscord.storage.SkriptStorageProvider;
import com.anonymouslyfast.lunarDiscord.storage.StorageProvider;
import com.anonymouslyfast.lunarDiscord.utils.Colours;
import com.anonymouslyfast.lunarDiscord.utils.LogAppender;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Message;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.awt.*;
import java.time.Instant;
import java.util.logging.Level;


import static net.dv8tion.jda.api.requests.GatewayIntent.*;

public final class LunarDiscord extends JavaPlugin {

    private boolean botIsDisabled = false;
    public JDA jda;
    private static LunarDiscord instance;
    public boolean IsUsingSkriptStorageProvider = false;
    private StorageProvider storageProvider;
    private boolean isLinkingEnabled = false;
    private boolean isLogChannelEnabled = false;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        getLogger().info("Starting Discord bot...");
        boolean botTokenIsSet =
                getConfig().getString("bot-token") != null && !getConfig().getString("bot-token").isEmpty();
        boolean guildIdIsSet =
                getConfig().getString("guild-id") != null && !getConfig().getString("guild-id").isEmpty();
        boolean minecraftToDiscordChannelIsSet =
                getConfig().getString("minecraft-to-discord-channel-id") != null
                        && !getConfig().getString("minecraft-to-discord-channel-id").isEmpty();

        if (!botTokenIsSet || !guildIdIsSet || !minecraftToDiscordChannelIsSet) {
            getLogger().log(Level.SEVERE, "The bot token, guild id, or Minecraft to Discord channel id is not set! Please set it in LunarDiscord/config.yml to continue.");
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
            getLogger().info("Starting Discord bot has failed!");
            throw new RuntimeException(e);
        }

        if (getConfig().getBoolean("rich-presence-enabled")) {
            String presence = Bukkit.getOnlinePlayers().size() + "/" + Bukkit.getMaxPlayers() + " players";
            jda.getPresence().setActivity(Activity.watching(presence));
        }
        getLogger().info("Discord bot is started!");

        getLogger().info("Starting storage provider");
        IsUsingSkriptStorageProvider = getConfig().getBoolean("skript-storage-provider-toggle");
        if (IsUsingSkriptStorageProvider) {
            storageProvider = new SkriptStorageProvider();
            getLogger().info("Skript Storage provider has been enabled!");
        } else {
            storageProvider = new SQLiteStorageProvider();
            getLogger().info("SQLite Storage provider has been enabled!");
        }

        registerDiscordListeners();
        registerMinecraftListeners(getServer().getPluginManager());
        getCommand("link").setExecutor(new LinkMinecraftCommand());
        getCommand("unlink").setExecutor(new UnLinkCommand());

        isLinkingEnabled = getConfig().getBoolean("discord-verification-enabled");
        if (isLinkingEnabled) {
            getLogger().info("Enabling linking...");
            String linkingChannelID = getConfig().getString("discord-verification-channel-id") == null ? "" :  getConfig().getString("discord-verification-channel-id");
            String linkedRoleID = getConfig().getString("discord-verified-role-id") == null ? "" :  getConfig().getString("discord-verified-role-id");

            if (linkingChannelID.isEmpty() || linkedRoleID.isEmpty()) {
                getLogger().log(Level.WARNING, "You've enabled linking, but the channel id, and/or linking role id is not set! Linking has been disabled.");
                isLinkingEnabled = false;
            } else {
                String linkingEmbedID = getConfig().getString("linking-embed-id") == null ? "" :  getConfig().getString("linking-embed-id");
                if (linkingEmbedID.isEmpty()) {
                    if (!getConfig().getBoolean("predefined-embed-toggle")) {
                        getLogger().warning("You've disabled the pre-defined embed from sending, and the message embed id is not set. Please define a new embed id, or turn back on predefined embeds!");
                        isLinkingEnabled = false;
                    } else {
                        getLogger().info("Couldn't find linking embed, making one now!");
                        linkingEmbedID = sendPredefinedLinkingEmbed(linkingChannelID);
                        getConfig().set("linking-embed-id", linkingEmbedID);
                        saveConfig();
                    }
                }
                if (isLinkingEnabled) {
                    Message embed =  jda.getTextChannelById(linkingChannelID).retrieveMessageById(linkingEmbedID).complete();
                    String buttonLabel = getConfig().getString("button-label") == null ? "Click to verify!" : getConfig().getString("button-label");
                    Button button = Button.primary("linkingButton", buttonLabel);
                    embed.editMessageComponents(ActionRow.of(button)).queue();
                    getLogger().info("Linking has been enabled!");
                }
            }
        }

        // Setting up logs
        isLogChannelEnabled = getConfig().getBoolean("minecraft-logs-enabled");
        if (isLogChannelEnabled) {
            getLogger().info("Enabling logger...");
            String logChannelID = getConfig().getString("discord-verification-channel-id") == null ? "" :  getConfig().getString("discord-verification-channel-id");
            if (logChannelID == null || logChannelID.isEmpty()) {
                getLogger().log(Level.WARNING, "You've enabled logging, but the channel id is not set! Logging has been disabled.");
                isLinkingEnabled = false;
            } else {
                LoggerContext loggerContext = (LoggerContext) LogManager.getContext(false);
                Configuration loggerConfig = loggerContext.getConfiguration();

                LogAppender logAppender = new LogAppender();
                logAppender.start();

                loggerConfig.getRootLogger().addAppender(logAppender, null, null);
                loggerContext.updateLoggers();
                getLogger().info("Logger has been enabled!");
            }
        }



        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setColor(Colours.getColourFromName(getConfig().getString("server-enabled-embed-colour"), Color.green))
                .setDescription(getConfig().getString("server-enabled-embed-contents"))
                .setTimestamp(Instant.now());
        jda.getTextChannelById(getConfig().getString("minecraft-to-discord-channel-id")).sendMessageEmbeds(embedBuilder.build()).complete();
    }

    @Override
    public void onDisable() {

        if (isLogChannelEnabled) {
            LoggerContext loggerContext = (LoggerContext) LogManager.getContext(false);
            Configuration loggerConfig = loggerContext.getConfiguration();
            loggerConfig.getRootLogger().removeAppender("LunarDiscordAppender");
            loggerContext.updateLoggers();
        }

        if (botIsDisabled) return;

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setColor(Colours.getColourFromName(getConfig().getString("server-disabled-embed-colour"), Color.red))
                .setDescription(getConfig().getString("server-disabled-embed-contents"))
                .setTimestamp(Instant.now());
        jda.getTextChannelById(getConfig().getString("minecraft-to-discord-channel-id")).sendMessageEmbeds(embedBuilder.build()).complete();

        if (isLinkingEnabled) {
            String linkingChannelID = getConfig().getString("discord-verification-channel-id") == null ? "" :  getConfig().getString("discord-verification-channel-id");
            String linkingEmbedID = getConfig().getString("linking-embed-id") == null ? "" :  getConfig().getString("linking-embed-id");
            Message message = jda.getTextChannelById(linkingChannelID).retrieveMessageById(linkingEmbedID).complete();
            message.editMessageComponents().complete();
        }

        try {
            jda.shutdownNow();
            jda.awaitShutdown();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return the id of the sent embed
     */
    private String sendPredefinedLinkingEmbed(String channelID) {
        String embedTitle = getConfig().getString("embed-title") == null ? "" : getConfig().getString("embed-title");
        String embedDescription = getConfig().getString("embed-description") == null ? "" : getConfig().getString("embed-description");
        String embedThumbnail = getConfig().getString("embed-thumbnail") == null ? "" : getConfig().getString("embed-thumbnail");
        String embedFooter = getConfig().getString("embed-footer") == null ? "" : getConfig().getString("embed-footer");
        Color embedColour = Colours.getColourFromName(getConfig().getString("embed-colour"), Color.green);

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTitle(embedTitle)
                .setDescription(embedDescription)
                .setColor(embedColour)
                .setThumbnail(embedThumbnail)
                .setFooter(embedFooter);

        Message message = jda.getTextChannelById(channelID).sendMessageEmbeds(embedBuilder.build()).complete();
        return message.getId();
    }

    private void registerMinecraftListeners(PluginManager pluginManager) {
        pluginManager.registerEvents(new JoinListener(), instance);
        pluginManager.registerEvents(new QuitListener(), instance);
        pluginManager.registerEvents(new ChatListener(), instance);
        pluginManager.registerEvents(new DeathListener(), instance);
    }

    private void registerDiscordListeners() {
        jda.addEventListener(new DiscordChatListener());
        jda.addEventListener(new ButtonInteractionListener());
        jda.addEventListener(new ModalSubmitListener());
    }

    public static LunarDiscord getInstance() { return instance; }
    public JDA getJda() { return jda; }
    public StorageProvider getStorageProvider() { return storageProvider; }
    public boolean isLogChannelEnabled() { return isLogChannelEnabled; }
}
