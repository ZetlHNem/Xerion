# Xerion

A modular **Minecraft 1.20.1 Forge** mod focused on PvP combat, movement,
visual and world utility modules.

Java 17 · Forge 47.x

> Xerion —— Minecraft 1.20.1 Forge 模组：以 PvP 战斗、移动、视觉与世界功能模块为核心，
> 配套自研游戏内面板 UI。

## Feature modules

Every feature is an independent module with its own settings and hotkey binding; module
state and settings are persisted through the config store.

| Category | Modules |
| --- | --- |
| Combat | Kill Aura, Criticals, Knockback, WTap, Reach, Backtrack, Fake Lag, Velocity, Auto Crystal, Bed Aura, Anchor Aura, Web Aura, Auto Trap, Surround, Hole Fill |
| Movement | Speed, Flight, Elytra Fly, Step, Long Jump, High Jump, Jesus, Spider, Scaffold, Blink, Stuck, No Slow, Safe Walk |
| Visual / ESP | ESP, Xray, Chams, Freecam, Zoom, Fullbright, No Fog, Time Changer, View Model |
| World | Auto Mine, Nuker, Vein Miner, Breaker, Fast Break, Fast Web |
| Player | Auto Totem, Auto Armor, Auto Heal, Auto Pearl, Auto EXP, Auto Weapon, Chest Steal, Inventory Cleaner |
| Compatibility | Compatibility, Disabler, Ping Spoof, No Rotate |

## Components

- **In-game dashboard** — immediate-mode UI for browsing modules, editing settings and
  managing profiles, rendered with custom shader primitives.
- **Rendering pipeline** — shared render abstractions and custom shaders for overlays,
  glow, chams and post-processing.
- **Mixin layer** — a single Forge mixin package wires the modules into client, network
  and render entry points (1.20.1 SRG member names).
- **Loader-agnostic core** — `common` contains no Minecraft or Forge imports; the Forge
  module translates platform events into the shared event model.

## Repository layout

| Path | Purpose |
| --- | --- |
| `common/` | Loader-free core: modules, events, render abstractions, state, config models |
| `forge/` | Forge `@Mod` entry point, event adapters, mixins, client implementation |
| `docs/` | Architecture reports and development notes |
| `tools/` | Font / atlas generation helpers used by the UI |

## Building

Requires JDK 17 and a Forge 1.20.1 development environment.

```bash
./gradlew :forge:build
```

The build produces `forge/build/libs/xerion-forge-<version>.jar`; drop it into
`.minecraft/mods` of a 1.20.1 Forge 47.x instance.

## Development notes

- `common` intentionally has no Minecraft or Forge imports; platform events are
  translated by the Forge adapter and rendering is exposed through `RenderContext`.
- Mixin targets use 1.20.1 SRG member names (`m_…_` / `f_…_`) because the mod is built
  for the production runtime.
- See `docs/` for the architecture, migration and naming reports.
