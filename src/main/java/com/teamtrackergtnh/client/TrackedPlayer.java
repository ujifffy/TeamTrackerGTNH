package com.teamtrackergtnh.client;

import java.util.UUID;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;

import com.gtnewhorizons.navigator.api.model.locations.ILocationProvider;
import com.teamtrackergtnh.Config;

/**
 * One online ServerUtilities teammate.
 *
 * The server snapshot is always retained, but when vanilla already has the real
 * EntityPlayer we deliberately prefer its client-side coordinates. This mirrors
 * modern Xaero Tracked Players: live entity position while available, server
 * tracking only after the entity leaves client tracking range.
 */
public final class TrackedPlayer implements ILocationProvider {

    public final UUID uuid;
    public String name;
    public int dimension;

    private double fromX;
    private double fromY;
    private double fromZ;
    private double targetX;
    private double targetY;
    private double targetZ;
    private long interpolationStartMs;
    private long lastSnapshotMs;
    private long interpolationDurationMs = 500L;
    private boolean initialized;

    // A same-dimension jump this large between snapshots is overwhelmingly likely
    // to be /home, a portal/teleport or respawn, not ordinary movement. Smearing it
    // across half a second looks much worse than snapping, especially on the world map.
    private static final double TELEPORT_SNAP_DISTANCE = 128D;

    public TrackedPlayer(UUID uuid, String name, int dimension, double x, double y, double z) {
        this.uuid = uuid;
        this.name = name;
        this.dimension = dimension;
        setImmediate(x, y, z);
    }

    public void updateServer(String newName, int newDimension, double x, double y, double z) {
        name = newName;

        if (!initialized || dimension != newDimension || !Config.interpolateRemotePlayers) {
            dimension = newDimension;
            setImmediate(x, y, z);
            return;
        }

        long now = System.currentTimeMillis();
        double currentX = getRemoteX(now);
        double currentY = getRemoteY(now);
        double currentZ = getRemoteZ(now);

        double dx = x - currentX;
        double dy = y - currentY;
        double dz = z - currentZ;
        if (dx * dx + dy * dy + dz * dz >= TELEPORT_SNAP_DISTANCE * TELEPORT_SNAP_DISTANCE) {
            setImmediate(x, y, z);
            lastSnapshotMs = now;
            return;
        }

        fromX = currentX;
        fromY = currentY;
        fromZ = currentZ;
        targetX = x;
        targetY = y;
        targetZ = z;
        interpolationStartMs = now;

        // Use the interval we actually observe instead of assuming that the client and
        // server configs have identical syncIntervalTicks. This also behaves sensibly
        // during a short server hitch.
        if (lastSnapshotMs > 0L) {
            interpolationDurationMs = Math.max(50L, Math.min(2000L, now - lastSnapshotMs));
        } else {
            interpolationDurationMs = Math.max(50L, Config.syncIntervalTicks * 50L);
        }
        lastSnapshotMs = now;
    }

    private void setImmediate(double x, double y, double z) {
        fromX = targetX = x;
        fromY = targetY = y;
        fromZ = targetZ = z;
        long now = System.currentTimeMillis();
        interpolationStartMs = now;
        lastSnapshotMs = now;
        interpolationDurationMs = Math.max(50L, Config.syncIntervalTicks * 50L);
        initialized = true;
    }

    /** Returns the actual vanilla client entity if it is currently tracked. */
    public EntityPlayer getLiveEntity() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null || mc.thePlayer == null || mc.thePlayer.dimension != dimension) return null;
        // MCP stable 12 leaves World#getPlayerEntityByUUID under its SRG name in
        // 1.7.10. Calling the real 1.7.10 method avoids a newer-version-only API.
        EntityPlayer player = mc.theWorld.func_152378_a(uuid);
        if (player == null || player == mc.thePlayer || player.isDead) return null;
        return player;
    }

    public boolean hasLiveEntity() {
        return getLiveEntity() != null;
    }

    public double getDisplayX() {
        EntityPlayer live = getLiveEntity();
        return live != null ? live.posX : getRemoteX(System.currentTimeMillis());
    }

    public double getDisplayY() {
        EntityPlayer live = getLiveEntity();
        return live != null ? live.posY : getRemoteY(System.currentTimeMillis());
    }

    public double getDisplayZ() {
        EntityPlayer live = getLiveEntity();
        return live != null ? live.posZ : getRemoteZ(System.currentTimeMillis());
    }

    public double getWorldX(float partialTicks) {
        EntityPlayer live = getLiveEntity();
        if (live != null) return live.lastTickPosX + (live.posX - live.lastTickPosX) * partialTicks;
        return getRemoteX(System.currentTimeMillis());
    }

    public double getWorldY(float partialTicks) {
        EntityPlayer live = getLiveEntity();
        if (live != null) return live.lastTickPosY + (live.posY - live.lastTickPosY) * partialTicks;
        return getRemoteY(System.currentTimeMillis());
    }

    public double getWorldZ(float partialTicks) {
        EntityPlayer live = getLiveEntity();
        if (live != null) return live.lastTickPosZ + (live.posZ - live.lastTickPosZ) * partialTicks;
        return getRemoteZ(System.currentTimeMillis());
    }

    private double getRemoteX(long now) {
        return lerp(fromX, targetX, interpolationProgress(now));
    }

    private double getRemoteY(long now) {
        return lerp(fromY, targetY, interpolationProgress(now));
    }

    private double getRemoteZ(long now) {
        return lerp(fromZ, targetZ, interpolationProgress(now));
    }

    private double interpolationProgress(long now) {
        double t = (now - interpolationStartMs) / (double) Math.max(50L, interpolationDurationMs);
        return Math.max(0D, Math.min(1D, t));
    }

    private static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }

    @Override
    public int getDimensionId() {
        return dimension;
    }

    @Override
    public double getBlockX() {
        return getDisplayX();
    }

    @Override
    public double getBlockZ() {
        return getDisplayZ();
    }

    @Override
    public long toLong() {
        return uuid.getMostSignificantBits() ^ Long.rotateLeft(uuid.getLeastSignificantBits(), 1);
    }
}
