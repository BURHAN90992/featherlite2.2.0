package com.featherlite.gui;

import net.minecraft.client.gui.DrawContext;

/** Tiny pixel icons drawn in a 16x16 box. */
public final class Icons {
    private Icons() {}

    private static void r(DrawContext c, int x, int y, int ox, int oy, int w, int h, int col) {
        c.fill(x + ox, y + oy, x + ox + w, y + oy + h, col);
    }

    /** 9x9 settings gear. */
    public static void gear(DrawContext c, int x, int y, int col, int holeCol) {
        r(c, x, y, 4, 0, 1, 2, col);
        r(c, x, y, 4, 7, 1, 2, col);
        r(c, x, y, 0, 4, 2, 1, col);
        r(c, x, y, 7, 4, 2, 1, col);
        r(c, x, y, 1, 1, 1, 1, col);
        r(c, x, y, 7, 1, 1, 1, col);
        r(c, x, y, 1, 7, 1, 1, col);
        r(c, x, y, 7, 7, 1, 1, col);
        r(c, x, y, 2, 2, 5, 5, col);
        r(c, x, y, 3, 3, 3, 3, holeCol);
        r(c, x, y, 4, 4, 1, 1, col);
    }

    /** 9x9 move (four arrows) icon. */
    public static void move(DrawContext c, int x, int y, int col) {
        r(c, x, y, 4, 0, 1, 9, col);
        r(c, x, y, 0, 4, 9, 1, col);
        r(c, x, y, 3, 1, 1, 1, col);
        r(c, x, y, 5, 1, 1, 1, col);
        r(c, x, y, 3, 7, 1, 1, col);
        r(c, x, y, 5, 7, 1, 1, col);
        r(c, x, y, 1, 3, 1, 1, col);
        r(c, x, y, 1, 5, 1, 1, col);
        r(c, x, y, 7, 3, 1, 1, col);
        r(c, x, y, 7, 5, 1, 1, col);
    }

    public static void draw(DrawContext c, String id, int x, int y, int col) {
        switch (id) {
            case "keystrokes" -> {
                r(c, x, y, 6, 1, 4, 5, col);
                r(c, x, y, 0, 8, 4, 5, col);
                r(c, x, y, 6, 8, 4, 5, col);
                r(c, x, y, 12, 8, 4, 5, col);
            }
            case "cps" -> {
                for (int i = 0; i < 9; i++) r(c, x, y, 3, 1 + i, i + 1, 1, col);
                r(c, x, y, 7, 9, 2, 5, col);
            }
            case "fps" -> {
                r(c, x, y, 1, 2, 14, 1, col);
                r(c, x, y, 1, 10, 14, 1, col);
                r(c, x, y, 1, 2, 1, 9, col);
                r(c, x, y, 14, 2, 1, 9, col);
                r(c, x, y, 6, 11, 4, 2, col);
                r(c, x, y, 4, 13, 8, 1, col);
                r(c, x, y, 4, 7, 2, 3, col);
                r(c, x, y, 7, 5, 2, 5, col);
                r(c, x, y, 10, 4, 2, 6, col);
            }
            case "ping" -> {
                r(c, x, y, 1, 11, 3, 4, col);
                r(c, x, y, 5, 8, 3, 7, col);
                r(c, x, y, 9, 5, 3, 10, col);
                r(c, x, y, 13, 2, 3, 13, col);
            }
            case "coords" -> {
                r(c, x, y, 7, 1, 2, 5, col);
                r(c, x, y, 7, 10, 2, 5, col);
                r(c, x, y, 1, 7, 5, 2, col);
                r(c, x, y, 10, 7, 5, 2, col);
                r(c, x, y, 7, 7, 2, 2, col);
            }
            case "direction" -> {
                for (int i = 0; i < 7; i++) r(c, x, y, 7 - i, 1 + i, 2 + 2 * i, 1, col);
                r(c, x, y, 6, 8, 4, 7, col);
            }
            case "speed" -> {
                r(c, x, y, 9, 0, 3, 3, col);
                r(c, x, y, 7, 3, 3, 3, col);
                r(c, x, y, 5, 6, 7, 3, col);
                r(c, x, y, 7, 9, 3, 3, col);
                r(c, x, y, 5, 12, 3, 3, col);
            }
            case "clock" -> {
                r(c, x, y, 4, 0, 8, 1, col);
                r(c, x, y, 4, 15, 8, 1, col);
                r(c, x, y, 0, 4, 1, 8, col);
                r(c, x, y, 15, 4, 1, 8, col);
                r(c, x, y, 3, 1, 1, 1, col);
                r(c, x, y, 12, 1, 1, 1, col);
                r(c, x, y, 1, 3, 1, 1, col);
                r(c, x, y, 14, 3, 1, 1, col);
                r(c, x, y, 1, 12, 1, 1, col);
                r(c, x, y, 14, 12, 1, 1, col);
                r(c, x, y, 3, 14, 1, 1, col);
                r(c, x, y, 12, 14, 1, 1, col);
                r(c, x, y, 7, 3, 2, 6, col);
                r(c, x, y, 7, 8, 5, 2, col);
            }
            case "armor" -> {
                r(c, x, y, 1, 1, 4, 5, col);
                r(c, x, y, 11, 1, 4, 5, col);
                r(c, x, y, 5, 1, 2, 4, col);
                r(c, x, y, 9, 1, 2, 4, col);
                r(c, x, y, 3, 5, 10, 10, col);
            }
            case "hitboxes" -> {
                r(c, x, y, 1, 5, 10, 1, col);
                r(c, x, y, 1, 14, 10, 1, col);
                r(c, x, y, 1, 5, 1, 10, col);
                r(c, x, y, 10, 5, 1, 10, col);
                r(c, x, y, 5, 1, 10, 1, col);
                r(c, x, y, 14, 1, 1, 10, col);
                r(c, x, y, 12, 4, 1, 1, col);
                r(c, x, y, 13, 3, 1, 1, col);
                r(c, x, y, 14, 2, 1, 1, col);
                r(c, x, y, 12, 13, 1, 1, col);
                r(c, x, y, 13, 12, 1, 1, col);
                r(c, x, y, 14, 11, 1, 1, col);
            }
            case "freelook" -> {
                r(c, x, y, 4, 3, 8, 1, col);
                r(c, x, y, 2, 4, 2, 1, col);
                r(c, x, y, 12, 4, 2, 1, col);
                r(c, x, y, 1, 5, 1, 1, col);
                r(c, x, y, 14, 5, 1, 1, col);
                r(c, x, y, 0, 6, 1, 3, col);
                r(c, x, y, 15, 6, 1, 3, col);
                r(c, x, y, 1, 9, 1, 1, col);
                r(c, x, y, 14, 9, 1, 1, col);
                r(c, x, y, 2, 10, 2, 1, col);
                r(c, x, y, 12, 10, 2, 1, col);
                r(c, x, y, 4, 11, 8, 1, col);
                r(c, x, y, 6, 5, 4, 5, col);
            }
            case "zoom" -> {
                r(c, x, y, 3, 1, 6, 1, col);
                r(c, x, y, 3, 10, 6, 1, col);
                r(c, x, y, 1, 3, 1, 6, col);
                r(c, x, y, 11, 3, 1, 6, col);
                r(c, x, y, 2, 2, 1, 1, col);
                r(c, x, y, 9, 2, 1, 1, col);
                r(c, x, y, 2, 9, 1, 1, col);
                r(c, x, y, 9, 9, 1, 1, col);
                r(c, x, y, 10, 10, 2, 2, col);
                r(c, x, y, 12, 12, 2, 2, col);
                r(c, x, y, 13, 13, 2, 2, col);
                r(c, x, y, 4, 6, 4, 1, col);
                r(c, x, y, 5, 5, 1, 3, col);
            }
            case "togglesprint" -> {
                r(c, x, y, 2, 2, 2, 2, col);
                r(c, x, y, 4, 4, 2, 2, col);
                r(c, x, y, 6, 6, 2, 2, col);
                r(c, x, y, 4, 8, 2, 2, col);
                r(c, x, y, 2, 10, 2, 2, col);
                r(c, x, y, 8, 2, 2, 2, col);
                r(c, x, y, 10, 4, 2, 2, col);
                r(c, x, y, 12, 6, 2, 2, col);
                r(c, x, y, 10, 8, 2, 2, col);
                r(c, x, y, 8, 10, 2, 2, col);
            }
            case "mainmenu" -> {
                r(c, x, y, 1, 2, 14, 1, col);
                r(c, x, y, 1, 13, 14, 1, col);
                r(c, x, y, 1, 2, 1, 12, col);
                r(c, x, y, 14, 2, 1, 12, col);
                r(c, x, y, 1, 5, 14, 1, col);
                r(c, x, y, 4, 8, 8, 2, col);
            }
            default -> r(c, x, y, 4, 4, 8, 8, col);
        }
    }
}
