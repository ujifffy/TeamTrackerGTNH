package com.teamtrackergtnh.client;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderWorldLastEvent;

import org.lwjgl.opengl.GL11;

import com.gtnewhorizons.navigator.api.util.DrawUtils;
import com.teamtrackergtnh.Config;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import serverutils.lib.icon.PlayerHeadIcon;

/** Xaero-style in-world teammate marker, visible through terrain. */
public final class InWorldTeamRenderer {

    public static final InWorldTeamRenderer INSTANCE = new InWorldTeamRenderer();
    private final Map<UUID, PlayerHeadIcon> heads = new HashMap<UUID, PlayerHeadIcon>();

    private InWorldTeamRenderer() {}

    public void clearCache() {
        heads.clear();
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        if (!Config.showInWorldHeads) return;

        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer self = mc.thePlayer;
        Entity camera = mc.renderViewEntity;
        if (self == null || camera == null
            || mc.theWorld == null
            || mc.currentScreen != null
            || mc.gameSettings.hideGUI) return;

        float partial = event.partialTicks;
        double cameraX = camera.lastTickPosX + (camera.posX - camera.lastTickPosX) * partial;
        double cameraY = camera.lastTickPosY + (camera.posY - camera.lastTickPosY) * partial;
        double cameraZ = camera.lastTickPosZ + (camera.posZ - camera.lastTickPosZ) * partial;
        double eyeX = cameraX;
        double eyeY = cameraY + camera.getEyeHeight();
        double eyeZ = cameraZ;

        Collection<TrackedPlayer> tracked = ClientTeamData.snapshot();
        pruneHeadCache(tracked);

        for (TrackedPlayer player : tracked) {
            if (player.dimension != self.dimension) continue;

            double px = player.getWorldX(partial);
            double py = player.getWorldY(partial);
            double pz = player.getWorldZ(partial);

            double dx = px - eyeX;
            double dy = (py + 1.55D) - eyeY;
            double dz = pz - eyeZ;
            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (distance < Config.inWorldMinDistance || distance < 0.05D) continue;

            // Xaero's in-world waypoint/tracked-player renderer has a clamp-depth idea:
            // nearby markers remain at their true depth, while distant markers stop
            // shrinking after a configured depth. This reproduces that behaviour closely.
            double markerDistance = Math.min(distance, Config.inWorldMarkerDistance);
            double rayScale = markerDistance / distance;
            double markerX = eyeX + dx * rayScale;
            double markerY = eyeY + dy * rayScale;
            double markerZ = eyeZ + dz * rayScale;

            renderMarker(player, markerX, markerY, markerZ, distance, cameraX, cameraY, cameraZ);
        }
    }

    private void pruneHeadCache(Collection<TrackedPlayer> tracked) {
        if (heads.isEmpty()) return;
        Set<UUID> active = new HashSet<UUID>();
        for (TrackedPlayer player : tracked) active.add(player.uuid);
        heads.keySet()
            .retainAll(active);
    }

    private void renderMarker(TrackedPlayer player, double worldX, double worldY, double worldZ, double distance,
        double cameraX, double cameraY, double cameraZ) {
        float opacity = getMarkerOpacity(distance);
        int alpha = Math.max(13, Math.min(255, Math.round(opacity * 255F)));
        RenderManager rm = RenderManager.instance;

        GL11.glPushMatrix();
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        try {
            // RenderWorldLastEvent in 1.7.10 is not camera-relative for arbitrary
            // world-space vertices. GTNH renderers explicitly translate by -camera.
            GL11.glTranslated(-cameraX, -cameraY, -cameraZ);
            GL11.glTranslated(worldX, worldY, worldZ);
            GL11.glRotatef(-rm.playerViewY, 0F, 1F, 0F);
            GL11.glRotatef(rm.playerViewX, 1F, 0F, 0F);
            GL11.glScalef(-0.025F, -0.025F, 0.025F);

            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glDisable(GL11.GL_CULL_FACE);
            GL11.glDisable(GL11.GL_ALPHA_TEST);
            GL11.glDepthMask(false);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            // Alpha is carried by the tessellator vertices below. Applying it here as
            // well would square the opacity and make the hair/hat layer almost vanish.
            GL11.glColor4f(1F, 1F, 1F, 1F);

            double headSize = 16D * Config.inWorldIconScale;
            drawHead(player, -headSize / 2D, -headSize / 2D, headSize, alpha);

            int textAlpha = Math.max(32, alpha);
            int textColor = (textAlpha << 24) | 0xFFFFFF;
            double line = headSize / 2D + 4D;

            if (Config.showInWorldName) {
                drawCenteredScaledText(player.name, line, textColor, Config.inWorldNameScale);
                line += (Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT + 2D) * Config.inWorldNameScale;
            }
            if (Config.showInWorldDistance) {
                drawCenteredScaledText(formatDistance(distance), line, textColor, Config.inWorldNameScale);
            }
        } finally {
            GL11.glPopAttrib();
            GL11.glPopMatrix();
        }
    }

    private static float getMarkerOpacity(double distance) {
        float fullDistance = Math.max(1F, Config.inWorldFullOpacityDistance);
        float t = (float) Math.max(0D, Math.min(1D, distance / fullDistance));
        // Smoothstep avoids a visible brightness kink when the player crosses the
        // distance at which the marker reaches full opacity.
        t = t * t * (3F - 2F * t);
        float nearMultiplier = Math.max(0.05F, Math.min(1F, Config.inWorldNearOpacityMultiplier));
        float multiplier = nearMultiplier + (1F - nearMultiplier) * t;
        return Math.max(0.01F, Math.min(1F, Config.inWorldOpacity * multiplier));
    }

    private static String formatDistance(double distance) {
        if (distance >= 10000D) return String.format("%.1f km", distance / 1000D);
        return String.format("%.0f m", distance);
    }

    private static void drawCenteredScaledText(String text, double y, int color, double scale) {
        FontRenderer font = Minecraft.getMinecraft().fontRenderer;
        GL11.glPushMatrix();
        GL11.glScaled(scale, scale, 1D);
        int width = font.getStringWidth(text);
        font.drawStringWithShadow(text, -width / 2, (int) Math.round(y / scale), color);
        GL11.glPopMatrix();
    }

    private void drawHead(TrackedPlayer player, double x, double y, double size, int alpha) {
        PlayerHeadIcon head = heads.get(player.uuid);
        if (head == null) {
            head = new PlayerHeadIcon(player.uuid);
            heads.put(player.uuid, head);
        }
        head.bindTexture();
        GL11.glColor4f(1F, 1F, 1F, 1F);

        Tessellator tessellator = Tessellator.instance;
        // Base face.
        tessellator.startDrawingQuads();
        DrawUtils.addRectToBufferWithUV(tessellator, x, y, size, size, 0xFFFFFF, alpha, 0.125D, 0.25D, 0.25D, 0.50D);
        tessellator.draw();

        // Hat/hair overlay from the second head layer. This must be a separate pass
        // after the base so transparent pixels reveal the face below.
        tessellator.startDrawingQuads();
        DrawUtils.addRectToBufferWithUV(tessellator, x, y, size, size, 0xFFFFFF, alpha, 0.625D, 0.25D, 0.75D, 0.50D);
        tessellator.draw();
    }
}
