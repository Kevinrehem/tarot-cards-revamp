# Tasks: Fix Tarot Card Custom Effects

> **Change ID:** `fix-tarot-card-custom-effects`
> **Linked Proposal:** [proposal.md](./proposal.md)
> **Status:** completed
>
> All tasks must be `[x]` before this change can be archived via `sdd-spec-archive`.

---

## Implementation Tasks

- [x] Create `TarotEffectCalculator` with pure, unit-testable helper formulas for attribute deltas, outgoing/incoming combat modifiers, reflection, XP conversion, anvil cost discounts, breeding cooldowns, and Judgement Totem cooldown tracking.
- [x] Implement deterministic `AttributeModifier` reconciliation (`reconcileServerAttributes` & `onPlayerTickPre`) in `TarotEffectHandler` for:
  - `VII - The Chariot`: `Attributes.STEP_HEIGHT` (`+0.4` Ascended -> 1.0 block step assist)
  - `IX - The Hermit`: `Attributes.ARMOR` (`+2.0` Base / `+8.0` Ascended) & `Attributes.ARMOR_TOUGHNESS` (`+4.0` Ascended) when isolated (16 blocks)
  - `X - Wheel of Fortune`: `Attributes.LUCK` (`+0.5` Base / `+3.0` Ascended)
  - `XVII - The Star`: `Attributes.BLOCK_INTERACTION_RANGE` (`+0.5` Base / `+2.5` Ascended) & `Attributes.ENTITY_INTERACTION_RANGE` (`+1.5` Ascended)
  - `XIX - The Sun`: `Attributes.MAX_HEALTH` (`+4.0` Base / `+20.0` Ascended)
- [x] Implement tick-based passive buffs, auras, and physics in `TarotEffectHandler`:
  - `I - The Magician` (Ascended): Repair 1 durability every 5s (100 ticks) on held/equipped items.
  - `III - The Empress` (Base & Ascended): Accelerate nearby baby animal growth (+25% Base / instant Ascended) and attract passive animals within 10 blocks (Ascended).
  - `IV - The Emperor` (Base & Ascended): Apply `HERO_OF_THE_VILLAGE` I (Base) / V (Ascended).
  - `V - The Hierophant` (Ascended): Pull `ExperienceOrb` entities within 8 blocks toward the player.
  - `VI - The Lovers` (Base & Ascended): Apply `REGENERATION` I (Base, 8 blocks) / II (Ascended, 12 blocks + player) to nearby allied players and tamed pets.
  - `VII - The Chariot` (Ascended): Apply `SPEED` II to the player's living mount while riding.
  - `XII - The Hanged Man` (Ascended): Grant `RESISTANCE` III while player HP <= 30% of Max HP.
  - `XIV - The Temperance` (Base & Ascended): Reduce hunger consumption by 25% (Base) / 85% (Ascended).
  - `XV - The Devil` (Ascended): Cleanse active `WITHER` effect from the player.
  - `XVII - The Star` (Ascended): Apply permanent `HASTE` II.
  - `XVIII - The Moon` (Ascended): Add missing `SPEED` I alongside `STRENGTH` I and `REGENERATION` I in darkness/night.
  - `XXI - The World` (Ascended): Slow incoming non-owned `Projectile` entities within 8 blocks (`scale(0.5)`).
- [x] Implement outgoing combat effects in `TarotEffectHandler`:
  - `VIII - Strength`: +15% (Base) / +50% (Ascended) melee damage + 30% armor penetration (Ascended).
  - `XII - The Hanged Man`: +30% outgoing damage when player HP <= 30% (Ascended).
  - `XIII - Death`: +15% damage vs Undead (Base) / +50% damage vs all enemies, 15% Lifesteal, and instant execute on non-bosses below 15% HP (Ascended).
  - `XV - The Devil`: Ignite target for 3s + Wither I (Base) / Ignite target for 6s + Wither III + +35% damage vs burning or debuffed targets (Ascended).
  - `XX - Judgement`: 10% chance for 200% damage (Base) / 30% chance for 250% damage + visual-only `LightningBolt` (Ascended).
  - `II - The High Priestess`: +15% bonus damage when attacking with an enchanted weapon (Ascended).
  - `VI - The Lovers`: +50% outgoing damage dealt by the player's tamed pets (Ascended).
- [x] Implement incoming defense, reflection, and survival effects in `TarotEffectHandler`:
  - `IV - The Emperor` (Ascended): Cancel incoming damage from `IronGolem`, clear golem aggro against player, and rally nearby Iron Golems (16 blocks) to defend the player.
  - `VI - The Lovers` (Ascended): Reduce incoming damage taken by the player's tamed pets by 50%.
  - `XI - Justice`: Reflect 15% incoming combat damage (Base) / Reduce incoming combat damage by 15% and reflect 75% as True Damage with recursion guard (Ascended).
  - `XII - The Hanged Man`: Spawn `ExperienceOrb` for 10% (Base) / 50% (Ascended) of damage taken.
  - `XV - The Devil` (Ascended): Cancel incoming Wither damage.
  - `XVI - The Tower` (Ascended): Trigger non-griefing radial shockwave knockback within 6 blocks on fall, explosion, or heavy damage (>= 6.0).
  - `XX - Judgement` (Ascended): Intercept lethal damage with Totem of Undying effect (2.0 HP, Regen II, Absorption II, Fire Resistance I, entity event `35`) respecting `TarotConfig.JUDGEMENT_TOTEM_COOLDOWN_SECONDS`.
- [x] Implement world, crafting, and progression event hooks in `TarotEffectHandler`:
  - `I - The Magician`: 12.5% (Base) / 50% (Ascended) chance to restore 1 durability when breaking blocks or hitting enemies.
  - `II - The High Priestess`: Discount anvil XP cost by 15% (Base) / 50% (Ascended, min 1) in `PlayerXpEvent.LevelChange` when using `AnvilMenu`, plus +20% mining speed with enchanted tools (`PlayerEvent.BreakSpeed`).
  - `III - The Empress`: Set parent breeding cooldown to -25% (4500 ticks Base) / -75% (1500 ticks Ascended) and baby age to +25% (-18000 Base) / instant adult (`0` Ascended) in `BabyEntitySpawnEvent`.
  - `V - The Hierophant`: Award +25% (Base) / +150% (Ascended) bonus XP and heal `0.05 HP` per XP (Ascended) in `PlayerXpEvent.PickupXp`.
  - `IX - The Hermit` (Ascended): Cancel hostile mob targeting (`LivingChangeTargetEvent`) when isolated and distance > 8 blocks.
  - `X - Wheel of Fortune`: Grant 15% chance (Base) / guaranteed bonus drops (Ascended) in `LivingDropsEvent`.
  - `XIV - Temperance` (Ascended): Grant `ABSORPTION` II (60s) and heal `4.0 HP` when finishing eating food (`LivingEntityUseItemEvent.Finish`).

## Test Tasks

- [x] Write unit test `TP-12`: Verify attribute modifier deltas for Chariot, Hermit, Wheel of Fortune, Star, and Sun (Base vs Ascended).
- [x] Write unit test `TP-13`: Verify outgoing combat calculations for Strength, Death, Devil, High Priestess, and Hanged Man.
- [x] Write unit test `TP-14`: Verify incoming combat calculations for Justice reflection, Hanged Man XP conversion, and Lovers pet damage multipliers.
- [x] Write unit test `TP-15`: Verify Anvil XP discount (High Priestess), Breeding age reduction (Empress), and XP bonus/healing (Hierophant).
- [x] Write edge-case test `TE-05`: Verify Justice reflection recursion guard and boss exclusion from Death execute.
- [x] Write edge-case test `TE-06`: Verify Judgement Totem cooldown expiration and re-trigger blocking.

## Documentation Tasks

- [x] Update `openspec/changes/init-tarot-cards-revamp/tasks.md` to mark the 22 card effects task as completed once verified.

## Review Gate

- [x] All implementation tasks complete
- [x] All tests passing (`.\gradlew test`)
- [x] Proposal acceptance criteria verified
- [x] Ready for `sdd-spec-archive`
