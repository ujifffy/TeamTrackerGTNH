package com.teamtrackergtnh.client;

import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;

import com.gtnewhorizons.navigator.api.NavigatorApi;
import com.teamtrackergtnh.CommonProxy;
import com.teamtrackergtnh.net.TeamSyncPacket;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;

public class ClientProxy extends CommonProxy {

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        NavigatorApi.registerLayerManager(TeamLayerManager.INSTANCE);
        ClientKeyHandler.register();
        MinecraftForge.EVENT_BUS.register(InWorldTeamRenderer.INSTANCE);
        FMLCommonHandler.instance()
            .bus()
            .register(ClientLifecycleHandler.INSTANCE);
    }

    @Override
    public void handleTeamSync(final TeamSyncPacket packet) {
        final long sessionGeneration = ClientTeamData.getSessionGeneration();
        Minecraft.getMinecraft()
            .func_152344_a(new Runnable() {

                @Override
                public void run() {
                    Minecraft mc = Minecraft.getMinecraft();
                    if (ClientTeamData.isCurrentSession(sessionGeneration) && mc.theWorld != null
                        && mc.thePlayer != null) ClientTeamData.apply(packet);
                }
            });
    }
}
