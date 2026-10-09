package net.vg.combo.neoforge;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.vg.combo.Combo;

@Mod(Combo.MOD_ID)
public final class ComboNeoForge {
    public ComboNeoForge(IEventBus modEventBus) {
        Combo.init();
    }
}
