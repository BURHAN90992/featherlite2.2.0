package com.featherlite;

public final class Theme {
    public static final int[] ACCENTS = {
            0xFFE5484D, // red (default)
            0xFF4C8DFF, // blue
            0xFF9B5CFF, // purple
            0xFF2ECC71, // green
            0xFFFFA23A, // orange
            0xFF22D3EE  // cyan
    };
    /** Per-module colors. Index 0 means "follow the theme accent". */
    public static final int[] MODULE_COLORS = {
            0x00000000,
            0xFFFFFFFF, 0xFFE5484D, 0xFFFFA23A, 0xFFFFD93D,
            0xFF2ECC71, 0xFF22D3EE, 0xFF4C8DFF, 0xFF9B5CFF, 0xFFFF6BC1
    };
    public static int accentIndex = 0;

    private Theme() {}

    public static int accent() {
        return ACCENTS[Math.max(0, Math.min(ACCENTS.length - 1, accentIndex))];
    }
}
