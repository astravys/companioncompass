package astravys.companioncompass;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class CompassMarkerCalculator {
    private static final float FRONT_ANGLE = 90.0F;
    private static final float NEAR_SIZE = 8.0F;
    private static final float FAR_SIZE = 4.0F;

    private CompassMarkerCalculator() {
    }

    public static List<CompassMarker> calculate(LocalPlayer localPlayer, List<AbstractClientPlayer> players, float partialTick) {
        Vec3 localPosition = interpolatedPosition(localPlayer, partialTick);
        float viewYaw = localPlayer.getViewYRot(partialTick);
        List<CompassMarker> markers = new ArrayList<>();

        for (AbstractClientPlayer player : players) {
            Vec3 targetPosition = interpolatedPosition(player, partialTick);
            double dx = targetPosition.x - localPosition.x;
            double dy = targetPosition.y - localPosition.y;
            double dz = targetPosition.z - localPosition.z;
            double horizontalDistance = Math.sqrt(dx * dx + dz * dz);
            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

            float targetYaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0F;
            float relativeAngle = Mth.wrapDegrees(targetYaw - viewYaw);
            float clampedAngle = Mth.clamp(relativeAngle, -FRONT_ANGLE, FRONT_ANGLE);
            float x = clampedAngle / FRONT_ANGLE;
            float alpha = alphaForAngle(relativeAngle);
            float size = sizeForDistance(distance);
            float brightness = brightnessForHeight(dy, horizontalDistance);
            int color = colorFromUuid(player.getUUID());

            markers.add(new CompassMarker(player.getUUID(), x, size, brightness, alpha, distance, color));
        }

        markers.sort(Comparator.comparingDouble(CompassMarker::distance).reversed());
        return markers;
    }

    private static Vec3 interpolatedPosition(AbstractClientPlayer player, float partialTick) {
        double x = Mth.lerp(partialTick, player.xo, player.getX());
        double y = Mth.lerp(partialTick, player.yo, player.getY());
        double z = Mth.lerp(partialTick, player.zo, player.getZ());
        return new Vec3(x, y, z);
    }

    private static float alphaForAngle(float relativeAngle) {
        float angle = Math.abs(relativeAngle);
        if (angle <= FRONT_ANGLE) {
            return 1.0F;
        }
        float behind = Mth.clamp((angle - FRONT_ANGLE) / FRONT_ANGLE, 0.0F, 1.0F);
        return Mth.lerp(behind, 0.72F, 0.28F);
    }

    private static float sizeForDistance(double distance) {
        float normalized = Mth.clamp((float) ((distance - 8.0D) / 160.0D), 0.0F, 1.0F);
        return Mth.lerp(normalized, NEAR_SIZE, FAR_SIZE);
    }

    private static float brightnessForHeight(double dy, double horizontalDistance) {
        double ratio = Math.abs(dy) / Math.max(horizontalDistance, 1.0D);
        if (ratio < 0.20D) {
            return 1.0F;
        }
        float strength = Mth.clamp((float) ((ratio - 0.20D) / 0.80D), 0.0F, 1.0F);
        return dy > 0.0D ? Mth.lerp(strength, 1.0F, 1.35F) : Mth.lerp(strength, 1.0F, 0.55F);
    }

    private static int colorFromUuid(UUID uuid) {
        long bits = uuid.getMostSignificantBits() ^ uuid.getLeastSignificantBits();
        float hue = ((bits & 0xFFFFL) / 65535.0F);
        return Mth.hsvToRgb(hue, 0.68F, 0.95F);
    }
}
