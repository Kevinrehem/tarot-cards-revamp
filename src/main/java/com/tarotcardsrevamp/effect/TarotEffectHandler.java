package com.tarotcardsrevamp.effect;

import com.tarotcardsrevamp.card.TarotCardItem;
import com.tarotcardsrevamp.card.TarotCardType;
import com.tarotcardsrevamp.compat.CuriosCompat;
import com.tarotcardsrevamp.config.TarotConfig;
import com.tarotcardsrevamp.deck.TarotDeckItem;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.List;

public class TarotEffectHandler {

    public static ActiveCardResolver resolvePlayerActiveCards(Player player) {
        ActiveCardResolver resolver = new ActiveCardResolver();

        // 1. Scan player inventory
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.isEmpty()) continue;

            if (stack.getItem() instanceof TarotCardItem cardItem) {
                var data = TarotCardItem.getData(stack);
                resolver.offer(cardItem.getCardType(), data.active(), data.ascended(), data.questProgress());
            } else if (stack.getItem() instanceof TarotDeckItem) {
                var deck = TarotDeckItem.getData(stack);
                for (int s = 0; s < com.tarotcardsrevamp.deck.DeckDataComponent.TOTAL_SLOTS; s++) {
                    var entry = deck.getSlot(s);
                    if (entry.present()) {
                        TarotCardType cardType = TarotCardType.values()[s];
                        resolver.offer(cardType, entry.active(), entry.ascended(), entry.questProgress());
                    }
                }
            }
        }

        // 2. Scan Curios slots if installed
        CuriosCompat.scanCuriosSlots(player, resolver);

        return resolver;
    }

    @SubscribeEvent
    public static void onPlayerTickPre(PlayerTickEvent.Pre event) {
        Player player = event.getEntity();
        ActiveCardResolver active = resolvePlayerActiveCards(player);
        handleChariotFluidPhysics(player, active);
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        ActiveCardResolver active = resolvePlayerActiveCards(player);

        // Client & Server: Chariot fluid physics adjustment post-movement
        handleChariotFluidPhysics(player, active);

        // Server-only: Mob effects and area-of-effect auras
        if (!player.level().isClientSide()) {
            applyServerEffects(player, active);
        }
    }

    private static void handleChariotFluidPhysics(Player player, ActiveCardResolver active) {
        if (TarotConfig.ENABLE_ASCENDED_CHARIOT_FLUID_WALK != null && !TarotConfig.ENABLE_ASCENDED_CHARIOT_FLUID_WALK.get()) {
            return;
        }

        if (!active.isActive(TarotCardType.THE_CHARIOT) || !active.isAscended(TarotCardType.THE_CHARIOT)) {
            return;
        }

        if (player.getAbilities().flying) {
            return;
        }

        AABB bb = player.getBoundingBox();
        double feetY = bb.minY;

        // Check feet block and the block directly below feet
        BlockPos feetPos = BlockPos.containing(player.getX(), feetY, player.getZ());
        BlockPos belowFeetPos = BlockPos.containing(player.getX(), feetY - 0.2, player.getZ());

        FluidState fluidAtFeet = player.level().getFluidState(feetPos);
        FluidState fluidBelow = player.level().getFluidState(belowFeetPos);

        BlockPos targetFluidPos = null;
        FluidState targetFluid = null;

        if (!fluidAtFeet.isEmpty() && (fluidAtFeet.is(FluidTags.WATER) || fluidAtFeet.is(FluidTags.LAVA))) {
            targetFluidPos = feetPos;
            targetFluid = fluidAtFeet;
        } else if (!fluidBelow.isEmpty() && (fluidBelow.is(FluidTags.WATER) || fluidBelow.is(FluidTags.LAVA))) {
            targetFluidPos = belowFeetPos;
            targetFluid = fluidBelow;
        }

        if (targetFluid == null || targetFluidPos == null) {
            return;
        }

        double surfaceY = targetFluidPos.getY() + Math.max(0.875, (double) targetFluid.getHeight(player.level(), targetFluidPos));

        // Player is at or near the surface of the fluid
        if (feetY <= surfaceY + 0.2 && feetY >= surfaceY - 1.2) {
            boolean isMoving = player.isSprinting() || Math.hypot(player.getDeltaMovement().x, player.getDeltaMovement().z) > 0.05;

            if (isMoving) {
                // Ensure player doesn't sink beneath the surface
                if (player.getY() < surfaceY) {
                    player.setPos(player.getX(), surfaceY, player.getZ());
                }

                // Nullify downward velocity
                Vec3 motion = player.getDeltaMovement();
                if (motion.y < 0.0) {
                    player.setDeltaMovement(motion.x, 0.0, motion.z);
                }

                // Keep player considered on-ground for sprinting and reset fall damage
                player.setOnGround(true);
                player.resetFallDistance();

                // Counteract vanilla fluid sprint cancellation
                player.setSprinting(true);

                // Extinguish fire ticks
                player.clearFire();

                // Server-side fire resistance on lava
                if (!player.level().isClientSide() && targetFluid.is(FluidTags.LAVA)) {
                    player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 40, 0, true, false, false));
                }
            }
        }
    }

    private static void applyServerEffects(Player player, ActiveCardResolver active) {
        // 0 - The Fool
        if (active.isActive(TarotCardType.THE_FOOL)) {
            int amplifier = active.isAscended(TarotCardType.THE_FOOL) ? 2 : 0;
            player.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, 40, amplifier, true, false, true));
        }

        // VII - The Chariot
        if (active.isActive(TarotCardType.THE_CHARIOT)) {
            int amplifier = active.isAscended(TarotCardType.THE_CHARIOT) ? 1 : 0;
            player.addEffect(new MobEffectInstance(MobEffects.SPEED, 40, amplifier, true, false, true));
        }

        // XVIII - The Moon
        if (active.isActive(TarotCardType.THE_MOON)) {
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 240, 0, true, false, true));
            if (active.isAscended(TarotCardType.THE_MOON)) {
                long time = player.level().getDefaultClockTime() % 24000;
                boolean isNight = time >= 13000 && time <= 23000;
                if (isNight || player.level().getRawBrightness(player.blockPosition(), 0) < 5) {
                    player.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 40, 0, true, false, true));
                    player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 40, 0, true, false, true));
                }
            }
        }

        // XXI - The World (Temporal Aura)
        if (active.isActive(TarotCardType.THE_WORLD)) {
            double radius = active.isAscended(TarotCardType.THE_WORLD) ? 8.0 : 4.0;
            AABB area = player.getBoundingBox().inflate(radius);
            List<LivingEntity> nearby = player.level().getEntitiesOfClass(LivingEntity.class, area,
                    e -> e != player && !e.isAlliedTo(player));

            for (LivingEntity target : nearby) {
                int slownessAmp = active.isAscended(TarotCardType.THE_WORLD) ? 3 : 0;
                target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, slownessAmp, true, false));
                if (active.isAscended(TarotCardType.THE_WORLD)) {
                    target.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 40, 2, true, false));
                }
            }
        }
    }

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            ActiveCardResolver active = resolvePlayerActiveCards(player);

            // 0 - The Fool: Fall damage reduction or immunity
            if (event.getSource().is(DamageTypeTags.IS_FALL) && active.isActive(TarotCardType.THE_FOOL)) {
                if (active.isAscended(TarotCardType.THE_FOOL)) {
                    event.setCanceled(true);
                    return;
                } else {
                    event.setAmount(event.getAmount() * 0.75f);
                }
            }

            // VII - The Chariot: Fire/Lava/Magma damage immunity when Ascended
            if (active.isActive(TarotCardType.THE_CHARIOT) && active.isAscended(TarotCardType.THE_CHARIOT)) {
                if (event.getSource().is(DamageTypeTags.IS_FIRE)) {
                    event.setCanceled(true);
                    return;
                }
            }

            // XVI - The Tower: Explosion immunity / reduction
            if (event.getSource().is(DamageTypeTags.IS_EXPLOSION) && active.isActive(TarotCardType.THE_TOWER)) {
                if (active.isAscended(TarotCardType.THE_TOWER)) {
                    event.setCanceled(true);
                    return;
                } else {
                    event.setAmount(event.getAmount() * 0.75f);
                }
            }
        }
    }
}
