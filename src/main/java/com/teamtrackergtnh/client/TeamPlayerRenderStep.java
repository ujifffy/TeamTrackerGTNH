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
 * The team layer owns both loaded and remote players by default. Keeping the same
 * skin-head renderer across the vanilla tracking boundary prevents the corrupted
 * or differently styled icon seen when legacy Xaero's Entity Radar takes over.
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

        // Compatibility escape hatch only. The stable default keeps our own head
        // renderer active across the live/remote transition.
        if (live && !outOfBounds && Config.useXaeroRadarForLoadedPlayers) return;

        if (outOfBounds && Config.clampOffscreenMapHeads && distanceFromCenter > 0.0001D) {
            double clamp = radius / distanceFromCenter;
            x *= clamp;
            y *= clamp;
        }

        drawMarker(x, y, size, false);
    }

    private void drawWorldMap(double x, double y) {
        // Always render our team layer here so a nearby/live teammate cannot disappear.
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
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        try {
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glDisable(GL11.GL_ALPHA_TEST);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            head.bindTexture();
            GL11.glColor4f(1F, 1F, 1F, 1F);

            Tessellator tessellator = Tessellator.instance;
            // Base face: skin pixels x=8..15, y=8..15 on the normalized 64x32
            // PlayerHeadIcon texture supplied by ServerUtilities.
            tessellator.startDrawingQuads();
            DrawUtils.addRectToBufferWithUV(
                tessellator,
                x,
                y,
                size,
                size,
                0xFFFFFF,
                alpha,
                0.125D,
                0.25D,
                0.25D,
                0.50D);
            tessellator.draw();

            // Outer head layer: hair, hats and glasses. It is intentionally a
            // second blended pass over the base face, never a replacement texture.
            tessellator.startDrawingQuads();
            DrawUtils.addRectToBufferWithUV(
                tessellator,
                x,
                y,
                size,
                size,
                0xFFFFFF,
                alpha,
                0.625D,
                0.25D,
                0.75D,
                0.50D);
            tessellator.draw();
        } finally {
            GL11.glPopAttrib();
        }
    }
}
