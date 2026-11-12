package net.vg.combo.network;

import dev.architectury.networking.NetworkManager;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
//? if >=1.21 {
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
//? } else {
/*import net.minecraft.network.FriendlyByteBuf;
import io.netty.buffer.Unpooled;
*///? }
import net.vg.combo.combat.ComboDodge;
import java.util.List;

/** Only adapts Architectury's channel API; gameplay and packet layouts stay shared. */
public final class ComboNetworking {
    private ComboNetworking() {}

    public static void register() {
        //? if >=1.21 {
        NetworkManager.registerReceiver(NetworkManager.c2s(), ComboDodgeRequestPayload.TYPE,
                ComboDodgeRequestPayload.STREAM_CODEC, (payload, context) -> context.queue(() -> dodge(payload, context)));
        if (Platform.getEnvironment() == Env.SERVER) {
            NetworkManager.registerS2CPayloadType(ComboSyncPayload.TYPE, ComboSyncPayload.STREAM_CODEC);
            NetworkManager.registerS2CPayloadType(ComboDamagePopupPayload.TYPE, ComboDamagePopupPayload.STREAM_CODEC);
            NetworkManager.registerS2CPayloadType(ComboStaggerPayload.TYPE, ComboStaggerPayload.STREAM_CODEC);
            NetworkManager.registerS2CPayloadType(ComboBackstabPayload.TYPE, ComboBackstabPayload.STREAM_CODEC);
            NetworkManager.registerS2CPayloadType(ComboStaminaPayload.TYPE, ComboStaminaPayload.STREAM_CODEC);
        }
        //? } else {
        /*NetworkManager.registerReceiver(NetworkManager.c2s(), ComboDodgeRequestPayload.ID, (buffer, context) -> {
            var payload = new ComboDodgeRequestPayload(buffer);
            context.queue(() -> dodge(payload, context));
        });
        *///? }
    }

    private static void dodge(ComboDodgeRequestPayload payload, NetworkManager.PacketContext context) {
        if (context.getEnvironment() == Env.SERVER && context.getPlayer() instanceof ServerPlayer player
                && player.level() instanceof ServerLevel level) {
            //? if >=26.1 {
            ComboDodge.attemptDodge(player, level, level.getGameTime());
            //? } else {
            /*ComboDodge.attemptDodge(player, level, level.getGameTime(), payload.left(), payload.forward());
            *///? }
        }
    }

    public static void sendToPlayer(ServerPlayer player,
            //? if >=1.21 {
            CustomPacketPayload payload
            //? } else {
            /*ComboLegacyPayload payload
            *///? }
    ) {
        //? if >=1.21 {
        NetworkManager.sendToPlayer(player, payload);
        //? } else {
        /*var buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            payload.write(buffer);
            NetworkManager.sendToPlayer(player, payload.id(), buffer);
        } finally { buffer.release(); }
        *///? }
    }

    public static void sendToPlayers(List<ServerPlayer> players,
            //? if >=1.21 {
            CustomPacketPayload payload
            //? } else {
            /*ComboLegacyPayload payload
            *///? }
    ) {
        for (ServerPlayer player : players) sendToPlayer(player, payload);
    }

    public static void sendToServer(ComboDodgeRequestPayload payload) {
        //? if >=1.21 {
        NetworkManager.sendToServer(payload);
        //? } else {
        /*var buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            payload.write(buffer);
            NetworkManager.sendToServer(payload.id(), buffer);
        } finally { buffer.release(); }
        *///? }
    }
}
