package astravys.companioncompass;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class CompanionCompassNetwork {
    private static final String NETWORK_VERSION = "1";

    private CompanionCompassNetwork() {
    }

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(NETWORK_VERSION).optional();
        registrar.playToClient(PlayerPositionsPayload.TYPE, PlayerPositionsPayload.STREAM_CODEC, CompanionCompassNetwork::handlePlayerPositions);
    }

    private static void handlePlayerPositions(PlayerPositionsPayload payload, net.neoforged.neoforge.network.handling.IPayloadContext context) {
        ServerPlayerSnapshotStore.acceptSnapshot(payload);
    }
}
