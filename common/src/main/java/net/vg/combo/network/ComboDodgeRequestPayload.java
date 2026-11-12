package net.vg.combo.network;

//? if >=1.21 {
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.vg.combo.util.ComboIds;

/** Server validates dodge cost/cooldown; legacy clients also supply held movement input. */
//? if >=26.1 {
public record ComboDodgeRequestPayload() implements CustomPacketPayload {
//? } else {
/*public record ComboDodgeRequestPayload(float left, float forward) implements CustomPacketPayload {
*///? }
    public static final CustomPacketPayload.Type<ComboDodgeRequestPayload> TYPE =
            new CustomPacketPayload.Type<>(ComboIds.id("dodge_request"));

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
//? } else {
/*import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.vg.combo.util.ComboIds;

public record ComboDodgeRequestPayload(float left, float forward) implements ComboLegacyPayload {
    public static final Identifier ID = ComboIds.id("dodge_request");

    public ComboDodgeRequestPayload(FriendlyByteBuf buffer) {
        this(buffer.readFloat(), buffer.readFloat());
    }

    @Override public Identifier id() { return ID; }
    @Override public void write(FriendlyByteBuf buffer) {
        buffer.writeFloat(left);
        buffer.writeFloat(forward);
    }
}
*///? }
