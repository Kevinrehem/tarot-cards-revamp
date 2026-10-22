---
id: "tarot-cards-effects"
title: "Tarot Cards Revamp — Major Arcana Effects Specification"
version: "1.0.0"
status: "draft"
owners:
  - "@jaca"
scope:
  paths:
    - "src/main/java/com/tarotcardsrevamp/effect/**"
resources: []
---

# Tarot Cards Revamp — Major Arcana Effects Specification

> This specification defines the authoritative behavioral contract for all 22 Major Arcana Base (50%) and Ascended (100%) effects in Minecraft `26.3` / NeoForge `26.3.0.95`.

---

## Context & Objectives

Each of the 22 Major Arcana Tarot Cards grants a passive effect when active in the player's inventory, Tarot Deck, or Curios slot. Duplicate copies of the same card do not stack (`ActiveCardResolver` resolves the highest tier, prioritizing Ascended over Base).

## Functional Requirements

- **[RF-EFF-00] 0 - The Fool**:
  - *Base*: `JUMP_BOOST` I, -25% fall damage.
  - *Ascended*: `JUMP_BOOST` III, 100% fall damage immunity, mid-air Double Jump.
- **[RF-EFF-01] I - The Magician**:
  - *Base*: 12.5% chance to preserve 1 durability on block break or melee hit.
  - *Ascended*: 50% chance to preserve 1 durability + repairs 1 durability every 100 ticks (5s) on held and armor items.
- **[RF-EFF-02] II - The High Priestess**:
  - *Base*: -15% anvil XP level cost (minimum 1).
  - *Ascended*: -50% anvil XP level cost (minimum 1) + +15% combat damage and +20% mining speed when using enchanted equipment.
- **[RF-EFF-03] III - The Empress**:
  - *Base*: -25% parent breeding cooldown (`4500` ticks) and +25% baby growth speed (`-18000` ticks).
  - *Ascended*: -75% parent breeding cooldown (`1500` ticks), instant baby growth (`0` age), and passive animals within 10 blocks follow the player.
- **[RF-EFF-04] IV - The Emperor**:
  - *Base*: `HERO_OF_THE_VILLAGE` I.
  - *Ascended*: `HERO_OF_THE_VILLAGE` V, immunity to `IronGolem` attacks, clears golem aggro, and rallies nearby Iron Golems (16 blocks) to attack enemies fighting the player.
- **[RF-EFF-05] V - The Hierophant**:
  - *Base*: +25% bonus XP points when picking up XP orbs.
  - *Ascended*: +150% bonus XP points, pulls XP orbs within 8 blocks toward the player, and heals `0.05 HP` per XP value collected.
- **[RF-EFF-06] VI - The Lovers**:
  - *Base*: `REGENERATION` I to nearby allies and tamed pets within 8 blocks.
  - *Ascended*: `REGENERATION` II to player, allies, and tamed pets within 12 blocks; tamed pets take -50% damage and deal +50% damage.
- **[RF-EFF-07] VII - The Chariot**:
  - *Base*: `SPEED` I.
  - *Ascended*: `SPEED` II, `+0.4` `Attributes.STEP_HEIGHT` (1.0 full block step assist), `SPEED` II to ridden mounts, fluid surface running on water and lava, and fire/magma immunity.
- **[RF-EFF-08] VIII - Strength**:
  - *Base*: +15% melee attack damage.
  - *Ascended*: +50% melee attack damage + 30% target armor bypass.
- **[RF-EFF-09] IX - The Hermit**:
  - *Base*: `+2.0` `Attributes.ARMOR` when isolated from other players and villagers within 16 blocks.
  - *Ascended*: `+8.0` `Attributes.ARMOR`, `+4.0` `Attributes.ARMOR_TOUGHNESS`, and cancels hostile mob targeting beyond 8 blocks when isolated.
- **[RF-EFF-10] X - Wheel of Fortune**:
  - *Base*: `+0.5` `Attributes.LUCK` and 15% chance for +1 extra drop on mob kills and block mining.
  - *Ascended*: `+3.0` `Attributes.LUCK` and guaranteed bonus loot rolls (+1 to +2 extra drops) on stackable mob drops and ore/resource blocks.
- **[RF-EFF-11] XI - Justice**:
  - *Base*: Reflects 15% of incoming combat damage back to the attacker.
  - *Ascended*: Reduces incoming combat damage by 15% and reflects 75% of pre-mitigation damage back to the attacker as True Damage (guarded against infinite reflection loops).
- **[RF-EFF-12] XII - The Hanged Man**:
  - *Base*: Spawns XP orbs equal to 10% of damage taken.
  - *Ascended*: Spawns XP orbs equal to 50% of damage taken; grants `RESISTANCE` III and +30% outgoing damage when player HP <= 30% of Max HP.
- **[RF-EFF-13] XIII - Death**:
  - *Base*: +15% damage against Undead mobs.
  - *Ascended*: +50% damage against all enemies, 15% Lifesteal on hit, and instant execution of non-boss enemies whose post-hit HP falls below 15%.
- **[RF-EFF-14] XIV - Temperance**:
  - *Base*: Reduces hunger exhaustion accumulation by 25%.
  - *Ascended*: Reduces hunger exhaustion accumulation by 85%; eating any food item grants `ABSORPTION` II (60s) and heals `4.0 HP`.
- **[RF-EFF-15] XV - The Devil**:
  - *Base*: Attacks ignite target for 3s and apply `WITHER` I (3s).
  - *Ascended*: Attacks ignite target for 6s and apply `WITHER` III (6s); grants 100% Wither immunity and +35% outgoing damage against burning or debuffed targets.
- **[RF-EFF-16] XVI - The Tower**:
  - *Base*: -25% explosion damage taken.
  - *Ascended*: 100% explosion immunity + radial non-griefing shockwave knockback (6 blocks) when taking fall, explosion, or heavy damage (>= 6.0).
- **[RF-EFF-17] XVII - The Star**:
  - *Base*: `+0.5` `Attributes.BLOCK_INTERACTION_RANGE`.
  - *Ascended*: `+2.5` `Attributes.BLOCK_INTERACTION_RANGE`, `+1.5` `Attributes.ENTITY_INTERACTION_RANGE`, and permanent `HASTE` II.
- **[RF-EFF-18] XVIII - The Moon**:
  - *Base*: Permanent `NIGHT_VISION`.
  - *Ascended*: `NIGHT_VISION` + `SPEED` I, `STRENGTH` I, and `REGENERATION` I while in darkness or at night.
- **[RF-EFF-19] XIX - The Sun**:
  - *Base*: `+4.0` `Attributes.MAX_HEALTH` (+2 Hearts).
  - *Ascended*: `+20.0` `Attributes.MAX_HEALTH` (+10 Hearts), passive sunlight regeneration, and fire resistance shield.
- **[RF-EFF-20] XX - Judgement**:
  - *Base*: 10% chance to deal 200% damage on hit.
  - *Ascended*: 30% chance to deal 250% damage on hit with visual-only lightning + built-in Totem of Undying effect (configurable cooldown, default 600s).
- **[RF-EFF-21] XXI - The World**:
  - *Base*: `SLOWNESS` I aura on enemies within 4 blocks.
  - *Ascended*: `SLOWNESS` IV and `MINING_FATIGUE` III aura on enemies within 8 blocks + slows incoming projectiles by 50% within 8 blocks.

## Non-Functional Requirements

- **[RNF-EFF-01] Deterministic Attribute Cleanup**: Every `AttributeModifier` must use a deterministic `net.minecraft.resources.Identifier` and be removed immediately on the tick a card becomes inactive.
- **[RNF-EFF-02] Recursion & Griefing Safety**: Damage reflection must never infinite-loop, lightning strikes must be visual-only (`setVisualOnly(true)`), and shockwaves must never break blocks.

## Revision History

| Version | Date | Author | Summary |
|---------|------|--------|---------|
| 1.0.0 | 2026-10-22 | @jaca | Initial specification of all 22 Major Arcana effects |
