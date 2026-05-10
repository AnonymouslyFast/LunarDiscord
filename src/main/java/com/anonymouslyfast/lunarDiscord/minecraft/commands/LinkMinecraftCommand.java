package com.anonymouslyfast.lunarDiscord.minecraft.commands;

import ch.njol.skript.variables.Variables;
import com.anonymouslyfast.lunarDiscord.LunarDiscord;
import com.anonymouslyfast.lunarDiscord.storage.StorageProvider;
import com.anonymouslyfast.lunarDiscord.utils.Colours;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class LinkMinecraftCommand implements CommandExecutor {

    private final StorageProvider storageProvider = LunarDiscord.getInstance().getStorageProvider();
    FileConfiguration config = LunarDiscord.getInstance().getConfig();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be run by a player!");
            return true;
        }

        if (storageProvider.isPlayerLinked(player.getUniqueId())) {
            sender.sendMessage(Colours.translateLegacyColours(config.getString("minecraft-already-linked-message")));
            return true;
        }

        Random random = new Random();
        String varName = config.getString("skript-one-time-code-variable-name") != null ? config.getString("skript-one-time-code-variable-name") : "-linkingCode";
        Integer code = (Integer) Variables.getVariable(varName + "::" + player.getUniqueId(), null, false);
        if (code != null) {
            String message = config.getString("minecraft-link-code-already-have");
            message = message.replace("%ONE_TIME_CODE%", String.valueOf(code));
            player.sendMessage(Colours.translateLegacyColours(message));
            return true;
        }

        code = random.nextInt(config.getInt("random-number-min"), config.getInt("random-number-max"));
        int expirationTime = config.getInt("minutes-before-expiration");
        long expirationTimeInTicks = (expirationTime * 60L) * 20;

        String message = config.getString("minecraft-link-code-message");
        message = message.replace("%ONE_TIME_CODE%", String.valueOf(code));
        message = message.replace("%EXPIRATION_TIME%", String.valueOf(expirationTime));
        player.sendMessage(Colours.translateLegacyColours(message));

        Variables.setVariable(varName + "::" + player.getUniqueId(), code, null, false);

        Bukkit.getScheduler().runTaskLater(LunarDiscord.getInstance(), bukkitTask -> {
            if (storageProvider.isPlayerLinked(player.getUniqueId())) return;
            String expired = config.getString("minecraft-link-code-expiration-message");
            player.sendMessage(Colours.translateLegacyColours(expired));
            Variables.deleteVariable(varName + "::" + player.getUniqueId(), null, false);
        }, expirationTimeInTicks);


        return true;
    }


}
