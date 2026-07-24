package com.github.leawind.thirdperson.api.base;

import com.github.leawind.thirdperson.api.client.event.CalculateMoveImpulseEvent;
import com.github.leawind.thirdperson.api.client.event.EntityTurnStartEvent;
import com.github.leawind.thirdperson.api.client.event.MouseTurnPlayerStartEvent;
import com.github.leawind.thirdperson.api.client.event.RenderEntityEvent;
import com.github.leawind.thirdperson.api.client.event.RenderTickStartEvent;
import com.github.leawind.thirdperson.api.client.event.ThirdPersonCameraSetupEvent;
import java.util.function.Consumer;
import java.util.function.DoublePredicate;
import java.util.function.Function;

public final class GameEvents {
  public static Consumer<ThirdPersonCameraSetupEvent> thirdPersonCameraSetup = null;
  public static Consumer<RenderTickStartEvent> renderTickStart = null;
  public static Consumer<CalculateMoveImpulseEvent> calculateMoveImpulse = null;
  public static Function<RenderEntityEvent, Boolean> renderEntity = null;
  public static Runnable handleKeybindsStart = null;
  public static Consumer<MouseTurnPlayerStartEvent> mouseTurnPlayerStart = null;
  public static Consumer<EntityTurnStartEvent> entityTurnStart = null;

  /**
   * Fired before the vanilla mouse-scroll handling. Receives the vertical scroll amount and returns
   * {@code true} if the default (hotbar) handling should be cancelled.
   */
  public static DoublePredicate mouseScroll = null;
}
