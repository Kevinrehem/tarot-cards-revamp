# Tasks: Init Tarot Cards Revamp

> **Change ID:** `init-tarot-cards-revamp`
> **Linked Proposal:** [proposal.md](./proposal.md)
> **Status:** in-progress
>
> All tasks must be `[x]` before this change can be archived via `sdd-spec-archive`.
>
> **2026-10-21 correction:** an earlier revision marked every task `[x]`. After the first in-game launch
> (`latest.log`: `Item id not set`) the code was re-audited and the tasks below were reopened to reflect reality.
> Phase 1 (mod loads, basic items work) is done; Phase 2 items are open.

---

## Phase 1 — Mod loads and basic items work (DONE)

- [x] Fix crash `Item id not set`: register items through `DeferredRegister.Items#registerItem` (MC 26.3 requires the item id in `Item.Properties`).
- [x] Remove default-component read from `TarotCardItem` constructor (registry-ordering risk).
- [x] Add item model definitions `assets/tarotcardsrevamp/items/*.json` (24 items).
- [x] Convert recipes to the 26.3 string-ingredient format (`tarot_deck`, `recycle_tarot_cards`).
- [x] Localize the 22 ascension advancements (`translate` keys in `en_us`/`pt_br`); strip UTF-8 BOM from JSON.
- [x] Disable opening the Deck menu until a client Screen exists (shows "coming soon" overlay message).
- [x] Smoke test: `gradlew runGameTestServer` boots registries/datapacks with 0 WARN/ERROR/FATAL.

## Implementation Tasks

- [x] Configure Gradle build (`settings.gradle`, `gradle.properties`, `build.gradle`) and `META-INF/neoforge.mods.toml` for Minecraft `26.3` and NeoForge `26.3.0.95` with mod ID `tarotcardsrevamp`.
- [x] Implement `TarotCardType` enum (all 22 Major Arcana 0–XXI, quest types, default quest goals).
- [ ] Wire `TarotConfig` (Server/Common TOML config): currently registered but no value is read by gameplay code (loot chance, quest goal multiplier, Judgement cooldown, Chariot toggle).
- [x] Implement `CardDataComponent` (Codec & StreamCodec for `active`, `questProgress`, `ascended`) and `TarotCardItem` (right-click toggle, quest tooltip, ascended glint).
- [ ] Implement Deck: `TarotDeckScreen` + `RegisterMenuScreensEvent`, persist deck contents on the item (`DeckDataComponent` is not wired to any `DataComponentType`), auto-collect, slot toggle, and C2S networking payloads.
- [ ] Implement `CuriosCompat` (currently a no-op) and scan Deck contents for active cards.
- [ ] Implement all 22 Base (50%) and Ascended buffs in `TarotEffectHandler` (currently only The Fool, The Chariot, The Moon, The World, The Tower).
- [ ] Implement quest tracking for all 22 cards in `TarotQuestHandler` (currently only The Star, Death, The Hanged Man) using `PlayerTarotTracker` (not yet attached to players).
- [ ] Grant advancements (`triggerAscension` is empty; all criteria use `minecraft:impossible`) and wire discovery/ascension milestones (1, 10, 17, 22).
- [ ] Implement Global Loot Modifier (`TarotLootModifier` + GLM JSON) for structure chests with smart weighting (`SmartCardSelector` exists but is unused by loot).
- [ ] Complete `/tarot` admin command (`giveall` and `ascend_held` exist; `progress` and `reset` missing).
- [x] Generate 16x16 placeholder textures for the 22 cards, Unidentified card and Deck, item models, and `en_us.json`/`pt_br.json`.
- [ ] Replace placeholder textures with final art and add the Ascended visual overlay.

## Security Validation Tasks

- [ ] Validate all C2S packet indices (`0..21`) and verify deck ownership on the server (no C2S packets exist yet).
- [x] `CardDataComponent` clamps progress to non-negative bounds and `/tarot` requires permission level 2 (`Commands.hasPermission(LEVEL_GAMEMASTERS)`).

## Shift-Left Test Suite Tasks

### Happy Path
- [x] Write unit test TP-01: Quest progress increments on active cards and triggers `ascended = true` at 100% target.
- [x] Write unit test TP-02: Active card resolver deduplicates multiple copies of the same card and prioritizes Ascended over Base.
- [x] Write unit test TP-03: Tarot Deck 22-slot data structure accepts each `TarotCardType` in its designated slot.
- [x] Write unit test TP-04: `PlayerTarotTracker` triggers discovery and ascension milestones at 1, 10, 17, and 22 unique cards.

### Edge Cases & Failure Modes
- [x] Write unit test TE-01: `CardDataComponent` clamps negative or excessive progress values safely.
- [x] Write unit test TE-02: Toggled-off (`active = false`) cards neither grant buffs nor gain quest progress.
- [x] Write unit test TE-03: Smart card selector falls back cleanly when all 22 cards are already discovered.
- [x] Write unit test TE-04: Out-of-bounds slot index in Tarot Deck operations is rejected safely.

### Integration
- [x] `gradlew runGameTestServer` smoke test (registries + datapack load) — regression guard for the `Item id not set` crash.
- [ ] Add real GameTests for card toggle, quest progression, and recipe results.

## Documentation Tasks

- [x] Document build, mechanics, and commands in `README.md` (with an honest implementation-status section).

## Review Gate

- [ ] All implementation tasks complete
- [ ] All security validation tasks complete
- [ ] All tests passing (`gradlew build runGameTestServer`)
- [ ] Proposal acceptance criteria verified (run sdd-spec-validate)
- [ ] Ready for `sdd-spec-archive`
