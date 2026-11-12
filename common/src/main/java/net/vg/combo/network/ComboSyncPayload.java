package net.vg.combo.network;

//? if >=1.21 {
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.vg.combo.util.ComboIds;

public record ComboSyncPayload(int stage, int remainingTicks, boolean finisher) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ComboSyncPayload> TYPE =
            new CustomPacketPayload.Type<>(ComboIds.id("combo_sync"));

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
//? } else {
/*import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.vg.combo.util.ComboIds;

public record ComboSyncPayload(int stage, int remainingTicks, boolean finisher) implements ComboLegacyPayload {
    public static final Identifier ID = ComboIds.id("combo_sync");

    public ComboSyncPayload(FriendlyByteBuf buffer) {
        this(buffer.readVarInt(), buffer.readVarInt(), buffer.readBoolean());
    }

    @Override public Identifier id() { return ID; }
    @Override public void write(FriendlyByteBuf buffer) {
        buffer.writeVarInt(stage);
        buffer.writeVarInt(remainingTicks);
        buffer.writeBoolean(finisher);
    }
}
*///? }
