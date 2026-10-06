package com.featherlite;

import com.featherlite.module.Module;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public final class Config {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final class Entry {
        boolean enabled;
        double x, y;
        double scale = 1.0;
        int color = 0;
        boolean useCustom = false;
        int custom = 0xFFFFFFFF;
        int key = Integer.MIN_VALUE;
        boolean background = true;
        double alpha = 0.6;
    }

    private static final class Data {
        int accent;
        Map<String, Entry> modules = new LinkedHashMap<>();
    }

    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("featherlite.json");
    }

    public static void load() {
        try {
            if (!Files.exists(file())) return;
            Data d = GSON.fromJson(Files.readString(file()), Data.class);
            if (d == null) return;
            Theme.accentIndex = Math.max(0, Math.min(Theme.ACCENTS.length - 1, d.accent));
            if (d.modules == null) return;
            for (Module m : FeatherLite.MODULES) {
                Entry e = d.modules.get(m.id);
                if (e == null) continue;
                m.enabled = e.enabled;
                m.posX = clamp(e.x);
                m.posY = clamp(e.y);
                m.scale = Math.max(0.5, Math.min(2.0, e.scale));
                m.colorIndex = Math.max(0, Math.min(Theme.MODULE_COLORS.length - 1, e.color));
                m.useCustom = e.useCustom;
                m.customColor = e.custom;
                if (e.key != Integer.MIN_VALUE) m.keyCode = e.key;
                m.background = e.background;
                m.bgAlpha = clamp(e.alpha);
            }
        } catch (Exception ex) {
            System.err.println("[FeatherLite] Could not load config: " + ex);
        }
    }

    public static void save() {
        try {
            Data d = new Data();
            d.accent = Theme.accentIndex;
            for (Module m : FeatherLite.MODULES) {
                Entry e = new Entry();
                e.enabled = m.enabled;
                e.x = m.posX;
                e.y = m.posY;
                e.scale = m.scale;
                e.color = m.colorIndex;
                e.useCustom = m.useCustom;
                e.custom = m.customColor;
                e.key = m.keyCode;
                e.background = m.background;
                e.alpha = m.bgAlpha;
                d.modules.put(m.id, e);
            }
            Files.writeString(file(), GSON.toJson(d));
        } catch (Exception ex) {
            System.err.println("[FeatherLite] Could not save config: " + ex);
        }
    }

    public static double clamp(double v) {
        return Math.max(0, Math.min(1, v));
    }
}
