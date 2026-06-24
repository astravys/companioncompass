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
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
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
    private static final ResourceLocation EXPERIENCE_BAR_BACKGROUND = ResourceLocation.withDefaultNamespace("hud/experience_bar_background");
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

        List<TrackedPlayer> players = ClientPlayerFinder.findOtherPlayers(minecraft);
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
        int top = screenHeight - 32 + 3;
        int centerY = top + SCALE_HEIGHT / 2;

        guiGraphics.blitSprite(EXPERIENCE_BAR_BACKGROUND, left, top, SCALE_WIDTH, SCALE_HEIGHT);

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
        int radius = Math.max(2, Math.round(size / 2.0F));
        drawCircle(guiGraphics, centerX, centerY, radius + 1, outlineColor);
        drawCircle(guiGraphics, centerX, centerY, radius, fillColor);
    }

    private static void drawCircle(GuiGraphics guiGraphics, int centerX, int centerY, int radius, int color) {
        int radiusSquared = radius * radius;
        for (int y = -radius; y <= radius; y++) {
            int x = 0;
            while (x * x + y * y <= radiusSquared) {
                x++;
            }
            int halfWidth = x - 1;
            guiGraphics.fill(centerX - halfWidth, centerY + y, centerX + halfWidth + 1, centerY + y + 1, color);
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
