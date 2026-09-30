package net.vg.combo.neoforge.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.vg.combo.Combo;
import net.vg.combo.client.ComboDamagePopups;
import net.vg.combo.client.ComboStaggerEffects;

@EventBusSubscriber(modid = Combo.MOD_ID, value = Dist.CLIENT)
public final class ComboNeoForgeRenderHooks {
    private ComboNeoForgeRenderHooks() {
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent.AfterLevel event) {
        ComboDamagePopups.drainAndSpawn();
        ComboStaggerEffects.drainAndRender();
    }
}
