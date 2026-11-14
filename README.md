# Xerion

A Minecraft **1.20.1 Forge** mod built around a custom in-game dashboard, a
lightweight rendering pipeline and a modular feature framework.

Java 17 · Forge 47.x

> Xerion —— Minecraft 1.20.1 Forge 模组：自研游戏内面板 UI、渲染管线与模块化功能框架。

## Highlights

- **In-game dashboard** — an immediate-mode UI layer with its own layout, animation,
  popup and HUD widgets, drawn through custom shader primitives instead of vanilla
  screens.
- **Rendering pipeline** — shared render abstractions plus custom shaders for
  overlays, glow, chams and post-processing effects.
- **Modular feature framework** — features are registered as modules with their own
  settings, persisted through a profile/config store.
- **Mixin integration layer** — a single Forge mixin package wires the mod into
  client, network and render entry points (1.20.1 SRG member names).
- **Loader-agnostic core** — `common` contains no Minecraft or Forge imports; the
  Forge module translates platform events into the shared event model.

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
- Mixin targets use 1.20.1 SRG member names (`m_…_` / `f_…_`) because the mod is
  built for the production runtime.
- See `docs/` for the architecture, migration and naming reports.
