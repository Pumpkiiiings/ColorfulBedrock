package com.pumpkings.colorfulbedrock.converter;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Translates Adventure Components into Bedrock-safe components
 * with quantized RGB colors and legacy conversion.
 */
public final class ComponentBedrockTranslator {

    private static final LegacyComponentSerializer SECTION_SERIALIZER = 
            LegacyComponentSerializer.legacySection();

    private ComponentBedrockTranslator() {}

    /**
     * Translates a Component by converting RGB colors to Bedrock codes.
     */
    public static Component translate(Component component) {
        if (component == null) return Component.empty();

        // Serialize to legacy text, translate Hex to Bedrock codes, and reconstruct component
        String legacy = SECTION_SERIALIZER.serialize(component);
        String translated = BedrockColorConverter.translateHexToBedrock(legacy);

        return SECTION_SERIALIZER.deserialize(translated);
    }

    /**
     * Converts a Component directly to a Bedrock-compatible string.
     */
    public static String toBedrockString(Component component) {
        if (component == null) return "";
        String legacy = SECTION_SERIALIZER.serialize(component);
        return BedrockColorConverter.translateHexToBedrock(legacy);
    }
}
