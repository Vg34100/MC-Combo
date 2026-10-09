package net.vg.combo;

import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.event.events.common.TickEvent;
import dev.architectury.networking.NetworkManager;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.vg.combo.client.ComboClientState;
import net.vg.combo.client.ComboDamagePopups;
import net.vg.combo.client.ComboDodgeClient;
import net.vg.combo.client.ComboStaggerEffects;
import net.vg.combo.client.ComboStaminaClient;
import net.vg.combo.client.ComboVignetteEffects;
import net.vg.combo.combat.ComboCombat;
import net.vg.combo.combat.ComboDodge;
import net.vg.combo.combat.ComboParry;
import net.vg.combo.combat.ComboStamina;
import net.vg.combo.config.ComboConfig;
import net.vg.combo.network.ComboBackstabPayload;
import net.vg.combo.network.ComboDamagePopupPayload;
import net.vg.combo.network.ComboDodgeRequestPayload;
import net.vg.combo.network.ComboStaggerPayload;
import net.vg.combo.network.ComboStaminaPayload;
import net.vg.combo.network.ComboSyncPayload;
import net.vg.combo.stats.ComboStats;

public final class Combo {
    public static final String MOD_ID = "combo";

    private Combo() {
    }

    public static void init() {
        ComboConfig.registerConfigs();
        ComboStats.register();

        // Current Architectury registers S2C types on both physical sides. Its legacy
        // Fabric receiver adaptor is client-only, so dedicated servers register types instead.
        //? if <26.1 {
        /*if (dev.architectury.platform.Platform.getEnvironment() == dev.architectury.utils.Env.CLIENT) {
        *///? }
        NetworkManager.registerReceiver(NetworkManager.s2c(), ComboSyncPayload.TYPE, ComboSyncPayload.STREAM_CODEC,
                (payload, context) -> context.queue(() ->
                        ComboClientState.update(payload.stage(), payload.remainingTicks(), payload.finisher())));

        NetworkManager.registerReceiver(NetworkManager.s2c(), ComboDamagePopupPayload.TYPE, ComboDamagePopupPayload.STREAM_CODEC,
                (payload, context) -> context.queue(() -> {
                    if (ComboConfig.damagePopupEnabled) {
                        ComboDamagePopups.enqueue(payload.entityId(), payload.damage(), payload.finisher());
                    }
                }));

        NetworkManager.registerReceiver(NetworkManager.s2c(), ComboStaggerPayload.TYPE, ComboStaggerPayload.STREAM_CODEC,
                (payload, context) -> context.queue(() -> {
                    if (ComboConfig.staggerVisualEnabled) {
                        ComboStaggerEffects.register(payload.entityId(), payload.durationTicks());
                    }
                }));

        NetworkManager.registerReceiver(NetworkManager.s2c(), ComboBackstabPayload.TYPE, ComboBackstabPayload.STREAM_CODEC,
                (payload, context) -> context.queue(() -> {
                    if (ComboConfig.backstabVignetteEnabled) {
                        ComboVignetteEffects.trigger();
                    }
                }));

        NetworkManager.registerReceiver(NetworkManager.s2c(), ComboStaminaPayload.TYPE, ComboStaminaPayload.STREAM_CODEC,
                (payload, context) -> context.queue(() -> ComboStaminaClient.update(payload.current(), payload.max())));

        //? if <26.1 {
        /*} else {
            NetworkManager.registerS2CPayloadType(ComboSyncPayload.TYPE, ComboSyncPayload.STREAM_CODEC);
            NetworkManager.registerS2CPayloadType(ComboDamagePopupPayload.TYPE, ComboDamagePopupPayload.STREAM_CODEC);
            NetworkManager.registerS2CPayloadType(ComboStaggerPayload.TYPE, ComboStaggerPayload.STREAM_CODEC);
            NetworkManager.registerS2CPayloadType(ComboBackstabPayload.TYPE, ComboBackstabPayload.STREAM_CODEC);
            NetworkManager.registerS2CPayloadType(ComboStaminaPayload.TYPE, ComboStaminaPayload.STREAM_CODEC);
        }
        *///? }

        NetworkManager.registerReceiver(NetworkManager.c2s(), ComboDodgeRequestPayload.TYPE, ComboDodgeRequestPayload.STREAM_CODEC,
                (payload, context) -> context.queue(() -> {
                    if (context.getPlayer() instanceof ServerPlayer serverPlayer
                            && serverPlayer.level() instanceof ServerLevel serverLevel) {
                        //? if >=26.1 {
                        ComboDodge.attemptDodge(serverPlayer, serverLevel, serverLevel.getGameTime());
                        //? } else {
                        /*ComboDodge.attemptDodge(serverPlayer, serverLevel, serverLevel.getGameTime(), payload.left(), payload.forward());
                        *///? }
                    }
                }));

        TickEvent.PLAYER_POST.register(player -> {
            if (player instanceof ServerPlayer serverPlayer) {
                long gameTime = serverPlayer.level().getGameTime();
                ComboParry.trackBlockState(serverPlayer, gameTime);
                ComboStamina.tick(serverPlayer, gameTime);
                ComboDodge.tick(serverPlayer, gameTime);
            }
        });

        PlayerEvent.PLAYER_QUIT.register(ComboCombat::clear);
        PlayerEvent.PLAYER_QUIT.register(ComboParry::clear);
        PlayerEvent.PLAYER_QUIT.register(ComboStamina::clear);
        PlayerEvent.PLAYER_QUIT.register(ComboDodge::clear);
        PlayerEvent.PLAYER_RESPAWN.register((newPlayer, conqueredEnd, removalReason) -> {
            ComboCombat.clear(newPlayer);
            ComboParry.clear(newPlayer);
            ComboStamina.clear(newPlayer);
            ComboDodge.clear(newPlayer);
        });
    }

    /**
     * Client-only wiring. Must only be called from loader client entrypoints, never from
     * {@link #init()} — {@code ClientTickEvent} only ever fires on the physical client, but
     * this keeps the same safety convention as the rest of the client-only registration here.
     */
    public static void initClient() {
        KeyMappingRegistry.register(ComboDodgeClient.KEY_BINDING);
        ClientTickEvent.CLIENT_POST.register(minecraft -> {
            ComboClientState.tick();
            ComboDodgeClient.tick();
        });
    }
}
