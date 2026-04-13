# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Biomes is a BentoBox addon for Minecraft Spigot servers that allows players to change biomes on their islands. It supports multiple game modes (SkyBlock, CaveBlock, AcidIsland, SkyGrid, etc.) and integrates with optional addons: Bank, Level, and Greenhouses.

- **Main class:** `world.bentobox.biomes.BiomesAddon`
- **Java version:** 21
- **Current version:** 2.2.1-SNAPSHOT

## Build Commands

```bash
mvn clean package          # Build the plugin JAR (output: target/)
mvn clean compile          # Compile only
mvn verify                 # Compile + run tests + verify
mvn test                   # Run tests
mvn test -Dtest=ClassName  # Run a single test class
mvn jacoco:report          # Generate coverage report (target/site/jacoco/)
```

The shaded JAR (with dependencies) is produced automatically during `package`. The CI pipeline runs `mvn -B verify org.sonarsource.scanner.maven:sonar-maven-plugin:sonar`.

## Architecture

### Core Structure

```
BiomesAddon (extends BentoBox Addon)
├── BiomesAddonManager      — biome data, unlock logic, biome change coordination
├── BiomesImportManager     — import/export biome definitions from YAML
├── WebManager              — request handler registration for inter-addon API
├── Settings                — configuration (config.yml)
├── Commands                — BiomesCommand (player), AdminCommand (admin)
├── Listeners               — ChangeOwnerListener, JoinLeaveListener, IslandLevelListener
└── Tasks                   — UpdateQueue, BiomeUpdateTask, BiomeUpdateHelper (async execution)
```

### Key Data Models (`database.objects`)

- **`BiomesObject`** — A biome definition: name, cost, unlock conditions, requirements, permissions
- **`BiomesIslandDataObject`** — Per-island state: which biomes are unlocked, purchase history
- **`BiomesBundleObject`** — Named group of biomes for bulk operations

### Biome Update Modes (3 strategies)

- `ISLAND` — Changes biome across the entire island
- `CHUNK` — Changes biome by chunk(s) around the player
- `RANGE` — Changes biome within a block radius

Biome changes are async; `UpdateQueue` manages concurrency. `BiomeUpdateTask` does the actual block-by-block biome assignment. The `use-chunk-refresh` setting triggers client-side chunk refresh for immediate visual updates.

### GUI Panels (`panels/`)

User-facing panels: `BiomesPanel`, `BuyPanel`, `AdvancedPanel`
Admin panels: `AdminPanel`, `BiomeEditPanel`, `BundleManagePanel`, `BiomeListPanel`

Panel layouts are defined in YAML under `src/main/resources/panels/` and loaded at runtime by the BentoBox PanelUtils library (shaded into the JAR).

### Events (`events/`)

Custom events fired during biome lifecycle: `BiomePreChangeEvent` (cancellable), `BiomeChangedEvent`, `BiomePurchasedEvent`, `BiomeUnlockedEvent`.

### Request Handlers (`handlers/`)

Inter-addon API via BentoBox's web request system: `BiomeDataRequestHandler`, `BiomeListRequestHandler`, `ChangeBiomeRequestHandler`.

## Key Dependencies

| Dependency | Version | Role |
|-----------|---------|------|
| BentoBox | 3.4.0+ | Core framework (required) |
| Spigot | 1.21.5+ | Minecraft server API (required) |
| Bank | 1.4.0+ | Island bank economy (optional) |
| Level | 2.5.0+ | Island level unlock conditions (optional) |
| Greenhouses | 1.9.2+ | Biome effects (optional) |
| Vault | 1.7+ | Economy fallback (optional) |
| PanelUtils | 1.2.0+ | GUI utilities (shaded) |

## Configuration Files

- `config.yml` — Main settings (update mode, cooldowns, feature flags)
- `biomesTemplate.yml` — Default biome definitions loaded on first run
- `panels/*.yml` — GUI panel layouts
- `locales/` — Translations (11 languages)

The addon is installed by placing the JAR in `plugins/BentoBox/addons/`.

## Testing

Tests use JUnit 4 + Mockito + PowerMock. There are currently no tests implemented, but the infrastructure (maven-surefire, JaCoCo) is configured in pom.xml.

## CI/CD

GitHub Actions (`.github/workflows/build.yml`) triggers on push to `develop`/`master` and PRs. It runs Maven verify + SonarCloud analysis. Artifacts are deployed to `repo.codemc.org`.

Version suffixes: `-SNAPSHOT` (local), `-b{N}` (CI build), no suffix (master releases).
