package com.teamtrackergtnh.client;

import net.minecraft.client.settings.KeyBinding;

import org.lwjgl.input.Keyboard;

import com.teamtrackergtnh.Config;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;

/** Xaero-like independent toggles for on-map and in-world tracked players. */
public final class ClientKeyHandler {

    public static final ClientKeyHandler INSTANCE = new ClientKeyHandler();

    public static final KeyBinding TOGGLE_MAP = new KeyBinding(
        "key.teamtrackergtnh.toggle_map",
        Keyboard.KEY_NONE,
        "key.categories.teamtrackergtnh");

    public static final KeyBinding TOGGLE_WORLD = new KeyBinding(
        "key.teamtrackergtnh.toggle_world",
        Keyboard.KEY_NONE,
        "key.categories.teamtrackergtnh");

    private ClientKeyHandler() {}

    public static void register() {
        ClientRegistry.registerKeyBinding(TOGGLE_MAP);
        ClientRegistry.registerKeyBinding(TOGGLE_WORLD);
        FMLCommonHandler.instance()
            .bus()
            .register(INSTANCE);
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (TOGGLE_MAP.isPressed()) {
            AlwaysOnTeamButton.INSTANCE.toggle();
            TeamLayerManager.INSTANCE.forceRefresh();
        }
        if (TOGGLE_WORLD.isPressed()) {
            Config.setShowInWorldHeads(!Config.showInWorldHeads);
        }
    }
}
