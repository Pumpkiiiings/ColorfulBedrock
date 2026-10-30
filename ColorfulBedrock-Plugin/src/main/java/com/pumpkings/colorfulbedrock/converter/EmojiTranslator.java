package com.pumpkings.colorfulbedrock.converter;

import java.util.HashMap;
import java.util.Map;

/**
 * High-performance Emoji & Unicode Symbol Translator for Bedrock parity.
 * Replaces SMP (Plane 1) and unsupported Unicode symbols with authentic
 * pixel-art glyphs defined in glyph_E0.png.
 */
public final class EmojiTranslator {

    private static final Map<String, String> REPLACEMENTS = new HashMap<>();

    static {
        // Swords & Weapons -> \uE010
        REPLACEMENTS.put("\uD83D\uDDE1", "\uE010"); // 🗡 Dagger / Sword
        REPLACEMENTS.put("\u2694", "\uE010");       // ⚔ Crossed Swords

        // Skulls & Deaths -> \uE011
        REPLACEMENTS.put("\u2620", "\uE011");       // ☠ Skull and Crossbones
        REPLACEMENTS.put("\uD83D\uDC80", "\uE011"); // 💀 Skull
        REPLACEMENTS.put("\u2622", "\uE011");       // ☢ Radioactive

        // Shields & Clans -> \uE012
        REPLACEMENTS.put("\u26E8", "\uE012");       // ⛨ Shield with Cross
        REPLACEMENTS.put("\uD83D\uDEE1", "\uE012"); // 🛡 Shield

        // Bows & KoTH -> \uE013
        REPLACEMENTS.put("\uD83C\uDFF9", "\uE013"); // 🏹 Bow and Arrow

        // Clocks, Watches & Dates -> \uE014
        REPLACEMENTS.put("\u231A", "\uE014");       // ⌚ Watch
        REPLACEMENTS.put("\u23F0", "\uE014");       // ⏰ Alarm Clock
        REPLACEMENTS.put("\u270E", "\uE014");       // ✎ Pencil
        REPLACEMENTS.put("\u270D", "\uE014");       // ✍ Writing Hand

        // Anchors & Naval -> \uE015
        REPLACEMENTS.put("\u2693", "\uE015");       // ⚓ Anchor

        // Stars & Shards -> \uE016
        REPLACEMENTS.put("\u2606", "\uE016");       // ☆ White Star
        REPLACEMENTS.put("\u2605", "\uE016");       // ★ Black Star
        REPLACEMENTS.put("\u2B50", "\uE016");       // ⭐ Medium Star

        // Economy & Money -> \uE017
        REPLACEMENTS.put("\uD83D\uDCB0", "\uE017"); // 💰 Money Bag
        REPLACEMENTS.put("\uD83D\uDCB5", "\uE017"); // 💵 Banknote

        // Hearts & Health -> \uE018
        REPLACEMENTS.put("\u2764", "\uE018");       // ❤ Heart
        REPLACEMENTS.put("\uD83D\uDC96", "\uE018"); // 💖 Sparkling Heart

        // UI Arrows -> \uE019, \uE01A
        REPLACEMENTS.put("\u25B6", "\uE019");       // ▶ Right Triangle
        REPLACEMENTS.put("\u25C0", "\uE01A");       // ◀ Left Triangle
    }

    private EmojiTranslator() {}

    /**
     * Translates emojis and complex symbols in the input string
     * to Bedrock-supported pixel font glyphs.
     */
    public static String translate(String input) {
        if (input == null || input.isEmpty()) return input;

        String result = input;
        for (Map.Entry<String, String> entry : REPLACEMENTS.entrySet()) {
            if (result.contains(entry.getKey())) {
                result = result.replace(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }
}
