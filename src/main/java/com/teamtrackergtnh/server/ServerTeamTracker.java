package com.teamtrackergtnh.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

import com.teamtrackergtnh.Config;
import com.teamtrackergtnh.TeamTrackerGTNH;
import com.teamtrackergtnh.net.TeamSyncPacket;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import serverutils.lib.data.ServerUtilitiesAPI;

public final class ServerTeamTracker {

    public static final ServerTeamTracker INSTANCE = new ServerTeamTracker();
    private int ticks;
    private long snapshotSequence;

    private ServerTeamTracker() {}

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (++ticks < Config.syncIntervalTicks) return;
        ticks = 0;

        MinecraftServer server = MinecraftServer.getServer();
        if (server == null || server.getConfigurationManager() == null) return;

        @SuppressWarnings("unchecked")
        List<EntityPlayerMP> online = server.getConfigurationManager().playerEntityList;

        // Snapshot party membership once per sync. Besides avoiding repeated API lookups,
        // this guarantees that every receiver is filtered against one consistent view of
        // the current tick's ServerUtilities membership.
        Map<UUID, Short> teamIds = new HashMap<UUID, Short>();
        Map<Short, List<EntityPlayerMP>> playersByTeam = new HashMap<Short, List<EntityPlayerMP>>();
        for (EntityPlayerMP player : online) {
            UUID playerId = player.getGameProfile()
                .getId();
            short teamId = ServerUtilitiesAPI.getTeamID(playerId);
            teamIds.put(playerId, teamId);
            if (teamId == 0) continue;

            List<EntityPlayerMP> members = playersByTeam.get(teamId);
            if (members == null) {
                members = new ArrayList<EntityPlayerMP>();
                playersByTeam.put(teamId, members);
            }
            members.add(player);
        }

        long sequence = nextSnapshotSequence();

        for (EntityPlayerMP receiver : online) {
            UUID receiverId = receiver.getGameProfile()
                .getId();
            Short teamId = teamIds.get(receiverId);
            List<TeamSyncPacket.Entry> entries = new ArrayList<TeamSyncPacket.Entry>();

            if (teamId != null && teamId.shortValue() != 0) {
                List<EntityPlayerMP> members = playersByTeam.get(teamId);
                if (members != null) for (EntityPlayerMP candidate : members) {
                    if (candidate == receiver) continue;
                    UUID candidateId = candidate.getGameProfile()
                        .getId();
                    // Team ID grouping is only an optimization. The public relation API is
                    // the final authorization check before coordinates enter the packet.
                    if (!ServerUtilitiesAPI.arePlayersInSameTeam(receiverId, candidateId)) continue;
                    if (!isFinite(candidate.posX) || !isFinite(candidate.posY) || !isFinite(candidate.posZ)) continue;

                    entries.add(
                        new TeamSyncPacket.Entry(
                            candidateId,
                            candidate.getCommandSenderName(),
                            candidate.dimension,
                            candidate.posX,
                            candidate.posY,
                            candidate.posZ));
                }
            }

            TeamTrackerGTNH.NETWORK.sendTo(new TeamSyncPacket(entries, sequence), receiver);
        }
    }

    private long nextSnapshotSequence() {
        snapshotSequence++;
        // A server restart disconnects clients and starts a new session. This guard
        // only makes the counter well-defined even after the practically impossible
        // signed-long wraparound in one JVM lifetime.
        if (snapshotSequence <= 0L) snapshotSequence = 1L;
        return snapshotSequence;
    }

    private static boolean isFinite(double value) {
        return !Double.isNaN(value) && !Double.isInfinite(value);
    }
}
