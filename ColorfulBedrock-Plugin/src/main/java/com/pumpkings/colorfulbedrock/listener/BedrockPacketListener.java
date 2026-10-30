package com.pumpkings.colorfulbedrock.listener;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.*;
import com.pumpkings.colorfulbedrock.ColorfulBedrockPlugin;
import com.pumpkings.colorfulbedrock.config.PluginConfig;
import com.pumpkings.colorfulbedrock.converter.BedrockColorConverter;
import com.pumpkings.colorfulbedrock.converter.ComponentBedrockTranslator;
import com.pumpkings.colorfulbedrock.util.PingGlyphs;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class BedrockPacketListener implements PacketListener {

    private final ColorfulBedrockPlugin plugin;
    private final PluginConfig config;

    public BedrockPacketListener(ColorfulBedrockPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getPluginConfig();
    }

    /**
     * Checks whether a player is connected from Bedrock Edition via Geyser or Floodgate.
     */
    private boolean isBedrockPlayer(UUID uuid) {
        // 1. Direct Geyser Spigot connection
        try {
            if (org.geysermc.geyser.api.GeyserApi.api() != null &&
                org.geysermc.geyser.api.GeyserApi.api().isBedrockPlayer(uuid)) {
                return true;
            }
        } catch (Throwable ignored) {}

        // 2. Floodgate connection
        try {
            Class<?> floodgateApiClass = Class.forName("org.geysermc.floodgate.api.FloodgateApi");
            Object instance = floodgateApiClass.getMethod("getInstance").invoke(null);
            if (instance != null) {
                Object result = floodgateApiClass.getMethod("isFloodgatePlayer", UUID.class).invoke(instance, uuid);
                if (result instanceof Boolean b && b) {
                    return true;
                }
            }
        } catch (Throwable ignored) {}

        return false;
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        Object playerObj = event.getPlayer();
        if (!(playerObj instanceof Player player)) {
            return;
        }

        UUID uuid = player.getUniqueId();

        // PARITY RULE: Java players always receive unmodified packets
        if (!isBedrockPlayer(uuid)) {
            return;
        }

        // 1. Handle Scoreboard Packets
        if (config.isScoreboardEnabled()) {
            if (event.getPacketType() == PacketType.Play.Server.SCOREBOARD_OBJECTIVE) {
                handleScoreboardObjective(event);
                return;
            }
            if (event.getPacketType() == PacketType.Play.Server.UPDATE_SCORE) {
                handleUpdateScore(event);
                return;
            }
            if (event.getPacketType() == PacketType.Play.Server.TEAMS) {
                handleTeams(event);
                return;
            }
        }

        // 2. Handle Tab List Packets
        if (config.isTablistEnabled()) {
            if (event.getPacketType() == PacketType.Play.Server.PLAYER_LIST_HEADER_AND_FOOTER) {
                handleTabListHeaderFooter(event);
                return;
            }
            if (event.getPacketType() == PacketType.Play.Server.PLAYER_INFO_UPDATE) {
                handlePlayerInfoUpdate(event);
                return;
            }
        }

        // 3. Handle Holograms & Text Displays
        if (config.isHologramsEnabled()) {
            if (event.getPacketType() == PacketType.Play.Server.ENTITY_METADATA) {
                handleEntityMetadata(event);
            }
        }
    }

    private void handleScoreboardObjective(PacketSendEvent event) {
        try {
            WrapperPlayServerScoreboardObjective wrapper = new WrapperPlayServerScoreboardObjective(event);
            Component displayName = wrapper.getDisplayName();
            if (displayName != null && config.isScoreboardConvertHex()) {
                wrapper.setDisplayName(ComponentBedrockTranslator.translate(displayName));
            }
        } catch (Throwable ignored) {}
    }

    private void handleUpdateScore(PacketSendEvent event) {
        try {
            WrapperPlayServerUpdateScore wrapper = new WrapperPlayServerUpdateScore(event);
            
            // Modern 1.20.3+ display component
            Component displayComp = wrapper.getEntityDisplayName();
            if (displayComp != null && config.isScoreboardConvertHex()) {
                wrapper.setEntityDisplayName(ComponentBedrockTranslator.translate(displayComp));
            }

            // Legacy entity name line
            String entityName = wrapper.getEntityName();
            if (entityName != null && config.isScoreboardConvertHex()) {
                wrapper.setEntityName(BedrockColorConverter.translateHexToBedrock(entityName));
            }
        } catch (Throwable ignored) {}
    }

    private void handleTeams(PacketSendEvent event) {
        try {
            WrapperPlayServerTeams wrapper = new WrapperPlayServerTeams(event);
            Optional<WrapperPlayServerTeams.ScoreBoardTeamInfo> optInfo = wrapper.getTeamInfo();
            if (optInfo.isPresent() && config.isScoreboardConvertHex()) {
                WrapperPlayServerTeams.ScoreBoardTeamInfo info = optInfo.get();
                
                Component prefix = info.getPrefix();
                if (prefix != null) {
                    info.setPrefix(ComponentBedrockTranslator.translate(prefix));
                }

                Component suffix = info.getSuffix();
                if (suffix != null) {
                    info.setSuffix(ComponentBedrockTranslator.translate(suffix));
                }

                Component displayName = info.getDisplayName();
                if (displayName != null) {
                    info.setDisplayName(ComponentBedrockTranslator.translate(displayName));
                }
            }
        } catch (Throwable ignored) {}
    }

    private void handleTabListHeaderFooter(PacketSendEvent event) {
        try {
            WrapperPlayServerPlayerListHeaderAndFooter wrapper = 
                    new WrapperPlayServerPlayerListHeaderAndFooter(event);

            Component header = wrapper.getHeader();
            if (header != null) {
                wrapper.setHeader(ComponentBedrockTranslator.translate(header));
            }

            Component footer = wrapper.getFooter();
            if (footer != null) {
                wrapper.setFooter(ComponentBedrockTranslator.translate(footer));
            }
        } catch (Throwable ignored) {}
    }

    private void handlePlayerInfoUpdate(PacketSendEvent event) {
        try {
            WrapperPlayServerPlayerInfoUpdate wrapper = new WrapperPlayServerPlayerInfoUpdate(event);
            List<WrapperPlayServerPlayerInfoUpdate.PlayerInfo> entries = wrapper.getEntries();
            if (entries == null) return;

            for (WrapperPlayServerPlayerInfoUpdate.PlayerInfo entry : entries) {
                Component displayName = entry.getDisplayName();
                
                // If display name is present, convert hex colors and inject ping glyph
                if (displayName != null) {
                    Component translated = ComponentBedrockTranslator.translate(displayName);

                    if (config.isTablistFormatPing()) {
                        int latency = entry.getLatency();
                        String pingGlyph = PingGlyphs.formatPing(latency, config.isTablistUseCustomGlyphs());
                        translated = translated.append(Component.text(" " + pingGlyph));
                    }

                    entry.setDisplayName(translated);
                }
            }
        } catch (Throwable ignored) {}
    }

    @SuppressWarnings("unchecked")
    private void handleEntityMetadata(PacketSendEvent event) {
        try {
            WrapperPlayServerEntityMetadata wrapper = new WrapperPlayServerEntityMetadata(event);
            List<? extends EntityData<?>> metadataList = wrapper.getEntityMetadata();
            if (metadataList == null) return;

            for (EntityData<?> data : metadataList) {
                int index = data.getIndex();
                Object val = data.getValue();

                // General Entity Custom Name: Index 2
                if (index == 2) {
                    if (val instanceof Optional<?> opt && opt.isPresent()) {
                        Object inner = opt.get();
                        if (inner instanceof Component comp) {
                            ((EntityData<Optional<Component>>) data).setValue(
                                    Optional.of(ComponentBedrockTranslator.translate(comp))
                            );
                        }
                    } else if (val instanceof Component comp) {
                        ((EntityData<Component>) data).setValue(ComponentBedrockTranslator.translate(comp));
                    }
                }

                // TextDisplay Metadata (Minecraft 1.19.4 - 1.21+)
                // Index 15: Text component
                if (index == 15 && val instanceof Component comp && config.isHologramsConvertHex()) {
                    ((EntityData<Component>) data).setValue(ComponentBedrockTranslator.translate(comp));
                }

                // Index 17: Background ARGB Color
                // Remove black box: force alpha to 0x00000000
                if (index == 17 && config.isHologramsRemoveBlackBox()) {
                    ((EntityData<Integer>) data).setValue(0x00000000);
                }

                // Index 19: Display Flags (Bit 0x01 = Shadow)
                if (index == 19 && config.isHologramsForceShadow() && val instanceof Byte flags) {
                    ((EntityData<Byte>) data).setValue((byte) (flags | 0x01));
                }
            }
        } catch (Throwable ignored) {}
    }
}
