package net.vg.combo.client;

import dev.architectury.networking.NetworkManager;
import net.vg.combo.config.ComboConfig;
import net.vg.combo.network.*;

/** Loaded only on the physical client, including legacy Architectury. */
public final class ComboClientNetworking {
    private ComboClientNetworking() {}

    public static void register() {
        //? if >=1.21 {
        NetworkManager.registerReceiver(NetworkManager.s2c(), ComboSyncPayload.TYPE, ComboSyncPayload.STREAM_CODEC,
                (payload, context) -> context.queue(() -> accept(payload)));
        //? } else {
        /*NetworkManager.registerReceiver(NetworkManager.s2c(), ComboSyncPayload.ID, (buffer, context) -> {
            var payload = new ComboSyncPayload(buffer);
            context.queue(() -> accept(payload));
        });
        *///? }
        //? if >=1.21 {
        NetworkManager.registerReceiver(NetworkManager.s2c(), ComboDamagePopupPayload.TYPE, ComboDamagePopupPayload.STREAM_CODEC,
                (payload, context) -> context.queue(() -> accept(payload)));
        //? } else {
        /*NetworkManager.registerReceiver(NetworkManager.s2c(), ComboDamagePopupPayload.ID, (buffer, context) -> {
            var payload = new ComboDamagePopupPayload(buffer);
            context.queue(() -> accept(payload));
        });
        *///? }
        //? if >=1.21 {
        NetworkManager.registerReceiver(NetworkManager.s2c(), ComboStaggerPayload.TYPE, ComboStaggerPayload.STREAM_CODEC,
                (payload, context) -> context.queue(() -> accept(payload)));
        //? } else {
        /*NetworkManager.registerReceiver(NetworkManager.s2c(), ComboStaggerPayload.ID, (buffer, context) -> {
            var payload = new ComboStaggerPayload(buffer);
            context.queue(() -> accept(payload));
        });
        *///? }
        //? if >=1.21 {
        NetworkManager.registerReceiver(NetworkManager.s2c(), ComboBackstabPayload.TYPE, ComboBackstabPayload.STREAM_CODEC,
                (payload, context) -> context.queue(() -> accept(payload)));
        //? } else {
        /*NetworkManager.registerReceiver(NetworkManager.s2c(), ComboBackstabPayload.ID, (buffer, context) -> {
            var payload = new ComboBackstabPayload();
            context.queue(() -> accept(payload));
        });
        *///? }
        //? if >=1.21 {
        NetworkManager.registerReceiver(NetworkManager.s2c(), ComboStaminaPayload.TYPE, ComboStaminaPayload.STREAM_CODEC,
                (payload, context) -> context.queue(() -> accept(payload)));
        //? } else {
        /*NetworkManager.registerReceiver(NetworkManager.s2c(), ComboStaminaPayload.ID, (buffer, context) -> {
            var payload = new ComboStaminaPayload(buffer);
            context.queue(() -> accept(payload));
        });
        *///? }
    }

    private static void accept(ComboSyncPayload payload) {
        ComboClientState.update(payload.stage(), payload.remainingTicks(), payload.finisher());
    }

    private static void accept(ComboDamagePopupPayload payload) {
        if (ComboConfig.damagePopupEnabled) ComboDamagePopups.enqueue(payload.entityId(), payload.damage(), payload.finisher());
    }

    private static void accept(ComboStaggerPayload payload) {
        if (ComboConfig.staggerVisualEnabled) ComboStaggerEffects.register(payload.entityId(), payload.durationTicks());
    }

    private static void accept(ComboBackstabPayload payload) {
        if (ComboConfig.backstabVignetteEnabled) ComboVignetteEffects.trigger();
    }

    private static void accept(ComboStaminaPayload payload) {
        ComboStaminaClient.update(payload.current(), payload.max());
    }
}
