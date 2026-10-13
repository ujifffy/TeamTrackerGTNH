package com.teamtrackergtnh;

import com.teamtrackergtnh.net.TeamSyncPacket;
import com.teamtrackergtnh.server.ServerTeamTracker;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.relauncher.Side;

public class CommonProxy {

    public void preInit(FMLPreInitializationEvent event) {
        Config.load(event.getSuggestedConfigurationFile());
        TeamTrackerGTNH.NETWORK.registerMessage(TeamSyncPacket.Handler.class, TeamSyncPacket.class, 0, Side.CLIENT);
    }

    public void init(FMLInitializationEvent event) {
        FMLCommonHandler.instance()
            .bus()
            .register(ServerTeamTracker.INSTANCE);
    }

    public void handleTeamSync(TeamSyncPacket packet) {
        // ClientProxy handles this on the Minecraft client thread.
    }
}
