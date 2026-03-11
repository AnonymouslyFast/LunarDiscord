package com.anonymouslyfast.lunarDiscord.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.awt.*;
import java.lang.reflect.Field;

public class Colours {

    public static Color getColourFromName(String name, Color defaultColor) {
        Color colour = defaultColor;
        try {
            Field field = Class.forName("java.awt.Color").getField(name);
            colour = (Color)field.get(null);
        } catch (Exception ignored) {}
        return colour;
    }

    public static Component translateLegacyColours(String original) {
        Component message = LegacyComponentSerializer.legacy('&').deserialize(original);
        return message;
    }

}
