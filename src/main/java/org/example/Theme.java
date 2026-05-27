package org.example;

import java.awt.*;

public class Theme {
    // Dark background palette
    public static final Color BG_DARKEST   = new Color(0x0F, 0x10, 0x14);
    public static final Color BG_DARK      = new Color(0x16, 0x18, 0x1E);
    public static final Color BG_PANEL     = new Color(0x1C, 0x1F, 0x28);
    public static final Color BG_CARD      = new Color(0x22, 0x26, 0x32);
    public static final Color BG_HOVER     = new Color(0x2A, 0x2F, 0x3E);
    public static final Color BG_SELECTED  = new Color(0x1E, 0x3A, 0x5F);

    // Accent — electric blue
    public static final Color ACCENT       = new Color(0x3D, 0x8E, 0xFF);
    public static final Color ACCENT_DIM   = new Color(0x1A, 0x4A, 0x99);
    public static final Color ACCENT_GLOW  = new Color(0x5A, 0xA8, 0xFF);

    // Text
    public static final Color TEXT_PRIMARY   = new Color(0xE8, 0xEA, 0xF0);
    public static final Color TEXT_SECONDARY = new Color(0x8A, 0x90, 0xA8);
    public static final Color TEXT_MUTED     = new Color(0x50, 0x56, 0x6E);
    public static final Color TEXT_ACCENT    = new Color(0x5A, 0xA8, 0xFF);

    // Status
    public static final Color SUCCESS = new Color(0x30, 0xC4, 0x7A);
    public static final Color WARNING = new Color(0xFF, 0xB8, 0x30);
    public static final Color ERROR   = new Color(0xFF, 0x4D, 0x6A);

    // Borders
    public static final Color BORDER       = new Color(0x2E, 0x33, 0x45);
    public static final Color BORDER_FOCUS = new Color(0x3D, 0x8E, 0xFF);

    // Fonts
    public static final Font FONT_TITLE   = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_HEADING = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_BODY    = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL   = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_MONO    = new Font("Consolas", Font.PLAIN, 12);
    public static final Font FONT_BADGE   = new Font("Segoe UI", Font.BOLD, 10);
}