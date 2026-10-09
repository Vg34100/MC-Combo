package net.vg.combo.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.architectury.networking.NetworkManager;
import net.minecraft.client.KeyMapping;
import net.vg.combo.network.ComboDodgeRequestPayload;

/** Owns the dodge keybind. Cost/cooldown are enforced server-side; this just forwards the request. */
public final class ComboDodgeClient {
    //? if >=26.1 {
    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.GAMEPLAY;
    //? } else {
    /*private static final String CATEGORY = KeyMapping.CATEGORY_GAMEPLAY;
    *///? }
    public static final KeyMapping KEY_BINDING = new KeyMapping(
            "key.combo.dodge", InputConstants.KEY_V, CATEGORY);

    private ComboDodgeClient() {
    }

    public static void tick() {
        while (KEY_BINDING.consumeClick()) {
            //? if >=26.1 {
            NetworkManager.sendToServer(new ComboDodgeRequestPayload());
            //? } else {
            /*net.minecraft.client.Minecraft minecraft = net.minecraft.client.Minecraft.getInstance();
            if (minecraft.player != null) {
                NetworkManager.sendToServer(new ComboDodgeRequestPayload(
                        minecraft.player.input.leftImpulse, minecraft.player.input.forwardImpulse));
            }
            *///? }
        }
    }
}
