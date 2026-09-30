package net.vg.combo.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.vg.combo.Combo;

/** Client-to-server: zero-payload request to perform a dodge, subject to server-side cost/cooldown. */
public record ComboDodgeRequestPayload() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ComboDodgeRequestPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Combo.MOD_ID, "dodge_request"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ComboDodgeRequestPayload> STREAM_CODEC =
            StreamCodec.unit(new ComboDodgeRequestPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
