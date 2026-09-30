package net.vg.combo.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.architectury.networking.NetworkManager;
import net.minecraft.client.KeyMapping;
import net.vg.combo.network.ComboDodgeRequestPayload;

/** Owns the dodge keybind. Cost/cooldown are enforced server-side; this just forwards the request. */
public final class ComboDodgeClient {
    public static final KeyMapping KEY_BINDING = new KeyMapping(
            "key.combo.dodge", InputConstants.KEY_V, KeyMapping.Category.GAMEPLAY);

    private ComboDodgeClient() {
    }

    public static void tick() {
        while (KEY_BINDING.consumeClick()) {
            NetworkManager.sendToServer(new ComboDodgeRequestPayload());
        }
    }
}
