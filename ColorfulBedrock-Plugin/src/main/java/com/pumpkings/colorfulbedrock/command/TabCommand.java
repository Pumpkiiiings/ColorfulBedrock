package com.pumpkings.colorfulbedrock.command;

import com.pumpkings.colorfulbedrock.ColorfulBedrockPlugin;
import com.pumpkings.colorfulbedrock.util.PingGlyphs;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

import java.util.UUID;

/**
 * Handles /tab, /tablist and /playerlist commands.
 * Displays an interactive Bedrock SimpleForm with header, footer, player list, and ping glyphs
 * for Bedrock users, or standard info for Java players.
 */
public class TabCommand implements CommandExecutor {

    private final ColorfulBedrockPlugin plugin;

    public TabCommand(ColorfulBedrockPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cSolo jugadores pueden ejecutar este comando.");
            return true;
        }

        UUID uuid = player.getUniqueId();
        boolean isBedrock = plugin.isBedrockPlayer(uuid);

        if (!isBedrock) {
            player.sendMessage("§e[ColorfulBedrock] §7Mantén presionada la tecla §eTAB §7para ver la lista de jugadores.");
            return true;
        }

        // Build Tab list text
        StringBuilder sb = new StringBuilder();

        String header = plugin.getLatestHeader();
        if (header != null && !header.isBlank()) {
            sb.append(header).append("\n\n");
        }

        sb.append("§7----------------------------------------\n");
        sb.append("§f   Jugadores Conectados (§a").append(Bukkit.getOnlinePlayers().size()).append("§f)\n");
        sb.append("§7----------------------------------------\n\n");

        for (Player online : Bukkit.getOnlinePlayers()) {
            int ping = online.getPing();
            String pingGlyph = PingGlyphs.formatPing(ping, true);

            String prefix = "";
            try {
                Team team = online.getScoreboard().getEntryTeam(online.getName());
                if (team != null && team.getPrefix() != null) {
                    prefix = team.getPrefix();
                }
            } catch (Throwable ignored) {}

            sb.append(" §8• §r").append(prefix).append(online.getName())
              .append(" §7[").append(pingGlyph).append("§7]\n");
        }

        sb.append("\n§7----------------------------------------\n\n");

        String footer = plugin.getLatestFooter();
        if (footer != null && !footer.isBlank()) {
            sb.append(footer).append("\n");
        }

        // Attempt to send as Bedrock Form
        boolean formSent = false;
        try {
            Class<?> simpleFormBuilderClass = Class.forName("org.geysermc.cumulus.form.SimpleForm");
            Object builder = simpleFormBuilderClass.getMethod("builder").invoke(null);
            builder.getClass().getMethod("title", String.class).invoke(builder, "§e§lCOLORFUL§b§lSMP §8| §fTab List");
            builder.getClass().getMethod("content", String.class).invoke(builder, sb.toString());
            builder.getClass().getMethod("button", String.class).invoke(builder, "§cCerrar");
            Object form = builder.getClass().getMethod("build").invoke(builder);

            // 1. Try Floodgate
            try {
                Class<?> floodgateApiClass = Class.forName("org.geysermc.floodgate.api.FloodgateApi");
                Object fgInstance = floodgateApiClass.getMethod("getInstance").invoke(null);
                if (fgInstance != null) {
                    Object res = floodgateApiClass.getMethod("sendForm", UUID.class, Class.forName("org.geysermc.cumulus.form.Form"))
                            .invoke(fgInstance, uuid, form);
                    if (res instanceof Boolean b && b) {
                        formSent = true;
                    }
                }
            } catch (Throwable ignored) {}

            // 2. Try Geyser if not sent
            if (!formSent) {
                try {
                    Class<?> geyserApiClass = Class.forName("org.geysermc.geyser.api.GeyserApi");
                    Object gApi = geyserApiClass.getMethod("api").invoke(null);
                    if (gApi != null) {
                        Object conn = geyserApiClass.getMethod("connectionByUuid", UUID.class).invoke(gApi, uuid);
                        if (conn != null) {
                            conn.getClass().getMethod("sendForm", Class.forName("org.geysermc.cumulus.form.Form")).invoke(conn, form);
                            formSent = true;
                        }
                    }
                } catch (Throwable ignored) {}
            }
        } catch (Throwable ignored) {}

        // Fallback to chat if form could not be rendered
        if (!formSent) {
            player.sendMessage(sb.toString());
        }

        return true;
    }
}
