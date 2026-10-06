package com.featherlite.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.debug.DebugHudEntries;
import net.minecraft.client.gui.hud.debug.DebugHudEntryVisibility;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;

import org.lwjgl.glfw.GLFW;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Locale;

public final class Modules {
    private Modules() {}

    public static class Fps extends TextModule {
        public Fps() {
            super("fps", "FPS", "Frames per second", "FPS", 0.01, 0.01);
            refreshMs = 250;
        }
        @Override protected String value(MinecraftClient mc) { return String.valueOf(mc.getCurrentFps()); }
    }

    public static class Cps extends TextModule {
        private final Deque<Long> left = new ArrayDeque<>();
        private final Deque<Long> right = new ArrayDeque<>();
        private boolean lastL, lastR;

        public Cps() { super("cps", "CPS", "Clicks per second", "PvP", "CPS", 0.01, 0.08); }

        @Override protected void update(MinecraftClient mc) {
            long now = System.currentTimeMillis();
            boolean l = mc.options.attackKey.isPressed();
            boolean r = mc.options.useKey.isPressed();
            if (l && !lastL) left.add(now);
            if (r && !lastR) right.add(now);
            lastL = l;
            lastR = r;
            prune(left, now);
            prune(right, now);
        }

        private static void prune(Deque<Long> q, long now) {
            while (!q.isEmpty() && now - q.peekFirst() > 1000) q.pollFirst();
        }

        @Override protected String value(MinecraftClient mc) {
            return left.size() + " | " + right.size();
        }
    }

    public static class Ping extends TextModule {
        public Ping() {
            super("ping", "Ping", "Server latency", "PING", 0.01, 0.15);
            refreshMs = 1000;
        }
        @Override protected String value(MinecraftClient mc) {
            PlayerListEntry e = mc.getNetworkHandler() == null ? null
                    : mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
            return e == null ? "-" : e.getLatency() + " ms";
        }
    }

    public static class Coords extends TextModule {
        public Coords() { super("coords", "Coordinates", "Your XYZ position", "XYZ", 0.01, 0.22); }
        @Override protected String value(MinecraftClient mc) {
            return String.format(Locale.ROOT, "%.1f / %.1f / %.1f",
                    mc.player.getX(), mc.player.getY(), mc.player.getZ());
        }
    }

    public static class Direction extends TextModule {
        public Direction() {
            super("direction", "Direction", "Facing direction", "DIR", 0.01, 0.29);
            refreshMs = 100;
        }
        @Override protected String value(MinecraftClient mc) {
            String d = mc.player.getHorizontalFacing().asString();
            return Character.toUpperCase(d.charAt(0)) + d.substring(1);
        }
    }

    public static class Speed extends TextModule {
        private long lastNs;
        private double lx, lz, bps;

        public Speed() {
            super("speed", "Speed", "Blocks per second", "BPS", 0.01, 0.36);
            refreshMs = 250;
        }

        @Override protected void update(MinecraftClient mc) {
            long now = System.nanoTime();
            double x = mc.player.getX(), z = mc.player.getZ();
            if (lastNs == 0) {
                lastNs = now;
                lx = x;
                lz = z;
            } else if (now - lastNs >= 250_000_000L) {
                double dt = (now - lastNs) / 1.0e9;
                bps = Math.hypot(x - lx, z - lz) / dt;
                lastNs = now;
                lx = x;
                lz = z;
            }
        }

        @Override protected String value(MinecraftClient mc) {
            return String.format(Locale.ROOT, "%.1f", bps);
        }
    }

    public static class Clock extends TextModule {
        private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("HH:mm");
        public Clock() {
            super("clock", "Clock", "Current time", "TIME", 0.01, 0.43);
            refreshMs = 1000;
        }
        @Override protected String value(MinecraftClient mc) { return LocalTime.now().format(FMT); }
    }

    public static class Keystrokes extends Module {
        public Keystrokes() { super("keystrokes", "Keystrokes", "WASD, mouse, space", "PvP", 0.01, 0.55); }

        @Override public int width(MinecraftClient mc) { return 64; }
        @Override public int height(MinecraftClient mc) { return 78; }

        @Override public void render(DrawContext ctx, MinecraftClient mc, int px, int py) {
            var o = mc.options;
            key(ctx, mc, px + 22, py, 20, 20, "W", o.forwardKey);
            key(ctx, mc, px, py + 22, 20, 20, "A", o.leftKey);
            key(ctx, mc, px + 22, py + 22, 20, 20, "S", o.backKey);
            key(ctx, mc, px + 44, py + 22, 20, 20, "D", o.rightKey);
            key(ctx, mc, px, py + 44, 31, 20, "LMB", o.attackKey);
            key(ctx, mc, px + 33, py + 44, 31, 20, "RMB", o.useKey);
            key(ctx, mc, px, py + 66, 64, 12, "SPACE", o.jumpKey);
        }

        private void key(DrawContext ctx, MinecraftClient mc, int x, int y, int w, int h,
                         String label, KeyBinding kb) {
            boolean down = kb.isPressed();
            if (down) {
                ctx.fill(x, y, x + w, y + h, highlight());
            } else if (background) {
                ctx.fill(x, y, x + w, y + h, bg());
                ctx.fill(x, y + h - 1, x + w, y + h, accent());
            }
            TextRenderer tr = mc.textRenderer;
            ctx.drawText(tr, label, x + (w - tr.getWidth(label)) / 2,
                    y + (h - tr.fontHeight) / 2 + 1, down ? 0xFF101318 : textColor(), false);
        }
    }

    public static class Armor extends Module {
        private static final EquipmentSlot[] SLOTS = {
                EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
        };

        public Armor() { super("armor", "Armor Status", "Armor durability", "PvP", 0.99, 0.5); }

        @Override public int width(MinecraftClient mc) { return 22; }
        @Override public int height(MinecraftClient mc) { return 4 * 18 + 4; }

        @Override public void render(DrawContext ctx, MinecraftClient mc, int px, int py) {
            if (background) {
                ctx.fill(px, py, px + 22, py + 76, bg());
                ctx.fill(px, py, px + 2, py + 76, accent());
            }
            for (int i = 0; i < SLOTS.length; i++) {
                ItemStack s = mc.player.getEquippedStack(SLOTS[i]);
                if (s.isEmpty()) continue;
                int ix = px + 4, iy = py + 2 + i * 18;
                ctx.drawItem(s, ix, iy);
                ctx.drawStackOverlay(mc.textRenderer, s, ix, iy);
            }
        }
    }

    /** Hold a key to look around without turning the player. Logic in Features. */
    public static class Freelook extends Module {
        public Freelook() {
            super("freelook", "Freelook", "Hold key to look around", "Utility", 0, 0);
            defaultKey = GLFW.GLFW_KEY_LEFT_ALT;
            keyCode = defaultKey;
        }
        @Override public boolean hasHud() { return false; }
    }

    /** Hold a key to zoom in. Logic in Features. */
    public static class Zoom extends Module {
        public Zoom() {
            super("zoom", "Zoom", "Hold key to zoom in", "Utility", 0, 0);
            defaultKey = GLFW.GLFW_KEY_C;
            keyCode = defaultKey;
        }
        @Override public boolean hasHud() { return false; }
    }

    /** Sprint automatically while moving forward. Logic in Features. */
    public static class ToggleSprint extends Module {
        public ToggleSprint() { super("togglesprint", "Toggle Sprint", "Always sprint forward", "Utility", 0, 0); }
        @Override public boolean hasHud() { return false; }
    }

    /** Switch for the custom main menu. No HUD element. */
    public static class MainMenu extends Module {
        public MainMenu() { super("mainmenu", "Custom Menu", "Feather-style main menu", "Misc", 0, 0); }
        @Override public boolean hasHud() { return false; }
    }

    /** Toggles vanilla entity hitbox rendering (same as F3+B). No HUD element. */
    public static class Hitboxes extends Module {
        private boolean applied;

        public Hitboxes() {
            super("hitboxes", "Hitboxes", "Show entity hitboxes", "PvP", 0, 0);
            enabled = false;
        }

        @Override public boolean hasHud() { return false; }

        @Override public void frame(MinecraftClient mc) {
            if (!applied) {
                applied = true;
                apply(mc);
            }
        }

        @Override protected void onToggle(MinecraftClient mc) { apply(mc); }

        private void apply(MinecraftClient mc) {
            mc.debugHudEntryList.setEntryVisibility(DebugHudEntries.ENTITY_HITBOXES,
                    enabled ? DebugHudEntryVisibility.ALWAYS_ON : DebugHudEntryVisibility.NEVER);
            mc.debugHudEntryList.updateVisibleEntries();
        }
    }
}
