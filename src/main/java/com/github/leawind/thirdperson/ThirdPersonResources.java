package com.github.leawind.thirdperson;

import com.github.leawind.thirdperson.resources.ItemPredicateManager;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.server.packs.PackType;

/** 自定义资源包 */
public final class ThirdPersonResources {
  public static final ItemPredicateManager itemPredicateManager = new ItemPredicateManager();

  public static void register() {
    ResourceManagerHelper.get(PackType.CLIENT_RESOURCES)
        .registerReloadListener(ThirdPersonResources.itemPredicateManager);
  }
}
