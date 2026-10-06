package com.featherlite.gui;

import com.featherlite.Config;
import com.featherlite.Theme;
import com.featherlite.module.Module;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Arrays;

/** Per-module settings: size, background, opacity, color ring + custom color box. */
public class ModuleSettingsScreen extends Screen {
    private static final int PW = 330, PH = 256;
    private static final int SLIDER_W = 160;
    private static final int R_OUT = 38, R_IN = 28, SQ = 36;
    private static final int DRAG_NONE = 0, DRAG_SIZE = 1, DRAG_ALPHA = 2, DRAG_RING = 3, DRAG_SQUARE = 4;

    // precomputed hue ring (2x2 blocks)
    private static final int[] RX, RY, RC;

    static {
        int cap = (R_OUT + 1) * (R_OUT + 1);
        int[] xs = new int[cap], ys = new int[cap], cs = new int[cap];
        int n = 0;
        for (int dy = -R_OUT; dy < R_OUT; dy += 2) {
            for (int dx = -R_OUT; dx < R_OUT; dx += 2) {
                int cx = dx + 1, cy = dy + 1;
                int d2 = cx * cx + cy * cy;
                if (d2 > R_OUT * R_OUT || d2 < R_IN * R_IN) continue;
                float h = (float) (Math.atan2(cy, cx) / (2 * Math.PI));
                if (h < 0) h += 1f;
                xs[n] = dx;
                ys[n] = dy;
                cs[n] = hsv(h, 1f, 1f);
                n++;
            }
        }
        RX = Arrays.copyOf(xs, n);
        RY = Arrays.copyOf(ys, n);
        RC = Arrays.copyOf(cs, n);
    }

    private final Screen parent;
    private final Module m;
    private int drag = DRAG_NONE;
    private float hue, sat = 1f, val = 1f;
    private TextFieldWidget hex;
    private boolean hexGuard;

    public ModuleSettingsScreen(Screen parent, Module m) {
        super(Text.literal(m.name + " settings"));
        this.parent = parent;
        this.m = m;
    }

    @Override public boolean shouldPause() { return false; }
    @Override public void close() { client.setScreen(parent); }
    @Override public void removed() { Config.save(); }

    // ---------- color math ----------
    static int hsv(float h, float s, float v) {
        float c = v * s;
        float hh = (h * 6f) % 6f;
        float x = c * (1f - Math.abs(hh % 2f - 1f));
        float mm = v - c;
        float r, g, b;
        switch ((int) hh) {
            case 0 -> { r = c; g = x; b = 0; }
            case 1 -> { r = x; g = c; b = 0; }
            case 2 -> { r = 0; g = c; b = x; }
            case 3 -> { r = 0; g = x; b = c; }
            case 4 -> { r = x; g = 0; b = c; }
            default -> { r = c; g = 0; b = x; }
        }
        int R = Math.round((r + mm) * 255), G = Math.round((g + mm) * 255), B = Math.round((b + mm) * 255);
        return 0xFF000000 | (R << 16) | (G << 8) | B;
    }

    private void setHsvFromRgb(int rgb) {
        float r = ((rgb >> 16) & 255) / 255f, g = ((rgb >> 8) & 255) / 255f, b = (rgb & 255) / 255f;
        float max = Math.max(r, Math.max(g, b)), min = Math.min(r, Math.min(g, b));
        float d = max - min;
        float h;
        if (d == 0) h = hue;
        else if (max == r) h = (((g - b) / d) % 6f) / 6f;
        else if (max == g) h = (((b - r) / d) + 2f) / 6f;
        else h = (((r - g) / d) + 4f) / 6f;
        if (h < 0) h += 1f;
        hue = h;
        sat = max == 0 ? 0 : d / max;
        val = max;
    }

    private int effective() { return m.accent(); }

    private void setHexText(int rgb) {
        hexGuard = true;
        hex.setText(String.format("#%06X", rgb & 0xFFFFFF));
        hexGuard = false;
    }

    private void commitPicker() {
        m.customColor = hsv(hue, sat, val);
        m.useCustom = true;
        setHexText(m.customColor);
    }

    // ---------- layout ----------
    private int px() { return (width - PW) / 2; }
    private int py() { return Math.max(4, (height - PH) / 2); }
    private int ctlX() { return px() + 100; }
    private int rowY(int i) { return py() + 40 + i * 26; }
    private int ringCx() { return px() + 62; }
    private int ringCy() { return py() + 176; }
    private int presetX(int i) { return px() + 128 + (i % 5) * 24; }
    private int presetY(int i) { return py() + 136 + (i / 5) * 24; }
    private int hexX() { return px() + 128; }
    private int hexY() { return py() + 188; }

    private static boolean in(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    private static void rrect(DrawContext c, int x, int y, int w, int h, int col) {
        c.fill(x + 2, y, x + w - 2, y + h, col);
        c.fill(x + 1, y + 1, x + w - 1, y + h - 1, col);
        c.fill(x, y + 2, x + w, y + h - 2, col);
    }

    // ---------- init ----------
    @Override
    protected void init() {
        hex = new TextFieldWidget(textRenderer, hexX() + 6, hexY() + 5, 70, 10, Text.literal("Hex"));
        hex.setDrawsBackground(false);
        hex.setPlaceholder(Text.literal("#RRGGBB"));
        hex.setMaxLength(7);
        setHexText(effective());
        setHsvFromRgb(effective());
        hex.setChangedListener(s -> {
            if (hexGuard) return;
            String t = s.startsWith("#") ? s.substring(1) : s;
            if (t.length() != 6) return;
            try {
                int rgb = Integer.parseInt(t, 16);
                m.customColor = 0xFF000000 | rgb;
                m.useCustom = true;
                setHsvFromRgb(rgb);
            } catch (NumberFormatException ignored) {
                // not a valid hex yet
            }
        });
        addSelectableChild(hex);
    }

    // ---------- render ----------
    private void slider(DrawContext c, int row, double t, String text) {
        int sx = ctlX(), sy = rowY(row);
        t = Math.max(0, Math.min(1, t));
        c.fill(sx, sy + 6, sx + SLIDER_W, sy + 10, 0xFF262C3C);
        c.fill(sx, sy + 6, sx + 3 + (int) (t * (SLIDER_W - 6)), sy + 10, m.accent());
        int kx = sx + (int) (t * (SLIDER_W - 6));
        c.fill(kx, sy + 1, kx + 6, sy + 15, 0xFFFFFFFF);
        c.drawText(textRenderer, text, sx + SLIDER_W + 10, sy + 4, 0xFFB8C0CF, false);
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        TextRenderer tr = textRenderer;
        int accent = Theme.accent();

        if (client.world == null) ctx.fillGradient(0, 0, width, height, 0xFF0A0C13, 0xFF161B29);
        else ctx.fill(0, 0, width, height, 0x55000000);

        int px = px(), py = py();
        rrect(ctx, px, py, PW, PH, 0xF2101420);

        int nameW = tr.getWidth(m.name) + 18;
        rrect(ctx, px + 12, py + 10, nameW, 18, accent);
        ctx.drawText(tr, Text.literal(m.name).formatted(Formatting.BOLD), px + 21, py + 15, 0xFFFFFFFF, false);
        ctx.drawText(tr, "Settings", px + 12 + nameW + 8, py + 15, 0xFF7C8596, false);

        // Size
        ctx.drawText(tr, "Size", px + 16, rowY(0) + 4, 0xFFFFFFFF, false);
        slider(ctx, 0, (m.scale - 0.5) / 1.5, Math.round(m.scale * 100) + "%");

        // Background
        ctx.drawText(tr, "Background", px + 16, rowY(1) + 4, 0xFFFFFFFF, false);
        int tx = ctlX(), ty = rowY(1) + 2;
        rrect(ctx, tx, ty, 24, 12, m.background ? accent : 0xFF3A3F4B);
        rrect(ctx, m.background ? tx + 14 : tx + 2, ty + 2, 8, 8, 0xFFFFFFFF);
        ctx.drawText(tr, m.background ? "On" : "Off", tx + 32, ty + 2, 0xFFB8C0CF, false);

        // Opacity
        ctx.drawText(tr, "Opacity", px + 16, rowY(2) + 4, m.background ? 0xFFFFFFFF : 0xFF566073, false);
        slider(ctx, 2, m.bgAlpha, Math.round(m.bgAlpha * 100) + "%");

        // Color section
        ctx.fill(px + 12, py + 118, px + PW - 12, py + 119, 0xFF262B38);
        ctx.drawText(tr, "Color", px + 16, py + 126, 0xFFFFFFFF, false);

        int cx = ringCx(), cy = ringCy();
        for (int k = 0; k < RX.length; k++) {
            ctx.fill(cx + RX[k], cy + RY[k], cx + RX[k] + 2, cy + RY[k] + 2, RC[k]);
        }
        double ang = hue * 2 * Math.PI;
        int mx = cx + (int) Math.round(Math.cos(ang) * (R_IN + R_OUT) / 2.0);
        int my = cy + (int) Math.round(Math.sin(ang) * (R_IN + R_OUT) / 2.0);
        ctx.fill(mx - 4, my - 4, mx + 4, my + 4, 0xFFFFFFFF);
        ctx.fill(mx - 3, my - 3, mx + 3, my + 3, hsv(hue, 1f, 1f));

        // saturation / brightness square inside the ring
        int sqx = cx - SQ / 2, sqy = cy - SQ / 2;
        for (int i = 0; i < SQ; i++) {
            float s = i / (float) (SQ - 1);
            ctx.fillGradient(sqx + i, sqy, sqx + i + 1, sqy + SQ, hsv(hue, s, 1f), 0xFF000000);
        }
        int kx = sqx + Math.round(sat * (SQ - 1));
        int ky = sqy + Math.round((1f - val) * (SQ - 1));
        ctx.fill(kx - 3, ky - 3, kx + 4, ky + 4, 0xFFFFFFFF);
        ctx.fill(kx - 2, ky - 2, kx + 3, ky + 3, hsv(hue, sat, val));

        // presets (first = follow theme)
        for (int i = 0; i < Theme.MODULE_COLORS.length; i++) {
            int sx = presetX(i), sy = presetY(i);
            boolean sel = !m.useCustom && i == m.colorIndex;
            if (sel) ctx.fill(sx - 2, sy - 2, sx + 18, sy + 18, 0xFFFFFFFF);
            ctx.fill(sx, sy, sx + 16, sy + 16, i == 0 ? accent : Theme.MODULE_COLORS[i]);
            if (i == 0) ctx.drawText(tr, "A", sx + 5, sy + 4, 0xFFFFFFFF, true);
        }

        // custom color box (hex) + preview
        rrect(ctx, hexX(), hexY(), 84, 20, 0xFF1A1F2D);
        hex.render(ctx, mouseX, mouseY, delta);
        int pvx = hexX() + 92;
        ctx.fill(pvx - 1, hexY() - 1, pvx + 33, hexY() + 21, 0xFF262C3C);
        ctx.fill(pvx, hexY(), pvx + 32, hexY() + 20, effective());
        ctx.drawText(tr, "Custom color", hexX(), hexY() + 26, 0xFF7C8596, false);

        // buttons
        int by = py + PH - 30;
        boolean h1 = in(mouseX, mouseY, px + 12, by, 70, 20);
        rrect(ctx, px + 12, by, 70, 20, h1 ? 0xFF2C3345 : 0xFF222838);
        ctx.drawText(tr, "Reset", px + 12 + (70 - tr.getWidth("Reset")) / 2, by + 6, 0xFFB8C0CF, false);
        boolean h2 = in(mouseX, mouseY, px + PW - 82, by, 70, 20);
        rrect(ctx, px + PW - 82, by, 70, 20, h2 ? 0xFFEF6A6E : accent);
        ctx.drawText(tr, "Done", px + PW - 82 + (70 - tr.getWidth("Done")) / 2, by + 6, 0xFFFFFFFF, false);
    }

    // ---------- input ----------
    private void applySlider(int which, double mx) {
        double t = Math.max(0, Math.min(1, (mx - ctlX() - 3) / (SLIDER_W - 6)));
        if (which == DRAG_SIZE) m.scale = Math.round((0.5 + t * 1.5) * 20) / 20.0;
        else m.bgAlpha = Math.round(t * 20) / 20.0;
    }

    private void applyRing(double mx, double my) {
        double a = Math.atan2(my - ringCy(), mx - ringCx());
        float h = (float) (a / (2 * Math.PI));
        if (h < 0) h += 1f;
        hue = h;
        commitPicker();
    }

    private void applySquare(double mx, double my) {
        int sqx = ringCx() - SQ / 2, sqy = ringCy() - SQ / 2;
        sat = (float) Math.max(0, Math.min(1, (mx - sqx) / (SQ - 1)));
        val = 1f - (float) Math.max(0, Math.min(1, (my - sqy) / (SQ - 1)));
        commitPicker();
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (click.button() != 0) return super.mouseClicked(click, doubled);
        double mx = click.x(), my = click.y();

        if (in(mx, my, ctlX(), rowY(0), SLIDER_W, 16)) {
            drag = DRAG_SIZE;
            applySlider(drag, mx);
            return true;
        }
        if (in(mx, my, ctlX(), rowY(2), SLIDER_W, 16)) {
            drag = DRAG_ALPHA;
            applySlider(drag, mx);
            return true;
        }
        if (in(mx, my, ctlX(), rowY(1) + 1, 60, 14)) {
            m.background = !m.background;
            Config.save();
            return true;
        }

        for (int i = 0; i < Theme.MODULE_COLORS.length; i++) {
            if (in(mx, my, presetX(i), presetY(i), 16, 16)) {
                m.useCustom = false;
                m.colorIndex = i;
                setHexText(effective());
                setHsvFromRgb(effective());
                Config.save();
                return true;
            }
        }

        double dx = mx - ringCx(), dy = my - ringCy();
        double dist = Math.sqrt(dx * dx + dy * dy);
        if (dist >= R_IN - 3 && dist <= R_OUT + 3) {
            drag = DRAG_RING;
            applyRing(mx, my);
            return true;
        }
        if (in(mx, my, ringCx() - SQ / 2, ringCy() - SQ / 2, SQ, SQ)) {
            drag = DRAG_SQUARE;
            applySquare(mx, my);
            return true;
        }

        if (in(mx, my, hexX(), hexY(), 84, 20)) {
            setFocused(hex);
            return true;
        }
        setFocused(null);

        int by = py() + PH - 30;
        if (in(mx, my, px() + 12, by, 70, 20)) {
            m.resetSettings();
            setHexText(effective());
            setHsvFromRgb(effective());
            Config.save();
            return true;
        }
        if (in(mx, my, px() + PW - 82, by, 70, 20)) {
            close();
            return true;
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseDragged(Click click, double offsetX, double offsetY) {
        switch (drag) {
            case DRAG_SIZE, DRAG_ALPHA -> applySlider(drag, click.x());
            case DRAG_RING -> applyRing(click.x(), click.y());
            case DRAG_SQUARE -> applySquare(click.x(), click.y());
            default -> {
                return super.mouseDragged(click, offsetX, offsetY);
            }
        }
        return true;
    }

    @Override
    public boolean mouseReleased(Click click) {
        if (drag != DRAG_NONE) {
            drag = DRAG_NONE;
            Config.save();
            return true;
        }
        return super.mouseReleased(click);
    }
}
