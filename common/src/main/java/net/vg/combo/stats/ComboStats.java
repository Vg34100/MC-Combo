package net.vg.combo.stats;

import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.stats.StatFormatter;
import net.minecraft.stats.Stats;
import net.vg.combo.Combo;

public final class ComboStats {
    private static final DeferredRegister<Identifier> CUSTOM_STATS =
            DeferredRegister.create(Combo.MOD_ID, Registries.CUSTOM_STAT);

    public static final RegistrySupplier<Identifier> HITS_LANDED =
            CUSTOM_STATS.register("hits_landed",
                    () -> Identifier.fromNamespaceAndPath(Combo.MOD_ID, "hits_landed"));

    public static final RegistrySupplier<Identifier> FINISHERS_LANDED =
            CUSTOM_STATS.register("finishers_landed",
                    () -> Identifier.fromNamespaceAndPath(Combo.MOD_ID, "finishers_landed"));

    public static final RegistrySupplier<Identifier> DAMAGE_DEALT =
            CUSTOM_STATS.register("damage_dealt",
                    () -> Identifier.fromNamespaceAndPath(Combo.MOD_ID, "damage_dealt"));

    private ComboStats() {
    }

    public static void register() {
        CUSTOM_STATS.register();
        LifecycleEvent.SETUP.register(ComboStats::registerFormatters);
    }

    private static void registerFormatters() {
        Stats.CUSTOM.get(HITS_LANDED.value(), StatFormatter.DEFAULT);
        Stats.CUSTOM.get(FINISHERS_LANDED.value(), StatFormatter.DEFAULT);
        Stats.CUSTOM.get(DAMAGE_DEALT.value(), StatFormatter.DEFAULT);
    }
}
