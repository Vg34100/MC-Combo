package net.vg.combo.forge.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.vg.combo.Combo;
import net.vg.combo.client.ComboConfigScreen;
import net.vg.combo.client.ComboHud;
import net.vg.combo.client.ComboDodgeClient;

@Mod.EventBusSubscriber(modid = Combo.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ComboForgeClient {
    private ComboForgeClient() {}

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory((minecraft, parent) -> new ComboConfigScreen(parent)));
        event.enqueueWork(Combo::initClientTicks);
    }

    @SubscribeEvent
    public static void keyMappings(RegisterKeyMappingsEvent event) {
        event.register(ComboDodgeClient.KEY_BINDING);
    }

    @SubscribeEvent
    public static void overlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("combo_hud", (gui, graphics, partialTick, width, height) -> ComboHud.render(graphics, partialTick));
    }
}
