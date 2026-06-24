package astravys.companioncompass;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(modid = CompanionCompass.MODID, value = Dist.CLIENT)
public final class CompanionCompassHud {
    private static final int SCALE_WIDTH = 182;
    private static final int SCALE_HEIGHT = 5;
    private static final int SCALE_COLOR = 0x8A101010;
    private static final int SCALE_EDGE_COLOR = 0xA0303030;
    private static final int OUTLINE_COLOR = 0xFF101010;
    private static final Map<UUID, SmoothMarker> SMOOTH_MARKERS = new HashMap<>();

    private CompanionCompassHud() {
    }

    @SubscribeEvent
    public static void hideExperienceWhenCompassIsVisible(RenderGuiLayerEvent.Pre event) {
        if (!isExperienceLayer(event) || Screen.hasShiftDown() || !hasOtherPlayers(Minecraft.getInstance())) {
            return;
        }
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void renderCompass(RenderGuiEvent.Post event) {
        if (Screen.hasShiftDown()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer localPlayer = minecraft.player;
        if (localPlayer == null) {
            SMOOTH_MARKERS.clear();
            return;
        }

        List<AbstractClientPlayer> players = ClientPlayerFinder.findOtherPlayers(minecraft);
        if (players.isEmpty()) {
            SMOOTH_MARKERS.clear();
            return;
        }

        float partialTick = partialTick(event.getPartialTick());
        List<CompassMarker> markers = CompassMarkerCalculator.calculate(localPlayer, players, partialTick);
        drawCompass(event.getGuiGraphics(), markers);
    }

    private static boolean isExperienceLayer(RenderGuiLayerEvent.Pre event) {
        return VanillaGuiLayers.EXPERIENCE_BAR.equals(event.getName()) || VanillaGuiLayers.EXPERIENCE_LEVEL.equals(event.getName());
    }

    private static boolean hasOtherPlayers(Minecraft minecraft) {
        return !ClientPlayerFinder.findOtherPlayers(minecraft).isEmpty();
    }

    private static float partialTick(DeltaTracker deltaTracker) {
        return deltaTracker.getGameTimeDeltaPartialTick(false);
    }

    private static void drawCompass(GuiGraphics guiGraphics, List<CompassMarker> markers) {
        int screenWidth = guiGraphics.guiWidth();
        int screenHeight = guiGraphics.guiHeight();
        int left = screenWidth / 2 - SCALE_WIDTH / 2;
        int centerY = screenHeight - 32 + 3;
        int top = centerY - SCALE_HEIGHT / 2;

        guiGraphics.fill(left, top, left + SCALE_WIDTH, top + SCALE_HEIGHT, SCALE_COLOR);
        guiGraphics.fill(left, top, left + 1, top + SCALE_HEIGHT, SCALE_EDGE_COLOR);
        guiGraphics.fill(left + SCALE_WIDTH - 1, top, left + SCALE_WIDTH, top + SCALE_HEIGHT, SCALE_EDGE_COLOR);

        Set<UUID> visibleIds = new HashSet<>();
        for (CompassMarker marker : markers) {
            visibleIds.add(marker.playerId());
            SmoothMarker smoothMarker = SMOOTH_MARKERS.computeIfAbsent(marker.playerId(), id -> new SmoothMarker(marker));
            smoothMarker.moveToward(marker);

            int x = Math.round(left + SCALE_WIDTH / 2.0F + smoothMarker.x * (SCALE_WIDTH / 2.0F));
            int y = centerY;
            int color = applyAlpha(applyBrightness(marker.color(), smoothMarker.brightness), smoothMarker.alpha);
            int outline = applyAlpha(OUTLINE_COLOR, smoothMarker.alpha);
            drawMarker(guiGraphics, x, y, smoothMarker.size, outline, color);
        }
        SMOOTH_MARKERS.keySet().removeIf(id -> !visibleIds.contains(id));
    }

    private static void drawMarker(GuiGraphics guiGraphics, int centerX, int centerY, float size, int outlineColor, int fillColor) {
        drawMarkerShape(guiGraphics, centerX - 1, centerY, size, outlineColor);
        drawMarkerShape(guiGraphics, centerX + 1, centerY, size, outlineColor);
        drawMarkerShape(guiGraphics, centerX, centerY - 1, size, outlineColor);
        drawMarkerShape(guiGraphics, centerX, centerY + 1, size, outlineColor);
        drawMarkerShape(guiGraphics, centerX, centerY, size, fillColor);
    }

    private static void drawMarkerShape(GuiGraphics guiGraphics, int centerX, int centerY, float size, int color) {
        int width = Math.max(4, Math.round(size));
        int height = Math.max(3, Math.round(size * 0.72F));
        int[] rowWidths = { 3, 5, 7, 7, 5, 3 };

        for (int row = 0; row < rowWidths.length; row++) {
            int y0 = centerY - height / 2 + Math.round(row * height / 6.0F);
            int y1 = centerY - height / 2 + Math.round((row + 1) * height / 6.0F);
            if (y1 <= y0) {
                y1 = y0 + 1;
            }

            int rowWidth = Math.max(1, Math.round(width * rowWidths[row] / 7.0F));
            int x0 = centerX - rowWidth / 2;
            int x1 = x0 + rowWidth;
            guiGraphics.fill(x0, y0, x1, y1, color);
        }
    }

    private static int applyBrightness(int rgb, float brightness) {
        int red = Mth.clamp(Math.round(((rgb >> 16) & 0xFF) * brightness), 0, 255);
        int green = Mth.clamp(Math.round(((rgb >> 8) & 0xFF) * brightness), 0, 255);
        int blue = Mth.clamp(Math.round((rgb & 0xFF) * brightness), 0, 255);
        return (red << 16) | (green << 8) | blue;
    }

    private static int applyAlpha(int rgb, float alpha) {
        int alphaByte = Mth.clamp(Math.round(alpha * 255.0F), 0, 255);
        return (alphaByte << 24) | (rgb & 0xFFFFFF);
    }

    private static final class SmoothMarker {
        private static final float SMOOTHING = 0.28F;

        private float x;
        private float size;
        private float brightness;
        private float alpha;

        private SmoothMarker(CompassMarker marker) {
            this.x = marker.x();
            this.size = marker.size();
            this.brightness = marker.brightness();
            this.alpha = marker.alpha();
        }

        private void moveToward(CompassMarker marker) {
            this.x = Mth.lerp(SMOOTHING, this.x, marker.x());
            this.size = Mth.lerp(SMOOTHING, this.size, marker.size());
            this.brightness = Mth.lerp(SMOOTHING, this.brightness, marker.brightness());
            this.alpha = Mth.lerp(SMOOTHING, this.alpha, marker.alpha());
        }
    }
}
