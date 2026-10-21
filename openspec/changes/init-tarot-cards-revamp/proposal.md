# Proposal: Init Tarot Cards Revamp

> **Change ID:** `init-tarot-cards-revamp`
> **Status:** under-review
> **Author:** @jaca
> **Created:** 2026-10-21
> **Last Updated:** 2026-10-21

---

## Summary

Implement the complete **Tarot Cards Revamp** (`tarotcardsrevamp`) mod for Minecraft `26.3` on NeoForge `26.3.0.95`. The mod introduces the 22 Major Arcana Tarot Cards (0 - The Fool through XXI - The World), a craftable **Tarot Deck** container with a dedicated 22-slot GUI and optional Curios API integration, an item-level Quest & Ascension progression system stored via `DataComponents` where each card starts at 50% power and automatically ascends at 100% quest completion into an overpowered Awakened/Ascended state, custom Advancements (Common, Rare per card, and Legendary collection/ascension challenges), Global Loot Modifiers for structure chests with duplicate recycling, NeoForge configuration, `/tarot` admin commands, localization (`en_us` and `pt_br`), and procedural 16x16 pixel art textures.

## Motivation

Classic Tarot Cards mods provided passive buffs when held in the inventory or Tarot Deck, but lacked progression depth—finding a card immediately granted full power with no gameplay loop attached to mastering each Arcana. By starting each card at 50% strength and requiring a thematic in-game quest stored on the card itself to ascend it into a significantly stronger version with unique mechanics (e.g., running on fluids while sprinting with Ascended Chariot, temporal slowness aura with Ascended World, double jump and fall immunity with Ascended Fool), players experience a rewarding sense of progression, collection, and achievement hunting.

## Scope

### In Scope
- **Project Build & NeoForge 26.3.0.95 Setup**: Gradle configuration (`settings.gradle`, `gradle.properties`, `build.gradle`), `neoforge.mods.toml`, and main mod entrypoint `com.tarotcardsrevamp.TarotCardsRevampMod`.
- **22 Major Arcana Tarot Cards (`TarotCardType` & `TarotCardItem`)**:
  - **0 - The Fool**: Base: Jump Boost I + -25% fall damage | Quest: Survive 200 fall damage (or jump 1,000 times) | Ascended: Jump Boost III, 100% fall damage immunity, Double Jump.
  - **I - The Magician**: Base: 12.5% chance not to consume item durability | Quest: Consume 2,000 durability or enchant 15 items | Ascended: 50% chance not to consume durability + held items repair 1 durability every 5s.
  - **II - The High Priestess**: Base: -15% anvil XP cost | Quest: Gain 50 XP levels or use 10 enchanted books | Ascended: -50% anvil XP cost + boosts compatible held equipment enchantments by +1 level.
  - **III - The Empress**: Base: -25% animal breeding cooldown & +25% baby growth speed nearby | Quest: Breed 50 animals | Ascended: Instant baby animal growth nearby, -75% breeding cooldown, passive animals follow player.
  - **IV - The Emperor**: Base: Hero of the Village I | Quest: Complete 64 Villager trades | Ascended: Hero of the Village V + Iron Golems never aggro and defend the player.
  - **V - The Hierophant**: Base: +25% XP orb value | Quest: Collect 2,500 raw XP | Ascended: +150% XP orb value (2.5x), long-range XP magnet, heals 0.5 heart per 20 XP collected.
  - **VI - The Lovers**: Base: Weak Regeneration I to nearby allies/pets | Quest: Heal 100 HP on allies/pets or tame 10 animals | Ascended: Regeneration II to player and allies within 12 blocks + pets take 50% less damage and deal +50% damage.
  - **VII - The Chariot**: Base: +10% Movement Speed | Quest: Travel 10,000 blocks sprinting or mounted | Ascended: +40% Movement Speed, 1.0 block Step Assist, +50% mount speed, and **sprint across fluids (water & lava) without sinking or taking magma/lava foot damage**.
  - **VIII - Strength**: Base: +15% Melee Damage | Quest: Deal 1,500 melee damage to hostile mobs | Ascended: +50% Melee Damage + attacks bypass 30% of target armor.
  - **IX - The Hermit**: Base: +2 Armor when no allies/players within 16 blocks | Quest: Spend 3 in-game days isolated from players/villagers | Ascended: +8 Armor, +4 Armor Toughness, and reduced mob detection range when isolated.
  - **X - Wheel of Fortune**: Base: +0.5 Luck & 15% chance for +1 extra Looting/Fortune roll | Quest: Open 25 loot chests or mine 100 rare ores | Ascended: +3 Luck + effective +2 Fortune and +2 Looting bonus drops.
  - **XI - Justice**: Base: Reflects 15% incoming damage back to attacker | Quest: Take 300 combat damage | Ascended: Reflects 75% incoming damage as True Damage + reduces incoming damage by 15%.
  - **XII - The Hanged Man**: Base: Converts 10% of damage taken into XP | Quest: Take 400 damage total | Ascended: Converts 50% of damage taken into XP orbs + grants Resistance III and +30% damage when below 30% HP.
  - **XIII - Death**: Base: +15% damage vs Undead | Quest: Kill 150 hostile mobs | Ascended: +50% damage to all hostiles, 15% Lifesteal, and instant execute on non-boss targets below 15% HP.
  - **XIV - Temperance**: Base: -25% hunger exhaustion | Quest: Eat 64 food items | Ascended: -85% hunger exhaustion + eating any food grants Absorption II and extra instant healing.
  - **XV - The Devil**: Base: Attacks inflict 3s fire and weak Wither I | Quest: Kill 100 Nether/hostile mobs | Ascended: Attacks inflict Wither III (6s) & Fire, player is 100% immune to Wither, and deals +35% damage to debuffed targets.
  - **XVI - The Tower**: Base: -25% explosion damage | Quest: Survive 15 explosions | Ascended: 100% explosion immunity + heavy hits/falls trigger a non-griefing shockwave knocking back nearby enemies.
  - **XVII - The Star**: Base: +0.5 Block Reach | Quest: Break or place 2,000 blocks | Ascended: +2.5 Block Reach, +1.5 Entity Attack Reach, and permanent Haste II.
  - **XVIII - The Moon**: Base: Permanent Night Vision | Quest: Walk 3,000 blocks at night or kill 50 mobs at night | Ascended: Night Vision + while in darkness/night grants +25% Speed, +25% Damage, and Regeneration I.
  - **XIX - The Sun**: Base: +4 Max HP (+2 Hearts) | Quest: Walk 3,000 blocks under sunlight or survive 10 dawns | Ascended: +20 Max HP (+10 Hearts), passive sunlight regeneration, and 10s Fire Resistance shield.
  - **XX - Judgement**: Base: 10% chance to deal 200% damage on hit | Quest: Land 100 critical hits | Ascended: 30% chance to deal 250% damage + strikes target with safe divine lightning + built-in Totem of Undying effect (10-minute cooldown).
  - **XXI - The World**: Base: Slowness I aura (4 blocks) on enemies | Quest: Visit Overworld, Nether, and End + defeat 200 mobs | Ascended: Temporal Aura within 8 blocks (Slowness IV + Mining Fatigue III on enemies + slows incoming projectiles).
- **Card Activation & DataComponents**:
  - Right-click toggle (active/inactive) per card.
  - Active in player inventory or inside the Tarot Deck (inventory or Curios slot); duplicate cards of the same type do not stack buffs (highest tier takes precedence).
  - Codec-backed `CardDataComponent` storing `active` (boolean), `questProgress` (int), and `ascended` (boolean).
- **Tarot Deck & GUI**:
  - Recipe: Chest in center, 4 Paper in cross, 4 Gold Nuggets in corners.
  - Dedicated 22-slot Container/Screen (`TarotDeckMenu` & `TarotDeckScreen`), auto-collect button, and individual slot toggle support.
  - Soft Curios API integration (`CuriosCompat`) + hotkey to open equipped Tarot Deck.
- **Advancement System**:
  - Common (`task`): Find 1 card (root), 10 unique cards, 17 unique cards.
  - Rare (`goal`): 22 individual advancements—one for ascending each specific Arcana card.
  - Legendary (`challenge`): Find all 22 unique cards + Ascend all 22 unique cards.
- **Loot & Duplicate Recycling**:
  - Global Loot Modifier (`TarotLootModifier`) injecting into structure chests (Villages, Mineshafts, Dungeons, Temples, Bastions, End Cities, Strongholds) with smart weighting favoring undiscovered cards.
  - Crafting recipe to recycle 3 duplicate Tarot Cards + 1 Gold Ingot/Amethyst into an Unidentified Tarot Card that reveals a missing card (or random if all 22 are owned).
- **Config, Commands & Assets**:
  - `TarotConfig` (NeoForge Server/Common config) for loot rates, quest targets, and buff multipliers.
  - `/tarot` command (`giveall`, `progress`, `ascend`, `reset`) requiring permission level 2.
  - Full `en_us.json` and `pt_br.json` translations + generated 16x16 pixel art textures.

### Out of Scope
- Minor Arcana (56 suited cards).
- Destructive block griefing from card effects.

## Proposed Solution

We will structure the mod under `com.tarotcardsrevamp` with clean separation of concerns:
1. `core/`: Registries (`ModItems`, `ModDataComponents`, `ModMenuTypes`, `ModAttachmentTypes`, `ModLootModifiers`, `ModCreativeTabs`), `TarotConfig`.
2. `card/`: `TarotCardType` enum defining the 22 cards, their quest goals, base/ascended descriptions, and `TarotCardItem` + `CardDataComponent`.
3. `effect/`: `TarotEffectHandler` & `TarotQuestHandler` listening to NeoForge player/entity events to apply passive attributes/effects and increment quest progress on active cards, triggering Ascension & Advancements at 100%.
4. `deck/`: `TarotDeckItem`, `TarotDeckMenu`, `TarotDeckScreen`, and networking payloads for GUI toggle/auto-collect and hotkey opening.
5. `compat/`: `CuriosCompat` using reflection/soft-loading so Curios API is supported when present without crashing when absent.
6. `loot/` & `recipe/`: `TarotLootModifier`, `UnidentifiedTarotCardItem`, and `TarotDuplicateRecycleRecipe`.
7. `advancement/` & `command/`: `PlayerTarotTracker` attachment, custom advancement trigger/granting logic, and `TarotCommand`.
8. `test/`: Automated unit tests for card data components, quest math, non-stacking duplicate resolution, deck storage rules, and config validation.

### Impacted Files
| File | Change Type | Notes |
|------|-------------|-------|
| `settings.gradle` | Create | Gradle settings for NeoForge 26.3.0.95 |
| `gradle.properties` | Create | Mod metadata and version properties |
| `build.gradle` | Create | Build script with NeoForge 26.3.0.95 & JUnit 5 |
| `src/main/java/com/tarotcardsrevamp/TarotCardsRevampMod.java` | Create | Main mod entrypoint |
| `src/main/java/com/tarotcardsrevamp/card/TarotCardType.java` | Create | Enum of 22 Major Arcana, quests, and stats |
| `src/main/java/com/tarotcardsrevamp/card/CardDataComponent.java` | Create | Immutable record + Codec/StreamCodec for card state |
| `src/main/java/com/tarotcardsrevamp/card/TarotCardItem.java` | Create | Item implementation, tooltip, toggle, glint |
| `src/main/java/com/tarotcardsrevamp/deck/TarotDeckItem.java` | Create | Deck item, inventory provider, auto-collect |
| `src/main/java/com/tarotcardsrevamp/deck/TarotDeckMenu.java` | Create | 22-slot container menu |
| `src/main/java/com/tarotcardsrevamp/deck/TarotDeckScreen.java` | Create | Client GUI screen for the Tarot Deck |
| `src/main/java/com/tarotcardsrevamp/effect/TarotEffectHandler.java` | Create | Tick & combat passive buff processor (Base & Ascended) |
| `src/main/java/com/tarotcardsrevamp/effect/TarotQuestHandler.java` | Create | Event-driven quest progression & Ascension trigger |
| `src/main/java/com/tarotcardsrevamp/compat/CuriosCompat.java` | Create | Optional Curios API slot discovery |
| `src/main/java/com/tarotcardsrevamp/loot/TarotLootModifier.java` | Create | Structure chest GLM with smart card selection |
| `src/main/java/com/tarotcardsrevamp/command/TarotCommand.java` | Create | `/tarot` OP admin commands |
| `src/main/resources/META-INF/neoforge.mods.toml` | Create | Mod descriptor |
| `src/main/resources/assets/tarotcardsrevamp/lang/en_us.json` | Create | English localization |
| `src/main/resources/assets/tarotcardsrevamp/lang/pt_br.json` | Create | Brazilian Portuguese localization |
| `src/test/java/com/tarotcardsrevamp/TarotCardLogicTest.java` | Create | Unit test suite for card progression, deck, and buffs |

## Acceptance Criteria

- [ ] **Given** a player crafts a Chest in the center with 4 Paper in a cross and 4 Gold Nuggets in the corners **When** the crafting completes **Then** they receive 1 `tarot_deck`.
- [ ] **Given** a player holds an un-ascended Tarot Card in their inventory or inside their Tarot Deck (inventory or Curios slot) **When** the card is active **Then** only the 50% Base Buff is applied, and duplicate copies of the same card do not stack.
- [ ] **Given** a player performs the quest action corresponding to an active Tarot Card **When** the quest progress reaches 100% of its target **Then** the card automatically ascends, plays a sound, gains an enchanted glint, activates its Ascended buff, and grants the corresponding Rare (`goal`) advancement.
- [ ] **Given** a player has an Ascended `the_chariot` active **When** the player sprints over water or lava **Then** the player runs across the fluid surface without sinking or taking lava/magma foot damage.
- [ ] **Given** a player discovers 1, 10, 17, or 22 unique Tarot Cards, or ascends all 22 Tarot Cards **When** the milestone is reached **Then** the corresponding Common (`task`) or Legendary (`challenge`) advancement is awarded.
- [ ] **Given** a player opens the Tarot Deck GUI **When** they interact with the 22 Arcana slots or click Auto-Collect **Then** matching cards from their inventory are placed into their dedicated slots and can be toggled on/off.

## Defensive Security & Validation Considerations

### Input Validation
- All C2S network payloads (opening deck via keybind, auto-collecting cards, toggling card state in GUI) validate on the server that the player actually possesses the `TarotDeckItem`, that slot indices are strictly within `0..21`, and that `CardDataComponent` progress values are clamped to `[0, maxGoal]`.
- `/tarot` commands strictly enforce permission level 2 (`Commands.LEVEL_GAMEMASTERS`).

### Secret Isolation
- No API keys, tokens, or external network requests exist in the mod.

### Authorization & Authentication Impact
- Server-authoritative state for all quest progression, item mutations, and advancement grants. Client cannot spoof ascension state via custom packets.

## Testing Strategy & Shift-Left Plan

### Happy Path Tests
| Test ID | Scenario | Test Type | Expected Result |
|---------|----------|-----------|-----------------|
| TP-01 | Quest progress increment and auto-ascension at 100% | Unit | `CardDataComponent` transitions `ascended = true` when `progress >= goal` |
| TP-02 | Active card resolution across inventory and Tarot Deck | Unit | Collects all unique active cards; Ascended copy overrides Base copy of same Arcana |
| TP-03 | Tarot Deck 22-slot index mapping and duplicate rejection | Unit | Each `TarotCardType` maps to its exact ordinal slot `0..21` and rejects mismatched types |
| TP-04 | Discovery & Ascension milestone tracking (1, 10, 17, 22) | Unit | `PlayerTarotTracker` accurately triggers milestone thresholds |

### Edge Case & Failure Mode Tests
| Test ID | Scenario | Test Type | Expected Result |
|---------|----------|-----------|-----------------|
| TE-01 | Negative or overflow quest progress values | Unit | `CardDataComponent` clamps progress to `[0, target]` without overflow |
| TE-02 | Inactive (toggled off) card in deck or inventory | Unit | Card neither grants buffs nor accumulates quest progress |
| TE-03 | Duplicate recycling when player already owns all 22 cards | Unit | Returns a valid fallback random Tarot Card without throwing an exception |
| TE-04 | Invalid slot index in Deck C2S packet (`< 0` or `>= 22`) | Unit | Rejected safely without IndexOutOfBoundsException |

## Risks and Mitigations

| Risk | Likelihood | Impact | Mitigation |
|------|-----------|--------|------------|
| Curios API not installed in player's modpack | High | Med | Implement `CuriosCompat` via soft `ModList.get().isLoaded("curios")` check and fallback to inventory scanning |
| Attribute modifier leaks when cards are removed or toggled off | Med | High | Reconcile deterministic `ResourceLocation` attribute modifier IDs every tick and strip modifiers whose card is no longer active |

## References

- [SDD Stack Contract](../../specs/sdd-stack-contract.md)
- [Tarot Cards Revamp Domain Spec](./specs/tarot-cards-revamp.md)
