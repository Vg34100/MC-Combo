package net.vg.combo.fabric.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.vg.combo.client.ComboConfigScreen;

public final class ComboModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ComboConfigScreen::new;
    }
}
