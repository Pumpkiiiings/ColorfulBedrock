package com.pumpkings.colorfulbedrock;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.manager.server.ServerManager;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.pumpkings.colorfulbedrock.command.TabCommand;
import com.pumpkings.colorfulbedrock.config.PluginConfig;
import com.pumpkings.colorfulbedrock.listener.BedrockPacketListener;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.util.UUID;

public final class ColorfulBedrockPlugin extends JavaPlugin implements CommandExecutor {

    private final PluginConfig pluginConfig = new PluginConfig();
    private volatile String latestHeader = "";
    private volatile String latestFooter = "";

    @Override
    public void onLoad() {
        // 1. Build and set PacketEvents API for Spigot/Paper
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));

        // 2. Enable packet re-encoding by default for Bedrock packet modification reliability
        PacketEvents.getAPI().getSettings().reEncodeByDefault(true);

        // 3. Fix server version if custom fork breaks version detection
        fixPacketEventsVersion();

        // 4. Load PacketEvents
        PacketEvents.getAPI().load();
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        pluginConfig.load(getConfig());

        // 4. Initialize PacketEvents
        PacketEvents.getAPI().init();

        // Ensure version is set properly even after init
        fixPacketEventsVersion();

        // Register Packet Listener for Bedrock players
        PacketEvents.getAPI().getEventManager().registerListener(
                new BedrockPacketListener(this),
                PacketListenerPriority.NORMAL
        );

        // Register /colorfulbedrock command
        if (getCommand("colorfulbedrock") != null) {
            getCommand("colorfulbedrock").setExecutor(this);
        }

        // Register /tab, /tablist, /playerlist commands
        TabCommand tabCommand = new TabCommand(this);
        if (getCommand("tab") != null) {
            getCommand("tab").setExecutor(tabCommand);
        }
        if (getCommand("tablist") != null) {
            getCommand("tablist").setExecutor(tabCommand);
        }
        if (getCommand("playerlist") != null) {
            getCommand("playerlist").setExecutor(tabCommand);
        }

        getLogger().info("==========================================");
        getLogger().info(" ColorfulBedrock v" + getDescription().getVersion() + " Enabled!");
        getLogger().info(" PacketEvents: v2.14.0 (Server: " + PacketEvents.getAPI().getServerManager().getVersion() + ")");
        getLogger().info(" Scoreboard Clean: " + (pluginConfig.isScoreboardCleanNumbers() ? "ON" : "OFF"));
        getLogger().info(" Hologram Polish:  " + (pluginConfig.isHologramsRemoveBlackBox() ? "ON" : "OFF"));
        getLogger().info(" Bedrock RGB Match: ON (Extended 27-Color Palette)");
        getLogger().info(" Emoji Translation: ON (Native Pixel Glyphs)");
        getLogger().info(" Tab List Function: ON (/tab & Keybind)");
        getLogger().info("==========================================");
    }

    public boolean isBedrockPlayer(UUID uuid) {
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

    public String getLatestHeader() {
        return latestHeader;
    }

    public void setLatestHeader(String header) {
        this.latestHeader = header;
    }

    public String getLatestFooter() {
        return latestFooter;
    }

    public void setLatestFooter(String footer) {
        this.latestFooter = footer;
    }

    public PluginConfig getPluginConfig() {
        return pluginConfig;
    }

    private void fixPacketEventsVersion() {
        try {
            ServerManager serverManager = PacketEvents.getAPI().getServerManager();
            if (serverManager != null && (serverManager.getVersion() == null || serverManager.getVersion() == ServerVersion.V_1_8_8)) {
                Field field = serverManager.getClass().getDeclaredField("serverVersion");
                field.setAccessible(true);
                field.set(serverManager, ServerVersion.V_1_21_1);
                getLogger().info("Corrected PacketEvents server version: Forced to V_1_21_1!");
            }
        } catch (Throwable t) {
            // Version already set or inaccessible
        }
    }

    @Override
    public void onDisable() {
        PacketEvents.getAPI().terminate();
        getLogger().info("ColorfulBedrock has been disabled.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("colorfulbedrock.admin")) {
                sender.sendMessage("§cYou do not have permission to use this command.");
                return true;
            }
            reloadConfig();
            pluginConfig.load(getConfig());
            sender.sendMessage("§a[ColorfulBedrock] Configuration reloaded successfully!");
            return true;
        }

        sender.sendMessage("§6[ColorfulBedrock] §7Version: §f" + getDescription().getVersion());
        sender.sendMessage("§7Use §f/colorfulbedrock reload §7to reload the configuration.");
        return true;
    }
}
