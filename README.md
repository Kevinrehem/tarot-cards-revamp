# Tarot Cards Revamp (`tarotcardsrevamp`)

A modern reimagining of the classic Tarot Cards mod for **Minecraft 26.3** running on **NeoForge 26.3.0.95**.

---

## 🌟 Features

- **22 Major Arcana Tarot Cards (0 - The Fool to XXI - The World)**:
  - Each card begins at **50% base strength**.
  - Right-click in hand to toggle **Active / Inactive**.
  - Active cards track in-game progression via `DataComponents` on the `ItemStack`.
  - Upon reaching **100% of the quest goal**, the card automatically **Awakens / Ascends** into an overpowered form with enchanted glint and ascension sound effects!
- **Ascended Powers**:
  - **The Chariot (VII)**: Sprint across water and lava without sinking or burning!
  - **The World (XXI)**: Creates a temporal aura inflicting Slowness IV and Mining Fatigue III on nearby enemies.
  - **The Fool (0)**: 100% Fall damage immunity + Jump Boost III.
  - And 19 more unique ascended abilities.
- **Tarot Deck**:
  - Dedicated container storing all 22 Major Arcana.
  - Craftable via: 1 Chest (center), 4 Paper (cross), 4 Gold Nuggets (corners).
  - Works in player inventory and has soft compatibility with the **Curios API** accessory slot.
- **Loot & Card Recycling**:
  - Generates in structure chests (Villages, Mineshafts, Dungeons, Temples, Strongholds, End Cities).
  - Smart loot weighting prioritizes cards you have not yet found.
  - Recycle **any 3 Tarot Cards** + 1 Gold Ingot into an **Unidentified Tarot Card**!
- **Advancements**:
  - Task: Find 1, 10, and 17 unique Tarot Cards.
  - Goal: 22 individual advancements for each card ascended.
  - Challenge: Master of Destiny (Collect all 22) & God of Fate (Ascend all 22).
- **Admin Commands**:
  - `/tarot giveall`: Grants all 22 cards and the Tarot Deck (OP level 2).
  - `/tarot ascend_held`: Instantly completes the quest of the held card (OP level 2).
- **Localization**:
  - Full translations in English (`en_us`) and Brazilian Portuguese (`pt_br`).

---

## 🛠️ Building & Testing

Run unit tests via Maven or Gradle:

```bash
mvn -f pom-test.xml test
```
