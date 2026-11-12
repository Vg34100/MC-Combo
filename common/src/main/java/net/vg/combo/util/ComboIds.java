package net.vg.combo.util;

import net.minecraft.resources.Identifier;
import net.vg.combo.Combo;

/** Keeps the constructor boundary in one place without changing serialized IDs. */
public final class ComboIds {
    private ComboIds() {}

    public static Identifier id(String path) {
        //? if >=1.21 {
        return Identifier.fromNamespaceAndPath(Combo.MOD_ID, path);
        //? } else {
        /*return new Identifier(Combo.MOD_ID, path);
        *///? }
    }
}
