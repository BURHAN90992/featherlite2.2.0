package com.featherlite.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

/**
 * Label + value box. The value string and its width are cached and only
 * rebuilt every refreshMs, so rendering a frame does no string work.
 */
public abstract class TextModule extends Module {
    protected final String label;
    protected long refreshMs = 50;

    private long nextRefresh;
    private String cached = "";
    private int labelW = -1;
    private int cachedW;

    protected TextModule(String id, String name, String desc, String label, double x, double y) {
        this(id, name, desc, "Info", label, x, y);
    }

    protected TextModule(String id, String name, String desc, String tab, String label, double x, double y) {
        super(id, name, desc, tab, x, y);
        this.label = label;
    }

    protected abstract String value(MinecraftClient mc);

    /** Per-frame hook for subclasses (cheap work only). */
    protected void update(MinecraftClient mc) {}

    @Override
    public final void frame(MinecraftClient mc) {
        update(mc);
        long now = System.currentTimeMillis();
        if (now < nextRefresh) return;
        nextRefresh = now + refreshMs;
        TextRenderer tr = mc.textRenderer;
        if (labelW < 0) labelW = tr.getWidth(label);
        cached = value(mc);
        cachedW = tr.getWidth(cached);
    }

    @Override
    public int width(MinecraftClient mc) {
        return 7 + Math.max(labelW, 0) + 5 + cachedW + 6;
    }

    @Override
    public int height(MinecraftClient mc) {
        return mc.textRenderer.fontHeight + 8;
    }

    @Override
    public void render(DrawContext ctx, MinecraftClient mc, int px, int py) {
        int w = width(mc), h = height(mc);
        TextRenderer tr = mc.textRenderer;
        if (background) {
            ctx.fill(px, py, px + w, py + h, bg());
            ctx.fill(px, py, px + 2, py + h, accent());
        }
        int ty = py + (h - tr.fontHeight) / 2 + 1;
        ctx.drawText(tr, label, px + 7, ty, 0xFF9AA4B2, false);
        ctx.drawText(tr, cached, px + 7 + labelW + 5, ty, textColor(), true);
    }
}
