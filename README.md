<div align="center">

# Third Person Plus

**An MMORPG-style third-person camera for Minecraft 26.1+ (Fabric).**

[![License: MIT](https://img.shields.io/badge/license-MIT-blue.svg)](./LICENSE.txt)
[![Minecraft](https://img.shields.io/badge/Minecraft-26.1%2B-62B47A.svg)](https://www.minecraft.net/)
[![Loader](https://img.shields.io/badge/loader-Fabric-DBD0B4.svg)](https://fabricmc.net/)

</div>

> **Third Person Plus is a fork of [Leawind's Third Person](https://github.com/Leawind/Third-Person) (MIT).**
> It ports the mod to Minecraft **26.1+** and adds a **soft target lock**. Full credit for the original
> camera engine goes to Leawind — see [Credits](#credits).

Third Person Plus gives Minecraft a camera like the one in modern action MMORPGs (The Elder Scrolls
Online, Guild Wars 2): an over-the-shoulder view that you can pull back to a bird's-eye follow-cam or
push all the way in to first person, with smooth body and head movement.

## Features

- **Client-side only** — no server installation required; works on any server.
- **Over-the-shoulder MMORPG camera** — aim from behind the shoulder up close, pull the camera back
  (hold `Z` + mouse wheel) to a high follow view, or push in to first person.
- **Free rotation while standing** — rotate the view freely without turning the player's body.
- **Camera-relative movement** — the player moves relative to where the camera looks; the body turns
  smoothly toward the movement direction and the head tracks the aim (looking backwards turns the body
  to face forward instead).
- **Smooth everything** — body rotation, head rotation, camera position and perspective transitions are
  all exponentially smoothed.
- **Free camera adjustment** — hold `Z` and move the mouse to shift the on-screen offset; the mouse
  wheel changes the distance. Tap `CapsLock` to switch shoulders (left / right); hold to center.
- **Soft target lock** *(new in Third Person Plus)* — press the lock key to softly lock onto the best
  entity in a cone in front of the crosshair. Your head and aim smoothly track the target and it gets
  a glowing outline, while the camera stays fully under your control. Press again to cycle to the next
  target (cycling past the last one releases the lock), or hold the key to release it immediately. The
  lock clears itself when the target dies, leaves range, or stays out of sight. Candidates are ranked
  by a configurable weighted score (aim accuracy, proximity, current target, whether it is attacking
  you, bosses and hostiles).
- **Smart aim mode** — automatically switches to an aiming offset based on the item in use
  (customizable, so it works with items from other mods).
- **Disable anytime** — toggle the mod off in-game to revert to the vanilla third-person view.

## Requirements

| Dependency | Notes |
|---|---|
| Minecraft | 26.1+ |
| Fabric Loader | 0.19.3+ |
| [Fabric API](https://modrinth.com/mod/fabric-api) | required |
| [Cloth Config](https://modrinth.com/mod/cloth-config) | optional — enables the settings screen |
| [Mod Menu](https://modrinth.com/mod/modmenu) | optional — opens the settings screen from the mods list |

## Installation

1. Install Fabric Loader for Minecraft 26.1+.
2. Download the latest `thirdpersonplus-*.jar` from the
   [Releases](https://github.com/UnvibeStudio/ThirdPersonPlus/releases) page.
3. Drop it into your `mods/` folder together with Fabric API
   (and, optionally, Cloth Config + Mod Menu).

## Default keybinds

All keybinds are configurable in *Options → Controls* under the **Third Person Plus** category.

| Action | Default |
|---|---|
| Adjust camera position (hold) + mouse / wheel | `Z` |
| Switch camera side (tap) / center (hold) | `Caps Lock` |
| Force aiming (hold) | unbound |
| Toggle aiming | unbound |
| Toggle mod on/off | unbound |
| Open configuration menu | unbound |
| Soft target lock / cycle target | unbound |

## Configuration

If Cloth Config (and optionally Mod Menu) is installed, open the settings via **Mods → Third Person
Plus → Configure**, or with the *Open configuration menu* keybind. Settings are saved to
`config/thirdpersonplus.json`. You can tune camera distance and offsets, smoothing strength, aiming
rules, and the soft-target-lock behavior (range, cone angle, target filters, highlight and marker).

## Building from source

Requires **JDK 25**.

```bash
./gradlew build
```

The built mod jar is written to `build/libs/`.
To launch a development client: `./gradlew runClient`.

## Releases

Tagged releases are built and published automatically by GitHub Actions. Push a tag like `v0.1.0` and
the [release workflow](.github/workflows/release.yml) builds the Fabric jar and attaches it to a
GitHub Release.

## Credits

Third Person Plus is a fork of **[Leawind's Third Person](https://github.com/Leawind/Third-Person)** by
**Leawind**, used under the MIT License. The core third-person camera, smoothing and rotation systems
are their work. See [`NOTICE.md`](./NOTICE.md) for details.

## License

[MIT](./LICENSE.txt) — © 2023–2024 Leawind, © 2026 UnvibeStudio (modifications).
