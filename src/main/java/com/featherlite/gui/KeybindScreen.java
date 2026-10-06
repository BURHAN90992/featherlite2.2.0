package com.featherlite.gui;

import com.featherlite.Config;
import com.featherlite.Theme;
import com.featherlite.module.Module;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

/** Key setting for hold-to-use modules (Freelook, Zoom). */
public class KeybindScreen extends Screen {
    private static final int PW = 260, PH = 124;

    private final Screen parent;
    private final Module m;
    private boolean listening;

    public KeybindScreen(Screen parent, Module m) {
        super(Text.literal(m.name + " key"));
        this.parent = parent;
        this.m = m;
    }

    @Override public boolean shouldPause() { return false; }
    @Override public void close() { client.setScreen(parent); }
    @Override public void removed() { Config.save(); }

    private int px() { return (width - PW) / 2; }
    private int py() { return (height - PH) / 2; }
    private int btnX() { return px() + 80; }
    private int btnY() { return py() + 44; }

    private static boolean in(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    private static void rrect(DrawContext c, int x, int y, int w, int h, int col) {
        c.fill(x + 2, y, x + w - 2, y + h, col);
        c.fill(x + 1, y + 1, x + w - 1, y + h - 1, col);
        c.fill(x, y + 2, x + w, y + h - 2, col);
    }

    private String keyName() {
        if (m.keyCode <= 0) return "None";
        return InputUtil.Type.KEYSYM.createFromCode(m.keyCode).getLocalizedText().getString();
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
        ctx.drawText(tr, "Key", px + 12 + nameW + 8, py + 15, 0xFF7C8596, false);

        ctx.drawText(tr, "Key", px + 16, btnY() + 6, 0xFFFFFFFF, false);
        boolean hv = in(mouseX, mouseY, btnX(), btnY(), 160, 20);
        rrect(ctx, btnX(), btnY(), 160, 20, listening ? accent : (hv ? 0xFF2E3547 : 0xFF262C3C));
        rrect(ctx, btnX() + 1, btnY() + 1, 158, 18, listening ? 0xFF171B27 : (hv ? 0xFF222838 : 0xFF1A1F2D));
        String label = listening ? "Press a key..." : keyName();
        ctx.drawText(tr, label, btnX() + (160 - tr.getWidth(label)) / 2, btnY() + 6,
                listening ? accent : 0xFFFFFFFF, false);

        ctx.drawText(tr, m.desc + "  |  ESC cancels", px + 16, py + 74, 0xFF6B7385, false);

        int by = py + PH - 30;
        boolean h1 = in(mouseX, mouseY, px + 12, by, 70, 20);
        rrect(ctx, px + 12, by, 70, 20, h1 ? 0xFF2C3345 : 0xFF222838);
        ctx.drawText(tr, "Reset", px + 12 + (70 - tr.getWidth("Reset")) / 2, by + 6, 0xFFB8C0CF, false);
        boolean h2 = in(mouseX, mouseY, px + PW - 82, by, 70, 20);
        rrect(ctx, px + PW - 82, by, 70, 20, h2 ? 0xFFEF6A6E : accent);
        ctx.drawText(tr, "Done", px + PW - 82 + (70 - tr.getWidth("Done")) / 2, by + 6, 0xFFFFFFFF, false);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (click.button() != 0) return super.mouseClicked(click, doubled);
        double mx = click.x(), my = click.y();
        int by = py() + PH - 30;

        if (in(mx, my, btnX(), btnY(), 160, 20)) {
            listening = !listening;
            return true;
        }
        listening = false;
        if (in(mx, my, px() + 12, by, 70, 20)) {
            m.keyCode = m.defaultKey;
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
    public boolean keyPressed(KeyInput input) {
        if (listening) {
            int k = input.key();
            if (k == GLFW.GLFW_KEY_ESCAPE) {
                listening = false;
                return true;
            }
            if (k != GLFW.GLFW_KEY_UNKNOWN) {
                m.keyCode = k;
                listening = false;
                Config.save();
                return true;
            }
        }
        return super.keyPressed(input);
    }
}
