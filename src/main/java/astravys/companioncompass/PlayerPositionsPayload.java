package astravys.companioncompass;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public record PlayerPositionsPayload(long snapshotId, List<Entry> entries) implements CustomPacketPayload {
    public static final Type<PlayerPositionsPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(CompanionCompass.MODID, "player_positions"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PlayerPositionsPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public PlayerPositionsPayload decode(RegistryFriendlyByteBuf buffer) {
            long snapshotId = buffer.readVarLong();
            int size = buffer.readVarInt();
            List<Entry> entries = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                UUID playerId = buffer.readUUID();
                ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION, buffer.readResourceLocation());
                double x = buffer.readDouble();
                double y = buffer.readDouble();
                double z = buffer.readDouble();
                entries.add(new Entry(playerId, dimension, new Vec3(x, y, z)));
            }
            return new PlayerPositionsPayload(snapshotId, List.copyOf(entries));
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buffer, PlayerPositionsPayload payload) {
            buffer.writeVarLong(payload.snapshotId());
            buffer.writeVarInt(payload.entries().size());
            for (Entry entry : payload.entries()) {
                buffer.writeUUID(entry.playerId());
                buffer.writeResourceLocation(entry.dimension().location());
                buffer.writeDouble(entry.position().x);
                buffer.writeDouble(entry.position().y);
                buffer.writeDouble(entry.position().z);
            }
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public record Entry(UUID playerId, ResourceKey<Level> dimension, Vec3 position) {
    }
}
