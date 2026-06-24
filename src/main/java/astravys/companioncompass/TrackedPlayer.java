package astravys.companioncompass;

import java.util.UUID;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public record TrackedPlayer(
        UUID playerId,
        ResourceKey<Level> dimension,
        Vec3 previousPosition,
        Vec3 currentPosition) {
}
