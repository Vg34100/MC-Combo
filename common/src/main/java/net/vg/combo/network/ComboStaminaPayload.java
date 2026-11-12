package net.vg.combo.network;

//? if >=1.21 {
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.vg.combo.util.ComboIds;

/** Server-to-client: keeps the client's stamina bar in sync with the server-authoritative value. */
public record ComboStaminaPayload(float current, float max) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ComboStaminaPayload> TYPE =
            new CustomPacketPayload.Type<>(ComboIds.id("stamina"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ComboStaminaPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, ComboStaminaPayload::current,
            ByteBufCodecs.FLOAT, ComboStaminaPayload::max,
            ComboStaminaPayload::new
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

public record ComboStaminaPayload(float current, float max) implements ComboLegacyPayload {
    public static final Identifier ID = ComboIds.id("stamina");

    public ComboStaminaPayload(FriendlyByteBuf buffer) {
        this(buffer.readFloat(), buffer.readFloat());
    }

    @Override public Identifier id() { return ID; }
    @Override public void write(FriendlyByteBuf buffer) {
        buffer.writeFloat(current);
        buffer.writeFloat(max);
    }
}
*///? }
