package com.pumpkings.colorfulbedrock.converter;

import java.awt.Color;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * High-performance Color Matcher for Minecraft Bedrock.
 * Quantizes 24-bit Java RGB colors and gradients to Bedrock's extended color palette.
 */
public final class BedrockColorConverter {

    private static final Pattern HEX_PATTERN_1 = Pattern.compile("§x(§[0-9a-fA-F]){6}");
    private static final Pattern HEX_PATTERN_2 = Pattern.compile("&#([0-9a-fA-F]{6})");
    private static final Pattern HEX_PATTERN_3 = Pattern.compile("<#([0-9a-fA-F]{6})>");

    // Bedrock's extended palette: Vanilla 16 + Modern Bedrock material colors
    private static final Map<String, Color> BEDROCK_PALETTE;

    static {
        Map<String, Color> map = new HashMap<>();
        // Standard Minecraft 16 colors
        map.put("§0", new Color(0, 0, 0));
        map.put("§1", new Color(0, 0, 170));
        map.put("§2", new Color(0, 170, 0));
        map.put("§3", new Color(0, 170, 170));
        map.put("§4", new Color(170, 0, 0));
        map.put("§5", new Color(170, 0, 170));
        map.put("§6", new Color(255, 170, 0));
        map.put("§7", new Color(170, 170, 170));
        map.put("§8", new Color(85, 85, 85));
        map.put("§9", new Color(85, 85, 255));
        map.put("§a", new Color(85, 255, 85));
        map.put("§b", new Color(85, 255, 255));
        map.put("§c", new Color(255, 85, 85));
        map.put("§d", new Color(255, 85, 255));
        map.put("§e", new Color(255, 255, 85));
        map.put("§f", new Color(255, 255, 255));

        // Extended Bedrock-native material codes
        map.put("§g", new Color(221, 214, 5));   // Minecoin Gold
        map.put("§h", new Color(227, 212, 209)); // Quartz
        map.put("§m", new Color(151, 22, 7));    // Redstone
        map.put("§p", new Color(180, 104, 77));  // Material Copper
        map.put("§q", new Color(222, 177, 45));  // Material Gold
        map.put("§s", new Color(71, 160, 54));   // Material Emerald
        map.put("§t", new Color(44, 186, 168));  // Material Diamond
        map.put("§u", new Color(68, 58, 59));    // Material Netherite

        BEDROCK_PALETTE = Collections.unmodifiableMap(map);
    }

    private BedrockColorConverter() {}

    /**
     * Finds the closest Bedrock native color code for a given RGB Color
     * using human eye weighted Euclidean distance.
     */
    public static String getClosestBedrockColor(Color color) {
        if (color == null) return "§f";

        String closestCode = "§f";
        double minDistance = Double.MAX_VALUE;

        for (Map.Entry<String, Color> entry : BEDROCK_PALETTE.entrySet()) {
            Color paletteColor = entry.getValue();

            double rmean = (color.getRed() + paletteColor.getRed()) / 2.0;
            double r = color.getRed() - paletteColor.getRed();
            double g = color.getGreen() - paletteColor.getGreen();
            double b = color.getBlue() - paletteColor.getBlue();

            // Weighted RGB distance (Redmean formula)
            double weightR = 2.0 + (rmean / 256.0);
            double weightG = 4.0;
            double weightB = 2.0 + ((255.0 - rmean) / 256.0);

            double distance = (weightR * r * r) + (weightG * g * g) + (weightB * b * b);

            if (distance < minDistance) {
                minDistance = distance;
                closestCode = entry.getKey();
            }
        }

        return closestCode;
    }

    /**
     * Translates any string containing Java Hex codes (§x§r§r§g§g§b§b, &#RRGGBB, or <#RRGGBB>)
     * into Bedrock-compatible extended color codes.
     */
    public static String translateHexToBedrock(String input) {
        if (input == null || input.isEmpty()) return input;

        String result = input;

        // 1. Match §x§r§r§g§g§b§b
        Matcher matcher1 = HEX_PATTERN_1.matcher(result);
        StringBuffer sb1 = new StringBuffer();
        while (matcher1.find()) {
            String hexGroup = matcher1.group().replace("§x", "").replace("§", "");
            Color color = hexToColor(hexGroup);
            matcher1.appendReplacement(sb1, getClosestBedrockColor(color));
        }
        matcher1.appendTail(sb1);
        result = sb1.toString();

        // 2. Match &#RRGGBB
        Matcher matcher2 = HEX_PATTERN_2.matcher(result);
        StringBuffer sb2 = new StringBuffer();
        while (matcher2.find()) {
            Color color = hexToColor(matcher2.group(1));
            matcher2.appendReplacement(sb2, getClosestBedrockColor(color));
        }
        matcher2.appendTail(sb2);
        result = sb2.toString();

        // 3. Match <#RRGGBB>
        Matcher matcher3 = HEX_PATTERN_3.matcher(result);
        StringBuffer sb3 = new StringBuffer();
        while (matcher3.find()) {
            Color color = hexToColor(matcher3.group(1));
            matcher3.appendReplacement(sb3, getClosestBedrockColor(color));
        }
        matcher3.appendTail(sb3);
        result = sb3.toString();

        return result;
    }

    private static Color hexToColor(String hex) {
        try {
            int rgb = Integer.parseInt(hex, 16);
            return new Color((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF);
        } catch (Exception e) {
            return Color.WHITE;
        }
    }
}
