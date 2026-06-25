package astravys.companioncompass;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

@EventBusSubscriber(modid = CompanionCompass.MODID, value = Dist.CLIENT)
public class CompanionCompassClient {
    @SubscribeEvent
    public static void clearSnapshotsOnLogin(ClientPlayerNetworkEvent.LoggingIn event) {
        ServerPlayerSnapshotStore.clear();
    }

    @SubscribeEvent
    public static void clearSnapshotsOnLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        ServerPlayerSnapshotStore.clear();
    }

    @SubscribeEvent
    public static void clearSnapshotsOnClone(ClientPlayerNetworkEvent.Clone event) {
        ServerPlayerSnapshotStore.clear();
    }
}
