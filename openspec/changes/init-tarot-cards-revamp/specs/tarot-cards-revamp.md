---
id: "tarot-cards-revamp"
title: "Tarot Cards Revamp — Living Specification"
version: "1.0.0"
status: "draft"
owners:
  - "@jaca"
scope:
  paths:
    - "src/main/java/com/tarotcardsrevamp/**"
    - "src/main/resources/**"
resources: []
---

# Tarot Cards Revamp — Living Specification

> This is the authoritative source of truth for this module's current functional state.
> Update this file only after merging a completed change from `openspec/changes/`.

---

## Context & Objectives

**Tarot Cards Revamp** (`tarotcardsrevamp`) is a Minecraft `26.3` (NeoForge `26.3.0.95`) mod inspired by the classic Tarot Cards mod, reimagined with an item-based Quest and Ascension progression system. Players discover the 22 Major Arcana cards in structure loot chests, store them in a craftable Tarot Deck (compatible with Curios API or standard inventory), and complete thematic quests for each card to ascend it from 50% base strength to an overpowered Awakened/Ascended form.

## Functional Requirements

- **[RF-01]** **22 Major Arcana Cards**: Provide items for all 22 Major Arcana (`0 - The Fool` through `XXI - The World`), each starting at 50% Base strength and evolving into an Ascended version upon completing its card-specific quest.
- **[RF-02]** **Item-Bound Quest Progression**: Store `active` state, `questProgress`, and `ascended` state in a custom `DataComponentType` on each card's `ItemStack`, rendering a progress bar and current tier in the item tooltip.
- **[RF-03]** **Automatic Ascension & Advancements**: Automatically ascend a card when its quest reaches 100%, playing an ascension sound, enabling enchantment glint, and awarding custom Advancements:
  - Common (`task`): Discover 1 card, 10 unique cards, 17 unique cards.
  - Rare (`goal`): 22 individual card ascension advancements.
  - Legendary (`challenge`): Discover all 22 unique cards; Ascend all 22 unique cards.
- **[RF-04]** **Tarot Deck & GUI**: Craftable with 1 Chest (center), 4 Paper (cross), and 4 Gold Nuggets (corners). Opens a 22-slot GUI where each slot corresponds to one Major Arcana card, with an Auto-Collect button and per-card toggle support. Works in player inventory and Curios slot.
- **[RF-05]** **Ascended Chariot Fluid Sprint**: While Ascended `the_chariot` is active and the player is sprinting, the player runs across water and lava surfaces without sinking or taking lava/magma contact damage.
- **[RF-06]** **Loot & Duplicate Recycling**: Inject cards into structure loot chests via Global Loot Modifiers with smart weighting toward undiscovered cards, and allow recycling 3 duplicate cards into an Unidentified Tarot Card.
- **[RF-07]** **Configuration & Admin Commands**: Expose configurable quest goals, buff multipliers, and loot rates via NeoForge Config, and provide `/tarot` OP commands for testing.

## Non-Functional Requirements

- **[RNF-01]** **Soft Curios Dependency**: Must run without Curios API installed and seamlessly enable Curios slot detection when Curios API is loaded.
- **[RNF-02]** **Server Authority & Performance**: Tick evaluation must deduplicate active cards in $O(N)$ inventory scan and clean up attribute modifiers deterministically without memory leaks.
- **[RNF-03]** **Bilingual Localization**: All items, tooltips, GUI labels, and advancements must be localized in `en_us.json` and `pt_br.json`.

## Out of Scope

- Minor Arcana (56 suited cards).
- Destructive block griefing from player abilities.

## Revision History

| Version | Date | Author | Summary |
|---------|------|--------|---------|
| 1.0.0 | 2026-10-21 | @jaca | Initial draft |
