package com.github.leawind.thirdperson.mixin;

import com.github.leawind.thirdperson.ThirdPerson;
import com.github.leawind.thirdperson.ThirdPersonStatus;
import com.github.leawind.thirdperson.api.base.GameEvents;
import com.github.leawind.thirdperson.api.client.event.ThirdPersonCameraSetupEvent;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("unused")
@Mixin(value = Camera.class, priority = 2000)
public class CameraMixin {
  /**
   * Override the camera placement after vanilla has positioned it.
   *
   * <p>Minecraft 26.1 removed {@code Camera#setup}; the camera is now positioned in {@code
   * Camera#alignWithEntity(float)} (called from {@code Camera#update}), and the old {@code
   * Camera#move(FFF)} call sites are gone. Injecting at the tail lets vanilla finish its own setup
   * and then simply replaces the position/rotation when the mod is driving the camera.
   *
   * <p>Call path: {@code GameRenderer#render -> Camera#update -> Camera#alignWithEntity}
   */
  @Inject(method = "alignWithEntity", at = @At("TAIL"))
  private void afterAlignWithEntity(float partialTick, CallbackInfo ci) {
    if (GameEvents.thirdPersonCameraSetup != null) {
      var event = new ThirdPersonCameraSetupEvent(partialTick);
      GameEvents.thirdPersonCameraSetup.accept(event);
      if (event.set()) {
        var camera = (Camera) (Object) this;
        ((CameraInvoker) camera).invokeSetPosition(event.pos);
        ((CameraInvoker) camera).invokeSetRotation(event.yRot, event.xRot);
      }
    }
  }

  /**
   * Apply the mod's smooth FOV divisor.
   *
   * <p>In 26.1 the field of view is computed by {@code Camera#calculateFov} and stored on the
   * camera, so modifying its return value affects both rendering and {@link Camera#getFov()}.
   */
  @ModifyReturnValue(method = "calculateFov", at = @At("RETURN"))
  private float modifyFov(float fov) {
    var camera = (Camera) (Object) this;
    if (!camera.isPanoramicMode()
        && ThirdPerson.isAvailable()
        && ThirdPersonStatus.isRenderingInThirdPerson()) {
      fov /= (float) ThirdPerson.CAMERA_AGENT.getSmoothFovDivisor();
    }
    return fov;
  }
}
