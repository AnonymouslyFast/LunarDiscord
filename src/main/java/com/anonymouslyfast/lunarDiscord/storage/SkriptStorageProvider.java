package com.anonymouslyfast.lunarDiscord.storage;

import ch.njol.skript.variables.Variables;
import com.anonymouslyfast.lunarDiscord.LunarDiscord;
import org.bukkit.configuration.file.FileConfiguration;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.UUID;

public class SkriptStorageProvider implements StorageProvider {

    FileConfiguration config = LunarDiscord.getInstance().getConfig();

    @Override
    public void saveLinkedAccount(UUID playerUUID, String userID) {
        String varName = getVariableName();
        varName = varName.replace("*", playerUUID.toString());
        Variables.setVariable(varName, userID, null, false);

        String reversedVarName = getReversedVariableName();
        reversedVarName = reversedVarName.replace("*", userID);
        Variables.setVariable(reversedVarName, playerUUID, null, false);
    }

    @Override
    public void deleteLinkedAccount(UUID playerUUID) {
        String varName = getVariableName();
        varName = varName.replace("*", playerUUID.toString());
        Variables.deleteVariable(varName, null, false);
    }

    @Override
    public Boolean isPlayerLinked(UUID playerUUID) {
        String varName = getVariableName();
        varName = varName.replace("*", playerUUID.toString());
        return Variables.getVariable(varName, null, false) != null;
    }

    @Override
    public Boolean isDiscordUserLinked(String userID) {
        String reversedVarName = getReversedVariableName();
        reversedVarName = reversedVarName.replace("*", userID);
        return Variables.getVariable(reversedVarName, null, false) != null;
    }

    @Override
    public @Nullable String getLinkedDiscordID(UUID playerUUID) {
        String varName = getVariableName();
        varName = varName.replace("*", playerUUID.toString());
        Map<String, Object> value = (Map<String, Object>) Variables.getVariable(varName, null, false);
        return value.get(playerUUID.toString()).toString() == null ? null : value.get(playerUUID.toString()).toString(); // trying to fix null warning, but fuck it, won't go away.
    }

    @Override
    public @Nullable UUID getLinkedMinecraftUUID(String userID) {
        String reversedVarName = getReversedVariableName();
        reversedVarName = reversedVarName.replace("*", userID);
        Map<String, Object> value = (Map<String, Object>) Variables.getVariable(reversedVarName, null, false);
        return value.get(userID) == null ? null : UUID.fromString((String) value.get(userID));
    }

    private String getVariableName() {
        String variableName = config.getString("skript-variable-list-name");
        if (variableName == null) variableName = "linkedAccounts"; // default to the callback/normal var name.
        variableName = variableName + "::*"; // Appends the list thingy
        return variableName;
    }

    private String getReversedVariableName() {
        String variableName = config.getString("skript-variable-reversed-list-name:");
        if (variableName == null) variableName = "linkedAccountsReversed"; // default to the callback/normal var name.
        variableName = variableName + "::*"; // Appends the list thingy
        return variableName;
    }
}
