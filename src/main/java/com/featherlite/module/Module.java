package com.featherlite.module;

import com.featherlite.Theme;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public abstract class Module {
    public final String id;
    public final String name;
    public final String desc;
    /** Menu tab: PvP, Info or Misc. */
    public final String tab;
    public boolean enabled = true;
    /** Position as a fraction (0..1) of the free screen space. */
    public double posX, posY;

    // ---- per-module settings ----
    /** Size multiplier, 0.5 - 2.0. */
    public double scale = 1.0;
    /** Index into Theme.MODULE_COLORS (0 = follow theme accent). */
    public int colorIndex = 0;
    /** Custom color from the ring picker / hex box (used when useCustom is true). */
    public boolean useCustom = false;
    public int customColor = 0xFFFFFFFF;
    public boolean background = true;
    public double bgAlpha = 0.6;

    // ---- optional key (used by hold-to-use modules like Freelook / Zoom) ----
    /** GLFW key code, -1 = none. */
    public int keyCode = -1;
    public int defaultKey = -1;

    protected Module(String id, String name, String desc, String tab, double posX, double posY) {
        this.id = id;
        this.name = name;
        this.desc = desc;
        this.tab = tab;
        this.posX = posX;
        this.posY = posY;
    }

    public boolean hasHud() { return true; }
    public boolean hasKey() { return defaultKey > 0; }
    public boolean hasSettings() { return hasHud() || hasKey(); }

    /** Unscaled content size. */
    public int width(MinecraftClient mc) { return 0; }
    public int height(MinecraftClient mc) { return 0; }
    public void render(DrawContext ctx, MinecraftClient mc, int px, int py) {}
    /** Called once per rendered frame while in a world, only when enabled. */
    public void frame(MinecraftClient mc) {}

    // ---- scaled size and position ----
    public int sw(MinecraftClient mc) { return Math.round(width(mc) * (float) scale); }
    public int sh(MinecraftClient mc) { return Math.round(height(mc) * (float) scale); }

    public int screenX(MinecraftClient mc, int screenW) {
        return (int) (posX * Math.max(0, screenW - sw(mc)));
    }

    public int screenY(MinecraftClient mc, int screenH) {
        return (int) (posY * Math.max(0, screenH - sh(mc)));
    }

    // ---- colors ----
    /** Color chosen for this module (custom, preset, or theme accent). */
    public int accent() {
        if (useCustom) return customColor | 0xFF000000;
        return colorIndex <= 0 ? Theme.accent() : Theme.MODULE_COLORS[colorIndex];
    }

    public int textColor() {
        if (useCustom) return customColor | 0xFF000000;
        return colorIndex <= 0 ? 0xFFFFFFFF : Theme.MODULE_COLORS[colorIndex];
    }

    /** Pressed-key / highlight color. */
    public int highlight() {
        int c = (useCustom || colorIndex > 0) ? textColor() : 0xFFFFFFFF;
        return 0xDD000000 | (c & 0xFFFFFF);
    }

    public int bg() {
        return (((int) (bgAlpha * 255)) << 24) | 0x101318;
    }

    public void resetSettings() {
        scale = 1.0;
        colorIndex = 0;
        useCustom = false;
        keyCode = defaultKey;
        background = true;
        bgAlpha = 0.6;
    }

    public void setEnabled(MinecraftClient mc, boolean on) {
        enabled = on;
        onToggle(mc);
    }

    protected void onToggle(MinecraftClient mc) {}
}
