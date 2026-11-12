package net.vg.combo.network;

//? if >=1.21 {
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.vg.combo.util.ComboIds;

/** Zero-payload signal sent only to the attacking player to flash their own screen vignette. */
public record ComboBackstabPayload() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ComboBackstabPayload> TYPE =
            new CustomPacketPayload.Type<>(ComboIds.id("backstab"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ComboBackstabPayload> STREAM_CODEC =
            StreamCodec.unit(new ComboBackstabPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
//? } else {
/*import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.vg.combo.util.ComboIds;

public record ComboBackstabPayload() implements ComboLegacyPayload {
    public static final Identifier ID = ComboIds.id("backstab");

    @Override public Identifier id() { return ID; }
    @Override public void write(FriendlyByteBuf buffer) {

    }
}
*///? }
