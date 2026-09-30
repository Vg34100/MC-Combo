package net.vg.combo.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.vg.combo.Combo;

/** Server-to-client: keeps the client's stamina bar in sync with the server-authoritative value. */
public record ComboStaminaPayload(float current, float max) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ComboStaminaPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Combo.MOD_ID, "stamina"));

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
