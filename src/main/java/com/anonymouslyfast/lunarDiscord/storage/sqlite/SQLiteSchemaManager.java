package com.anonymouslyfast.lunarDiscord.storage.sqlite;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public final class SQLiteSchemaManager {

    private final Connection connection;

    public final String TABLE_NAME = "linked_accounts";
    public final String MINECRAFT_UUID_KEY_NAME = "minecraft_uuid";
    public final String DISCORD_ID_KEY_NAME = "discord_id";

    public SQLiteSchemaManager(Connection connection) {
        this.connection = connection;
    }

    private final String tableCode = """
            CREATE TABLE IF NOT EXISTS linked_accounts (
                minecraft_uuid varchar(36) NOT NULL UNIQUE PRIMARY KEY,
                discord_id varchar(20) NOT NULL UNIQUE
            );
            """;

    public void createTableIfNotExists() {
        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate(tableCode);
            statement.close();
        } catch (SQLException ignored) {}
    }


}
