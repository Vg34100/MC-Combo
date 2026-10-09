package net.vg.combo.combat;

import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.vg.combo.config.ComboConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * A directional dash with brief total damage immunity (via {@code Entity#setInvulnerable}, the
 * same flag creative players/spawn protection use), gated by a flat stamina cost and its own
 * cooldown independent of stamina (so full stamina alone can't turn it into a button mash).
 */
public final class ComboDodge {
    private static final Map<UUID, Long> LAST_DODGE_TICK = new HashMap<>();
    private static final Map<UUID, Long> INVULNERABLE_UNTIL_TICK = new HashMap<>();

    private ComboDodge() {
    }

    public static boolean attemptDodge(ServerPlayer player, ServerLevel level, long gameTime
            //? if <26.1 {
            /*, float left, float forward
            *///? }
    ) {
        //? if <26.1 {
        /*// Reject invalid input before spending stamina or applying movement.
        if (!Float.isFinite(left) || !Float.isFinite(forward)
                || Math.abs(left) > 1.0F || Math.abs(forward) > 1.0F) {
            return false;
        }
        *///? }
        Long lastDodge = LAST_DODGE_TICK.get(player.getUUID());
        if (lastDodge != null && gameTime - lastDodge < ComboConfig.dodgeCooldownTicks) {
            return false;
        }
        if (!ComboStamina.consume(player, (float) ComboConfig.staminaDodgeCost)) {
            return false;
        }

        LAST_DODGE_TICK.put(player.getUUID(), gameTime);

        // getDeltaMovement() doesn't reflect real WASD state for a player server-side (movement is
        // packet/position-driven, not physics-simulated the way it is for mobs — confirmed via
        // bytecode, this was why every dodge fell through to the "no input" branch below).
        // getLastClientMoveIntent() is the real held-key direction, already rotated into world
        // space by the player's yaw (the same getInputVector() routine vanilla's own movement uses).
        //? if >=26.1 {
        Vec3 moveIntent = player.getLastClientMoveIntent();
        //? } else {
        /*// Legacy vanilla forwards WASD to the server only while riding.
        // Rotate the explicit local input by server yaw, then normalize below.
        Vec3 moveIntent = new Vec3(left, 0.0D, forward).yRot(-player.getYRot() * (float) (Math.PI / 180.0D));
        *///? }
        Vec3 direction;
        if (moveIntent.lengthSqr() > 1.0E-4) {
            direction = moveIntent.normalize();
        } else {
            Vec3 look = player.getLookAngle();
            Vec3 horizontalLook = new Vec3(look.x, 0.0D, look.z);
            direction = horizontalLook.lengthSqr() > 1.0E-4
                    ? horizontalLook.normalize().scale(-1.0D)
                    : new Vec3(0.0D, 0.0D, 1.0D);
        }

        player.push(direction.x * ComboConfig.dodgeStrength, 0.1D, direction.z * ComboConfig.dodgeStrength);
        // The standard entity-tracking sync deliberately doesn't send a player motion updates about
        // their own entity (the client is assumed authoritative over its own movement), so a
        // server-side push() alone never reaches the controlling client. Forcing it through
        // explicitly is the same mechanism vanilla uses for explosion/knockback-on-self.
        player.connection.send(new ClientboundSetEntityMotionPacket(player));

        player.setInvulnerable(true);
        INVULNERABLE_UNTIL_TICK.put(player.getUUID(), gameTime + ComboConfig.dodgeInvulnerabilityTicks);

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0F, 1.5F);

        return true;
    }

    /** Clears the invulnerability window once it expires. Called every server tick per player. */
    public static void tick(ServerPlayer player, long gameTime) {
        Long until = INVULNERABLE_UNTIL_TICK.get(player.getUUID());
        if (until != null && gameTime >= until) {
            player.setInvulnerable(false);
            INVULNERABLE_UNTIL_TICK.remove(player.getUUID());
        }
    }

    public static void clear(Player player) {
        UUID id = player.getUUID();
        LAST_DODGE_TICK.remove(id);
        if (INVULNERABLE_UNTIL_TICK.remove(id) != null && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.setInvulnerable(false);
        }
    }
}
