package astravys.companioncompass;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.network.ConnectionProtocol;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.registration.NetworkRegistry;

@EventBusSubscriber(modid = CompanionCompass.MODID)
public final class ServerPlayerPositionBroadcaster {
    public static final int SNAPSHOT_INTERVAL_TICKS = 5;

    private ServerPlayerPositionBroadcaster() {
    }

    @SubscribeEvent
    public static void sendSnapshots(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        int tickCount = server.getTickCount();
        if (tickCount % SNAPSHOT_INTERVAL_TICKS != 0) {
            return;
        }

        List<ServerPlayer> players = server.getPlayerList().getPlayers();
        for (ServerPlayer recipient : players) {
            if (!canReceiveSnapshots(recipient)) {
                continue;
            }

            List<PlayerPositionsPayload.Entry> entries = new ArrayList<>();
            for (ServerPlayer otherPlayer : players) {
                if (otherPlayer == recipient || !otherPlayer.level().dimension().equals(recipient.level().dimension())) {
                    continue;
                }
                entries.add(new PlayerPositionsPayload.Entry(
                        otherPlayer.getUUID(),
                        otherPlayer.level().dimension(),
                        otherPlayer.position()));
            }
            PacketDistributor.sendToPlayer(recipient, new PlayerPositionsPayload(tickCount, entries));
        }
    }

    private static boolean canReceiveSnapshots(ServerPlayer player) {
        return NetworkRegistry.hasChannel(player.connection.getConnection(), ConnectionProtocol.PLAY, PlayerPositionsPayload.TYPE.id());
    }
}
