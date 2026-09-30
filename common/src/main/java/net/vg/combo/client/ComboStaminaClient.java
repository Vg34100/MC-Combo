package net.vg.combo.client;

/** Client-side mirror of the server's stamina value, purely for HUD display. */
public final class ComboStaminaClient {
    private static volatile float current;
    private static volatile float max = 1.0F;

    private ComboStaminaClient() {
    }

    public static void update(float current, float max) {
        ComboStaminaClient.current = current;
        ComboStaminaClient.max = max;
    }

    public static float current() {
        return current;
    }

    public static float max() {
        return max;
    }
}
