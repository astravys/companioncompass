package astravys.companioncompass;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class ClientPlayerFinder {
    private static ResourceKey<Level> lastLocalDimension;

    private ClientPlayerFinder() {
    }

    public static List<TrackedPlayer> findOtherPlayers(Minecraft minecraft) {
        LocalPlayer localPlayer = minecraft.player;
        ClientLevel level = minecraft.level;
        if (localPlayer == null || level == null) {
            ServerPlayerSnapshotStore.clear();
            lastLocalDimension = null;
            return List.of();
        }

        ResourceKey<Level> localDimension = localPlayer.level().dimension();
        if (lastLocalDimension != null && !lastLocalDimension.equals(localDimension)) {
            ServerPlayerSnapshotStore.clear();
        }
        lastLocalDimension = localDimension;

        if (ServerPlayerSnapshotStore.hasFreshSnapshot()) {
            return ServerPlayerSnapshotStore.findPlayers(localDimension);
        }

        return findLoadedPlayers(localPlayer, level, localDimension);
    }

    private static List<TrackedPlayer> findLoadedPlayers(LocalPlayer localPlayer, ClientLevel level, ResourceKey<Level> localDimension) {
        List<TrackedPlayer> players = new ArrayList<>();
        for (AbstractClientPlayer player : level.players()) {
            if (player == localPlayer) {
                continue;
            }
            ResourceKey<Level> playerDimension = player.level().dimension();
            if (!playerDimension.equals(localDimension)) {
                continue;
            }
            players.add(new TrackedPlayer(
                    player.getUUID(),
                    playerDimension,
                    new Vec3(player.xo, player.yo, player.zo),
                    player.position()));
        }
        return players;
    }
}
