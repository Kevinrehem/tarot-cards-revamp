package com.tarotcardsrevamp.effect;

import com.tarotcardsrevamp.card.CardDataComponent;
import com.tarotcardsrevamp.card.TarotCardItem;
import com.tarotcardsrevamp.card.TarotCardType;
import com.tarotcardsrevamp.compat.CuriosCompat;
import com.tarotcardsrevamp.deck.DeckDataComponent;
import com.tarotcardsrevamp.deck.TarotDeckItem;
import com.tarotcardsrevamp.deck.TarotDeckMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractChestBlock;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.BabyEntitySpawnEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;
import net.neoforged.neoforge.event.entity.player.CriticalHitEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.player.PlayerXpEvent;
import net.neoforged.neoforge.event.entity.player.TradeWithVillagerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TarotQuestHandler {

    private static final Map<UUID, Vec3> lastPlayerPositions = new ConcurrentHashMap<>();
    private static final Map<UUID, Double> chariotDistanceAccumulator = new ConcurrentHashMap<>();
    private static final Map<UUID, Double> moonDistanceAccumulator = new ConcurrentHashMap<>();
    private static final Map<UUID, Double> sunDistanceAccumulator = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> chestCooldown = new ConcurrentHashMap<>();

    public static void progressQuest(Player player, TarotCardType targetCard, int amount) {
        if (player == null || player.level().isClientSide() || amount <= 0 || targetCard == null) return;

        boolean[] triggeredAscension = new boolean[]{false};
        int goal = targetCard.getDefaultGoal();

        // 1. Scan player main, armor, and offhand inventory
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.isEmpty()) continue;

            if (stack.getItem() instanceof TarotCardItem cardItem && cardItem.getCardType() == targetCard) {
                CardDataComponent data = TarotCardItem.getData(stack);
                if (data.active() && !data.ascended()) {
                    CardDataComponent updated = data.addProgress(amount, goal);
                    TarotCardItem.setData(stack, updated);

                    if (updated.ascended() && !triggeredAscension[0]) {
                        triggeredAscension[0] = true;
                        triggerAscension(player, targetCard);
                    }
                }
            } else if (stack.getItem() instanceof TarotDeckItem) {
                DeckDataComponent deck = TarotDeckItem.getData(stack);
                DeckDataComponent.DeckSlotEntry entry = deck.getSlot(targetCard);
                if (entry.present() && entry.active() && !entry.ascended()) {
                    DeckDataComponent updated = deck.addProgress(targetCard, amount, goal);
                    TarotDeckItem.setData(stack, updated);

                    if (updated.getSlot(targetCard).ascended() && !triggeredAscension[0]) {
                        triggeredAscension[0] = true;
                        triggerAscension(player, targetCard);
                    }
                }
            }
        }

        // 2. If the player currently has the TarotDeckMenu open, update open menu slots
        if (player.containerMenu instanceof TarotDeckMenu deckMenu) {
            int slotIdx = targetCard.ordinal();
            if (slotIdx >= 0 && slotIdx < DeckDataComponent.TOTAL_SLOTS) {
                ItemStack slotStack = deckMenu.getSlot(slotIdx).getItem();
                if (!slotStack.isEmpty() && slotStack.getItem() instanceof TarotCardItem cardItem && cardItem.getCardType() == targetCard) {
                    CardDataComponent data = TarotCardItem.getData(slotStack);
                    if (data.active() && !data.ascended()) {
                        CardDataComponent updated = data.addProgress(amount, goal);
                        TarotCardItem.setData(slotStack, updated);
                        deckMenu.getSlot(slotIdx).setChanged();

                        if (updated.ascended() && !triggeredAscension[0]) {
                            triggeredAscension[0] = true;
                            triggerAscension(player, targetCard);
                        }
                    }
                }
            }
        }

        // 3. Scan Curios slots if equipped
        CuriosCompat.forEachCuriosStack(player, stack -> {
            if (stack.getItem() instanceof TarotCardItem cardItem && cardItem.getCardType() == targetCard) {
                CardDataComponent data = TarotCardItem.getData(stack);
                if (data.active() && !data.ascended()) {
                    CardDataComponent updated = data.addProgress(amount, goal);
                    TarotCardItem.setData(stack, updated);

                    if (updated.ascended() && !triggeredAscension[0]) {
                        triggeredAscension[0] = true;
                        triggerAscension(player, targetCard);
                    }
                }
            } else if (stack.getItem() instanceof TarotDeckItem) {
                DeckDataComponent deck = TarotDeckItem.getData(stack);
                DeckDataComponent.DeckSlotEntry entry = deck.getSlot(targetCard);
                if (entry.present() && entry.active() && !entry.ascended()) {
                    DeckDataComponent updated = deck.addProgress(targetCard, amount, goal);
                    TarotDeckItem.setData(stack, updated);

                    if (updated.getSlot(targetCard).ascended() && !triggeredAscension[0]) {
                        triggeredAscension[0] = true;
                        triggerAscension(player, targetCard);
                    }
                }
            }
        });
    }

    private static void triggerAscension(Player player, TarotCardType card) {
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0f, 1.0f);

        player.sendSystemMessage(Component.translatable("tarotcardsrevamp.status.ascended")
                .append(": " + card.getEnglishName())
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));

        player.sendOverlayMessage(Component.translatable("tarotcardsrevamp.status.ascended")
                .append(": " + card.getEnglishName())
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID uuid = event.getEntity().getUUID();
        lastPlayerPositions.remove(uuid);
        chariotDistanceAccumulator.remove(uuid);
        moonDistanceAccumulator.remove(uuid);
        sunDistanceAccumulator.remove(uuid);
        chestCooldown.remove(uuid);
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;

        UUID uuid = player.getUUID();
        Vec3 currentPos = player.position();
        Vec3 lastPos = lastPlayerPositions.put(uuid, currentPos);

        // IX - The Hermit: Solitary survival (every 20 ticks = 1 second)
        if (player.tickCount % 20 == 0) {
            AABB isolationZone = player.getBoundingBox().inflate(64.0);
            List<Player> nearby = player.level().getEntitiesOfClass(Player.class, isolationZone,
                    p -> p != player && !p.isSpectator());
            if (nearby.isEmpty()) {
                progressQuest(player, TarotCardType.THE_HERMIT, 20);
            }
        }

        if (lastPos == null) return;

        double dx = currentPos.x - lastPos.x;
        double dz = currentPos.z - lastPos.z;
        double horizontalDist = Math.sqrt(dx * dx + dz * dz);

        // Guard against teleports or standing still
        if (horizontalDist <= 0.0001 || horizontalDist > 16.0) return;

        // VII - The Chariot: Sprinting, mounted or sprint-swimming distance
        if (player.isSprinting() || player.isPassenger() || player.isSwimming()) {
            double accumulated = chariotDistanceAccumulator.compute(uuid, (k, v) -> (v == null ? 0.0 : v) + horizontalDist);
            if (accumulated >= 1.0) {
                int blocks = (int) Math.floor(accumulated);
                chariotDistanceAccumulator.put(uuid, accumulated - blocks);
                progressQuest(player, TarotCardType.THE_CHARIOT, blocks);
            }
        }

        // XVIII - The Moon: Distance traveled in night or dark conditions
        long timeOfDay = player.level().getDefaultClockTime() % 24000;
        boolean isNight = timeOfDay >= 13000 && timeOfDay <= 23000;
        boolean isDark = player.level().getRawBrightness(player.blockPosition(), 0) < 5;
        if (isNight || isDark) {
            double accumulated = moonDistanceAccumulator.compute(uuid, (k, v) -> (v == null ? 0.0 : v) + horizontalDist);
            if (accumulated >= 1.0) {
                int blocks = (int) Math.floor(accumulated);
                moonDistanceAccumulator.put(uuid, accumulated - blocks);
                progressQuest(player, TarotCardType.THE_MOON, blocks);
            }
        }

        // XIX - The Sun: Distance traveled in open daylight
        boolean isDay = timeOfDay < 13000 || timeOfDay > 23000;
        boolean canSeeSky = player.level().canSeeSky(player.blockPosition());
        if (isDay && canSeeSky) {
            double accumulated = sunDistanceAccumulator.compute(uuid, (k, v) -> (v == null ? 0.0 : v) + horizontalDist);
            if (accumulated >= 1.0) {
                int blocks = (int) Math.floor(accumulated);
                sunDistanceAccumulator.put(uuid, accumulated - blocks);
                progressQuest(player, TarotCardType.THE_SUN, blocks);
            }
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BreakBlockEvent event) {
        progressQuest(event.getPlayer(), TarotCardType.THE_STAR, 1);
        progressQuest(event.getPlayer(), TarotCardType.THE_MAGICIAN, 1);
    }

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getEntity() instanceof Player player) {
            progressQuest(player, TarotCardType.THE_STAR, 1);
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getSource().getEntity() instanceof Player player) {
            // XIII - Death: Hostile mobs
            if (event.getEntity() instanceof Enemy) {
                progressQuest(player, TarotCardType.DEATH, 1);
            }

            // XV - The Devil: Nether dimension or fire/burning kills
            if (player.level().dimension() == Level.NETHER || event.getSource().is(DamageTypeTags.IS_FIRE) || event.getEntity().isOnFire()) {
                progressQuest(player, TarotCardType.THE_DEVIL, 1);
            }

            // XXI - The World: Mobs killed across dimensions
            progressQuest(player, TarotCardType.THE_WORLD, 1);
        }
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Post event) {
        // Player as Victim
        if (event.getEntity() instanceof Player player) {
            int damage = (int) Math.ceil(event.getInflictedDamage());

            // XII - The Hanged Man: All damage taken
            progressQuest(player, TarotCardType.THE_HANGED_MAN, Math.max(1, damage));

            // 0 - The Fool: Fall damage taken
            if (event.getSource().is(DamageTypeTags.IS_FALL)) {
                progressQuest(player, TarotCardType.THE_FOOL, Math.max(1, damage));
            }

            // XI - Justice: Combat damage taken
            if (event.getSource().getEntity() != null) {
                progressQuest(player, TarotCardType.JUSTICE, Math.max(1, damage));
            }

            // XVI - The Tower: Explosion survived
            if (event.getSource().is(DamageTypeTags.IS_EXPLOSION)) {
                progressQuest(player, TarotCardType.THE_TOWER, 1);
            }
        }

        // Player as Attacker (Melee damage dealt)
        if (event.getSource().getEntity() instanceof Player player) {
            if (!event.getSource().is(DamageTypeTags.IS_PROJECTILE)) {
                int damage = (int) Math.ceil(event.getInflictedDamage());
                progressQuest(player, TarotCardType.STRENGTH, Math.max(1, damage));
                progressQuest(player, TarotCardType.THE_MAGICIAN, 1);
            }
        }
    }

    @SubscribeEvent
    public static void onFoodEaten(LivingEntityUseItemEvent.Finish event) {
        if (event.getEntity() instanceof Player player) {
            if (event.getItem().has(DataComponents.FOOD)) {
                progressQuest(player, TarotCardType.TEMPERANCE, 1);
            }
        }
    }

    @SubscribeEvent
    public static void onAnimalBred(BabyEntitySpawnEvent event) {
        if (event.getCausedByPlayer() instanceof Player player) {
            progressQuest(player, TarotCardType.THE_EMPRESS, 1);
        }
    }

    @SubscribeEvent
    public static void onXpPickup(PlayerXpEvent.PickupXp event) {
        int val = event.getOrb().getValue();
        if (val > 0) {
            progressQuest(event.getEntity(), TarotCardType.THE_HIEROPHANT, val);
        }
    }

    @SubscribeEvent
    public static void onXpLevelChange(PlayerXpEvent.LevelChange event) {
        if (event.getLevels() > 0) {
            progressQuest(event.getEntity(), TarotCardType.THE_HIGH_PRIESTESS, event.getLevels());
        }
    }

    @SubscribeEvent
    public static void onVillagerTrade(TradeWithVillagerEvent event) {
        if (event.getEntity() instanceof Player player) {
            progressQuest(player, TarotCardType.THE_EMPEROR, 1);
        }
    }

    @SubscribeEvent
    public static void onHeal(LivingHealEvent event) {
        if (event.getEntity() instanceof Player player) {
            progressQuest(player, TarotCardType.THE_LOVERS, Math.max(1, (int) event.getAmount()));
        } else if (event.getEntity() != null) {
            AABB radius = event.getEntity().getBoundingBox().inflate(8.0);
            List<Player> allies = event.getEntity().level().getEntitiesOfClass(Player.class, radius,
                    p -> !p.isSpectator() && (event.getEntity().isAlliedTo(p) || p.isPassengerOfSameVehicle(event.getEntity())));
            for (Player ally : allies) {
                progressQuest(ally, TarotCardType.THE_LOVERS, Math.max(1, (int) event.getAmount()));
            }
        }
    }

    @SubscribeEvent
    public static void onCriticalHit(CriticalHitEvent event) {
        if (event.isVanillaCritical()) {
            progressQuest(event.getEntity(), TarotCardType.JUDGEMENT, 1);
        }
    }

    @SubscribeEvent
    public static void onChestOpened(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide()) return;
        var state = event.getLevel().getBlockState(event.getPos());
        if (state.getBlock() instanceof AbstractChestBlock<?> || state.getBlock() instanceof BarrelBlock || state.getBlock() instanceof ShulkerBoxBlock) {
            Player player = event.getEntity();
            long now = player.level().getGameTime();
            long last = chestCooldown.getOrDefault(player.getUUID(), 0L);
            if (now - last > 20L) {
                chestCooldown.put(player.getUUID(), now);
                progressQuest(player, TarotCardType.WHEEL_OF_FORTUNE, 1);
            }
        }
    }
}
