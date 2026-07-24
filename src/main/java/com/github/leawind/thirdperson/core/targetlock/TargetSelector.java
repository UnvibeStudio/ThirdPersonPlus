package com.github.leawind.thirdperson.core.targetlock;

import com.github.leawind.thirdperson.ThirdPerson;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Picks the best soft-lock candidate inside a cone that starts at the camera and points along the
 * crosshair direction.
 *
 * <p>Candidates are ranked by a weighted sum of named criteria, so new criteria can be added without
 * restructuring the selection. All weights are configurable.
 */
public final class TargetSelector {
  private TargetSelector() {}

  /**
   * @param exclude an entity that must not be returned, used to cycle to the "next" target
   * @return the best candidate, or {@code null} if the cone is empty
   */
  public static @Nullable LivingEntity selectBest(@Nullable LivingEntity exclude) {
    var minecraft = Minecraft.getInstance();
    var player = minecraft.player;
    var level = minecraft.level;
    if (player == null || level == null) {
      return null;
    }
    var config = ThirdPerson.getConfig();
    if (!config.target_lock_enabled) {
      return null;
    }

    final double maxDistance = config.target_lock_max_distance;
    final double coneAngle = Math.max(1, config.target_lock_cone_angle);

    // The cone starts at the camera and follows the crosshair, so what the player sees is what gets
    // locked, independent of where the body happens to face.
    var camera = ThirdPerson.CAMERA_AGENT.getRawCamera();
    var origin = camera.position();
    var forward = camera.forwardVector();
    var axis = new Vec3(forward.x(), forward.y(), forward.z());
    if (axis.lengthSqr() < 1.0E-8) {
      return null;
    }
    axis = axis.normalize();

    var searchBox = player.getBoundingBox().inflate(maxDistance);
    var candidates =
        level.getEntitiesOfClass(
            LivingEntity.class, searchBox, candidate -> isEligible(player, candidate));

    LivingEntity best = null;
    double bestScore = Double.NEGATIVE_INFINITY;
    for (var candidate : candidates) {
      if (candidate == exclude) {
        continue;
      }
      var toTarget = candidate.getEyePosition().subtract(origin);
      double distance = toTarget.length();
      if (distance < 1.0E-4 || distance > maxDistance) {
        continue;
      }
      double cos = toTarget.scale(1 / distance).dot(axis);
      double angle = Math.toDegrees(Math.acos(Math.clamp(cos, -1.0, 1.0)));
      if (angle > coneAngle) {
        continue;
      }
      if (config.target_lock_require_line_of_sight
          && !hasLineOfSight(level, player, origin, candidate)) {
        continue;
      }

      double score =
          config.target_lock_weight_angle * (1 - angle / coneAngle)
              + config.target_lock_weight_distance * (1 - distance / maxDistance);
      if (candidate == TargetLockManager.getTarget()) {
        // Keeps the current target sticky so the selection does not flicker between two entities.
        score += config.target_lock_bonus_current;
      }
      if (isAggressor(candidate, player)) {
        score += config.target_lock_bonus_aggressor;
      }
      if (isBoss(candidate)) {
        score += config.target_lock_bonus_boss;
      }
      if (candidate instanceof Enemy) {
        score += config.target_lock_bonus_hostile;
      }

      if (score > bestScore) {
        bestScore = score;
        best = candidate;
      }
    }
    return best;
  }

  /** Whether an entity may be locked on to at all, according to the configured filters. */
  public static boolean isEligible(@NotNull Player player, @Nullable LivingEntity candidate) {
    if (candidate == null || candidate == player || !candidate.isAlive() || candidate.isSpectator()) {
      return false;
    }
    var config = ThirdPerson.getConfig();
    if (candidate instanceof Player) {
      if (!config.target_lock_include_players) {
        return false;
      }
    } else if (!config.target_lock_include_passive && !(candidate instanceof Enemy)) {
      return false;
    }
    return !config.target_lock_exclude_allies || !player.isAlliedTo(candidate);
  }

  static boolean hasLineOfSight(
      @NotNull Level level, @NotNull Player player, @NotNull Vec3 origin, @NotNull Entity target) {
    var targetEyes = target.getEyePosition();
    var hit =
        level.clip(
            new ClipContext(
                origin, targetEyes, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, player));
    return hit.getType() == HitResult.Type.MISS
        || hit.getLocation().distanceToSqr(targetEyes) < 1.0;
  }

  /** The candidate is currently attacking the player. */
  private static boolean isAggressor(@NotNull LivingEntity candidate, @NotNull Player player) {
    return candidate instanceof Mob mob && mob.getTarget() == player;
  }

  private static boolean isBoss(@NotNull LivingEntity candidate) {
    return candidate instanceof EnderDragon || candidate instanceof WitherBoss;
  }
}
