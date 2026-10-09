package net.vg.combo.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.vg.combo.Combo;

/** Server validates dodge cost/cooldown; legacy clients also supply held movement input. */
//? if >=26.1 {
public record ComboDodgeRequestPayload() implements CustomPacketPayload {
//? } else {
/*public record ComboDodgeRequestPayload(float left, float forward) implements CustomPacketPayload {
*///? }
    public static final CustomPacketPayload.Type<ComboDodgeRequestPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Combo.MOD_ID, "dodge_request"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ComboDodgeRequestPayload> STREAM_CODEC =
            //? if >=26.1 {
            StreamCodec.unit(new ComboDodgeRequestPayload());
            //? } else {
            /*StreamCodec.composite(
                    ByteBufCodecs.FLOAT, ComboDodgeRequestPayload::left,
                    ByteBufCodecs.FLOAT, ComboDodgeRequestPayload::forward,
                    ComboDodgeRequestPayload::new);
            *///? }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
