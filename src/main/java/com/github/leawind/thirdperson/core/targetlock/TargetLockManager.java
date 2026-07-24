package com.github.leawind.thirdperson.core.targetlock;

import com.github.leawind.thirdperson.ThirdPerson;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Soft target lock.
 *
 * <p>"Soft" means the camera is never taken away from the player: locking only makes the player's
 * head and aim follow the target (through the existing interest-point rotation) and highlights it.
 *
 * <p>Interaction model: pressing the lock key locks the best candidate in the cone; pressing it
 * again cycles to the next best one and clears the lock once there is nothing else to cycle to.
 * Holding the key clears immediately. The lock is also dropped automatically when the target dies,
 * leaves the configured range, or stays out of sight for too long.
 */
public final class TargetLockManager {
  private static @Nullable LivingEntity target = null;
  private static int lostSightTicks = 0;

  private TargetLockManager() {}

  public static boolean isLocked() {
    return target != null;
  }

  public static @Nullable LivingEntity getTarget() {
    return target;
  }

  public static boolean isTarget(@Nullable Entity entity) {
    return entity != null && entity == target;
  }

  /** The point the player should look at while locked, or {@code null} when not locked. */
  public static @Nullable Vec3 getInterestPoint() {
    var current = target;
    return current == null ? null : current.getEyePosition();
  }

  /** Lock the best candidate, or cycle to the next one when already locked. */
  public static void onLockKeyPressed() {
    if (!ThirdPerson.getConfig().target_lock_enabled || !ThirdPerson.isAvailable()) {
      return;
    }
    if (target == null) {
      setTarget(TargetSelector.selectBest(null));
    } else {
      // Cycle: pick the best candidate that is not the current one. Running out of candidates
      // releases the lock, which doubles as a quick way to unlock.
      setTarget(TargetSelector.selectBest(target));
    }
  }

  public static void clear() {
    setTarget(null);
  }

  private static void setTarget(@Nullable LivingEntity newTarget) {
    target = newTarget;
    lostSightTicks = 0;
  }

  /** Drops the lock when the target is no longer a legal or reachable target. */
  public static void tick() {
    var current = target;
    if (current == null) {
      return;
    }
    var minecraft = Minecraft.getInstance();
    var player = minecraft.player;
    var level = minecraft.level;
    if (player == null || level == null || current.isRemoved() || !current.isAlive()) {
      clear();
      return;
    }
    var config = ThirdPerson.getConfig();
    if (!TargetSelector.isEligible(player, current)) {
      clear();
      return;
    }

    // A little hysteresis so the lock does not drop while fighting right at the edge of the range.
    double maxDistance = config.target_lock_max_distance * 1.25;
    if (player.distanceTo(current) > maxDistance) {
      clear();
      return;
    }

    if (config.target_lock_require_line_of_sight) {
      var origin = ThirdPerson.CAMERA_AGENT.getRawCamera().position();
      if (TargetSelector.hasLineOfSight(level, player, origin, current)) {
        lostSightTicks = 0;
      } else if (++lostSightTicks > config.target_lock_lost_sight_grace_ticks) {
        clear();
      }
    }
  }
}
