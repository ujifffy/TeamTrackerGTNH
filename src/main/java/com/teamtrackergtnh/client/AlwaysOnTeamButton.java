package com.teamtrackergtnh.client;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import com.gtnewhorizons.navigator.api.model.SupportedMods;
import com.gtnewhorizons.navigator.api.model.buttons.ButtonManager;
import com.teamtrackergtnh.Config;

public final class AlwaysOnTeamButton extends ButtonManager {

    public static final AlwaysOnTeamButton INSTANCE = new AlwaysOnTeamButton();

    private AlwaysOnTeamButton() {
        isActive = Config.showMapHeads;
        setOnToggle(Config::setShowMapHeads);
    }

    @Override
    public ResourceLocation getIcon(SupportedMods mod, String theme) {
        return new ResourceLocation("teamtrackergtnh", "textures/gui/team.png");
    }

    @Override
    public String getButtonText() {
        return StatCollector.translateToLocal("gui.teamtrackergtnh.navigator_layer");
    }
}
