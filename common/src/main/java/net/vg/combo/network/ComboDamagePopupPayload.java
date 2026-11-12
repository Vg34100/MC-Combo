package net.vg.combo.network;

//? if >=1.21 {
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.vg.combo.util.ComboIds;

public record ComboDamagePopupPayload(int entityId, float damage, boolean finisher) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ComboDamagePopupPayload> TYPE =
            new CustomPacketPayload.Type<>(ComboIds.id("damage_popup"));

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
//? } else {
/*import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.vg.combo.util.ComboIds;

public record ComboDamagePopupPayload(int entityId, float damage, boolean finisher) implements ComboLegacyPayload {
    public static final Identifier ID = ComboIds.id("damage_popup");

    public ComboDamagePopupPayload(FriendlyByteBuf buffer) {
        this(buffer.readVarInt(), buffer.readFloat(), buffer.readBoolean());
    }

    @Override public Identifier id() { return ID; }
    @Override public void write(FriendlyByteBuf buffer) {
        buffer.writeVarInt(entityId);
        buffer.writeFloat(damage);
        buffer.writeBoolean(finisher);
    }
}
*///? }
