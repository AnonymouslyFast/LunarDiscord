package com.anonymouslyfast.lunarDiscord.storage;

import com.anonymouslyfast.lunarDiscord.LunarDiscord;
import com.anonymouslyfast.lunarDiscord.hooks.SQLiteHook;
import com.anonymouslyfast.lunarDiscord.storage.sqlite.SQLiteSchemaManager;
import org.bukkit.Bukkit;
import org.jspecify.annotations.Nullable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

public class SQLiteStorageProvider implements StorageProvider {

    HashMap<UUID, String> minecraftToDiscordCache = new HashMap<>();
    HashMap<String, UUID> discordToMinecraftCache = new HashMap<>();

    private final SQLiteHook sqLiteHook;
    private final SQLiteSchemaManager schemaManager;

    private final LunarDiscord lunarDiscord = LunarDiscord.getInstance();

    public SQLiteStorageProvider() {
        String databaseFilePath = LunarDiscord.getInstance().getDataPath() + "/database.db";
        sqLiteHook = new SQLiteHook(databaseFilePath);

        schemaManager = new SQLiteSchemaManager(sqLiteHook.getConnection());
        schemaManager.createTableIfNotExists();
    }

    @Override
    public void saveLinkedAccount(UUID playerUUID, String userID) {
        Bukkit.getAsyncScheduler().runNow(lunarDiscord, (task) -> {
            String sql = "INSERT INTO linked_accounts (minecraft_uuid, discord_id) VALUES (?, ?) ON CONFLICT(minecraft_uuid) DO NOTHING";

            try (PreparedStatement statement = sqLiteHook.getConnection().prepareStatement(sql)) {
                statement.setString(1, playerUUID.toString());
                statement.setString(2, userID);
                statement.executeUpdate();
                // setting newly linked account into cache
                minecraftToDiscordCache.put(playerUUID, userID);
                discordToMinecraftCache.put(userID, playerUUID);
            } catch (SQLException e) {
                lunarDiscord.getLogger().log(Level.WARNING, "Failed to save linked account for player " + playerUUID, e);
            }
        });

    }

    @Override
    public boolean deleteLinkedAccount(UUID playerUUID) {
        String discordID = getLinkedDiscordID(playerUUID);
        if (discordID == null) return false;

        Bukkit.getAsyncScheduler().runNow(lunarDiscord, (task) -> {
            String sql = "DELETE FROM linked_accounts WHERE minecraft_uuid = ?";
            try (PreparedStatement statement = sqLiteHook.getConnection().prepareStatement(sql)) {
                statement.setString(1, playerUUID.toString());
                statement.executeUpdate();
                // removing account from cache
                minecraftToDiscordCache.remove(playerUUID);
                discordToMinecraftCache.remove(discordID);
            } catch (SQLException e) {
                lunarDiscord.getLogger().log(Level.WARNING, "Failed to remove linked account from database for player " + playerUUID, e);

            }
        });
        return true;
    }

    @Override
    public Boolean isPlayerLinked(UUID playerUUID) {
        AtomicBoolean result = new AtomicBoolean(false);
        CountDownLatch countDownLatch =  new CountDownLatch(1);

        Bukkit.getAsyncScheduler().runNow(lunarDiscord, (task) -> {
            String sql = "SELECT 1 FROM linked_accounts WHERE minecraft_uuid = ?";

            try (PreparedStatement statement = sqLiteHook.getConnection().prepareStatement(sql)) {
                statement.setString(1, playerUUID.toString());

                try (ResultSet resultSet = statement.executeQuery()) {
                    result.set(resultSet.next());
                }
            } catch (SQLException e) {
                lunarDiscord.getLogger().log(Level.WARNING, "Failed to check player existence: " + playerUUID, e);
            } finally {
                countDownLatch.countDown();
            }
        });

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return result.get();
    }

    @Override
    public Boolean isDiscordUserLinked(String userID) {
        AtomicBoolean result = new AtomicBoolean(false);
        CountDownLatch countDownLatch =  new CountDownLatch(1);

        Bukkit.getAsyncScheduler().runNow(lunarDiscord, (task) -> {
            String sql = "SELECT 1 FROM linked_accounts WHERE discord_id = ?";

            try (PreparedStatement statement = sqLiteHook.getConnection().prepareStatement(sql)) {
                statement.setString(1, userID);

                try (ResultSet resultSet = statement.executeQuery()) {
                    result.set(resultSet.next());
                }
            } catch (SQLException e) {
                lunarDiscord.getLogger().log(Level.WARNING, "Failed to check user existence: " + userID, e);
            } finally {
                countDownLatch.countDown();
            }
        });

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return result.get();
    }

    @Override
    public @Nullable String getLinkedDiscordID(UUID playerUUID) {
        return minecraftToDiscordCache.computeIfAbsent(playerUUID, (uuid) -> {
            String id = loadFromDataBase("discord_id", "minecraft_uuid", playerUUID.toString());
            if (id == null) return null;

            // adding loaded account to cache
            discordToMinecraftCache.put(id, playerUUID);

            return id;
        });
    }

    @Override
    public @Nullable UUID getLinkedMinecraftUUID(String userID) {
        return discordToMinecraftCache.computeIfAbsent(userID, (id) -> {
            String uuid = loadFromDataBase("minecraft_uuid", "discord_id", id);
            if (uuid == null) return null;
            UUID result = UUID.fromString(uuid);

            // adding loaded account to cache
            minecraftToDiscordCache.put(result, userID);

            return result;
        });
    }

    private @Nullable String loadFromDataBase(String wantKeyName, String fromKeyName, String fromKeyValue) {
        AtomicReference<String> result = new AtomicReference<>();
        CountDownLatch countDownLatch =  new CountDownLatch(1);

        Bukkit.getAsyncScheduler().runNow(lunarDiscord, (task) -> {
            String sql = "SELECT " + wantKeyName + " FROM linked_accounts WHERE " + fromKeyName + " = ?";
            try (PreparedStatement statement = sqLiteHook.getConnection().prepareStatement(sql)) {
                statement.setString(1, fromKeyValue);
                ResultSet resultSet = statement.executeQuery();

                String answer = resultSet.next() ? resultSet.getString(wantKeyName) : null;
                result.set(answer);

            } catch (SQLException e) {
                lunarDiscord.getLogger().log(Level.WARNING, "Failed to load into existence: " + wantKeyName + " from: " + fromKeyName, e);
            } finally {
                countDownLatch.countDown();
            }
        });

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return result.get();
    }


}
