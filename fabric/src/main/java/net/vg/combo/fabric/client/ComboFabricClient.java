package net.vg.combo.fabric.client;

import net.fabricmc.api.ClientModInitializer;
//? if >=26.1 {
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
//? } else {
/*import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.vg.combo.client.ComboWorldText;
*///? }
import net.minecraft.resources.Identifier;
import net.vg.combo.Combo;
import net.vg.combo.client.ComboDamagePopups;
import net.vg.combo.client.ComboHud;
import net.vg.combo.client.ComboStaggerEffects;

public final class ComboFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Combo.initClient();
        //? if >=26.1 {
        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(Combo.MOD_ID, "combo_hud"), ComboHud::render);
        LevelRenderEvents.BEFORE_GIZMOS.register(ctx -> {
            ComboDamagePopups.drainAndSpawn();
            ComboStaggerEffects.drainAndRender();
        });
        //? } else {
        /*HudRenderCallback.EVENT.register(ComboHud::render);
        WorldRenderEvents.AFTER_ENTITIES.register(ctx ->
                ComboWorldText.renderFrame(ctx.matrixStack(), ctx.camera(), ctx.consumers()));
        *///? }
    }
}
