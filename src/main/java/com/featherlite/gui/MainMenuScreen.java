package com.featherlite.gui;

import com.featherlite.Theme;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class MainMenuScreen extends Screen {
    private static final String[] LABELS = {"Singleplayer", "Multiplayer", "Mod Menu", "Options", "Quit Game"};
    private static final int BW = 150, BH = 22, BGAP = 6;

    public MainMenuScreen() {
        super(Text.literal("FeatherLite"));
    }

    @Override public boolean shouldCloseOnEsc() { return false; }

    private int bx() { return (width - BW) / 2; }

    private int by(int i) { return height / 2 - 28 + i * (BH + BGAP) + (i == LABELS.length - 1 ? 6 : 0); }

    private static void rrect(DrawContext c, int x, int y, int w, int h, int col) {
        c.fill(x + 2, y, x + w - 2, y + h, col);
        c.fill(x + 1, y + 1, x + w - 1, y + h - 1, col);
        c.fill(x, y + 2, x + w, y + h - 2, col);
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        TextRenderer tr = textRenderer;
        int accent = Theme.accent();
        ctx.fillGradient(0, 0, width, height, 0xFF0A0C13, 0xFF161B29);

        // title
        Text title = Text.literal("FEATHERLITE").formatted(Formatting.BOLD);
        int tw = tr.getWidth(title);
        ctx.drawText(tr, title, (width - tw) / 2, height / 2 - 62, 0xFFFFFFFF, true);
        ctx.fill(width / 2 - 18, height / 2 - 49, width / 2 + 18, height / 2 - 48, accent);

        for (int i = 0; i < LABELS.length; i++) {
            boolean quit = i == LABELS.length - 1;
            boolean hv = mouseX >= bx() && mouseX < bx() + BW && mouseY >= by(i) && mouseY < by(i) + BH;
            int bg = quit ? (hv ? 0xFFEF5A5F : accent) : (hv ? 0xFF2A3042 : 0xE01A1F2D);
            rrect(ctx, bx(), by(i), BW, BH, bg);
            String s = LABELS[i];
            ctx.drawText(tr, s, (width - tr.getWidth(s)) / 2, by(i) + 7, 0xFFFFFFFF, false);
        }

        ctx.drawText(tr, "FeatherLite 2.2  |  Minecraft 1.21.11", 8, height - 14, 0xFF7C8596, false);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (click.button() == 0) {
            double mx = click.x(), my = click.y();
            for (int i = 0; i < LABELS.length; i++) {
                if (mx >= bx() && mx < bx() + BW && my >= by(i) && my < by(i) + BH) {
                    switch (i) {
                        case 0 -> client.setScreen(new SelectWorldScreen(this));
                        case 1 -> client.setScreen(new MultiplayerScreen(this));
                        case 2 -> client.setScreen(new ModulesScreen(this));
                        case 3 -> client.setScreen(new OptionsScreen(this, client.options));
                        default -> client.scheduleStop();
                    }
                    return true;
                }
            }
        }
        return super.mouseClicked(click, doubled);
    }
}
