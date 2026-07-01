# Threads of Fate

A Fabric server-side mod for Minecraft 26.1.2: everyone starts with 3 lives (max 7).
Die and drop a **Thread of Fate** you can pick back up for another life. Run out of
lives and you're banned until someone crafts a **Revival Totem** from your dropped
head and unbans/revives you.

## How it plays

- **3 starting lives**, cap of **7**.
- Non-final death: you drop a *Thread of Fate* stamped with your name. Pick up your
  own thread to get that life back (someone else can grab and hold it to deny you).
- Final death (0 lives left): you drop your **player head** instead, and you're
  server-banned with a message telling people to revive you.
- Revival recipe (3x3 crafting table):

  ```
  Totem    Diamond  Totem
  Diamond  PlayerHead Diamond
  Totem    Diamond  Totem
  ```

  This produces a **Revival Totem** bound to that specific player. Right-click it to
  unban them and restore them to 1 life.
- `/lives` - check your own lives. `/lives <player>` - check someone else's.
- `/threadsoffate set <player> <amount>` - op-only admin override.

## IMPORTANT - about the Minecraft version

Minecraft **26.1** is a very new release (it dropped the "1." version prefix, is now
fully **unobfuscated**, and Fabric dropped Yarn mappings entirely in favor of Mojang's
official names). I built this against the current Fabric docs for 26.1.2, but this
version's API surface is still fresh, and a few method/field names may drift by the
time you compile (particularly `Player#die`'s exact signature, `Inventory#items`,
and `ResolvableProfile`'s constructor). I've commented every spot I'm least certain
about. If something doesn't compile:

1. Open the project in IntelliJ IDEA 2025.3+ (see `Setting Up Your IDE` and
   `Generating Sources` in the Fabric docs) so you get real autocomplete against the
   actual 26.1.2 sources.
2. Right-click the broken symbol -> "Go to declaration" is gone (no obfuscation!) so
   you can read the real method signature directly and adjust.
3. Everything version-sensitive lives in `PlayerDeathMixin`, `ReviveTotemRecipe`,
   `ReviveTotemItem`, and `LivesManager` - the rest (items, commands, resources)
   is much more stable API.

## Building

Requires **Java 25** and **Gradle 9.4+** (a wrapper jar isn't included here since I
can't ship a binary from this environment - run `gradle wrapper --gradle-version 9.4`
once inside the project folder to generate one, or just use a system Gradle install).

```bash
cd threads-of-fate
gradle wrapper --gradle-version 9.4   # optional, generates gradlew
./gradlew build
```

The output jar will be in `build/libs/threads-of-fate-1.0.0.jar`. Minecraft 26.1
no longer needs a "remap" step - the built jar is ready to use directly.

## Installing on your server

1. Install **Fabric Loader 0.18.4+** for Minecraft **26.1.2** on your server
   (installer at fabricmc.net).
2. Download **Fabric API 0.154.0+26.1.2** (or newer for 26.1.x) into `mods/`.
3. Drop `threads-of-fate-1.0.0.jar` into `mods/`.
4. Start the server.

This mod is server-side logic only (custom items/recipe/commands/ban hook) - it
does not require players to install anything client-side, though if you want the
Thread of Fate / Revival Totem to render with distinct textures rather than reused
vanilla ones (string / totem), you'll want to give players a resource pack, or swap
the two placeholder textures in `assets/threadsoffate/models/item/*.json`.

## Notes / things you may want to tweak

- **Textures**: I reused vanilla `string` and `totem_of_undying` textures as
  placeholders since I can't generate custom art here - swap the `layer0` texture
  paths in the two model JSONs for real art.
- **Thread reclaim window**: currently a thread never expires - it sits in the
  world until picked up. Add a despawn/notify timer if you want urgency.
- **PvP note**: because anyone can pick up someone else's thread, players can
  deliberately deny each other lives - that's an intentional part of the "Threads
  of Fate" tension, but let me know if you'd rather threads only ever be
  pickupable by their owner.
