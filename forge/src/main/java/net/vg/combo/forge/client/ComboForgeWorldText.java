package net.vg.combo.forge.client;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.vg.combo.Combo;
import net.vg.combo.client.ComboWorldText;

@Mod.EventBusSubscriber(modid = Combo.MOD_ID, value = Dist.CLIENT)
public final class ComboForgeWorldText {
    private ComboForgeWorldText() {}

    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
            ComboWorldText.renderFrame(event.getPoseStack(), event.getCamera(),
                    Minecraft.getInstance().renderBuffers().bufferSource());
        }
    }
}
