package com.anonymouslyfast.lunarDiscord.linking.minecraft;

import ch.njol.skript.variables.Variables;
import com.anonymouslyfast.lunarDiscord.LunarDiscord;
import com.anonymouslyfast.lunarDiscord.linking.LinkingManager;
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
    private final FileConfiguration config = LunarDiscord.getInstance().getConfig();

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
        Integer code = LinkingManager.getInstance().getLinkingCode(player.getUniqueId());
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

        LinkingManager.getInstance().addLinkingCode(player.getUniqueId(), code);

        Bukkit.getScheduler().runTaskLater(LunarDiscord.getInstance(), bukkitTask -> {
            if (!storageProvider.isPlayerLinked(player.getUniqueId())) {
                String expired = config.getString("minecraft-link-code-expiration-message");
                player.sendMessage(Colours.translateLegacyColours(expired));
            }
            LinkingManager.getInstance().removeLinkingCode(player.getUniqueId());
        }, expirationTimeInTicks);

        return true;
    }

}
