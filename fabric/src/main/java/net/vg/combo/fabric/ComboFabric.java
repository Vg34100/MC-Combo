package net.vg.combo.fabric;

import net.vg.combo.Combo;
import net.fabricmc.api.ModInitializer;

public final class ComboFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        Combo.init();
    }
}
