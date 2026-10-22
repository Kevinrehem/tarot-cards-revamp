# Minecraft & Mod Loader Technical Contract Rule

## 1. Mandatory Version & Loader Verification
Before generating, refactoring, or reviewing any Java code, Gradle script, or JSON resource/data pack file:
- Always verify `minecraft_version`, `neo_version`, `neo_form_version`, and the Java toolchain in `gradle.properties` and `build.gradle`.
- Activate and follow the `minecraft-neoforge-tech-spec` skill (`.agents/skills/minecraft-neoforge-tech-spec/SKILL.md`) whenever implementing or modifying registries, items, blocks, components, attachments, screens, events, loot modifiers, or resource/data files.

## 2. Strictly Forbidden Legacy APIs (Minecraft 26.3 / NeoForge 26.3.x)
NEVER hallucinate or use deprecated 1.20.x / 1.21.1 Forge/NeoForge APIs:
- **Identifiers**: NEVER use `ResourceLocation`; always use `net.minecraft.resources.Identifier` (`Identifier.fromNamespaceAndPath(modId, path)`).
- **Registries & IDs**: NEVER use `ForgeRegistries` or `RegistryObject<T>`. Always use `DeferredRegister.Items` (`registerItem`), `DeferredRegister.Blocks` (`registerBlock`), or `DeferredHolder<R, T>` so registry keys are pre-set on `Properties` before `super(properties)`.
- **Persistence**: NEVER use raw `ItemStack` NBT (`getOrCreateTag`) or legacy `ICapabilityProvider` / `LazyOptional`. Always use `DataComponentType<T>` (with `Codec` + `StreamCodec`) for items and `AttachmentType<T>` (with `MapCodec` + `StreamCodec`) for entities/players.
- **Item & Block Signatures**: `Item.use` returns `InteractionResult` (not `InteractionResultHolder`). `Item.appendHoverText` uses `(ItemStack, TooltipContext, TooltipDisplay, Consumer<Component>, TooltipFlag)`. Blocks use `useItemOn` and `useWithoutItem` returning `InteractionResult`.
- **Client GUI Rendering**: NEVER use `GuiGraphics`, `renderBg`, or `renderTooltip`. Always use `GuiGraphicsExtractor`, `extractBackground`, and `extractTooltip` (`setTooltipForNextFrame`).
- **Events & World**: Use top-level `@EventBusSubscriber(modid = ...)` (no `bus =` parameter), `BreakBlockEvent` (not `BlockEvent.BreakEvent`), `LivingIncomingDamageEvent` / `LivingDamageEvent.Post`, and `level.getDefaultClockTime()` (not `getDayTime()`).
- **Assets & Data Packs**: Every item requires `assets/<modid>/items/<id>.json` in addition to `models/item/<id>.json`. Vanilla data pack folders under `data/<modid>/` MUST be singular (`recipe/`, `advancement/`, `loot_table/`, `tags/item/`, `tags/block/`), while NeoForge loot modifiers remain `loot_modifiers/`.
