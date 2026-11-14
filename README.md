# Xerion

Java 17 Minecraft mod for Forge.

## Architecture

- `common`: loader-free core logic. It owns modules, events, render abstractions, state, and config models.
- `forge`: Forge `@Mod` entrypoint and event adapters.

`common` intentionally does not import any Minecraft or Forge classes. The Forge module translates platform events into common events and exposes platform-specific rendering through `RenderContext`.

## IntelliJ IDEA

Open this directory as a Gradle project:

```text
F:\IDEA Projects\Xerion
```

After Gradle import, use the generated loader launch tasks/configurations. If IDEA does not show them immediately, run Gradle sync and then execute the relevant launch task once from the Gradle tool window.

## Event Forwarding

Loader callbacks are translated as follows:

- Forge tick callback -> `TickEvent.RuntimeStart` / `TickEvent.RuntimeEnd`
- Forge `RenderGuiEvent.Post` -> `RenderEvent.Hud`
- Forge world render callbacks -> `RenderEvent.World`

The starter `CoordinatesHudModule` renders through `RenderContext`, so it works through the Forge adapter without importing loader APIs.
