# Proposal: Fix Tarot Card Custom Effects

> **Change ID:** `fix-tarot-card-custom-effects`
> **Status:** approved
> **Author:** @jaca
> **Created:** 2026-10-22
> **Last Updated:** 2026-10-22

---

## Summary

Implement and fix all missing or non-functional Base (50%) and Ascended (100%) effects across the 22 Major Arcana Tarot Cards in `TarotEffectHandler` for Minecraft `26.3` / NeoForge `26.3.0.95`. Currently, only `0 - The Fool` and `XIX - The Sun` are 100% functional, 4 cards (`VII - The Chariot`, `XVI - The Tower`, `XVIII - The Moon`, `XXI - The World`) are missing their custom non-potion mechanics (such as Step Assist 1.0, shockwave knockback, darkness Speed, and projectile slowdown), and the remaining 16 cards have 0 lines of effect implementation in `TarotEffectHandler.java` (such as `XV - The Devil` fire/Wither/bonus damage, `I - The Magician` durability preservation/repair, `II - The High Priestess` anvil XP discount, `XI - Justice` true-damage reflection, `XIII - Death` lifesteal/execute, `XVII - The Star` interaction reach, and `XX - Judgement` crits/Totem of Undying).

## Motivation

Players invest significant time completing quests to ascend their Tarot Cards, expecting the exact powers described in the card tooltips. While potion-based buffs and fluid-walking/double-jump were implemented during the initial bootstrap, the majority of custom attribute modifiers, combat triggers, and world-interaction effects were left unimplemented or incomplete. For example:
- **VII - The Chariot (Ascended)** promises 1.0 block Step Assist and +50% mount speed, but `Attributes.STEP_HEIGHT` is never modified.
- **XV - The Devil** promises setting targets on fire (3s/6s), inflicting Wither I/III, 100% Wither immunity, and +35% damage against debuffed targets, but has zero code in `TarotEffectHandler`.
- **15 other Major Arcana cards** have working quest counters in `TarotQuestHandler` yet confer no gameplay effect whatsoever when active in the inventory, Tarot Deck, or Curios slot.

## Scope

### In Scope
- **Complete Effect Implementation & Bug Fixes for 20 Incomplete/Missing Cards**:
  1. **I - The Magician**:
     - *Base*: 12.5% chance to prevent item durability loss on block break or melee hit (restoring 1 durability point when triggered).
     - *Ascended*: 50% chance to prevent durability loss + repairs 1 durability point every 5 seconds (100 ticks) on held/equipped items.
  2. **II - The High Priestess**:
     - *Base*: -15% anvil XP level cost via `AnvilUpdateEvent`.
     - *Ascended*: -50% anvil XP level cost + dynamic combat/mining bonus equivalent to +1 level on held equipment enchantments (+15% bonus damage/mining speed when holding enchanted items).
  3. **III - The Empress**:
     - *Base*: -25% animal breeding cooldown (`parent.setAge(4500)`) and +25% baby growth speed in `BabyEntitySpawnEvent` + periodic nearby baby age acceleration.
     - *Ascended*: -75% breeding cooldown (`parent.setAge(1500)`), instant baby growth (`child.setAge(0)`), and passive animals within 10 blocks follow the player.
  4. **IV - The Emperor**:
     - *Base*: Applies `MobEffects.HERO_OF_THE_VILLAGE` I (amplifier 0).
     - *Ascended*: Applies `MobEffects.HERO_OF_THE_VILLAGE` V (amplifier 4); cancels incoming damage from `IronGolem` entities, clears their aggro against the player, and rallies nearby Iron Golems (16 blocks) to attack hostile mobs targeting or hit by the player.
  5. **V - The Hierophant**:
     - *Base*: +25% bonus XP points awarded in `PlayerXpEvent.PickupXp`.
     - *Ascended*: +150% bonus XP points (2.5x total), pulls `ExperienceOrb` entities within 8 blocks toward the player, and heals `0.05 HP` per XP value collected (0.5 heart per 20 XP).
  6. **VI - The Lovers**:
     - *Base*: Grants `MobEffects.REGENERATION` I to nearby allied players and tamed pets (`TamableAnimal` / `OwnableEntity`) within 8 blocks.
     - *Ascended*: Grants `MobEffects.REGENERATION` II to the player and allies/pets within 12 blocks; tamed pets owned by the player take 50% less incoming damage and deal +50% attack damage.
  7. **VII - The Chariot** *(Fix Missing Mechanics)*:
     - *Ascended*: Apply deterministic `AttributeModifier` (`+0.4`, `ADD_VALUE`) to `Attributes.STEP_HEIGHT` (raising step height from `0.6` to `1.0` full block) and remove it cleanly when inactive; apply `MobEffects.SPEED` II to the player's mount when riding a `LivingEntity`.
  8. **VIII - Strength**:
     - *Base*: +15% melee attack damage.
     - *Ascended*: +50% melee attack damage + attacks bypass 30% of the target's armor mitigation.
  9. **IX - The Hermit**:
     - *Base*: Dynamic `AttributeModifier` granting `+2.0` `Attributes.ARMOR` when no other players or villagers are within 16 blocks.
     - *Ascended*: Grants `+8.0` `Attributes.ARMOR`, `+4.0` `Attributes.ARMOR_TOUGHNESS`, and cancels hostile mob targeting (`LivingChangeTargetEvent`) beyond 8 blocks when isolated.
  10. **X - Wheel of Fortune**:
      - *Base*: Dynamic `AttributeModifier` granting `+0.5` `Attributes.LUCK` and 15% chance to duplicate non-equipment mob drops in `LivingDropsEvent`.
      - *Ascended*: Grants `+3.0` `Attributes.LUCK` and bonus loot rolls (+1 to +2 extra stack count on stackable mob drops and ore/resource drops).
  11. **XI - Justice**:
      - *Base*: Reflects 15% of incoming combat damage back to the attacker (with recursion guard).
      - *Ascended*: Reduces incoming combat damage by 15% and reflects 75% of the pre-mitigation damage back to the attacker as armor-bypassing True Damage (`genericKill` / magic damage with recursion guard).
  12. **XII - The Hanged Man**:
      - *Base*: Spawns `ExperienceOrb` worth 10% of damage taken (minimum 1 XP when damage >= 2).
      - *Ascended*: Spawns `ExperienceOrb` worth 50% of damage taken; while player HP is at or below 30% of Max HP, grants `MobEffects.RESISTANCE` III (amplifier 2) and +30% outgoing attack damage.
  13. **XIII - Death**:
      - *Base*: +15% damage against Undead mobs (`EntityTypeTags.UNDEAD`).
      - *Ascended*: +50% damage against all hostile/living targets, heals the player for 15% of damage dealt (Lifesteal) in `LivingDamageEvent.Post`, and instantly executes non-boss targets (`!target.getType().is(Tags.EntityTypes.BOSSES)` and `target.getMaxHealth() <= 100`) whose remaining HP after the hit would fall below 15%.
  14. **XIV - Temperance**:
      - *Base*: Reduces hunger exhaustion accumulation by 25% per tick.
      - *Ascended*: Reduces hunger exhaustion accumulation by 85% per tick; consuming any item with `DataComponents.FOOD` in `LivingEntityUseItemEvent.Finish` grants `MobEffects.ABSORPTION` II (60 seconds) and heals 4.0 HP (2 hearts).
  15. **XV - The Devil** *(Fix Missing Fire & Wither)*:
      - *Base*: Attacks set the target on fire for 3 seconds (`target.igniteForSeconds(3)`) and apply `MobEffects.WITHER` I (60 ticks, amplifier 0).
      - *Ascended*: Attacks set the target on fire for 6 seconds (`target.igniteForSeconds(6)`) and apply `MobEffects.WITHER` III (120 ticks, amplifier 2); grants the player 100% immunity to Wither damage and removes active `MobEffects.WITHER` on tick; deals +35% bonus damage to targets that are on fire or have any active `MobEffectCategory.HARMFUL` effect.
  16. **XVI - The Tower** *(Fix Missing Shockwave)*:
      - *Ascended*: When the player takes fall damage, explosion damage, or heavy damage (>= 6.0 pre-mitigation), triggers a non-griefing radial shockwave within 6 blocks that knocks back nearby hostile/living enemies and spawns explosion emitter particles.
  17. **XVII - The Star**:
      - *Base*: Dynamic `AttributeModifier` granting `+0.5` `Attributes.BLOCK_INTERACTION_RANGE`.
      - *Ascended*: Dynamic `AttributeModifier` granting `+2.5` `Attributes.BLOCK_INTERACTION_RANGE`, `+1.5` `Attributes.ENTITY_INTERACTION_RANGE`, and permanent `MobEffects.HASTE` II (amplifier 1).
  18. **XVIII - The Moon** *(Fix Missing Night Speed)*:
      - *Ascended*: Add `MobEffects.SPEED` I (`+20-25%` movement speed) alongside Strength I and Regeneration I when in darkness or at night.
  19. **XX - Judgement**:
      - *Base*: 10% chance on hit to deal 200% damage (2.0x multiplier) with critical hit sound/particles.
      - *Ascended*: 30% chance on hit to deal 250% damage (2.5x multiplier) and summon a visual-only `LightningBolt` (`setVisualOnly(true)`); if the player takes lethal damage (`health - incomingDamage <= 0`) while off cooldown (`TarotConfig.JUDGEMENT_TOTEM_COOLDOWN_SECONDS`), cancels death, sets HP to 2.0f, grants Regeneration II (45s), Absorption II (5s), and Fire Resistance I (40s), broadcasts totem animation (`(byte) 35`), and starts the cooldown timer.
  20. **XXI - The World** *(Fix Missing Projectile Slowdown)*:
      - *Ascended*: Slows incoming non-owned `Projectile` entities within 8 blocks by scaling their `deltaMovement` by `0.5` each tick.
- **Pure Logic Extraction & Unit Testing**:
  - Extract testable combat, attribute, and cooldown calculation helpers into `TarotEffectCalculator` (or static pure methods) and cover all 22 cards' effect calculations with unit tests in `TarotCardLogicTest.java`.

### Out of Scope
- Adding new Tarot Cards beyond the 22 Major Arcana.
- Changing card textures, models, or crafting recipes.
- Block-destroying explosions or griefing mechanics.

## Proposed Solution

### Approach
We will refactor `TarotEffectHandler` to systematically process all 22 Major Arcana cards across four lifecycle hooks without duplicating inventory scans or leaking attribute modifiers:
1. **Deterministic Attribute Reconciliation (`reconcileAttributes`)**:
   Every server tick (`PlayerTickEvent.Post`), reconcile `Identifier`-keyed `AttributeModifier` instances for:
   - `VII - The Chariot`: `Attributes.STEP_HEIGHT` (`+0.4` Ascended).
   - `IX - The Hermit`: `Attributes.ARMOR` (`+2.0` Base / `+8.0` Ascended when isolated) and `Attributes.ARMOR_TOUGHNESS` (`+4.0` Ascended when isolated).
   - `X - Wheel of Fortune`: `Attributes.LUCK` (`+0.5` Base / `+3.0` Ascended).
   - `XVII - The Star`: `Attributes.BLOCK_INTERACTION_RANGE` (`+0.5` Base / `+2.5` Ascended) and `Attributes.ENTITY_INTERACTION_RANGE` (`+1.5` Ascended).
   - `XIX - The Sun`: `Attributes.MAX_HEALTH` (`+4.0` Base / `+20.0` Ascended).
   Any modifier whose condition is no longer met is immediately removed on the same tick.

2. **Tick-Based Auras, Repair, Hunger & Physics (`applyServerEffects` & `onPlayerTickPre`)**:
   - Handle potion buffs (`Fool`, `Emperor`, `Lovers`, `Chariot` + mount speed, `Hanged Man` low-HP Resistance, `Star` Haste II, `Moon` Night Vision + Darkness Speed/Strength/Regen, `Devil` Wither cleanse).
   - Handle periodic world/inventory effects: `Magician` 5-second durability repair, `Empress` passive animal attraction & nearby baby growth boost, `Hierophant` 8-block XP orb magnet, `Temperance` hunger exhaustion reduction, and `World` 8-block hostile projectile slowdown.

3. **Combat & Damage Pipeline (`onIncomingDamage` & `onDamagePost`)**:
   - **Player as Attacker**: Apply `Strength` (+15%/+50% + 30% armor penetration), `Death` (+15% undead / +50% all + <15% HP non-boss execute), `Devil` (`igniteForSeconds(3/6)`, Wither I/III, +35% vs burning/debuffed targets), `Judgement` (10% 2.0x / 30% 2.5x + visual `LightningBolt`), `High Priestess` (+15% enchanted weapon bonus when Ascended), and `Magician` (12.5%/50% weapon durability preservation).
   - **Pet as Attacker / Victim**: Apply `Lovers` (+50% pet outgoing damage, -50% pet incoming damage).
   - **Player as Victim**: Apply `Fool` (fall damage), `Chariot` / `Sun` (fire damage), `Devil` (Wither damage immunity), `Tower` (explosion reduction/immunity + radial shockwave knockback), `Emperor` (Iron Golem damage immunity & aggro reset), `Justice` (15% thorns / 15% damage reduction + 75% true damage reflection with recursion guard), `Hanged Man` (+30% low-HP damage boost & post-damage XP orb spawn), and `Judgement` (Totem of Undying lethal damage interception with configurable cooldown).

4. **Specialized Gameplay Events**:
   - `AnvilUpdateEvent`: `High Priestess` (-15% / -50% XP cost discount).
   - `BabyEntitySpawnEvent`: `Empress` (-25% / -75% parent breeding cooldown + +25% / instant baby growth).
   - `PlayerXpEvent.PickupXp`: `Hierophant` (+25% / +150% bonus XP + Ascended healing).
   - `LivingEntityUseItemEvent.Finish`: `Temperance` (Ascended Absorption II + 4 HP instant heal on eating food).
   - `BreakBlockEvent`: `Magician` (12.5% / 50% tool durability preservation) and `Wheel of Fortune` (bonus ore/resource drop chance).
   - `LivingDropsEvent`: `Wheel of Fortune` (15% Base / Ascended bonus stackable mob loot drops).
   - `LivingChangeTargetEvent`: `Hermit` (Ascended mob detection range reduction when isolated).

### Impacted Files
| File | Change Type | Notes |
|------|-------------|-------|
| `src/main/java/com/tarotcardsrevamp/effect/TarotEffectHandler.java` | Modify | Implement all missing Base and Ascended card mechanics, attributes, combat hooks, and world events |
| `src/main/java/com/tarotcardsrevamp/effect/TarotEffectCalculator.java` | Create | Pure calculation helper for damage multipliers, reflection, XP conversion, anvil discounts, and cooldowns |
| `src/test/java/com/tarotcardsrevamp/TarotCardLogicTest.java` | Modify | Add unit tests verifying effect math, attribute values, reflection recursion guards, and Judgement cooldowns |
| `openspec/changes/init-tarot-cards-revamp/tasks.md` | Modify | Synchronize task status once all 22 card effects are implemented |

### Data Model Changes
No persistent `DataComponent` or network packet schema changes are required; all effects operate on the existing `ActiveCardResolver` state and `TarotConfig` values.

## Acceptance Criteria

- [x] **Given** a player has an active Ascended `the_chariot` **When** the player walks up a 1-block tall ledge or rides a mount **Then** the player's `Attributes.STEP_HEIGHT` is `1.0` (stepping up 1 full block without jumping) and the mount receives Speed II, and removing/deactivating the card immediately restores `STEP_HEIGHT` to `0.6`.
- [x] **Given** a player has an active `the_devil` **When** the player hits an enemy **Then** Base inflicts 3s fire and Wither I, while Ascended inflicts 6s fire, Wither III, deals +35% damage to burning/debuffed targets, and makes the player immune to Wither.
- [x] **Given** a player has an active `the_magician`, `the_high_priestess`, `the_empress`, `the_emperor`, `the_hierophant`, or `the_lovers` **When** performing the respective action (using tools, using an anvil, breeding animals, trading/fighting near Iron Golems, picking up XP orbs, or fighting alongside tamed pets) **Then** the corresponding Base (50%) or Ascended (100%) effect is applied accurately.
- [x] **Given** a player has an active `strength`, `the_hermit`, `wheel_of_fortune`, `justice`, `the_hanged_man`, `death`, `temperance`, `the_tower`, `the_star`, `the_moon`, `judgement`, or `the_world` **When** in combat, isolated, mining, eating, or taking lethal damage **Then** all promised attribute modifiers, reflections, lifesteal/executes, shockwaves, reach bonuses, and Totem of Undying effects trigger as described in the tooltip.

## Defensive Security & Validation Considerations

### Input Validation
- All combat multipliers, health restorations, and XP awards clamp values to non-negative bounds (`Math.max(0, ...)`).
- Reflection damage in `XI - Justice` uses a `ThreadLocal<Boolean>` recursion guard (`IS_REFLECTING`) so two entities with damage reflection cannot trigger an infinite `StackOverflowError` loop.
- Execute threshold in `XIII - Death` explicitly excludes bosses (`!target.getType().is(Tags.EntityTypes.BOSSES)`) and players to prevent griefing or one-shotting boss encounters.

### Secret Isolation
- No secrets, credentials, or external endpoints are used.

### Authorization & Authentication Impact
- N/A — all effects execute strictly on the authoritative logical server (`!level.isClientSide()`), preventing client-side spoofing of damage, healing, XP, or attribute modifiers.

### Other Security Risks
- **Lightning Griefing Prevention**: `XX - Judgement` sets `lightningBolt.setVisualOnly(true)` so lightning strikes never burn drops, start uncontrolled forest fires, or transform villagers into witches.
- **Shockwave Griefing Prevention**: `XVI - The Tower` uses entity velocity vectors and particles rather than `Level#explode` block destruction.

## Testing Strategy & Shift-Left Plan

### Happy Path Tests
| Test ID | Scenario | Test Type | Expected Result |
|---------|----------|-----------|-----------------|
| TP-12 | Attribute values for Chariot (Step Height), Hermit (Armor/Toughness), Wheel (Luck), Star (Reach), and Sun (Max HP) | Unit | Returns exact Base and Ascended modifier deltas (`+0.4` step, `+2/+8` armor, `+0.5/+3.0` luck, `+0.5/+2.5` block reach) |
| TP-13 | Outgoing combat damage calculations for Strength, Death, Devil, and High Priestess | Unit | Correctly computes +15%/+50% multipliers, armor bypass bonus, +35% debuffed bonus, and <15% HP non-boss execute |
| TP-14 | Incoming combat calculations for Justice reflection, Hanged Man XP conversion, and Lovers pet damage scaling | Unit | Computes 15%/75% reflection, 15% reduction, 10%/50% XP conversion, and 0.5x/1.5x pet damage multipliers |
| TP-15 | Anvil XP cost discount (High Priestess), Breeding ages (Empress), and XP pickup bonus/heal (Hierophant) | Unit | Discounts anvil cost by 15%/50% (min 1), sets parent/baby ages accurately, and computes 1.25x/2.5x XP and heal |

### Edge Case & Failure Mode Tests
| Test ID | Scenario | Test Type | Expected Result |
|---------|----------|-----------|-----------------|
| TE-05 | Justice reflection recursion guard and boss immunity to Death execute | Unit | Reflection does not recurse when already reflecting; bosses are never executed at <15% HP |
| TE-06 | Judgement Totem cooldown enforcement across timestamps | Unit | Triggers when off cooldown and blocks re-triggering until `cooldownSeconds` have elapsed |

### Test Commands
- **Run tests:** `.\gradlew test`

## Risks and Mitigations

| Risk | Likelihood | Impact | Mitigation |
|------|-----------|--------|------------|
| Infinite damage loop when reflecting damage (`XI - Justice`) | Med | High | Guard reflection behind a `ThreadLocal<Boolean>` flag so reflected hits never trigger secondary reflection |
| Attribute modifiers persisting after a card is removed or toggled off | Med | High | Reconcile all card attribute modifiers every server tick in a centralized `reconcileAttributes` method |
| `AnvilUpdateEvent` reducing anvil cost to 0 and breaking vanilla anvil extraction | Med | High | Clamp discounted anvil cost to `Math.max(1, discountedCost)` whenever original cost > 0 |

## Specification Changes & Delta Stubs

### `specs/tarot-cards-effects.md`
- **Target Canonical Spec:** `openspec/specs/tarot-cards-effects.md` (New Specification)
- **Summary of Updates:** Defines the authoritative functional and technical contract for all 22 Major Arcana Base and Ascended gameplay effects, attribute modifier IDs, and event hooks in Minecraft 26.3 / NeoForge 26.3.x.

## References

- [SDD Stack Contract](../../specs/sdd-stack-contract.md)
- [Init Tarot Cards Revamp Proposal](../init-tarot-cards-revamp/proposal.md)
