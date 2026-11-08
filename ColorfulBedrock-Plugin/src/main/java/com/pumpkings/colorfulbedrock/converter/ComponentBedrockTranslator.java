package com.pumpkings.colorfulbedrock.converter;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

/**
 * Translates Adventure Components into Bedrock-safe components
 * with quantized RGB colors, translated emojis, and Bedrock section codes.
 */
public final class ComponentBedrockTranslator {

    private static final LegacyComponentSerializer HEX_SERIALIZER = 
            LegacyComponentSerializer.builder()
                    .character('§')
                    .hexColors()
                    .useUnusualXRepeatedCharacterHexFormat()
                    .build();

    private static final LegacyComponentSerializer LEGACY_DESERIALIZER =
            LegacyComponentSerializer.builder()
                    .character('§')
                    .build();

    private ComponentBedrockTranslator() {}

    /**
     * Translates a Component by preserving 24-bit hex, converting emojis,
     * quantizing RGB colors to Bedrock codes, and returning a Bedrock-safe component
     * with explicit text colors on each node so Geyser/Floodgate renders authentic colors.
     */
    public static Component translate(Component component) {
        if (component == null) return Component.empty();

        String legacy = HEX_SERIALIZER.serialize(component);
        String emojisTranslated = EmojiTranslator.translate(legacy);
        String bedrockFormatted = BedrockColorConverter.translateHexToBedrock(emojisTranslated);

        return LEGACY_DESERIALIZER.deserialize(bedrockFormatted);
    }

    /**
     * Converts a Component directly to a Bedrock-compatible string.
     */
    public static String toBedrockString(Component component) {
        if (component == null) return "";
        String legacy = HEX_SERIALIZER.serialize(component);
        String emojisTranslated = EmojiTranslator.translate(legacy);
        return BedrockColorConverter.translateHexToBedrock(emojisTranslated);
    }
}
