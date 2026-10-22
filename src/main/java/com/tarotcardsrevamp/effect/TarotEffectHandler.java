package com.tarotcardsrevamp.effect;

import com.tarotcardsrevamp.card.TarotCardItem;
import com.tarotcardsrevamp.card.TarotCardType;
import com.tarotcardsrevamp.compat.CuriosCompat;
import com.tarotcardsrevamp.config.TarotConfig;
import com.tarotcardsrevamp.deck.DeckDataComponent;
import com.tarotcardsrevamp.deck.TarotDeckItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.BabyEntitySpawnEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerXpEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TarotEffectHandler {

    public static final Identifier SUN_HEALTH_MODIFIER_ID = Identifier.fromNamespaceAndPath("tarotcardsrevamp", "the_sun_max_health");
    public static final Identifier CHARIOT_STEP_HEIGHT_ID = Identifier.fromNamespaceAndPath("tarotcardsrevamp", "the_chariot_step_height");
    public static final Identifier HERMIT_ARMOR_ID = Identifier.fromNamespaceAndPath("tarotcardsrevamp", "the_hermit_armor");
    public static final Identifier HERMIT_TOUGHNESS_ID = Identifier.fromNamespaceAndPath("tarotcardsrevamp", "the_hermit_armor_toughness");
    public static final Identifier WHEEL_LUCK_ID = Identifier.fromNamespaceAndPath("tarotcardsrevamp", "wheel_of_fortune_luck");
    public static final Identifier STAR_BLOCK_REACH_ID = Identifier.fromNamespaceAndPath("tarotcardsrevamp", "the_star_block_reach");
    public static final Identifier STAR_ENTITY_REACH_ID = Identifier.fromNamespaceAndPath("tarotcardsrevamp", "the_star_entity_reach");

    private static final ThreadLocal<Boolean> IS_REFLECTING = ThreadLocal.withInitial(() -> false);
    private static final Map<UUID, Long> judgementCooldowns = new ConcurrentHashMap<>();
    private static final Map<UUID, Float> lastPlayerSaturation = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> lastPlayerFoodLevel = new ConcurrentHashMap<>();

    public static ActiveCardResolver resolvePlayerActiveCards(Player player) {
        ActiveCardResolver resolver = new ActiveCardResolver();
        if (player == null) return resolver;

        // 1. Scan player inventory
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.isEmpty()) continue;

            if (stack.getItem() instanceof TarotCardItem cardItem) {
                var data = TarotCardItem.getData(stack);
                resolver.offer(cardItem.getCardType(), data.active(), data.ascended(), data.questProgress());
            } else if (stack.getItem() instanceof TarotDeckItem) {
                var deck = TarotDeckItem.getData(stack);
                for (int s = 0; s < DeckDataComponent.TOTAL_SLOTS; s++) {
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
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID uuid = event.getEntity().getUUID();
        lastPlayerSaturation.remove(uuid);
        lastPlayerFoodLevel.remove(uuid);
    }

    @SubscribeEvent
    public static void onPlayerTickPre(PlayerTickEvent.Pre event) {
        Player player = event.getEntity();
        ActiveCardResolver active = resolvePlayerActiveCards(player);

        // Apply Step Height & Reach on both Client and Server for immediate physics/interaction response
        updateAttribute(player, Attributes.STEP_HEIGHT, CHARIOT_STEP_HEIGHT_ID,
                TarotEffectCalculator.getChariotStepHeightBonus(
                        active.isActive(TarotCardType.THE_CHARIOT),
                        active.isAscended(TarotCardType.THE_CHARIOT)));

        updateAttribute(player, Attributes.BLOCK_INTERACTION_RANGE, STAR_BLOCK_REACH_ID,
                TarotEffectCalculator.getStarBlockReachBonus(
                        active.isActive(TarotCardType.THE_STAR),
                        active.isAscended(TarotCardType.THE_STAR)));

        updateAttribute(player, Attributes.ENTITY_INTERACTION_RANGE, STAR_ENTITY_REACH_ID,
                TarotEffectCalculator.getStarEntityReachBonus(
                        active.isActive(TarotCardType.THE_STAR),
                        active.isAscended(TarotCardType.THE_STAR)));

        handleChariotFluidPhysics(player, active);
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        ActiveCardResolver active = resolvePlayerActiveCards(player);

        // Client & Server: Chariot fluid physics adjustment post-movement
        handleChariotFluidPhysics(player, active);

        // Server-only: Attributes, Mob effects, Auras, Repair, and Hunger management
        if (!player.level().isClientSide()) {
            reconcileServerAttributes(player, active);
            applyServerEffects(player, active);
        }
    }

    private static void reconcileServerAttributes(Player player, ActiveCardResolver active) {
        // VII - The Chariot: Step Height (+0.4 -> 1.0 block)
        updateAttribute(player, Attributes.STEP_HEIGHT, CHARIOT_STEP_HEIGHT_ID,
                TarotEffectCalculator.getChariotStepHeightBonus(
                        active.isActive(TarotCardType.THE_CHARIOT),
                        active.isAscended(TarotCardType.THE_CHARIOT)));

        // IX - The Hermit: Armor & Toughness when isolated from players and villagers (16 blocks)
        boolean hermitActive = active.isActive(TarotCardType.THE_HERMIT);
        boolean hermitAscended = active.isAscended(TarotCardType.THE_HERMIT);
        boolean isolated = hermitActive && isPlayerIsolated(player, 16.0);
        updateAttribute(player, Attributes.ARMOR, HERMIT_ARMOR_ID,
                TarotEffectCalculator.getHermitArmorBonus(hermitActive, hermitAscended, isolated));
        updateAttribute(player, Attributes.ARMOR_TOUGHNESS, HERMIT_TOUGHNESS_ID,
                TarotEffectCalculator.getHermitArmorToughnessBonus(hermitActive, hermitAscended, isolated));

        // X - Wheel of Fortune: Luck (+0.5 Base / +3.0 Ascended)
        updateAttribute(player, Attributes.LUCK, WHEEL_LUCK_ID,
                TarotEffectCalculator.getWheelLuckBonus(
                        active.isActive(TarotCardType.WHEEL_OF_FORTUNE),
                        active.isAscended(TarotCardType.WHEEL_OF_FORTUNE)));

        // XVII - The Star: Block & Entity Interaction Reach
        updateAttribute(player, Attributes.BLOCK_INTERACTION_RANGE, STAR_BLOCK_REACH_ID,
                TarotEffectCalculator.getStarBlockReachBonus(
                        active.isActive(TarotCardType.THE_STAR),
                        active.isAscended(TarotCardType.THE_STAR)));
        updateAttribute(player, Attributes.ENTITY_INTERACTION_RANGE, STAR_ENTITY_REACH_ID,
                TarotEffectCalculator.getStarEntityReachBonus(
                        active.isActive(TarotCardType.THE_STAR),
                        active.isAscended(TarotCardType.THE_STAR)));

        // XIX - The Sun: Max Health (+4.0 Base / +20.0 Ascended)
        handleSunHealth(player, active);
    }

    private static void updateAttribute(Player player, Holder<Attribute> attribute, Identifier modifierId, double desiredBonus) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) return;

        AttributeModifier current = instance.getModifier(modifierId);
        if (desiredBonus > 0.0) {
            if (current == null || Math.abs(current.amount() - desiredBonus) > 0.0001) {
                instance.removeModifier(modifierId);
                instance.addPermanentModifier(new AttributeModifier(modifierId, desiredBonus, AttributeModifier.Operation.ADD_VALUE));
            }
        } else if (current != null) {
            instance.removeModifier(modifierId);
        }
    }

    private static boolean isPlayerIsolated(Player player, double radius) {
        AABB box = player.getBoundingBox().inflate(radius);
        List<LivingEntity> others = player.level().getEntitiesOfClass(LivingEntity.class, box,
                e -> e != player && !e.isSpectator() && (e instanceof Player || e instanceof AbstractVillager));
        return others.isEmpty();
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

        if (feetY <= surfaceY + 0.2 && feetY >= surfaceY - 1.2) {
            boolean isMoving = player.isSprinting() || Math.hypot(player.getDeltaMovement().x, player.getDeltaMovement().z) > 0.05;

            if (isMoving) {
                if (player.getY() < surfaceY) {
                    player.setPos(player.getX(), surfaceY, player.getZ());
                }

                Vec3 motion = player.getDeltaMovement();
                if (motion.y < 0.0) {
                    player.setDeltaMovement(motion.x, 0.0, motion.z);
                }

                player.setOnGround(true);
                player.resetFallDistance();
                player.setSprinting(true);
                player.clearFire();

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

        // I - The Magician (Ascended): Repair 1 durability on held & equipped items every 5s (100 ticks)
        if (active.isActive(TarotCardType.THE_MAGICIAN) && active.isAscended(TarotCardType.THE_MAGICIAN)) {
            if (player.tickCount % 100 == 0) {
                repairPlayerEquipment(player);
            }
        }

        // III - The Empress: Nearby baby growth acceleration & Ascended animal attraction
        if (active.isActive(TarotCardType.THE_EMPRESS) && player.tickCount % 20 == 0) {
            boolean ascended = active.isAscended(TarotCardType.THE_EMPRESS);
            double radius = ascended ? 10.0 : 8.0;
            List<AgeableMob> animals = player.level().getEntitiesOfClass(AgeableMob.class, player.getBoundingBox().inflate(radius));
            for (AgeableMob animal : animals) {
                if (animal instanceof AbstractVillager) continue;
                if (animal.isBaby()) {
                    if (ascended) {
                        animal.setAge(0);
                    } else {
                        animal.ageUp(1);
                    }
                } else if (ascended && animal.getAge() > 1500) {
                    animal.setAge(1500);
                }

                if (ascended && !(animal instanceof Enemy) && animal.distanceToSqr(player) > 6.25) {
                    animal.getNavigation().moveTo(player, 1.15);
                }
            }
        }

        // IV - The Emperor: Hero of the Village I (Base) / V (Ascended)
        if (active.isActive(TarotCardType.THE_EMPEROR)) {
            int amplifier = active.isAscended(TarotCardType.THE_EMPEROR) ? 4 : 0;
            player.addEffect(new MobEffectInstance(MobEffects.HERO_OF_THE_VILLAGE, 60, amplifier, true, false, true));
        }

        // V - The Hierophant (Ascended): Long-range XP orb magnet (8 blocks)
        if (active.isActive(TarotCardType.THE_HIEROPHANT) && active.isAscended(TarotCardType.THE_HIEROPHANT)) {
            List<ExperienceOrb> orbs = player.level().getEntitiesOfClass(ExperienceOrb.class, player.getBoundingBox().inflate(8.0));
            Vec3 targetPos = player.position().add(0.0, 0.5, 0.0);
            for (ExperienceOrb orb : orbs) {
                Vec3 diff = targetPos.subtract(orb.position());
                double dist = diff.length();
                if (dist > 0.1) {
                    Vec3 pull = diff.normalize().scale(0.35);
                    orb.setDeltaMovement(orb.getDeltaMovement().scale(0.6).add(pull));
                }
            }
        }

        // VI - The Lovers: Regeneration aura to allies and tamed pets
        if (active.isActive(TarotCardType.THE_LOVERS) && player.tickCount % 20 == 0) {
            boolean ascended = active.isAscended(TarotCardType.THE_LOVERS);
            int amp = ascended ? 1 : 0;
            double radius = ascended ? 12.0 : 8.0;
            if (ascended) {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 1, true, false, true));
            }
            List<LivingEntity> allies = player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(radius),
                    e -> e != player && isAlliedOrOwnedBy(e, player));
            for (LivingEntity ally : allies) {
                ally.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, amp, true, false, true));
            }
        }

        // VII - The Chariot: Speed I/II + Mount Speed when Ascended
        if (active.isActive(TarotCardType.THE_CHARIOT)) {
            boolean ascended = active.isAscended(TarotCardType.THE_CHARIOT);
            int amplifier = ascended ? 1 : 0;
            player.addEffect(new MobEffectInstance(MobEffects.SPEED, 40, amplifier, true, false, true));
            if (ascended && player.getVehicle() instanceof LivingEntity mount) {
                mount.addEffect(new MobEffectInstance(MobEffects.SPEED, 40, 1, true, false, false));
            }
        }

        // XII - The Hanged Man (Ascended): Resistance III when below 30% HP
        if (TarotEffectCalculator.isHangedManLowHealthActive(
                player.getHealth(), player.getMaxHealth(),
                active.isActive(TarotCardType.THE_HANGED_MAN),
                active.isAscended(TarotCardType.THE_HANGED_MAN))) {
            player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 40, 2, true, false, true));
        }

        // XIV - Temperance: Reduce hunger consumption (-25% Base / -85% Ascended)
        handleTemperanceHunger(player, active);

        // XV - The Devil (Ascended): Wither immunity cleanse
        if (active.isActive(TarotCardType.THE_DEVIL) && active.isAscended(TarotCardType.THE_DEVIL)) {
            if (player.hasEffect(MobEffects.WITHER)) {
                player.removeEffect(MobEffects.WITHER);
            }
        }

        // XVII - The Star (Ascended): Permanent Haste II
        if (active.isActive(TarotCardType.THE_STAR) && active.isAscended(TarotCardType.THE_STAR)) {
            player.addEffect(new MobEffectInstance(MobEffects.HASTE, 40, 1, true, false, true));
        }

        // XVIII - The Moon: Night Vision + Darkness Speed I, Strength I, Regeneration I
        if (active.isActive(TarotCardType.THE_MOON)) {
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 240, 0, true, false, true));
            if (active.isAscended(TarotCardType.THE_MOON)) {
                long time = player.level().getDefaultClockTime() % 24000;
                boolean isNight = time >= 13000 && time <= 23000;
                if (isNight || player.level().getRawBrightness(player.blockPosition(), 0) < 5) {
                    player.addEffect(new MobEffectInstance(MobEffects.SPEED, 40, 0, true, false, true));
                    player.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 40, 0, true, false, true));
                    player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 40, 0, true, false, true));
                }
            }
        }

        // XXI - The World (Temporal Aura + Projectile Slowdown)
        if (active.isActive(TarotCardType.THE_WORLD)) {
            boolean ascended = active.isAscended(TarotCardType.THE_WORLD);
            double radius = ascended ? 8.0 : 4.0;
            AABB area = player.getBoundingBox().inflate(radius);
            List<LivingEntity> nearby = player.level().getEntitiesOfClass(LivingEntity.class, area,
                    e -> e != player && !isAlliedOrOwnedBy(e, player));

            for (LivingEntity target : nearby) {
                int slownessAmp = ascended ? 3 : 0;
                target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, slownessAmp, true, false));
                if (ascended) {
                    target.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 40, 2, true, false));
                }
            }

            if (ascended) {
                List<Projectile> projectiles = player.level().getEntitiesOfClass(Projectile.class, area,
                        p -> p.getOwner() != player);
                for (Projectile proj : projectiles) {
                    proj.setDeltaMovement(proj.getDeltaMovement().scale(0.5));
                }
            }
        }
    }

    private static void repairPlayerEquipment(Player player) {
        EquipmentSlot[] slots = new EquipmentSlot[]{
                EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND,
                EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
        };
        for (EquipmentSlot slot : slots) {
            ItemStack stack = player.getItemBySlot(slot);
            if (!stack.isEmpty() && stack.isDamageableItem() && stack.getDamageValue() > 0) {
                stack.setDamageValue(stack.getDamageValue() - 1);
            }
        }
    }

    private static void handleTemperanceHunger(Player player, ActiveCardResolver active) {
        UUID uuid = player.getUUID();
        FoodData foodData = player.getFoodData();
        float currentSaturation = foodData.getSaturationLevel();
        int currentFood = foodData.getFoodLevel();

        Float prevSat = lastPlayerSaturation.get(uuid);
        Integer prevFood = lastPlayerFoodLevel.get(uuid);

        if (active.isActive(TarotCardType.TEMPERANCE)) {
            boolean ascended = active.isAscended(TarotCardType.TEMPERANCE);
            if (prevSat != null && currentSaturation < prevSat) {
                float delta = prevSat - currentSaturation;
                float refund = TarotEffectCalculator.computeTemperanceExhaustionReduction(delta, true, ascended);
                currentSaturation = Math.min(currentFood, currentSaturation + refund);
                foodData.setSaturation(currentSaturation);
            }
            if (prevFood != null && currentFood < prevFood) {
                double saveChance = ascended ? 0.85 : 0.25;
                if (player.getRandom().nextDouble() < saveChance) {
                    currentFood = prevFood;
                    foodData.setFoodLevel(currentFood);
                }
            }
        }

        lastPlayerSaturation.put(uuid, currentSaturation);
        lastPlayerFoodLevel.put(uuid, currentFood);
    }

    private static boolean isAlliedOrOwnedBy(LivingEntity entity, Player player) {
        if (entity.isAlliedTo(player)) return true;
        if (entity instanceof OwnableEntity ownable && ownable.getOwner() == player) {
            return true;
        }
        return false;
    }

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide()) return;

        // 1. Outgoing attacks from a Player
        if (event.getSource().getEntity() instanceof Player attacker) {
            handlePlayerOutgoingAttack(event, attacker, victim);
        }

        // 2. Outgoing attacks from a Player's Pet (VI - The Lovers Ascended: +50% pet damage)
        if (event.getSource().getEntity() instanceof OwnableEntity pet && pet.getOwner() instanceof Player owner) {
            ActiveCardResolver ownerCards = resolvePlayerActiveCards(owner);
            if (ownerCards.isActive(TarotCardType.THE_LOVERS) && ownerCards.isAscended(TarotCardType.THE_LOVERS)) {
                event.setAmount(TarotEffectCalculator.computeLoversPetOutgoingDamage(event.getAmount(), true, true));
            }
        }

        // 3. Incoming damage to a Player's Pet (VI - The Lovers Ascended: -50% pet damage taken)
        if (victim instanceof OwnableEntity pet && pet.getOwner() instanceof Player owner) {
            ActiveCardResolver ownerCards = resolvePlayerActiveCards(owner);
            if (ownerCards.isActive(TarotCardType.THE_LOVERS) && ownerCards.isAscended(TarotCardType.THE_LOVERS)) {
                event.setAmount(TarotEffectCalculator.computeLoversPetIncomingDamage(event.getAmount(), true, true));
            }
        }

        // 4. Incoming damage to a Player
        if (victim instanceof Player player) {
            handlePlayerIncomingDamage(event, player);
        }
    }

    private static void handlePlayerOutgoingAttack(LivingIncomingDamageEvent event, Player attacker, LivingEntity target) {
        ActiveCardResolver active = resolvePlayerActiveCards(attacker);
        float damage = event.getAmount();
        boolean isMelee = !event.getSource().is(DamageTypeTags.IS_PROJECTILE);

        // II - The High Priestess (Ascended): +15% damage when attacking with an enchanted weapon
        if (active.isActive(TarotCardType.THE_HIGH_PRIESTESS) && active.isAscended(TarotCardType.THE_HIGH_PRIESTESS)) {
            boolean hasEnchantments = !attacker.getMainHandItem().getEnchantments().isEmpty();
            damage = TarotEffectCalculator.computeHighPriestessEnchantedDamage(damage, true, true, hasEnchantments);
        }

        // VIII - Strength: +15% (Base) / +50% + 30% armor penetration (Ascended) on melee attacks
        if (isMelee && active.isActive(TarotCardType.STRENGTH)) {
            double targetArmor = target.getAttributeValue(Attributes.ARMOR);
            damage = TarotEffectCalculator.computeStrengthMeleeDamage(
                    damage, targetArmor, true, active.isAscended(TarotCardType.STRENGTH));
        }

        // XII - The Hanged Man (Ascended): +30% damage when below 30% HP
        if (active.isActive(TarotCardType.THE_HANGED_MAN)) {
            damage = TarotEffectCalculator.computeHangedManOutgoingDamage(
                    damage, attacker.getHealth(), attacker.getMaxHealth(),
                    true, active.isAscended(TarotCardType.THE_HANGED_MAN));
        }

        // XV - The Devil: Ignite (3s/6s), Wither I/III, and +35% damage vs burning/debuffed targets
        if (active.isActive(TarotCardType.THE_DEVIL)) {
            boolean ascended = active.isAscended(TarotCardType.THE_DEVIL);
            boolean targetDebuffed = target.isOnFire() || target.getActiveEffects().stream()
                    .anyMatch(e -> !e.getEffect().value().isBeneficial());
            damage = TarotEffectCalculator.computeDevilOutgoingDamage(damage, targetDebuffed, true, ascended);

            int fireSeconds = TarotEffectCalculator.getDevilFireSeconds(true, ascended);
            int witherAmp = TarotEffectCalculator.getDevilWitherAmplifier(true, ascended);
            target.igniteForSeconds(fireSeconds);
            if (witherAmp >= 0) {
                target.addEffect(new MobEffectInstance(MobEffects.WITHER, fireSeconds * 20, witherAmp));
            }
        }

        // XIII - Death: +15% vs Undead (Base) / +50% vs all + <15% HP non-boss execute (Ascended)
        if (active.isActive(TarotCardType.DEATH)) {
            boolean ascended = active.isAscended(TarotCardType.DEATH);
            boolean isUndead = target.getType().builtInRegistryHolder().is(EntityTypeTags.UNDEAD);
            damage = TarotEffectCalculator.computeDeathOutgoingDamage(damage, isUndead, true, ascended);

            boolean isBossOrPlayer = (target instanceof Player) || target.getMaxHealth() > 100.0f;
            if (TarotEffectCalculator.shouldExecuteTarget(
                    target.getHealth(), target.getMaxHealth(), damage, isBossOrPlayer, true, ascended)) {
                damage = target.getHealth() + 100.0f;
            }
        }

        // XX - Judgement: 10% for 200% damage (Base) / 30% for 250% damage + safe lightning (Ascended)
        if (active.isActive(TarotCardType.JUDGEMENT)) {
            boolean ascended = active.isAscended(TarotCardType.JUDGEMENT);
            double chance = TarotEffectCalculator.getJudgementCritChance(true, ascended);
            if (attacker.getRandom().nextDouble() < chance) {
                damage = TarotEffectCalculator.computeJudgementCritDamage(damage, true, ascended);
                if (ascended && attacker.level() instanceof ServerLevel serverLevel) {
                    LightningBolt bolt = EntityTypes.LIGHTNING_BOLT.create(serverLevel, EntitySpawnReason.TRIGGERED);
                    if (bolt != null) {
                        bolt.setPos(target.getX(), target.getY(), target.getZ());
                        bolt.setVisualOnly(true);
                        serverLevel.addFreshEntity(bolt);
                    }
                }
            }
        }

        // IV - The Emperor (Ascended): Rally nearby Iron Golems to attack the target
        if (active.isActive(TarotCardType.THE_EMPEROR) && active.isAscended(TarotCardType.THE_EMPEROR)) {
            if (!(target instanceof IronGolem) && !(target instanceof Player)) {
                rallyIronGolems(attacker, target);
            }
        }

        event.setAmount(damage);
    }

    private static void handlePlayerIncomingDamage(LivingIncomingDamageEvent event, Player player) {
        ActiveCardResolver active = resolvePlayerActiveCards(player);
        float originalDamage = event.getAmount();

        // 0 - The Fool: Fall damage reduction or immunity
        if (event.getSource().is(DamageTypeTags.IS_FALL) && active.isActive(TarotCardType.THE_FOOL)) {
            if (active.isAscended(TarotCardType.THE_FOOL)) {
                if (active.isActive(TarotCardType.THE_TOWER) && active.isAscended(TarotCardType.THE_TOWER) && originalDamage >= 3.0f) {
                    triggerTowerShockwave(player);
                }
                event.setCanceled(true);
                return;
            } else {
                event.setAmount(event.getAmount() * 0.75f);
            }
        }

        // IV - The Emperor (Ascended): Iron Golem immunity & aggro clear, plus rally golems against attackers
        if (active.isActive(TarotCardType.THE_EMPEROR) && active.isAscended(TarotCardType.THE_EMPEROR)) {
            if (event.getSource().getEntity() instanceof IronGolem golem) {
                golem.setTarget(null);
                event.setCanceled(true);
                return;
            } else if (event.getSource().getEntity() instanceof LivingEntity attackerEntity && !(attackerEntity instanceof Player)) {
                rallyIronGolems(player, attackerEntity);
            }
        }

        // VII - The Chariot & XIX - The Sun: Fire/Lava/Magma damage immunity when Ascended
        if (event.getSource().is(DamageTypeTags.IS_FIRE)) {
            if ((active.isActive(TarotCardType.THE_CHARIOT) && active.isAscended(TarotCardType.THE_CHARIOT))
                    || (active.isActive(TarotCardType.THE_SUN) && active.isAscended(TarotCardType.THE_SUN))) {
                event.setCanceled(true);
                return;
            }
        }

        // XV - The Devil (Ascended): Wither damage immunity
        if (active.isActive(TarotCardType.THE_DEVIL) && active.isAscended(TarotCardType.THE_DEVIL)) {
            if (event.getSource().is(DamageTypes.WITHER)) {
                event.setCanceled(true);
                return;
            }
        }

        // XVI - The Tower: Explosion immunity / reduction + Ascended Shockwave
        if (active.isActive(TarotCardType.THE_TOWER)) {
            boolean ascended = active.isAscended(TarotCardType.THE_TOWER);
            if (event.getSource().is(DamageTypeTags.IS_EXPLOSION)) {
                if (ascended) {
                    triggerTowerShockwave(player);
                    event.setCanceled(true);
                    return;
                } else {
                    event.setAmount(event.getAmount() * 0.75f);
                }
            } else if (ascended && (originalDamage >= 6.0f || (event.getSource().is(DamageTypeTags.IS_FALL) && originalDamage >= 3.0f))) {
                triggerTowerShockwave(player);
            }
        }

        // XI - Justice: Damage reflection (15% Base / 75% True Damage + 15% damage reduction Ascended)
        if (active.isActive(TarotCardType.JUSTICE) && event.getSource().getEntity() instanceof LivingEntity attacker && attacker != player) {
            boolean ascended = active.isAscended(TarotCardType.JUSTICE);
            event.setAmount(TarotEffectCalculator.computeJusticeIncomingDamage(event.getAmount(), true, ascended));

            if (!IS_REFLECTING.get() && player.level() instanceof ServerLevel serverLevel) {
                float reflected = TarotEffectCalculator.computeJusticeReflectedDamage(originalDamage, true, ascended);
                if (reflected > 0.0f) {
                    try {
                        IS_REFLECTING.set(true);
                        var source = ascended
                                ? player.damageSources().genericKill()
                                : player.damageSources().thorns(player);
                        attacker.hurtServer(serverLevel, source, reflected);
                    } finally {
                        IS_REFLECTING.set(false);
                    }
                }
            }
        }

        // XX - Judgement (Ascended): Built-in Totem of Undying on lethal damage
        if (active.isActive(TarotCardType.JUDGEMENT) && active.isAscended(TarotCardType.JUDGEMENT)) {
            if (!event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)
                    && (player.getHealth() - event.getAmount()) <= 0.0f) {
                int cooldownSec = TarotConfig.JUDGEMENT_TOTEM_COOLDOWN_SECONDS != null
                        ? TarotConfig.JUDGEMENT_TOTEM_COOLDOWN_SECONDS.get() : 600;
                long now = System.currentTimeMillis();
                Long lastUsed = judgementCooldowns.get(player.getUUID());
                if (TarotEffectCalculator.isJudgementTotemReady(lastUsed, now, cooldownSec)) {
                    judgementCooldowns.put(player.getUUID(), now);
                    event.setCanceled(true);
                    player.setHealth(2.0f);
                    player.removeAllEffects();
                    player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 900, 1));
                    player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100, 1));
                    player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 800, 0));
                    player.level().broadcastEntityEvent(player, (byte) 35);
                }
            }
        }
    }

    private static void triggerTowerShockwave(Player player) {
        AABB area = player.getBoundingBox().inflate(6.0);
        List<LivingEntity> targets = player.level().getEntitiesOfClass(LivingEntity.class, area,
                e -> e != player && !isAlliedOrOwnedBy(e, player));
        for (LivingEntity target : targets) {
            double dx = target.getX() - player.getX();
            double dz = target.getZ() - player.getZ();
            double dist = Math.hypot(dx, dz);
            if (dist > 0.001) {
                target.push((dx / dist) * 1.6, 0.45, (dz / dist) * 1.6);
            }
        }
        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.EXPLOSION,
                    player.getX(), player.getY() + 0.5, player.getZ(),
                    6, 1.5, 0.2, 1.5, 0.05);
            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.7f, 1.2f);
        }
    }

    private static void rallyIronGolems(Player player, LivingEntity enemy) {
        List<IronGolem> golems = player.level().getEntitiesOfClass(IronGolem.class, player.getBoundingBox().inflate(16.0));
        for (IronGolem golem : golems) {
            golem.setTarget(enemy);
        }
    }

    @SubscribeEvent
    public static void onDamagePost(LivingDamageEvent.Post event) {
        if (event.getEntity().level().isClientSide()) return;
        float inflicted = event.getInflictedDamage();
        if (inflicted <= 0.0f) return;

        // Player as Victim: XII - The Hanged Man (Convert 10% / 50% damage taken to XP orbs)
        if (event.getEntity() instanceof Player victimPlayer && victimPlayer.level() instanceof ServerLevel serverLevel) {
            ActiveCardResolver victimActive = resolvePlayerActiveCards(victimPlayer);
            if (victimActive.isActive(TarotCardType.THE_HANGED_MAN)) {
                int xp = TarotEffectCalculator.computeHangedManXp(
                        inflicted, true, victimActive.isAscended(TarotCardType.THE_HANGED_MAN));
                if (xp > 0) {
                    ExperienceOrb.award(serverLevel, victimPlayer.position(), xp);
                }
            }
        }

        // Player as Attacker: XIII - Death Lifesteal (15% Ascended) & I - The Magician Durability Save
        if (event.getSource().getEntity() instanceof Player attackerPlayer) {
            ActiveCardResolver attackerActive = resolvePlayerActiveCards(attackerPlayer);

            if (attackerActive.isActive(TarotCardType.DEATH) && attackerActive.isAscended(TarotCardType.DEATH)) {
                float heal = TarotEffectCalculator.computeDeathLifesteal(inflicted, true, true);
                if (heal > 0.0f) {
                    attackerPlayer.heal(heal);
                }
            }

            if (attackerActive.isActive(TarotCardType.THE_MAGICIAN)) {
                tryPreserveHeldDurability(attackerPlayer, attackerActive.isAscended(TarotCardType.THE_MAGICIAN));
            }
        }
    }

    private static void tryPreserveHeldDurability(Player player, boolean ascended) {
        ItemStack mainHand = player.getMainHandItem();
        if (!mainHand.isEmpty() && mainHand.isDamageableItem() && mainHand.getDamageValue() > 0) {
            double chance = TarotEffectCalculator.getMagicianDurabilitySaveChance(true, ascended);
            if (player.getRandom().nextDouble() < chance) {
                mainHand.setDamageValue(mainHand.getDamageValue() - 1);
            }
        }
    }

    @SubscribeEvent
    public static void onBreakBlock(BreakBlockEvent event) {
        Player player = event.getPlayer();
        if (player == null || player.level().isClientSide()) return;

        ActiveCardResolver active = resolvePlayerActiveCards(player);

        // I - The Magician: 12.5% / 50% chance to preserve tool durability
        if (active.isActive(TarotCardType.THE_MAGICIAN)) {
            tryPreserveHeldDurability(player, active.isAscended(TarotCardType.THE_MAGICIAN));
        }

        // X - Wheel of Fortune: Bonus XP/drop on ore mining
        if (active.isActive(TarotCardType.WHEEL_OF_FORTUNE) && player.level() instanceof ServerLevel serverLevel) {
            boolean ascended = active.isAscended(TarotCardType.WHEEL_OF_FORTUNE);
            double rollChance = ascended ? 0.50 : 0.15;
            if (player.getRandom().nextDouble() < rollChance && event.getState().getDestroySpeed(serverLevel, event.getPos()) > 1.0f) {
                ExperienceOrb.award(serverLevel, Vec3.atCenterOf(event.getPos()), ascended ? 3 : 1);
            }
        }
    }

    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();
        ActiveCardResolver active = resolvePlayerActiveCards(player);

        // II - The High Priestess (Ascended): Boost mining speed by +20% when holding an enchanted tool
        if (active.isActive(TarotCardType.THE_HIGH_PRIESTESS) && active.isAscended(TarotCardType.THE_HIGH_PRIESTESS)) {
            if (!player.getMainHandItem().getEnchantments().isEmpty()) {
                event.setNewSpeed(event.getNewSpeed() * 1.20f);
            }
        }
    }

    @SubscribeEvent
    public static void onXpLevelChange(PlayerXpEvent.LevelChange event) {
        Player player = event.getEntity();
        // II - The High Priestess: Discount levels spent at an Anvil (-15% Base / -50% Ascended)
        if (event.getLevels() < 0 && player.containerMenu instanceof AnvilMenu) {
            ActiveCardResolver active = resolvePlayerActiveCards(player);
            if (active.isActive(TarotCardType.THE_HIGH_PRIESTESS)) {
                int originalCost = -event.getLevels();
                int discountedCost = TarotEffectCalculator.computeDiscountedAnvilCost(
                        originalCost, true, active.isAscended(TarotCardType.THE_HIGH_PRIESTESS));
                event.setLevels(-discountedCost);
            }
        }
    }

    @SubscribeEvent
    public static void onXpPickup(PlayerXpEvent.PickupXp event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;

        int orbVal = event.getOrb().getValue();
        if (orbVal <= 0) return;

        ActiveCardResolver active = resolvePlayerActiveCards(player);
        if (active.isActive(TarotCardType.THE_HIEROPHANT)) {
            boolean ascended = active.isAscended(TarotCardType.THE_HIEROPHANT);
            int bonusXp = TarotEffectCalculator.computeHierophantBonusXp(orbVal, true, ascended);
            if (bonusXp > 0) {
                player.giveExperiencePoints(bonusXp);
            }
            if (ascended) {
                float heal = TarotEffectCalculator.computeHierophantHeal(orbVal, true, true);
                if (heal > 0.0f) {
                    player.heal(heal);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onBabySpawn(BabyEntitySpawnEvent event) {
        if (!(event.getCausedByPlayer() instanceof Player player) || player.level().isClientSide()) return;

        ActiveCardResolver active = resolvePlayerActiveCards(player);
        if (active.isActive(TarotCardType.THE_EMPRESS)) {
            boolean ascended = active.isAscended(TarotCardType.THE_EMPRESS);
            int parentCooldown = TarotEffectCalculator.computeEmpressParentBreedingCooldown(6000, true, ascended);
            if (event.getParentA() instanceof AgeableMob parentA) {
                parentA.setAge(parentCooldown);
            }
            if (event.getParentB() instanceof AgeableMob parentB) {
                parentB.setAge(parentCooldown);
            }
            if (event.getChild() != null) {
                int babyAge = TarotEffectCalculator.computeEmpressBabyStartingAge(-24000, true, ascended);
                event.getChild().setAge(babyAge);
            }
        }
    }

    @SubscribeEvent
    public static void onFoodEaten(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide()) return;

        if (event.getItem().has(DataComponents.FOOD)) {
            ActiveCardResolver active = resolvePlayerActiveCards(player);
            if (active.isActive(TarotCardType.TEMPERANCE) && active.isAscended(TarotCardType.TEMPERANCE)) {
                player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 1200, 1, true, false, true));
                player.heal(4.0f);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player) || player.level().isClientSide()) return;

        ActiveCardResolver active = resolvePlayerActiveCards(player);
        if (!active.isActive(TarotCardType.WHEEL_OF_FORTUNE)) return;

        boolean ascended = active.isAscended(TarotCardType.WHEEL_OF_FORTUNE);
        List<ItemEntity> bonusDrops = new ArrayList<>();
        for (ItemEntity itemEntity : event.getDrops()) {
            ItemStack stack = itemEntity.getItem();
            if (stack.getMaxStackSize() > 1) {
                if (ascended) {
                    ItemStack extra = stack.copy();
                    extra.setCount(Math.min(stack.getMaxStackSize(), 1 + player.getRandom().nextInt(2)));
                    bonusDrops.add(new ItemEntity(player.level(), itemEntity.getX(), itemEntity.getY(), itemEntity.getZ(), extra));
                } else if (player.getRandom().nextDouble() < 0.15) {
                    ItemStack extra = stack.copyWithCount(1);
                    bonusDrops.add(new ItemEntity(player.level(), itemEntity.getX(), itemEntity.getY(), itemEntity.getZ(), extra));
                }
            }
        }
        event.getDrops().addAll(bonusDrops);
    }

    @SubscribeEvent
    public static void onLivingChangeTarget(LivingChangeTargetEvent event) {
        if (!(event.getNewAboutToBeSetTarget() instanceof Player player) || player.level().isClientSide()) return;

        ActiveCardResolver active = resolvePlayerActiveCards(player);

        // IV - The Emperor (Ascended): Iron Golems never aggro the player
        if (event.getEntity() instanceof IronGolem
                && active.isActive(TarotCardType.THE_EMPEROR)
                && active.isAscended(TarotCardType.THE_EMPEROR)) {
            event.setCanceled(true);
            return;
        }

        // IX - The Hermit (Ascended): Reduced mob detection range (8 blocks) when isolated
        if (active.isActive(TarotCardType.THE_HERMIT) && active.isAscended(TarotCardType.THE_HERMIT)) {
            LivingEntity mob = event.getEntity();
            if (mob.distanceToSqr(player) > 64.0 && mob.getLastHurtByMob() != player && isPlayerIsolated(player, 16.0)) {
                event.setCanceled(true);
            }
        }
    }

    private static void handleSunHealth(Player player, ActiveCardResolver active) {
        AttributeInstance maxHealthAttr = player.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealthAttr == null) return;

        boolean sunActive = active.isActive(TarotCardType.THE_SUN);
        boolean sunAscended = active.isAscended(TarotCardType.THE_SUN);
        double desiredBonus = TarotEffectCalculator.getSunMaxHealthBonus(sunActive, sunAscended);

        AttributeModifier current = maxHealthAttr.getModifier(SUN_HEALTH_MODIFIER_ID);
        if (desiredBonus > 0.0) {
            if (current == null || Math.abs(current.amount() - desiredBonus) > 0.0001) {
                maxHealthAttr.removeModifier(SUN_HEALTH_MODIFIER_ID);
                maxHealthAttr.addPermanentModifier(new AttributeModifier(SUN_HEALTH_MODIFIER_ID, desiredBonus, AttributeModifier.Operation.ADD_VALUE));
            }

            // Passive sunlight regeneration
            long time = player.level().getDefaultClockTime() % 24000;
            boolean isDay = time < 13000 || time > 23000;
            if (isDay && player.level().canSeeSky(player.blockPosition())) {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 40, 0, true, false, true));
            }

            // Ascended fire resistance shield
            if (sunAscended && (player.isOnFire() || player.isInLava())) {
                player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 200, 0, true, false, true));
                player.clearFire();
            }
        } else {
            if (current != null) {
                maxHealthAttr.removeModifier(SUN_HEALTH_MODIFIER_ID);
                if (player.getHealth() > player.getMaxHealth()) {
                    player.setHealth(player.getMaxHealth());
                }
            }
        }
    }
}
