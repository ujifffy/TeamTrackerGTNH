package com.teamtrackergtnh;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;

@Mod(
    modid = TeamTrackerGTNH.MODID,
    name = TeamTrackerGTNH.NAME,
    version = TeamTrackerGTNH.VERSION,
    acceptedMinecraftVersions = "[1.7.10]",
    acceptableRemoteVersions = "[0.2.2,0.4.0)",
    dependencies = "required-after:serverutilities;required-after:navigator;")
public class TeamTrackerGTNH {

    public static final String MODID = "teamtrackergtnh";
    public static final String NAME = "Team Tracker GTNH";
    public static final String VERSION = "0.3.1";

    public static final Logger LOG = LogManager.getLogger(NAME);
    public static final SimpleNetworkWrapper NETWORK = NetworkRegistry.INSTANCE.newSimpleChannel(MODID);

    @SidedProxy(clientSide = "com.teamtrackergtnh.client.ClientProxy", serverSide = "com.teamtrackergtnh.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }
}
