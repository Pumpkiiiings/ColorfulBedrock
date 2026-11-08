package com.pumpkings.colorfulbedrock.converter;

import java.awt.Color;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Perceptual Color Matcher for Minecraft Bedrock parity.
 * Accurately translates 24-bit RGB colors and gradients into Bedrock's
 * saturated color palette using perceptual HSB hue preservation.
 */
public final class BedrockColorConverter {

    private static final Pattern HEX_PATTERN_1 = Pattern.compile("§x(§[0-9a-fA-F]){6}");
    private static final Pattern HEX_PATTERN_2 = Pattern.compile("&#([0-9a-fA-F]{6})");
    private static final Pattern HEX_PATTERN_3 = Pattern.compile("<#([0-9a-fA-F]{6})>");
    private static final Pattern HEX_PATTERN_4 = Pattern.compile("<color:#([0-9a-fA-F]{6})>", Pattern.CASE_INSENSITIVE);
    private static final Pattern TAGS_CLEANUP_PATTERN = Pattern.compile("</?(?:shadow|gradient|b|i|u|st|color)[^>]*>", Pattern.CASE_INSENSITIVE);

    // Bedrock vanilla 16 + extended material colors
    private static final Map<String, Color> BEDROCK_PALETTE;
    private static final Set<String> ACHROMATIC = Set.of("§0", "§7", "§8", "§f", "§h", "§i", "§j");

    static {
        Map<String, Color> map = new LinkedHashMap<>();
        // Standard Minecraft 16 colors
        map.put("§0", new Color(0, 0, 0));       // Black
        map.put("§1", new Color(0, 0, 170));     // Dark Blue
        map.put("§2", new Color(0, 170, 0));     // Dark Green
        map.put("§3", new Color(0, 170, 170));   // Dark Aqua
        map.put("§4", new Color(170, 0, 0));     // Dark Red
        map.put("§5", new Color(170, 0, 170));   // Dark Purple
        map.put("§6", new Color(255, 170, 0));   // Gold / Orange
        map.put("§7", new Color(170, 170, 170)); // Gray
        map.put("§8", new Color(85, 85, 85));    // Dark Gray
        map.put("§9", new Color(85, 85, 255));   // Blue
        map.put("§a", new Color(85, 255, 85));   // Green / Lime
        map.put("§b", new Color(85, 255, 255));  // Aqua / Cyan
        map.put("§c", new Color(255, 85, 85));   // Red
        map.put("§d", new Color(255, 85, 255));  // Light Purple / Pink
        map.put("§e", new Color(255, 255, 85));  // Yellow
        map.put("§f", new Color(255, 255, 255)); // White

        // Extended Bedrock-native material codes
        map.put("§g", new Color(221, 214, 5));   // Minecoin Gold (#DDD605)
        map.put("§h", new Color(227, 212, 209)); // Material Quartz (#E3D4D1)
        map.put("§i", new Color(206, 202, 202)); // Material Iron (#CECACA)
        map.put("§j", new Color(68, 58, 59));    // Material Netherite (#443A3B)
        map.put("§m", new Color(151, 22, 7));    // Material Redstone (#971607)
        map.put("§n", new Color(180, 104, 77));  // Material Copper (#B4684D)
        map.put("§p", new Color(222, 177, 45));  // Material Gold (#DEB12D)
        map.put("§q", new Color(71, 160, 54));   // Material Emerald (#47A036)
        map.put("§s", new Color(44, 186, 168));  // Material Diamond (#2CBAA8)
        map.put("§t", new Color(33, 73, 123));   // Material Lapis (#21497B)
        map.put("§u", new Color(154, 92, 198));  // Material Amethyst (#9A5CC6)

        BEDROCK_PALETTE = Collections.unmodifiableMap(map);
    }

    private BedrockColorConverter() {}

    /**
     * Finds the closest Bedrock color code using perceptual HSB hue matching.
     * Guarantees chromatic saturation so colorful Java text never washes out to gray.
     */
    public static String getClosestBedrockColor(Color target) {
        if (target == null) return "§f";

        float[] hsbTarget = Color.RGBtoHSB(target.getRed(), target.getGreen(), target.getBlue(), null);
        float hTarget = hsbTarget[0];
        float sTarget = hsbTarget[1];
        float bTarget = hsbTarget[2];

        boolean isTargetChromatic = sTarget > 0.18f && bTarget > 0.15f;

        // 1. Priority Hue Specializations for high-profile UI palettes
        if (isTargetChromatic) {
            // Bright pink / rose / magenta (e.g. #FF258F Shards)
            if (hTarget >= 0.88f && hTarget <= 0.94f && bTarget > 0.6f && sTarget > 0.4f) {
                return "§d"; // Pink
            }

            // Crimson / Ruby Red (e.g. #FF1C59 Muertes)
            if ((hTarget > 0.94f || hTarget < 0.04f) && bTarget > 0.5f) {
                return "§c"; // Red
            }

            // Soft diamond / Sky blue / Aqua (e.g. #75B3FF Asesinatos)
            if (hTarget >= 0.54f && hTarget <= 0.60f && bTarget > 0.65f) {
                return "§b"; // Aqua
            }

            // Vivid Blue (e.g. #5489FF)
            if (hTarget > 0.60f && hTarget <= 0.68f) {
                return "§9"; // Blue
            }

            // Lime / Bright Green (e.g. #7DFFA8 Jugando +)
            if (hTarget >= 0.28f && hTarget <= 0.42f && bTarget > 0.6f) {
                return "§a"; // Lime
            }

            // Warm Gold / Orange (e.g. #FFDB02, #FFA01F mc.colorfulsmp.us)
            if (hTarget >= 0.08f && hTarget <= 0.15f) {
                return "§6"; // Gold
            }
        }

        // 2. Perceptual Hue-weighted search
        String bestCode = "§f";
        double minScore = Double.MAX_VALUE;

        for (Map.Entry<String, Color> entry : BEDROCK_PALETTE.entrySet()) {
            String code = entry.getKey();
            Color pal = entry.getValue();

            // Prevent vibrant colors from collapsing to dull gray/white
            if (isTargetChromatic && ACHROMATIC.contains(code)) {
                continue;
            }

            float[] hsbPal = Color.RGBtoHSB(pal.getRed(), pal.getGreen(), pal.getBlue(), null);
            float hPal = hsbPal[0];
            float sPal = hsbPal[1];
            float bPal = hsbPal[2];

            double hDiff = Math.abs(hTarget - hPal);
            if (hDiff > 0.5) hDiff = 1.0 - hDiff;

            double sDiff = Math.abs(sTarget - sPal);
            double bDiff = Math.abs(bTarget - bPal);

            double score;
            if (isTargetChromatic) {
                score = (hDiff * 7.0) + (sDiff * 2.0) + (bDiff * 1.5);
            } else {
                score = (sDiff * 1.0) + (bDiff * 4.0);
            }

            if (score < minScore) {
                minScore = score;
                bestCode = code;
            }
        }

        return bestCode;
    }

    /**
     * Translates any string containing Java Hex codes (§x§r§r§g§g§b§b, &#RRGGBB, or <#RRGGBB>)
     * into Bedrock-compatible extended color codes and strips unsupported MiniMessage tags.
     */
    public static String translateHexToBedrock(String input) {
        if (input == null || input.isEmpty()) return input;

        String result = input;

        // 1. Clean unsupported MiniMessage / XML tags that might leak through
        result = TAGS_CLEANUP_PATTERN.matcher(result).replaceAll("");

        // 2. Match §x§r§r§g§g§b§b (Kyori Adventure hex output)
        Matcher matcher1 = HEX_PATTERN_1.matcher(result);
        StringBuilder sb1 = new StringBuilder();
        while (matcher1.find()) {
            String hexGroup = matcher1.group().replace("§x", "").replace("§", "");
            Color color = hexToColor(hexGroup);
            matcher1.appendReplacement(sb1, Matcher.quoteReplacement(getClosestBedrockColor(color)));
        }
        matcher1.appendTail(sb1);
        result = sb1.toString();

        // 3. Match &#RRGGBB
        Matcher matcher2 = HEX_PATTERN_2.matcher(result);
        StringBuilder sb2 = new StringBuilder();
        while (matcher2.find()) {
            Color color = hexToColor(matcher2.group(1));
            matcher2.appendReplacement(sb2, Matcher.quoteReplacement(getClosestBedrockColor(color)));
        }
        matcher2.appendTail(sb2);
        result = sb2.toString();

        // 4. Match <#RRGGBB>
        Matcher matcher3 = HEX_PATTERN_3.matcher(result);
        StringBuilder sb3 = new StringBuilder();
        while (matcher3.find()) {
            Color color = hexToColor(matcher3.group(1));
            matcher3.appendReplacement(sb3, Matcher.quoteReplacement(getClosestBedrockColor(color)));
        }
        matcher3.appendTail(sb3);
        result = sb3.toString();

        // 5. Match <color:#RRGGBB>
        Matcher matcher4 = HEX_PATTERN_4.matcher(result);
        StringBuilder sb4 = new StringBuilder();
        while (matcher4.find()) {
            Color color = hexToColor(matcher4.group(1));
            matcher4.appendReplacement(sb4, Matcher.quoteReplacement(getClosestBedrockColor(color)));
        }
        matcher4.appendTail(sb4);
        result = sb4.toString();

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
