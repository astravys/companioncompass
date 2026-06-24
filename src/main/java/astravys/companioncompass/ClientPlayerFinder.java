package astravys.companioncompass;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class ClientPlayerFinder {
    private static final Map<UUID, TrackedPlayer> TRACKED_PLAYERS = new HashMap<>();

    private ClientPlayerFinder() {
    }

    public static List<TrackedPlayer> findOtherPlayers(Minecraft minecraft) {
        LocalPlayer localPlayer = minecraft.player;
        ClientLevel level = minecraft.level;
        if (localPlayer == null || level == null) {
            TRACKED_PLAYERS.clear();
            return List.of();
        }

        removeOfflinePlayers(minecraft, localPlayer.getUUID());
        ResourceKey<Level> localDimension = localPlayer.level().dimension();

        for (AbstractClientPlayer player : level.players()) {
            if (player == localPlayer) {
                continue;
            }
            ResourceKey<Level> playerDimension = player.level().dimension();
            if (playerDimension != localDimension) {
                continue;
            }
            TRACKED_PLAYERS.put(player.getUUID(), new TrackedPlayer(
                    player.getUUID(),
                    playerDimension,
                    new Vec3(player.xo, player.yo, player.zo),
                    player.position()));
        }

        List<TrackedPlayer> players = new ArrayList<>();
        for (TrackedPlayer player : TRACKED_PLAYERS.values()) {
            if (player.dimension() == localDimension) {
                players.add(player);
            }
        }
        return players;
    }

    private static void removeOfflinePlayers(Minecraft minecraft, UUID localPlayerId) {
        if (minecraft.getConnection() == null) {
            TRACKED_PLAYERS.clear();
            return;
        }

        Set<UUID> onlinePlayers = new HashSet<>();
        for (PlayerInfo playerInfo : minecraft.getConnection().getOnlinePlayers()) {
            UUID playerId = playerInfo.getProfile().getId();
            if (!playerId.equals(localPlayerId)) {
                onlinePlayers.add(playerId);
            }
        }
        TRACKED_PLAYERS.keySet().removeIf(playerId -> !onlinePlayers.contains(playerId));
    }
}
