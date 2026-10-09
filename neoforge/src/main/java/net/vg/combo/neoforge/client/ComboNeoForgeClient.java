package net.vg.combo.neoforge.client;

import net.vg.combo.Combo;
import net.vg.combo.client.ComboConfigScreen;
import net.vg.combo.client.ComboHud;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.minecraft.resources.Identifier;

@Mod(value = Combo.MOD_ID, dist = net.neoforged.api.distmarker.Dist.CLIENT)
public final class ComboNeoForgeClient {
    public ComboNeoForgeClient(IEventBus modEventBus, ModContainer modContainer) {
        ModLoadingContext.get().registerExtensionPoint(IConfigScreenFactory.class,
                () -> (container, parent) -> new ComboConfigScreen(parent));

        modEventBus.addListener(this::clientSetup);
        modEventBus.addListener(this::registerGuiLayers);
    }

    private void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(Combo::initClient);
    }

    private void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(Identifier.fromNamespaceAndPath(Combo.MOD_ID, "combo_hud"), ComboHud::render);
    }
}
