package net.vg.combo.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.vg.combo.Combo;

public record ComboStaggerPayload(int entityId, int durationTicks) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ComboStaggerPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Combo.MOD_ID, "stagger"));

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
