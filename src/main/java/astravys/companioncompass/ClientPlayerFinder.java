package astravys.companioncompass;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;

public final class ClientPlayerFinder {
    private ClientPlayerFinder() {
    }

    public static List<AbstractClientPlayer> findOtherPlayers(Minecraft minecraft) {
        LocalPlayer localPlayer = minecraft.player;
        ClientLevel level = minecraft.level;
        if (localPlayer == null || level == null) {
            return List.of();
        }

        List<AbstractClientPlayer> players = new ArrayList<>();
        for (AbstractClientPlayer player : level.players()) {
            if (player == localPlayer) {
                continue;
            }
            if (player.level().dimension() != localPlayer.level().dimension()) {
                continue;
            }
            players.add(player);
        }
        return players;
    }
}
