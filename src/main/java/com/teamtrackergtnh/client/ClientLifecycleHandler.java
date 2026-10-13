package com.teamtrackergtnh.client;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent;

public final class ClientLifecycleHandler {

    public static final ClientLifecycleHandler INSTANCE = new ClientLifecycleHandler();

    private ClientLifecycleHandler() {}

    @SubscribeEvent
    public void onConnect(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        clearClientState();
    }

    @SubscribeEvent
    public void onDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        clearClientState();
    }

    private static void clearClientState() {
        ClientTeamData.beginSession();
        InWorldTeamRenderer.INSTANCE.clearCache();
    }
}
