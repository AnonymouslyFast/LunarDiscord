package com.anonymouslyfast.lunarDiscord.discord.listeners;

import ch.njol.skript.variables.Variables;
import com.anonymouslyfast.lunarDiscord.LunarDiscord;
import com.anonymouslyfast.lunarDiscord.storage.StorageProvider;
import com.anonymouslyfast.lunarDiscord.utils.Colours;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.exceptions.HierarchyException;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.List;

public class ModalSubmitListener extends ListenerAdapter {

    StorageProvider storageProvider = LunarDiscord.getInstance().getStorageProvider();
    FileConfiguration config = LunarDiscord.getInstance().getConfig();

    @Override
    public void onModalInteraction(ModalInteractionEvent event) {
        if (event.getModalId().equals("linkingModal")) {
            String username = event.getValue("minecraft_username").getAsString();
            String code = event.getValue("one_time_code").getAsString();

            if (username.isEmpty() || code.isEmpty()) {
                event.reply(":x: The specified code or username is wrong! Please try again.").setEphemeral(true).queue();
                return;
            }

            Player player = Bukkit.getPlayer(username);
            if (player == null) {
                event.reply(":x: The specified player is not online! Please try again.").setEphemeral(true).queue();
                return;
            }

            String varName = config.getString("skript-one-time-code-variable-name") != null ? config.getString("skript-one-time-code-variable-name") : "-linkingCode";
            Integer savedCode = (Integer) Variables.getVariable(varName + "::" + player.getUniqueId(), null, false);
            if (savedCode == null || !savedCode.equals(Integer.parseInt(code))) {
                event.reply(":x: The specified code is incorrect! Please try again.").setEphemeral(true).queue();
            }

            // Checking if they're already linked
            if (storageProvider.isPlayerLinked(player.getUniqueId())) {
                event.reply(":x: This account is already linked! If you would like to unlink, contact an admin.").setEphemeral(true).queue();
                return;
            } else if (storageProvider.isDiscordUserLinked(event.getUser().getId())) {
                event.reply(":x: Your discord account is already linked! If you would like to unlink, contact an admin.").setEphemeral(true).queue();
                return;
            }

            storageProvider.saveLinkedAccount(player.getUniqueId(), event.getUser().getId());

            if (config.getBoolean("linked-broadcast-enabled")) {
                String message = config.getString("linked-broadcast-message");
                message = message.replace("%PLAYER_NAME%", player.getName());
                message = message.replace("%DISCORD_NAME%", event.getUser().getName());
                message = message.replace("%nl%", "\n");

                Bukkit.broadcast(Colours.translateLegacyColours(message));
            }

            List<?> rewards = config.getList("Linking-rewards");
            if (rewards == null || rewards.isEmpty()) {
                LunarDiscord.getInstance().getLogger().warning(player.getName() + " has linked their account, but the rewards list is empty! No reward was given.");
                return;
            }

            event.reply(":white_check_mark: Your account is linked! Your rewards have been given to your account.").setEphemeral(true).queue();
            // Changing nickname
           try {
               event.getMember().modifyNickname(username).queue();
           } catch (HierarchyException ignored) {
                LunarDiscord.getInstance().getLogger().warning(event.getUser().getName() + " has linked their account, but their role is higher than the bot's current role! No nickname applied.");
           }

            // Adding linked role (prolly should move this to separate method, but too lazy rn)
            String roleID = config.getString("discord-verified-role-id");
            if (roleID == null || roleID.isEmpty()) {
                LunarDiscord.getInstance().getLogger().warning(event.getUser().getName() + " has linked their account, but the linked role id in config is empty! No role was given.");
            } else {
                Role role = LunarDiscord.getInstance().getJda().getRoleById(roleID);
                if (role == null) {
                    LunarDiscord.getInstance().getLogger().warning(event.getUser().getName() + " has linked their account, but the role id in config is null! No role was given.");
                } else {
                    event.getGuild().addRoleToMember(event.getUser(), role).queue();
                }
            }

            Bukkit.getScheduler().runTask(LunarDiscord.getInstance(), () -> {
                for (Object reward : rewards) {
                    if (reward instanceof String command) {
                        command = command.replaceAll("%PLAYER_NAME%",  player.getName());
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                    }
                }
            });
        }
    }

}
