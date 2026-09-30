package net.vg.combo.combat;

final class ComboState {
    int stage;
    long lastConfirmedTick;
    // 0 = not armed. Set by a successful parry; the next landed hit before this tick is
    // auto-upgraded to a finisher regardless of the current combo stage.
    long parryFinisherExpiresTick;
}
