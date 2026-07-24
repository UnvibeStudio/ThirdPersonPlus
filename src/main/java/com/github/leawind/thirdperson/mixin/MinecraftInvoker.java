package com.github.leawind.thirdperson.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * In Minecraft 26.1 the picking routine that refreshes {@link Minecraft#hitResult} and {@link
 * Minecraft#crosshairPickEntity} moved from {@code GameRenderer#pick(float)} to a private {@code
 * Minecraft#pick(float)}.
 */
@Mixin(Minecraft.class)
public interface MinecraftInvoker {
  @Invoker("pick")
  void invokePick(float partialTick);
}
