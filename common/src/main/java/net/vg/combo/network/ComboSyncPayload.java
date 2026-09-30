package net.vg.combo.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.vg.combo.Combo;

public record ComboSyncPayload(int stage, int remainingTicks, boolean finisher) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ComboSyncPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Combo.MOD_ID, "combo_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ComboSyncPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, ComboSyncPayload::stage,
            ByteBufCodecs.VAR_INT, ComboSyncPayload::remainingTicks,
            ByteBufCodecs.BOOL, ComboSyncPayload::finisher,
            ComboSyncPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
