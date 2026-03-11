package com.anonymouslyfast.lunarDiscord.storage;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.UUID;

public interface StorageProvider {

    void saveLinkedAccount(UUID playerUUID, String userID);
    void deleteLinkedAccount(UUID playerUUID);

    Boolean isPlayerLinked(UUID playerUUID);
    Boolean isDiscordUserLinked(String userID);
    @Nullable
    String getLinkedDiscordID(UUID playerUUID);
    @Nullable
    UUID getLinkedMinecraftUUID(String userID);

}
