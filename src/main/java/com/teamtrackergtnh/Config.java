package com.teamtrackergtnh;

import java.io.File;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

public final class Config {

    /** Increment only when a migration step is added below. */
    public static final int CURRENT_SCHEMA_VERSION = 3;

    private static Configuration configuration;

    public static int syncIntervalTicks = 10;

    public static boolean showMapHeads = true;
    public static boolean clampOffscreenMapHeads = true;
    public static boolean useXaeroRadarForLoadedPlayers = false;
    public static boolean interpolateRemotePlayers = true;
    public static float onMapIconScale = 1.0F;
    public static float minimapIconScale = 2.0F;
    public static float worldMapIconScale = 2.5F;
    public static boolean showWorldMapNames = true;
    public static float worldMapNameScale = 0.75F;

    public static boolean showInWorldHeads = true;
    public static boolean showInWorldName = false;
    public static boolean showInWorldDistance = false;
    public static float inWorldOpacity = 0.45F;
    /** Retained for config/ABI compatibility; schema 3 uses a fully hidden near zone instead. */
    public static float inWorldNearOpacityMultiplier = 0.35F;
    public static float inWorldHiddenDistance = 16F;
    public static float inWorldFullOpacityDistance = 32F;
    public static float inWorldMinDistance = 0F;
    public static float inWorldMarkerDistance = 8F;
    public static float inWorldIconScale = 1.0F;
    public static float inWorldNameScale = 1.0F;

    private Config() {}

    public static void load(File file) {
        Configuration cfg = new Configuration(file);
        configuration = cfg;
        cfg.load();

        migrate(cfg);

        syncIntervalTicks = cfg.getInt(
            "syncIntervalTicks",
            "server",
            10,
            2,
            100,
            "How often the server sends online teammate positions. 20 ticks = 1 second.");

        showMapHeads = cfg.getBoolean(
            "showMapHeads",
            "client.map",
            true,
            "Xaero-style tracked teammate heads on Minimap / World Map.");
        clampOffscreenMapHeads = cfg.getBoolean(
            "clampOffscreenMapHeads",
            "client.map",
            true,
            "Pin tracked teammate heads to the minimap edge when they are outside its frame, like modern Xaero.");
        useXaeroRadarForLoadedPlayers = cfg.getBoolean(
            "useXaeroRadarForLoadedPlayers",
            "client.map",
            false,
            "Compatibility switch: when true, a loaded EntityPlayer inside the minimap is handed to Xaero's normal entity radar. Keep false for stable teammate faces across the live/remote transition. The fullscreen world map always uses the team layer.");
        interpolateRemotePlayers = cfg.getBoolean(
            "interpolateRemotePlayers",
            "client.map",
            true,
            "Smooth server snapshots for teammates outside vanilla tracking range.");
        onMapIconScale = cfg.getFloat(
            "onMapIconScale",
            "client.map",
            1.0F,
            0.25F,
            4.0F,
            "Equivalent of modern Xaero's 'Tracked Player On-Map Scale'.");
        minimapIconScale = cfg.getFloat(
            "minimapIconScale",
            "client.map",
            2.0F,
            0.5F,
            4.0F,
            "Extra minimap-only size multiplier. 2.0 makes the legacy 10 px marker 20 px.");
        worldMapIconScale = cfg.getFloat(
            "worldMapIconScale",
            "client.map",
            2.5F,
            0.5F,
            5.0F,
            "Extra fullscreen-world-map size multiplier. 2.5 makes the legacy 12 px marker 30 px.");
        showWorldMapNames = cfg.getBoolean(
            "showWorldMapNames",
            "client.map",
            true,
            "Show names below remote teammate heads on the fullscreen map.");
        worldMapNameScale = cfg.getFloat(
            "worldMapNameScale",
            "client.map",
            0.75F,
            0.25F,
            3.0F,
            "Scale of teammate names on the fullscreen map.");

        showInWorldHeads = cfg.getBoolean(
            "showInWorldHeads",
            "client.world",
            true,
            "Equivalent of modern Xaero's 'In-World Tracked Players'.");
        showInWorldName = cfg
            .getBoolean("showInWorldName", "client.world", false, "Show teammate name below the in-world head.");
        showInWorldDistance = cfg.getBoolean(
            "showInWorldDistance",
            "client.world",
            false,
            "Show distance below the in-world head. This is a small extension over the target Xaero behaviour.");
        inWorldOpacity = cfg
            .getFloat("inWorldOpacity", "client.world", 0.45F, 0.05F, 1.0F, "Opacity of the in-world teammate marker.");
        inWorldNearOpacityMultiplier = cfg.getFloat(
            "inWorldNearOpacityMultiplier",
            "client.world",
            0.35F,
            0F,
            1.0F,
            "Legacy 0.3.1 setting retained for downgrade compatibility. Ignored by 0.3.2 and newer.");
        inWorldHiddenDistance = cfg.getFloat(
            "inWorldHiddenDistance",
            "client.world",
            16F,
            0F,
            127F,
            "POV marker is completely hidden at or below this distance, so it cannot cover a nearby teammate.");
        inWorldFullOpacityDistance = cfg.getFloat(
            "inWorldFullOpacityDistance",
            "client.world",
            32F,
            1F,
            128F,
            "Distance in blocks at which the POV marker finishes fading in and reaches inWorldOpacity.");
        inWorldMinDistance = cfg.getFloat(
            "inWorldMinDistance",
            "client.world",
            0F,
            0F,
            512F,
            "Hide the in-world marker when a teammate is closer than this many blocks. Modern Xaero effectively allows 0.");
        inWorldMarkerDistance = cfg.getFloat(
            "inWorldMarkerDistance",
            "client.world",
            8F,
            2F,
            32F,
            "Distance from the camera at which the directional in-world marker is rendered.");
        inWorldIconScale = cfg.getFloat(
            "inWorldIconScale",
            "client.world",
            1.0F,
            0.25F,
            4.0F,
            "Equivalent of modern Xaero's 'Tracked Player In-World Scale'.");
        inWorldNameScale = cfg.getFloat(
            "inWorldNameScale",
            "client.world",
            1.0F,
            0.25F,
            3.0F,
            "Equivalent of modern Xaero's 'Tracked Player Name Scale'.");

        if (cfg.hasChanged()) cfg.save();
    }

    private static void migrate(Configuration cfg) {
        Property schema = cfg.get(
            "internal",
            "schemaVersion",
            0,
            "TeamTrackerGTNH config schema. Managed automatically; unknown keys are preserved during upgrades.");
        int savedVersion = schema.getInt(0);

        if (savedVersion > CURRENT_SCHEMA_VERSION) {
            TeamTrackerGTNH.LOG.warn(
                "Config schema {} is newer than supported schema {}. Known settings will be read without downgrading the file.",
                savedVersion,
                CURRENT_SCHEMA_VERSION);
            return;
        }

        // Schema 0 is every 0.1.x/0.2.x config. Version 1 deliberately keeps all
        // existing category/key names and values, so the migration is metadata-only.
        // Future migrations must be append-only or explicitly copy old values before
        // introducing a replacement key. Configuration#save preserves unknown keys.
        if (savedVersion < 1) savedVersion = 1;

        // Schema 2 adds independent map-size multipliers and the near-player POV
        // opacity curve. Existing settings are deliberately retained; Forge inserts
        // the new keys with their defaults when they are first read above.
        if (savedVersion < 2) savedVersion = 2;

        if (savedVersion < 3) {
            // The old default delegated loaded players to Xaero Entity Radar. That
            // changes the icon renderer exactly at vanilla tracking distance and is
            // not stable across the legacy Xaero builds used by GTNH. Disable the
            // handoff on upgrade so the same skin-head path owns both states.
            Property radarHandoff = cfg.get("client.map", "useXaeroRadarForLoadedPlayers", true);
            if (radarHandoff.getBoolean(true)) radarHandoff.set(false);

            // Only replace previous defaults. Explicit custom scale/fade distances
            // remain user-owned. Name labels deliberately migrate to the new safer
            // default once; users can turn them back on afterwards.
            Property minimapScale = cfg.get("client.map", "minimapIconScale", 1.5D);
            if (approximately(minimapScale.getDouble(1.5D), 1.5D)) minimapScale.set(2.0D);

            Property fullOpacityDistance = cfg.get("client.world", "inWorldFullOpacityDistance", 20D);
            if (approximately(fullOpacityDistance.getDouble(20D), 20D)) fullOpacityDistance.set(32D);

            cfg.get("client.world", "showInWorldName", true)
                .set(false);
            savedVersion = 3;
        }

        schema.set(savedVersion);
    }

    private static boolean approximately(double value, double expected) {
        return Math.abs(value - expected) < 0.000001D;
    }

    public static void setShowMapHeads(boolean enabled) {
        showMapHeads = enabled;
        saveBoolean("client.map", "showMapHeads", enabled);
    }

    public static void setShowInWorldHeads(boolean enabled) {
        showInWorldHeads = enabled;
        saveBoolean("client.world", "showInWorldHeads", enabled);
    }

    private static void saveBoolean(String category, String key, boolean value) {
        Configuration cfg = configuration;
        if (cfg == null) return;
        cfg.get(category, key, value)
            .set(value);
        if (cfg.hasChanged()) cfg.save();
    }
}
