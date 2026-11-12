package net.vg.combo.network;

//? if >=1.21 {
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.vg.combo.util.ComboIds;

public record ComboStaggerPayload(int entityId, int durationTicks) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ComboStaggerPayload> TYPE =
            new CustomPacketPayload.Type<>(ComboIds.id("stagger"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ComboStaggerPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, ComboStaggerPayload::entityId,
            ByteBufCodecs.VAR_INT, ComboStaggerPayload::durationTicks,
            ComboStaggerPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
//? } else {
/*import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.vg.combo.util.ComboIds;

public record ComboStaggerPayload(int entityId, int durationTicks) implements ComboLegacyPayload {
    public static final Identifier ID = ComboIds.id("stagger");

    public ComboStaggerPayload(FriendlyByteBuf buffer) {
        this(buffer.readVarInt(), buffer.readVarInt());
    }

    @Override public Identifier id() { return ID; }
    @Override public void write(FriendlyByteBuf buffer) {
        buffer.writeVarInt(entityId);
        buffer.writeVarInt(durationTicks);
    }
}
*///? }
