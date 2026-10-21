# Tasks: Init Tarot Cards Revamp

> **Change ID:** `init-tarot-cards-revamp`
> **Linked Proposal:** [proposal.md](./proposal.md)
> **Status:** complete
>
> All tasks must be `[x]` before this change can be archived via `sdd-spec-archive`.

---

## Implementation Tasks

- [x] Configure Gradle build (`settings.gradle`, `gradle.properties`, `build.gradle`) and `META-INF/neoforge.mods.toml` for Minecraft `26.3` and NeoForge `26.3.0.95` with mod ID `tarotcardsrevamp`.
- [x] Implement `TarotCardType` enum (all 22 Major Arcana 0–XXI, base & ascended stats, quest types, and default quest goals) and `TarotConfig` (Server/Common TOML config).
- [x] Implement `CardDataComponent` (Codec & StreamCodec for `active`, `questProgress`, `ascended`, and deck contents) and `TarotCardItem` (right-click toggle, progress bar tooltip, ascended glint).
- [x] Implement `TarotDeckItem`, `TarotDeckMenu` (22 dedicated Arcana slots + auto-collect + slot toggle), `TarotDeckScreen`, and C2S networking payloads.
- [x] Implement soft `CuriosCompat` integration so the Tarot Deck and cards work in Curios slots when Curios is installed and in regular inventory otherwise.
- [x] Implement `TarotEffectHandler` for all 22 Base (50%) and Ascended buffs, including Ascended Chariot sprint fluid-walking over water and lava.
- [x] Implement `TarotQuestHandler` and `PlayerTarotTracker` attachment to track quest events on active cards, trigger automatic Ascension at 100%, and award Advancements.
- [x] Create JSON Advancements (Common: 1, 10, 17 unique cards; Rare: 22 individual card ascensions; Legendary: 22 unique found & 22 unique ascended), recipes (Tarot Deck crafting & recycling any 3 tarot cards via `#tarotcardsrevamp:tarot_cards`), and Global Loot Modifier (`TarotLootModifier`).
- [x] Implement `/tarot` admin command (`giveall`, `ascend`, `progress`, `reset`) with OP permission check.
- [x] Generate 16x16 pixel art textures for all 22 Tarot Cards (base/ascended overlay) and Tarot Deck, item models, and complete `en_us.json` and `pt_br.json` localization files.

## Security Validation Tasks

- [x] Validate all C2S packet indices (`0..21`) and verify deck ownership on the server before mutating item components.
- [x] Ensure `CardDataComponent` clamps progress values to non-negative bounds and `/tarot` commands enforce permission level 2.

## Shift-Left Test Suite Tasks

### Happy Path
- [x] Write unit test TP-01: Quest progress increments on active cards and triggers `ascended = true` at 100% target.
- [x] Write unit test TP-02: Active card resolver deduplicates multiple copies of the same card and prioritizes Ascended over Base.
- [x] Write unit test TP-03: Tarot Deck 22-slot validation accepts each `TarotCardType` in its designated slot and auto-collects from inventory.
- [x] Write unit test TP-04: `PlayerTarotTracker` triggers discovery and ascension milestones at 1, 10, 17, and 22 unique cards.

### Edge Cases & Failure Modes
- [x] Write unit test TE-01: `CardDataComponent` clamps negative or excessive progress values safely.
- [x] Write unit test TE-02: Toggled-off (`active = false`) cards neither grant buffs nor gain quest progress.
- [x] Write unit test TE-03: Smart card selector falls back cleanly when all 22 cards are already discovered.
- [x] Write unit test TE-04: Out-of-bounds slot index in Tarot Deck operations is rejected safely.

## Documentation Tasks

- [x] Document build, gameplay mechanics, 22 cards table, quests, and commands in `README.md`.

## Review Gate

- [x] All implementation tasks complete
- [x] All security validation tasks complete
- [x] All tests passing (`gradle test` / `mvn test`)
- [x] Proposal acceptance criteria verified (run sdd-spec-validate)
- [x] Ready for `sdd-spec-archive`
