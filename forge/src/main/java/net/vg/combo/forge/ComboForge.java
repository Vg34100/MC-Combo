package net.vg.combo.forge;

import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.vg.combo.Combo;

@Mod(Combo.MOD_ID)
public final class ComboForge {
    public ComboForge() {
        EventBuses.registerModEventBus(Combo.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        Combo.init();
    }
}
