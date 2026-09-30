package net.vg.combo.client;

/**
 * Client-side mirror of the server's combo sync packet. Holds no gameplay authority;
 * a HUD renderer reads it purely for display. The server never sends an "expired" packet
 * (it only syncs on confirmed advancing hits), so {@link #tick()} decays the display locally.
 */
public final class ComboClientState {
    // The server sends remainingTicks=0 on a finisher sync (the combo resets immediately
    // server-side); give the HUD a short flash window so the finisher accent is actually visible.
    private static final int FINISHER_FLASH_TICKS = 15;

    private static volatile int stage;
    private static volatile int remainingTicks;
    private static volatile boolean finisher;

    private ComboClientState() {
    }

    public static void update(int stage, int remainingTicks, boolean finisher) {
        ComboClientState.stage = stage;
        ComboClientState.remainingTicks = finisher ? FINISHER_FLASH_TICKS : remainingTicks;
        ComboClientState.finisher = finisher;
    }

    public static void tick() {
        if (stage <= 0) {
            return;
        }
        if (remainingTicks <= 0) {
            stage = 0;
            finisher = false;
        } else {
            remainingTicks--;
        }
    }

    public static int stage() {
        return stage;
    }

    public static int remainingTicks() {
        return remainingTicks;
    }

    public static boolean finisher() {
        return finisher;
    }
}
