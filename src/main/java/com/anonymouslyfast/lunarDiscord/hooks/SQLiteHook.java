package com.anonymouslyfast.lunarDiscord.hooks;

import com.anonymouslyfast.lunarDiscord.LunarDiscord;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;

public class SQLiteHook {
    private Connection connection;
    private final File dbFile;

    public SQLiteHook(String dbPath) {
        dbFile = new File(dbPath);
        if (!dbFile.getParentFile().exists()) dbFile.getParentFile().mkdir();
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getPath());
        } catch (SQLException e) {
            LunarDiscord.getInstance().getLogger().log(Level.WARNING, "Failed to connect to database " + dbFile.getPath(), e);
        }
    }

    public Connection getConnection() { return connection; }
    public File getDbFile() { return dbFile; }
}
