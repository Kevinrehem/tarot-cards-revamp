# SDD Stack Contract

> This file declares the technology stack and architectural constraints of this project.
> It is read by skills and subagents to make stack-aware decisions.
> Fill in each section and commit this file to version control.

---

## Stack Identity

- **Project Name:** Tarot Cards Revamp (`tarotcardsrevamp`)
- **Primary Language:** Java 21
- **Runtime / Platform:** JVM 21 / Minecraft 26.3
- **Framework:** NeoForge 26.3.0.95 (with optional Curios API integration)
- **Package Manager:** Gradle (ModDevGradle)

---

## Project Structure

- **Source Root:** `src/main/java/com/tarotcardsrevamp/`
- **Resources Root:** `src/main/resources/`
- **Test Root:** `src/test/java/com/tarotcardsrevamp/`
- **Entry Point:** `src/main/java/com/tarotcardsrevamp/TarotCardsRevampMod.java`
- **Build Output:** `build/libs/`

---

## Test Runner

- **Command:** `gradle test`
- **Test Framework:** JUnit 5 (Jupiter) & NeoForge GameTest Framework
- **Coverage Command:** `gradle test jacocoTestReport`

---

## Code Style

- **Linter:** javac (`-Xlint:all`)
- **Formatter:** Standard Java 21 / 4-space indentation
- **Format Command:** N/A

---

## API Contract (if applicable)

- **API Type:** Minecraft Mod (NeoForge Registries, DataComponents, Events, Networking Payload, Curios Soft-Integration)
- **Spec Location:** `openspec/specs/tarot-cards-revamp.md`
- **Base URL (dev):** N/A (Local Client/Server Run Configurations)

---

## Database (if applicable)

- **Engine:** Minecraft NBT / Codec-backed `DataComponentType` (on `ItemStack`) and `AttachmentType` (on `Player` for unique cards discovered/ascended tracking)
- **Migration Tool:** Mojang DataFixerUpper / Codec fallbacks
- **Migration Command:** N/A

---

## Notes

- Mod ID: `tarotcardsrevamp`
- Base Package: `com.tarotcardsrevamp`
- Localization required in both `en_us.json` and `pt_br.json`.
- Curios API must be handled as a soft/optional integration so the mod functions standalone in standard inventory as well as inside the Curios `tarot_deck` / `curio` slot when Curios is loaded.
