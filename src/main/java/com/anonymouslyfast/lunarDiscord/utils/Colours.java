package com.anonymouslyfast.lunarDiscord.utils;

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

}
