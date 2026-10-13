package com.teamtrackergtnh.client;

import java.util.Collection;

import javax.annotation.Nullable;

import com.gtnewhorizons.navigator.api.model.SupportedMods;
import com.gtnewhorizons.navigator.api.model.layers.LayerManager;
import com.gtnewhorizons.navigator.api.model.layers.LayerRenderer;
import com.gtnewhorizons.navigator.api.model.layers.UniversalLayerRenderer;
import com.gtnewhorizons.navigator.api.model.locations.ILocationProvider;

/** Navigator layer used for Xaero Minimap and Xaero World Map. */
public final class TeamLayerManager extends LayerManager {

    public static final TeamLayerManager INSTANCE = new TeamLayerManager();

    /**
     * Navigator's Xaero minimap integration passes minimapFrameSize * 2 as the
     * recache width. In UniversalRenderStep local coordinates this half-width is
     * also the useful clipping radius in screen pixels.
     */
    private volatile double minimapFrameRadius = 80D;

    private TeamLayerManager() {
        super(AlwaysOnTeamButton.INSTANCE);
    }

    @Override
    protected @Nullable LayerRenderer addLayerRenderer(LayerManager manager, SupportedMods mod) {
        return new UniversalLayerRenderer(manager)
            .withRenderStep(location -> new TeamPlayerRenderStep((TrackedPlayer) location))
            .withRenderPriority(1000);
    }

    @Override
    public void recacheMiniMap(int centerBlockX, int centerBlockZ, int blockWidth) {
        minimapFrameRadius = Math.max(8D, blockWidth / 2D);
        super.recacheMiniMap(centerBlockX, centerBlockZ, blockWidth);
    }

    @Override
    public void recacheMiniMap(int centerBlockX, int centerBlockZ, int blockWidth, int blockHeight) {
        minimapFrameRadius = Math.max(8D, Math.min(blockWidth, blockHeight) / 2D);
        super.recacheMiniMap(centerBlockX, centerBlockZ, blockWidth, blockHeight);
    }

    public double getMinimapFrameRadius() {
        return minimapFrameRadius;
    }

    @Override
    protected Collection<? extends ILocationProvider> generateVisibleLocations(int minBlockX, int minBlockZ,
        int maxBlockX, int maxBlockZ, int dimension) {
        // Deliberately return every teammate in this dimension, including players outside
        // the viewport. TeamPlayerRenderStep clamps off-map players to the minimap edge,
        // matching modern Xaero Tracked Players behaviour.
        return ClientTeamData.snapshotForDimension(dimension);
    }

    @Override
    public void updateElement(ILocationProvider location) {
        // TrackedPlayer instances are mutable and updated in-place by ClientTeamData.
    }
}
