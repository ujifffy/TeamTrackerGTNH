package com.teamtrackergtnh.net;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.teamtrackergtnh.TeamTrackerGTNH;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class TeamSyncPacket implements IMessage {

    /**
     * The discriminator and the legacy entry layout are part of the public wire
     * contract. Do not change them. New protocol data belongs in the optional,
     * length-prefixed extension written after all legacy entries.
     */
    public static final int MAX_PLAYERS = 512;
    public static final long NO_SNAPSHOT_SEQUENCE = -1L;

    private static final int EXTENSION_MAGIC = 0x54544733; // "TTG3"
    private static final int EXTENSION_VERSION = 1;
    private static final int EXTENSION_V1_LENGTH = 8;

    public static final class Entry {

        public final UUID uuid;
        public final String name;
        public final int dimension;
        public final double x;
        public final double y;
        public final double z;

        public Entry(UUID uuid, String name, int dimension, double x, double y, double z) {
            this.uuid = uuid;
            this.name = name;
            this.dimension = dimension;
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    public final List<Entry> players = new ArrayList<Entry>();
    private long snapshotSequence = NO_SNAPSHOT_SEQUENCE;
    private boolean valid = true;

    public TeamSyncPacket() {}

    public TeamSyncPacket(List<Entry> players) {
        this(players, NO_SNAPSHOT_SEQUENCE);
    }

    public TeamSyncPacket(List<Entry> players, long snapshotSequence) {
        this.players.addAll(players);
        this.snapshotSequence = snapshotSequence;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        players.clear();
        snapshotSequence = NO_SNAPSHOT_SEQUENCE;
        valid = true;

        try {
            int count = buf.readUnsignedShort();
            if (count > MAX_PLAYERS) {
                valid = false;
                return;
            }

            for (int i = 0; i < count; i++) {
                UUID uuid = new UUID(buf.readLong(), buf.readLong());
                String name = ByteBufUtils.readUTF8String(buf);
                int dimension = buf.readInt();
                double x = buf.readDouble();
                double y = buf.readDouble();
                double z = buf.readDouble();
                if (name == null || name.length() > 64) valid = false;
                players.add(new Entry(uuid, name, dimension, x, y, z));
            }

            readOptionalExtension(buf);
        } catch (RuntimeException malformedPacket) {
            players.clear();
            snapshotSequence = NO_SNAPSHOT_SEQUENCE;
            valid = false;
        }
    }

    private void readOptionalExtension(ByteBuf buf) {
        // 0.2.2/0.2.3 packets end immediately after the legacy entries.
        if (buf.readableBytes() < 7) return;

        int magic = buf.readInt();
        if (magic != EXTENSION_MAGIC) return;

        int version = buf.readUnsignedByte();
        int length = buf.readUnsignedShort();
        if (length > buf.readableBytes()) {
            valid = false;
            return;
        }

        if (version == EXTENSION_VERSION) {
            if (length < EXTENSION_V1_LENGTH) {
                valid = false;
                buf.skipBytes(length);
                return;
            }
            snapshotSequence = buf.readLong();
            buf.skipBytes(length - EXTENSION_V1_LENGTH);
        } else {
            // A future extension version is safely ignored. Its length still lets
            // this client stay aligned without knowing the new fields.
            buf.skipBytes(length);
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        int count = Math.min(players.size(), MAX_PLAYERS);
        buf.writeShort(count);
        for (int i = 0; i < count; i++) {
            Entry player = players.get(i);
            buf.writeLong(player.uuid.getMostSignificantBits());
            buf.writeLong(player.uuid.getLeastSignificantBits());
            ByteBufUtils.writeUTF8String(buf, player.name);
            buf.writeInt(player.dimension);
            buf.writeDouble(player.x);
            buf.writeDouble(player.y);
            buf.writeDouble(player.z);
        }

        if (snapshotSequence != NO_SNAPSHOT_SEQUENCE) {
            buf.writeInt(EXTENSION_MAGIC);
            buf.writeByte(EXTENSION_VERSION);
            buf.writeShort(EXTENSION_V1_LENGTH);
            buf.writeLong(snapshotSequence);
        }
    }

    public long getSnapshotSequence() {
        return snapshotSequence;
    }

    public boolean isValid() {
        return valid;
    }

    public static final class Handler implements IMessageHandler<TeamSyncPacket, IMessage> {

        @Override
        public IMessage onMessage(TeamSyncPacket message, MessageContext ctx) {
            if (message.isValid()) TeamTrackerGTNH.proxy.handleTeamSync(message);
            return null;
        }
    }
}
