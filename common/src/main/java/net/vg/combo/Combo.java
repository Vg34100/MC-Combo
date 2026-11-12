package net.vg.combo;

import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.event.events.common.TickEvent;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import net.minecraft.server.level.ServerPlayer;
import net.vg.combo.client.ComboClientState;
import net.vg.combo.client.ComboDodgeClient;
import net.vg.combo.combat.ComboCombat;
import net.vg.combo.combat.ComboDodge;
import net.vg.combo.combat.ComboParry;
import net.vg.combo.combat.ComboStamina;
import net.vg.combo.config.ComboConfig;
import net.vg.combo.stats.ComboStats;

public final class Combo {
    public static final String MOD_ID = "combo";

    private Combo() {
    }

    public static void init() {
        ComboConfig.registerConfigs();
        ComboStats.register();

        net.vg.combo.network.ComboNetworking.register();
        if (dev.architectury.platform.Platform.getEnvironment() == dev.architectury.utils.Env.CLIENT) {
            net.vg.combo.client.ComboClientNetworking.register();
        }

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
        //? if >=1.21 {
        PlayerEvent.PLAYER_RESPAWN.register((newPlayer, conqueredEnd, removalReason) -> {
        //? } else {
        /*PlayerEvent.PLAYER_RESPAWN.register((newPlayer, conqueredEnd) -> {
        *///? }
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
        initClientTicks();
    }

    /** Forge 47 registers its key in the native key-mapping event, before client setup. */
    public static void initClientTicks() {
        ClientTickEvent.CLIENT_POST.register(minecraft -> {
            ComboClientState.tick();
            ComboDodgeClient.tick();
        });
    }
}
