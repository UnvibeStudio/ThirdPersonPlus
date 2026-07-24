# AGENTS.md — contributor & agent guide

This file orients AI agents and human contributors working on **Third Person Plus**.

## What this project is

Third Person Plus is a **fork of [Leawind's Third Person](https://github.com/Leawind/Third-Person)**
(MIT), ported to **Minecraft 26.1+** on **Fabric**, with a new **soft target lock** feature.

The upstream mod was an Architectury multi-loader project (Fabric + NeoForge). This fork is
**Fabric-only, a single Gradle module** built with the **official `net.fabricmc.fabric-loom`** plugin.
Architectury was removed entirely (it was only ever a multi-loader abstraction).

Preserve the MIT attribution to Leawind (`LICENSE.txt`, `NOTICE.md`). The Java package
`com.github.leawind.thirdperson` is kept from upstream to minimise churn — do **not** mass-rename it.

## Minecraft 26.1 toolchain rules (important)

Minecraft 26.1 is the **first unobfuscated** release. This changes the build fundamentally:

- **No mappings.** There are no Mojang obfuscation maps and no working `intermediary` for 26.1. The
  build declares **no `mappings`** and uses `minecraft "com.mojang:minecraft:<version>"`.
- Class/field names are the **real Mojang names** (e.g. `MouseHandler`, `LocalPlayer`, `CameraType`,
  `Entity#yBodyRot`; note `ResourceLocation` is named **`Identifier`** in these mappings).
- Use official **`net.fabricmc.fabric-loom`** (see `loom_version` in `gradle.properties`).
- Dependencies use plain **`implementation`** (not `modImplementation`); the final artifact is the
  **`jar`** task (there is no `remapJar`).
- **Java 25** and **Gradle 9.x** are required. Reference: <https://fabricmc.net/2026/03/14/261.html>.

When bumping Minecraft, update `minecraft_version*`, `loader_version`, `fabric_api_version`,
`cloth_config_api_version`, `modmenu_version` and `loom_version` in `gradle.properties`.

## Project layout

```
build.gradle / settings.gradle / gradle.properties   # single-module Fabric + official loom
src/main/java/com/github/leawind/thirdperson/
  ThirdPerson.java            # entrypoint logic (init)
  ThirdPersonEvents.java      # event wiring (Fabric API events + internal GameEvents)
  ThirdPersonKeys.java        # keybind registration (KeyBindingHelper)
  ThirdPersonResources.java   # resource reload listener registration
  ThirdPersonConstants.java   # MOD_ID and tuning constants
  ThirdPersonStatus.java      # per-frame/per-tick state
  core/                       # camera + rotation engine
    CameraAgent.java          #   camera position/rotation + smoothing
    EntityAgent.java          #   player body/head rotation, movement
    cameraoffset/             #   over-the-shoulder / overhead offset scheme
    rotation/                 #   rotate targets & strategies (interest point, etc.)
    targetlock/               #   SOFT TARGET LOCK lives here (TargetSelector, TargetLockManager)
  mixin/                      # all mixins (see mixin config below)
  api/base/GameEvents.java    # internal event hooks fired from mixins
  api/client/event/           # internal event payload types
  config/, screen/            # Config + Cloth Config screen
  util/                       # math, smoothing (util/math/smoothvalue/Exp*)
  fabric/                     # Fabric entrypoints (ThirdPersonFabric, ModMenuEntry)
src/main/java/io/github/leawind/inventory/   # vendored MIT helpers (Lazy, EventEmitter)
src/main/resources/
  fabric.mod.json
  thirdpersonplus.mixins.json         # fabric-package mixins
  thirdpersonplus-common.mixins.json  # main mixins (client list)
  assets/thirdpersonplus/, assets/minecraft/lang/
```

## How events work

Most hooks are the mod's own `GameEvents` fields, assigned in `ThirdPersonEvents.register()` and
**fired from mixins** (camera setup, render tick, move impulse, entity/mouse turn, mouse scroll).
Only a few use **Fabric API** directly:

- client tick → `ClientTickEvents.START_CLIENT_TICK`
- client stopping → `ClientLifecycleEvents.CLIENT_STOPPING`
- player join / respawn / dimension change → `ClientEntityEvents.ENTITY_LOAD` (filtered to `LocalPlayer`)
- keybinds → `KeyMappingHelper.registerKeyMapping` (26.1 renamed the "key binding" API to "key mapping")
- resource reload → `ResourceManagerHelper` + `IdentifiableResourceReloadListener`
- mod-loaded checks → `FabricLoader.getInstance().isModLoaded(...)`
- mouse scroll → `MouseHandlerMixin#preScroll` fires `GameEvents.mouseScroll`

## Soft target lock

Lives in `core/targetlock/`:
- `TargetSelector` — scans a cone from the camera toward the crosshair and scores candidates
  (angle to cone axis, distance, current/aggressor/boss bonuses, ...). The scoring is a weighted sum
  of named criteria so new criteria can be added.
- `TargetLockManager` — holds the lock state, handles press (lock / cycle) and hold (release),
  validates the target every client tick, and drives the head/aim through the existing rotation
  **interest point** system, so the camera stays free (that is what makes the lock "soft").

Integration points: `EntityAgent#getInterestPoint` returns the locked target, `RotateStrategy` gains a
`target_lock` factor that selects `INTEREST_POINT` rotation, `EntityMixin` forces
`Entity#isCurrentlyGlowing` for the target (client-side highlight, no server involvement), and
`ThirdPersonEvents#onClientTickPre` calls `TargetLockManager.tick()`.

Not implemented yet: an on-screen target marker/reticle (the highlight is the glow outline only).

## Adding a mixin

Add the class under `mixin/` and list it in `thirdpersonplus-common.mixins.json` (`client` array).
Names are unobfuscated Mojang names; prefer reusing existing mixins over adding new ones.
MixinExtras annotations (`@WrapOperation`, `@ModifyExpressionValue`) are available (bundled by loader).

## Commands

```bash
./gradlew build       # build the mod jar into build/libs/
./gradlew runClient   # launch a dev client
```

## Before a release

- `./gradlew build` is green on JDK 25.
- Version bumped (`mod_version` in `gradle.properties`, or pass `-Pmod_version=` — the release CI does
  this from the git tag).
- Smoke-test `runClient`: third-person camera, standing free-look, camera-relative movement, target lock.
- Tag `vX.Y.Z` and push → `.github/workflows/release.yml` builds and publishes the GitHub Release.
