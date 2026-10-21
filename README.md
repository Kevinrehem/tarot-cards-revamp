# Tarot Cards Revamp (`tarotcardsrevamp`)

A reimagining of the classic Tarot Cards mod for **Minecraft 26.3** on **NeoForge 26.3.0.95** (requires **Java 25**).

Each of the 22 Major Arcana cards starts at **50% strength** and has an ascension quest that unlocks a much stronger form.

---

## Implementation status

The mod loads and the basic items work. Most gameplay is **still in progress** — see
[`openspec/changes/init-tarot-cards-revamp/tasks.md`](openspec/changes/init-tarot-cards-revamp/tasks.md) for the authoritative checklist.

| Area | Status |
|---|---|
| 22 cards + Unidentified card + Tarot Deck items, creative tab, EN/PT-BR lang, placeholder textures | Done |
| Right-click toggle Active/Inactive, quest tooltip, ascended glint | Done |
| Crafting: Tarot Deck (chest center, 4 paper cross, 4 gold nuggets corners) and recycling any 3 cards + 1 gold ingot | Done |
| `/tarot giveall`, `/tarot ascend_held` (OP level 2) | Done |
| Buffs | Partial: The Fool, The Chariot (incl. ascended fluid sprint), The Moon, The World, The Tower |
| Quest progress | Partial: The Star, Death, The Hanged Man |
| Tarot Deck GUI (22 slots), persistence, auto-collect | Not started (right-click shows a "coming soon" message) |
| Loot in structure chests | Not started |
| Curios API slot | Not started |
| Advancements (found 1/10/17/22, per-card ascension, all ascended) | JSON present, never granted yet |
| Config (`TarotConfig`) | Registered, values not yet used |

## Building and testing

Requires JDK 25 (configured via `org.gradle.java.installations.paths` in `gradle.properties`).

```bash
./gradlew build                 # compile + JUnit tests -> build/libs/tarotcardsrevamp-1.0.0.jar
./gradlew runGameTestServer     # smoke test: boots registries/datapacks headless
```
