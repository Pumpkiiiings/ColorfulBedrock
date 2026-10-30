package com.pumpkings.colorfulbedrock.config;

import org.bukkit.configuration.file.FileConfiguration;

public class PluginConfig {

    private boolean scoreboardEnabled;
    private boolean scoreboardConvertHex;
    private boolean scoreboardCleanNumbers;

    private boolean tablistEnabled;
    private boolean tablistFormatPing;
    private boolean tablistUseCustomGlyphs;

    private boolean hologramsEnabled;
    private boolean hologramsRemoveBlackBox;
    private boolean hologramsForceShadow;
    private boolean hologramsConvertHex;

    public void load(FileConfiguration config) {
        this.scoreboardEnabled = config.getBoolean("scoreboard.enabled", true);
        this.scoreboardConvertHex = config.getBoolean("scoreboard.convert-hex", true);
        this.scoreboardCleanNumbers = config.getBoolean("scoreboard.clean-numbers", true);

        this.tablistEnabled = config.getBoolean("tablist.enabled", true);
        this.tablistFormatPing = config.getBoolean("tablist.format-ping", true);
        this.tablistUseCustomGlyphs = config.getBoolean("tablist.use-custom-glyphs", false);

        this.hologramsEnabled = config.getBoolean("holograms.enabled", true);
        this.hologramsRemoveBlackBox = config.getBoolean("holograms.remove-black-box", true);
        this.hologramsForceShadow = config.getBoolean("holograms.force-shadow", true);
        this.hologramsConvertHex = config.getBoolean("holograms.convert-hex", true);
    }

    public boolean isScoreboardEnabled() { return scoreboardEnabled; }
    public boolean isScoreboardConvertHex() { return scoreboardConvertHex; }
    public boolean isScoreboardCleanNumbers() { return scoreboardCleanNumbers; }

    public boolean isTablistEnabled() { return tablistEnabled; }
    public boolean isTablistFormatPing() { return tablistFormatPing; }
    public boolean isTablistUseCustomGlyphs() { return tablistUseCustomGlyphs; }

    public boolean isHologramsEnabled() { return hologramsEnabled; }
    public boolean isHologramsRemoveBlackBox() { return hologramsRemoveBlackBox; }
    public boolean isHologramsForceShadow() { return hologramsForceShadow; }
    public boolean isHologramsConvertHex() { return hologramsConvertHex; }
}
