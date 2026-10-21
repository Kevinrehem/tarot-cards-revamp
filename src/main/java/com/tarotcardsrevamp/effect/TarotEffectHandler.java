package com.tarotcardsrevamp.effect;

import com.tarotcardsrevamp.card.TarotCardItem;
import com.tarotcardsrevamp.card.TarotCardType;
import com.tarotcardsrevamp.compat.CuriosCompat;
import com.tarotcardsrevamp.deck.TarotDeckItem;
import net.minecraft.core.BlockPos;
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
                // Inspect deck components if present
            }
        }

        // 2. Scan Curios slots if installed
        CuriosCompat.scanCuriosSlots(player, resolver);

        return resolver;
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;

        ActiveCardResolver active = resolvePlayerActiveCards(player);

        // 0 - The Fool
        if (active.isActive(TarotCardType.THE_FOOL)) {
            int amplifier = active.isAscended(TarotCardType.THE_FOOL) ? 2 : 0;
            player.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, 40, amplifier, true, false, true));
        }

        // VII - The Chariot
        if (active.isActive(TarotCardType.THE_CHARIOT)) {
            int amplifier = active.isAscended(TarotCardType.THE_CHARIOT) ? 1 : 0;
            player.addEffect(new MobEffectInstance(MobEffects.SPEED, 40, amplifier, true, false, true));

            // Ascended Chariot Sprint Fluid-Walking
            if (active.isAscended(TarotCardType.THE_CHARIOT) && player.isSprinting()) {
                BlockPos under = player.blockPosition().below();
                FluidState fluid = player.level().getFluidState(under);
                if (!fluid.isEmpty()) {
                    Vec3 delta = player.getDeltaMovement();
                    if (delta.y < 0.0) {
                        player.setDeltaMovement(delta.x, 0.0, delta.z);
                        player.setOnGround(true);
                    }
                    player.clearFire();
                }
            }
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
            if (event.getSource().is(net.minecraft.tags.DamageTypeTags.IS_FALL) && active.isActive(TarotCardType.THE_FOOL)) {
                if (active.isAscended(TarotCardType.THE_FOOL)) {
                    event.setCanceled(true);
                    return;
                } else {
                    event.setAmount(event.getAmount() * 0.75f);
                }
            }

            // XVI - The Tower: Explosion immunity / reduction
            if (event.getSource().is(net.minecraft.tags.DamageTypeTags.IS_EXPLOSION) && active.isActive(TarotCardType.THE_TOWER)) {
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
