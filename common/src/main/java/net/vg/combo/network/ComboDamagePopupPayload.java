package net.vg.combo.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.vg.combo.Combo;

public record ComboDamagePopupPayload(int entityId, float damage, boolean finisher) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ComboDamagePopupPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Combo.MOD_ID, "damage_popup"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ComboDamagePopupPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, ComboDamagePopupPayload::entityId,
            ByteBufCodecs.FLOAT, ComboDamagePopupPayload::damage,
            ByteBufCodecs.BOOL, ComboDamagePopupPayload::finisher,
            ComboDamagePopupPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
