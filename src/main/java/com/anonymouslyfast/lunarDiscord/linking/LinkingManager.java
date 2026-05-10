package com.anonymouslyfast.lunarDiscord.linking;

import ch.njol.skript.variables.Variables;
import com.anonymouslyfast.lunarDiscord.LunarDiscord;
import com.anonymouslyfast.lunarDiscord.storage.StorageProvider;
import org.bukkit.configuration.file.FileConfiguration;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.UUID;

/**
 * Singleton class to manage linking throughout plugin.
 */
public class LinkingManager {

    private static LinkingManager instance;
    private static final HashMap<UUID, Integer> linkingCodeCache = new HashMap<>();
    private static final StorageProvider storageProvider = LunarDiscord.getInstance().getStorageProvider();
    private static final FileConfiguration config = LunarDiscord.getInstance().getConfig();


    public void addLinkingCode(UUID uuid, int code) {
        if (LunarDiscord.getInstance().IsUsingSkriptStorageProvider) {
            String varName = config.getString("skript-one-time-code-variable-name") != null ?
                    config.getString("skript-one-time-code-variable-name") : "-linkingCode";
            Variables.setVariable(varName + "::" + uuid, code, null, false);
        } else {
            linkingCodeCache.put(uuid, code);
        }
    }

    public void removeLinkingCode(UUID uuid) {
        if (LunarDiscord.getInstance().IsUsingSkriptStorageProvider) {
            String varName = config.getString("skript-one-time-code-variable-name") != null ?
                    config.getString("skript-one-time-code-variable-name") : "-linkingCode";
            Variables.deleteVariable(varName + "::" + uuid, null, false);
        } else {
            linkingCodeCache.remove(uuid);
        }
    }

    @Nullable
    public Integer getLinkingCode(UUID uuid) {
        if (LunarDiscord.getInstance().IsUsingSkriptStorageProvider) {
            String varName = config.getString("skript-one-time-code-variable-name") != null ?
                    config.getString("skript-one-time-code-variable-name") : "-linkingCode";
            return (Integer) Variables.getVariable(varName + "::" + uuid, null, false);
        }
        return linkingCodeCache.get(uuid);
    }

    public static LinkingManager getInstance() {
        if (instance == null) instance = new LinkingManager();
        return instance;
    }

}
