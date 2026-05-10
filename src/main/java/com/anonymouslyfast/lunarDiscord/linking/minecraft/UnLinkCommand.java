package com.anonymouslyfast.lunarDiscord.linking.minecraft;

import com.anonymouslyfast.lunarDiscord.LunarDiscord;
import com.anonymouslyfast.lunarDiscord.storage.StorageProvider;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class UnLinkCommand implements CommandExecutor {

    private final StorageProvider storageProvider = LunarDiscord.getInstance().getStorageProvider();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length != 1) {
            sender.sendMessage(command.getUsage());
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        if (!storageProvider.isPlayerLinked(target.getUniqueId())) {
            sender.sendMessage("That player is not linked!");
            return true;
        }

        boolean status = storageProvider.deleteLinkedAccount(target.getUniqueId());
        if  (!status) {
            sender.sendMessage("That player is not linked!");
            return true;
        }

        sender.sendMessage("Successfully unlinked player " + target.getName());
        return true;
    }

}
