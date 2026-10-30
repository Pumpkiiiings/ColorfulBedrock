package com.pumpkings.colorfulbedrock;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.pumpkings.colorfulbedrock.config.PluginConfig;
import com.pumpkings.colorfulbedrock.listener.BedrockPacketListener;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public final class ColorfulBedrockPlugin extends JavaPlugin implements CommandExecutor {

    private final PluginConfig pluginConfig = new PluginConfig();

    @Override
    public void onLoad() {
        // 1. Build and set PacketEvents API for Spigot/Paper
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        // 2. Load PacketEvents
        PacketEvents.getAPI().load();
    }

    @Override
    public void onEnable() {
        // Save and load configuration
        saveDefaultConfig();
        pluginConfig.load(getConfig());

        // 3. Initialize PacketEvents
        PacketEvents.getAPI().init();

        // Register Packet Listener for Bedrock players
        PacketEvents.getAPI().getEventManager().registerListener(
                new BedrockPacketListener(this),
                PacketListenerPriority.NORMAL
        );

        // Register reload command
        if (getCommand("colorfulbedrock") != null) {
            getCommand("colorfulbedrock").setExecutor(this);
        }

        getLogger().info("==========================================");
        getLogger().info(" ColorfulBedrock v" + getDescription().getVersion() + " Enabled!");
        getLogger().info(" PacketEvents: v2.14.0");
        getLogger().info(" Scoreboard Clean: " + (pluginConfig.isScoreboardCleanNumbers() ? "ON" : "OFF"));
        getLogger().info(" Hologram Polish:  " + (pluginConfig.isHologramsRemoveBlackBox() ? "ON" : "OFF"));
        getLogger().info(" Bedrock RGB Match: ON (Extended Palette)");
        getLogger().info("==========================================");
    }

    @Override
    public void onDisable() {
        // 4. Terminate PacketEvents safely on shutdown
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

    public PluginConfig getPluginConfig() {
        return pluginConfig;
    }
}
