package net.vg.combo.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.vg.combo.Combo;

/** Zero-payload signal sent only to the attacking player to flash their own screen vignette. */
public record ComboBackstabPayload() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ComboBackstabPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Combo.MOD_ID, "backstab"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ComboBackstabPayload> STREAM_CODEC =
            StreamCodec.unit(new ComboBackstabPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
