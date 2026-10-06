package com.featherlite.gui;

import com.featherlite.Config;
import com.featherlite.FeatherLite;
import com.featherlite.Theme;
import com.featherlite.module.Module;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ModulesScreen extends Screen {
    private static final String[] TABS = {"All", "PvP", "Info", "Utility", "Misc"};
    private static final int COLS = 4, CH = 76, GAP = 8;
    private static final int TAB_W = 44, TAB_STEP = 48;
    private static final int SW_SIZE = 10, SW_STEP = 14;
    private static final int FOOTER = 30;
    private static final int GREEN_BG = 0xFF1E6B35, GREEN_TX = 0xFF8CFFAA;

    private final Screen parent;
    private int tab;
    private String query = "";
    private double scroll;
    private int maxScroll;
    private TextFieldWidget search;
    private boolean editing;
    private Module dragging;
    private double grabX, grabY;

    public ModulesScreen() { this(null); }

    public ModulesScreen(Screen parent) {
        super(Text.literal("FeatherLite"));
        this.parent = parent;
    }

    @Override public boolean shouldPause() { return false; }

    @Override public void close() { client.setScreen(parent); }

    @Override public void removed() { Config.save(); }

    // ---------- layout ----------
    private int pw() { return Math.min(width - 16, 440); }

    private int ph() {
        int rows = (FeatherLite.MODULES.size() + COLS - 1) / COLS;
        return Math.min(height - 16, 58 + rows * (CH + GAP) - GAP + FOOTER);
    }

    private int px() { return (width - pw()) / 2; }
    private int py() { return (height - ph()) / 2; }
    private int gx() { return px() + 12; }
    private int gy() { return py() + 58; }
    private int gw() { return pw() - 24; }
    private int gh() { return ph() - 58 - FOOTER; }
    private int cw() { return (gw() - (COLS - 1) * GAP) / COLS; }

    private int swatchX(int i) {
        int total = Theme.ACCENTS.length * SW_STEP - (SW_STEP - SW_SIZE);
        return px() + pw() - 12 - total + i * SW_STEP;
    }
    private int swatchY() { return py() + 11; }
    private int tabX(int i) { return px() + 12 + i * TAB_STEP; }
    private int tabY() { return py() + 34; }
    private int searchX() { return px() + pw() - 12 - 120; }
    private int editW() { return 104; }
    private int editX() { return px() + (pw() - editW()) / 2; }
    private int editY() { return py() + ph() - 26; }

    private boolean inWorld() { return client.world != null && client.player != null; }

    private static boolean in(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    private List<Module> shown() {
        List<Module> out = new ArrayList<>();
        String q = query.trim().toLowerCase(Locale.ROOT);
        for (Module m : FeatherLite.MODULES) {
            if (tab > 0 && !m.tab.equals(TABS[tab])) continue;
            if (!q.isEmpty() && !m.name.toLowerCase(Locale.ROOT).contains(q)
                    && !m.desc.toLowerCase(Locale.ROOT).contains(q)) continue;
            out.add(m);
        }
        return out;
    }

    private int cardX(int i) { return gx() + (i % COLS) * (cw() + GAP); }
    private int cardY(int i) { return gy() + (i / COLS) * (CH + GAP) - (int) scroll; }
    private int rowY(int i) { return cardY(i) + CH - 22; }
    private int statusW(Module m) { return m.hasSettings() ? cw() - 12 - 20 : cw() - 12; }
    private int gearBtnX(int i) { return cardX(i) + cw() - 6 - 16; }

    // ---------- init ----------
    @Override
    protected void init() {
        search = new TextFieldWidget(textRenderer, searchX() + 6, tabY() + 4, 108, 10, Text.literal("Search"));
        search.setDrawsBackground(false);
        search.setPlaceholder(Text.literal("Search..."));
        search.setMaxLength(24);
        search.setText(query);
        search.setChangedListener(s -> {
            query = s;
            scroll = 0;
        });
        addSelectableChild(search);
    }

    // ---------- drawing helpers ----------
    private static void rrect(DrawContext c, int x, int y, int w, int h, int col) {
        c.fill(x + 2, y, x + w - 2, y + h, col);
        c.fill(x + 1, y + 1, x + w - 1, y + h - 1, col);
        c.fill(x, y + 2, x + w, y + h - 2, col);
    }

    private static void outline(DrawContext c, int x, int y, int w, int h, int col) {
        c.fill(x - 1, y - 1, x + w + 1, y, col);
        c.fill(x - 1, y + h, x + w + 1, y + h + 1, col);
        c.fill(x - 1, y, x, y + h, col);
        c.fill(x + w, y, x + w + 1, y + h, col);
    }

    private static void centered(DrawContext c, TextRenderer tr, String s, int cx, int y, int col) {
        c.drawText(tr, s, cx - tr.getWidth(s) / 2, y, col, false);
    }

    // ---------- render ----------
    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        TextRenderer tr = textRenderer;
        int accent = Theme.accent();

        // no blur: plain dim (or a dark gradient on the main menu)
        if (!inWorld()) ctx.fillGradient(0, 0, width, height, 0xFF0A0C13, 0xFF161B29);
        else ctx.fill(0, 0, width, height, editing ? 0x30000000 : 0x55000000);

        if (inWorld()) {
            for (Module m : FeatherLite.MODULES) {
                if (!m.enabled || !m.hasHud()) continue;
                int x = m.screenX(client, width), y = m.screenY(client, height);
                outline(ctx, x, y, m.sw(client), m.sh(client), m == dragging ? accent : 0x90FFFFFF);
                if (editing) ctx.drawText(tr, m.name, x, y - 10, 0xFFFFFFFF, true);
            }
        }

        if (editing) {
            String hint = "HUD Editor  |  drag to move  |  double-click for settings";
            int hw = tr.getWidth(hint) + 20;
            rrect(ctx, (width - hw) / 2, 8, hw, 20, 0xE0101420);
            centered(ctx, tr, hint, width / 2, 14, 0xFFFFFFFF);
            int bx = width / 2 - 36, by = height - 34;
            boolean hv = in(mouseX, mouseY, bx, by, 72, 20);
            rrect(ctx, bx, by, 72, 20, hv ? 0xFFEF6A6E : accent);
            centered(ctx, tr, "Done", width / 2, by + 6, 0xFFFFFFFF);
            return;
        }

        int px = px(), py = py(), pw = pw(), ph = ph();
        rrect(ctx, px, py, pw, ph, 0xF2101420);

        // header
        rrect(ctx, px + 12, py + 8, 76, 18, accent);
        ctx.drawText(tr, Text.literal("MOD MENU").formatted(Formatting.BOLD), px + 19, py + 13, 0xFFFFFFFF, false);
        for (int i = 0; i < Theme.ACCENTS.length; i++) {
            int sx = swatchX(i), sy = swatchY();
            if (i == Theme.accentIndex) ctx.fill(sx - 1, sy - 1, sx + SW_SIZE + 1, sy + SW_SIZE + 1, 0xFFFFFFFF);
            ctx.fill(sx, sy, sx + SW_SIZE, sy + SW_SIZE, Theme.ACCENTS[i]);
        }

        // tabs
        for (int i = 0; i < TABS.length; i++) {
            int tx = tabX(i), ty = tabY();
            boolean sel = i == tab;
            boolean hv = in(mouseX, mouseY, tx, ty, TAB_W, 16);
            rrect(ctx, tx, ty, TAB_W, 16, sel ? accent : (hv ? 0xFF262C3C : 0xFF1A1F2D));
            centered(ctx, tr, TABS[i], tx + TAB_W / 2, ty + 4, sel ? 0xFFFFFFFF : 0xFF9AA4B6);
        }

        // search box
        rrect(ctx, searchX(), tabY(), 120, 16, 0xFF1A1F2D);
        search.render(ctx, mouseX, mouseY, delta);

        // cards
        List<Module> list = shown();
        int rows = (list.size() + COLS - 1) / COLS;
        int content = rows == 0 ? 0 : rows * (CH + GAP) - GAP;
        maxScroll = Math.max(0, content - gh());
        scroll = Math.max(0, Math.min(maxScroll, scroll));

        ctx.enableScissor(gx(), gy(), gx() + gw(), gy() + gh());
        for (int i = 0; i < list.size(); i++) {
            Module m = list.get(i);
            int cx = cardX(i), cy = cardY(i), cw = cw();
            if (cy + CH < gy() || cy > gy() + gh()) continue;
            boolean inGrid = mouseY >= gy() && mouseY < gy() + gh();
            rrect(ctx, cx, cy, cw, CH, 0xFF161A27);

            ctx.drawText(tr, m.name, cx + 7, cy + 7, 0xFFFFFFFF, false);
            Icons.draw(ctx, m.id, cx + cw / 2 - 8, cy + 24, m.enabled ? 0xFFFFFFFF : 0xFF566073);

            int by = rowY(i), sx = cx + 6, sw = statusW(m);
            boolean sh = inGrid && in(mouseX, mouseY, sx, by, sw, 16);
            int on = sh ? 0xFF25803F : GREEN_BG;
            int off = sh ? 0xFF303648 : 0xFF262B3A;
            rrect(ctx, sx, by, sw, 16, m.enabled ? on : off);
            centered(ctx, tr, m.enabled ? "Enabled" : "Disabled", sx + sw / 2, by + 4,
                    m.enabled ? GREEN_TX : 0xFF8A93A5);

            if (m.hasSettings()) {
                int gxp = gearBtnX(i);
                boolean gh = inGrid && in(mouseX, mouseY, gxp, by, 16, 16);
                rrect(ctx, gxp, by, 16, 16, gh ? 0xFF2C3345 : 0xFF222838);
                Icons.gear(ctx, gxp + 4, by + 4, gh ? 0xFFFFFFFF : 0xFFB8C0CF, gh ? 0xFF2C3345 : 0xFF222838);
            }
        }
        ctx.disableScissor();

        if (maxScroll > 0) {
            int th = Math.max(16, gh() * gh() / (gh() + maxScroll));
            int ty = gy() + (int) ((gh() - th) * (scroll / maxScroll));
            ctx.fill(px + pw - 8, gy(), px + pw - 5, gy() + gh(), 0xFF1A1F2D);
            ctx.fill(px + pw - 8, ty, px + pw - 5, ty + th, accent);
        }
        if (list.isEmpty()) centered(ctx, tr, "No modules found", width / 2, gy() + 20, 0xFF6B7385);

        // footer: clean Edit HUD button
        boolean canEdit = inWorld();
        boolean eh = canEdit && in(mouseX, mouseY, editX(), editY(), editW(), 20);
        int border = !canEdit ? 0xFF262B3A : (eh ? accent : 0xFF2E3547);
        rrect(ctx, editX(), editY(), editW(), 20, border);
        rrect(ctx, editX() + 1, editY() + 1, editW() - 2, 18, eh ? accent : 0xFF171B27);
        int fg = canEdit ? 0xFFFFFFFF : 0xFF566073;
        Icons.move(ctx, editX() + 12, editY() + 5, fg);
        ctx.drawText(tr, "Edit HUD", editX() + 28, editY() + 6, fg, false);
        ctx.drawText(tr, "FeatherLite 2.2", px + 12, py + ph - 18, 0xFF4A5264, false);
    }

    // ---------- input ----------
    private boolean startDrag(double mx, double my) {
        if (!inWorld()) return false;
        List<Module> mods = FeatherLite.MODULES;
        for (int i = mods.size() - 1; i >= 0; i--) {
            Module m = mods.get(i);
            if (!m.enabled || !m.hasHud()) continue;
            int x = m.screenX(client, width), y = m.screenY(client, height);
            if (in(mx, my, x, y, m.sw(client), m.sh(client))) {
                dragging = m;
                grabX = mx - x;
                grabY = my - y;
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (click.button() != 0) return super.mouseClicked(click, doubled);
        double mx = click.x(), my = click.y();

        if (editing) {
            if (in(mx, my, width / 2 - 36, height - 34, 72, 20)) {
                editing = false;
                return true;
            }
            if (startDrag(mx, my) && doubled) {
                Module m = dragging;
                dragging = null;
                client.setScreen(new ModuleSettingsScreen(this, m));
            }
            return true;
        }

        for (int i = 0; i < Theme.ACCENTS.length; i++) {
            if (in(mx, my, swatchX(i), swatchY(), SW_SIZE, SW_SIZE)) {
                Theme.accentIndex = i;
                Config.save();
                return true;
            }
        }
        for (int i = 0; i < TABS.length; i++) {
            if (in(mx, my, tabX(i), tabY(), TAB_W, 16)) {
                tab = i;
                scroll = 0;
                return true;
            }
        }
        if (in(mx, my, searchX(), tabY(), 120, 16)) {
            setFocused(search);
            return true;
        }
        setFocused(null);

        if (in(mx, my, editX(), editY(), editW(), 20)) {
            if (inWorld()) editing = true;
            return true;
        }

        if (in(mx, my, gx(), gy(), gw(), gh())) {
            List<Module> list = shown();
            for (int i = 0; i < list.size(); i++) {
                Module m = list.get(i);
                int by = rowY(i);
                if (m.hasSettings() && in(mx, my, gearBtnX(i), by, 16, 16)) {
                    client.setScreen(m.hasHud() ? new ModuleSettingsScreen(this, m) : new KeybindScreen(this, m));
                    return true;
                }
                if (in(mx, my, cardX(i) + 6, by, statusW(m), 16)) {
                    m.setEnabled(client, !m.enabled);
                    Config.save();
                    return true;
                }
            }
            return true;
        }

        if (in(mx, my, px(), py(), pw(), ph())) return true;
        if (startDrag(mx, my)) return true;
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseDragged(Click click, double offsetX, double offsetY) {
        if (dragging != null) {
            int w = dragging.sw(client), h = dragging.sh(client);
            dragging.posX = Config.clamp((click.x() - grabX) / Math.max(1, width - w));
            dragging.posY = Config.clamp((click.y() - grabY) / Math.max(1, height - h));
            return true;
        }
        return super.mouseDragged(click, offsetX, offsetY);
    }

    @Override
    public boolean mouseReleased(Click click) {
        if (dragging != null) {
            dragging = null;
            Config.save();
            return true;
        }
        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (!editing && maxScroll > 0) {
            scroll = Math.max(0, Math.min(maxScroll, scroll - verticalAmount * 18));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }
}
