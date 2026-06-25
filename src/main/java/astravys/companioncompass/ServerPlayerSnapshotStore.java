package astravys.companioncompass;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class ServerPlayerSnapshotStore {
    private static final long SNAPSHOT_TIMEOUT_MILLIS = 3_000L;
    private static final Map<UUID, TrackedPlayer> TRACKED_PLAYERS = new HashMap<>();
    private static long lastSnapshotId = Long.MIN_VALUE;
    private static long lastSnapshotTimeMillis;

    private ServerPlayerSnapshotStore() {
    }

    public static void acceptSnapshot(PlayerPositionsPayload payload) {
        if (payload.snapshotId() <= lastSnapshotId) {
            return;
        }

        Map<UUID, TrackedPlayer> nextPlayers = new HashMap<>();
        for (PlayerPositionsPayload.Entry entry : payload.entries()) {
            TrackedPlayer previous = TRACKED_PLAYERS.get(entry.playerId());
            Vec3 previousPosition = previous == null ? entry.position() : previous.currentPosition();
            nextPlayers.put(entry.playerId(), new TrackedPlayer(
                    entry.playerId(),
                    entry.dimension(),
                    previousPosition,
                    entry.position()));
        }

        TRACKED_PLAYERS.clear();
        TRACKED_PLAYERS.putAll(nextPlayers);
        lastSnapshotId = payload.snapshotId();
        lastSnapshotTimeMillis = System.currentTimeMillis();
    }

    public static List<TrackedPlayer> findPlayers(ResourceKey<Level> localDimension) {
        if (!hasFreshSnapshot()) {
            clear();
            return List.of();
        }

        List<TrackedPlayer> players = new ArrayList<>();
        for (TrackedPlayer player : TRACKED_PLAYERS.values()) {
            if (player.dimension() == localDimension) {
                players.add(player);
            }
        }
        return players;
    }

    public static boolean hasFreshSnapshot() {
        if (lastSnapshotTimeMillis <= 0L) {
            return false;
        }
        if (System.currentTimeMillis() - lastSnapshotTimeMillis > SNAPSHOT_TIMEOUT_MILLIS) {
            clear();
            return false;
        }
        return true;
    }

    public static void clear() {
        TRACKED_PLAYERS.clear();
        lastSnapshotId = Long.MIN_VALUE;
        lastSnapshotTimeMillis = 0L;
    }
}
