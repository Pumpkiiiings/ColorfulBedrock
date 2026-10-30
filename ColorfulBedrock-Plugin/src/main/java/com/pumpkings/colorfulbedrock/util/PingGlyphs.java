package com.pumpkings.colorfulbedrock.util;

/**
 * Utility for formatting ping into visual indicators or custom Unicode glyphs
 * for Bedrock clients.
 */
public final class PingGlyphs {

    private PingGlyphs() {}

    /**
     * Formats ping into custom glyphs or colored bars.
     *
     * @param ping Latency in milliseconds
     * @param useCustomGlyphs Whether to use custom PUA unicode glyphs
     * @return Formatted ping string
     */
    public static String formatPing(int ping, boolean useCustomGlyphs) {
        if (useCustomGlyphs) {
            if (ping <= 50)  return "§a\uE005 " + ping + "ms";
            if (ping <= 100) return "§a\uE004 " + ping + "ms";
            if (ping <= 170) return "§e\uE003 " + ping + "ms";
            if (ping <= 300) return "§6\uE002 " + ping + "ms";
            return "§c\uE001 " + ping + "ms";
        } else {
            if (ping <= 50)  return "§a[" + ping + "ms §a📶]";
            if (ping <= 100) return "§a[" + ping + "ms §a📶]";
            if (ping <= 170) return "§e[" + ping + "ms §e📶]";
            if (ping <= 300) return "§6[" + ping + "ms §6📶]";
            return "§c[" + ping + "ms §c📶]";
        }
    }
}
