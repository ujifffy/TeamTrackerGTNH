package com.teamtrackergtnh.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.teamtrackergtnh.net.TeamSyncPacket;

public final class ClientTeamData {

    private static final Map<UUID, TrackedPlayer> PLAYERS = new LinkedHashMap<UUID, TrackedPlayer>();
    private static long sessionGeneration;
    private static long lastPacketMs;
    private static long lastSnapshotSequence = TeamSyncPacket.NO_SNAPSHOT_SEQUENCE;
    private static final long STALE_TIMEOUT_MS = 10000L;

    private ClientTeamData() {}

    /**
     * Mutates existing TrackedPlayer instances instead of replacing them. Navigator
     * caches locations by UUID, and keeping the same logical object also lets us
     * interpolate server snapshots smoothly.
     */
    public static synchronized void apply(TeamSyncPacket packet) {
        if (!packet.isValid()) return;

        long sequence = packet.getSnapshotSequence();
        if (sequence == TeamSyncPacket.NO_SNAPSHOT_SEQUENCE) {
            // Legacy snapshots are valid when connected to a 0.2.2/0.2.3 server,
            // but must not overwrite sequenced data in the same connection.
            if (lastSnapshotSequence != TeamSyncPacket.NO_SNAPSHOT_SEQUENCE) return;
        } else {
            if (lastSnapshotSequence != TeamSyncPacket.NO_SNAPSHOT_SEQUENCE && sequence <= lastSnapshotSequence) return;
            lastSnapshotSequence = sequence;
        }

        Set<UUID> seen = new HashSet<UUID>();
        boolean membershipChanged = false;
        boolean dimensionChanged = false;

        for (TeamSyncPacket.Entry e : packet.players) {
            if (e.uuid == null || e.name == null || !isFinite(e.x) || !isFinite(e.y) || !isFinite(e.z)) continue;
            seen.add(e.uuid);
            TrackedPlayer player = PLAYERS.get(e.uuid);
            if (player == null) {
                PLAYERS.put(e.uuid, new TrackedPlayer(e.uuid, e.name, e.dimension, e.x, e.y, e.z));
                membershipChanged = true;
            } else {
                if (player.dimension != e.dimension) dimensionChanged = true;
                player.updateServer(e.name, e.dimension, e.x, e.y, e.z);
            }
        }

        lastPacketMs = System.currentTimeMillis();

        Iterator<Map.Entry<UUID, TrackedPlayer>> it = PLAYERS.entrySet()
            .iterator();
        while (it.hasNext()) {
            if (!seen.contains(
                it.next()
                    .getKey())) {
                it.remove();
                membershipChanged = true;
            }
        }

        if (membershipChanged || dimensionChanged) TeamLayerManager.INSTANCE.clearFullCache();
        else TeamLayerManager.INSTANCE.forceRefresh();
    }

    public static synchronized TrackedPlayer get(UUID uuid) {
        return PLAYERS.get(uuid);
    }

    public static synchronized Collection<TrackedPlayer> snapshot() {
        expireStaleData();
        return new ArrayList<TrackedPlayer>(PLAYERS.values());
    }

    public static synchronized Collection<TrackedPlayer> snapshotForDimension(int dimension) {
        expireStaleData();
        ArrayList<TrackedPlayer> result = new ArrayList<TrackedPlayer>();
        for (TrackedPlayer player : PLAYERS.values()) {
            if (player.dimension == dimension) result.add(player);
        }
        return result;
    }

    public static synchronized void clear() {
        clearPlayerData(true);
    }

    private static void clearPlayerData(boolean resetSequence) {
        PLAYERS.clear();
        lastPacketMs = 0L;
        if (resetSequence) lastSnapshotSequence = TeamSyncPacket.NO_SNAPSHOT_SEQUENCE;
        TeamLayerManager.INSTANCE.clearFullCache();
    }

    public static synchronized long beginSession() {
        sessionGeneration++;
        clear();
        return sessionGeneration;
    }

    public static synchronized long getSessionGeneration() {
        return sessionGeneration;
    }

    public static synchronized boolean isCurrentSession(long generation) {
        return generation == sessionGeneration;
    }

    private static void expireStaleData() {
        // Keep the sequence watermark. A delayed packet queued before the timeout
        // must not resurrect older coordinates after stale markers are cleared.
        if (lastPacketMs > 0L && System.currentTimeMillis() - lastPacketMs > STALE_TIMEOUT_MS) {
            clearPlayerData(false);
        }
    }

    private static boolean isFinite(double value) {
        return !Double.isNaN(value) && !Double.isInfinite(value);
    }
}
