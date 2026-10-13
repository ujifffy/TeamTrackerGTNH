package com.teamtrackergtnh.client;

import net.minecraft.client.renderer.Tessellator;

import org.lwjgl.opengl.GL11;

import com.gtnewhorizons.navigator.api.model.steps.UniversalRenderStep;
import com.gtnewhorizons.navigator.api.util.DrawUtils;
import com.teamtrackergtnh.Config;

import serverutils.lib.icon.PlayerHeadIcon;

/**
 * Xaero-like tracked player marker.
 *
 * While a real client EntityPlayer exists and is inside the minimap frame, Xaero's
 * normal entity radar is allowed to render it. This layer takes over when the
 * teammate is remote or outside the minimap frame, preventing duplicate heads and
 * reproducing the modern live-entity -> remote-tracker handoff.
 */
public final class TeamPlayerRenderStep extends UniversalRenderStep<TrackedPlayer> {

    private final PlayerHeadIcon head;

    public TeamPlayerRenderStep(TrackedPlayer location) {
        super(location);
        head = new PlayerHeadIcon(location.uuid);
        // Keep the head approximately screen-sized while zooming Xaero maps.
        setMinScale(0.0001D);
    }

    @Override
    public void draw(double x, double y, float drawScale, double zoom) {
        if (!Config.showMapHeads) return;

        if (isMinimap()) drawMinimap(x, y);
        else drawWorldMap(x, y);
    }

    private void drawMinimap(double x, double y) {
        double size = 10D * Config.onMapIconScale * Config.minimapIconScale;
        double radius = Math.max(4D, TeamLayerManager.INSTANCE.getMinimapFrameRadius() - size / 2D - 2D);
        double distanceFromCenter = Math.sqrt(x * x + y * y);
        boolean outOfBounds = distanceFromCenter > radius;
        boolean live = location.hasLiveEntity();

        // Inside the frame, the ordinary Xaero player radar already has the true entity
        // and gives us exactly the GCP Modern look. Once the player leaves the frame,
        // keep a tracked-player head pinned to the edge instead.
        if (live && !outOfBounds && Config.useXaeroRadarForLoadedPlayers) return;

        if (outOfBounds && Config.clampOffscreenMapHeads && distanceFromCenter > 0.0001D) {
            double clamp = radius / distanceFromCenter;
            x *= clamp;
            y *= clamp;
        }

        drawMarker(x, y, size, false);
    }

    private void drawWorldMap(double x, double y) {
        // Xaero's ordinary entity radar owns loaded players on the minimap, but it
        // cannot be relied on to populate the fullscreen world map. Always render our
        // team layer here so a nearby/live teammate cannot disappear from that map.
        double size = 12D * Config.onMapIconScale * Config.worldMapIconScale;
        drawMarker(x, y, size, Config.showWorldMapNames);
    }

    private void drawMarker(double x, double y, double size, boolean withName) {
        double left = x - size / 2D;
        double top = y - size / 2D;

        DrawUtils.drawRect(left - 1D, top - 1D, size + 2D, size + 2D, 0x000000, 150);
        drawHead(left, top, size, 255);

        if (withName) {
            DrawUtils.drawLabel(location.name, x, top + size + 3D, 0xFFFFFF, 0x000000, true, Config.worldMapNameScale);
        }
    }

    private void drawHead(double x, double y, double size, int alpha) {
        head.bindTexture();
        GL11.glColor4f(1F, 1F, 1F, 1F);

        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        DrawUtils.addRectToBufferWithUV(tessellator, x, y, size, size, 0xFFFFFF, alpha, 0.125D, 0.25D, 0.25D, 0.50D);
        tessellator.draw();

        tessellator.startDrawingQuads();
        DrawUtils.addRectToBufferWithUV(tessellator, x, y, size, size, 0xFFFFFF, alpha, 0.625D, 0.25D, 0.75D, 0.50D);
        tessellator.draw();
    }
}
