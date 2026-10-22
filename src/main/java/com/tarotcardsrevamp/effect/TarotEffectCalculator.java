package com.tarotcardsrevamp.effect;

import com.tarotcardsrevamp.card.TarotCardType;

/**
 * Pure calculation helper for Tarot Card Base (50%) and Ascended (100%) effects.
 * Isolates mathematical formulas and state checks from Minecraft world/entity dependencies
 * to allow fast, deterministic unit testing.
 */
public final class TarotEffectCalculator {

    private TarotEffectCalculator() {
    }

    // --- Attribute Modifier Deltas ---

    public static double getChariotStepHeightBonus(boolean active, boolean ascended) {
        return (active && ascended) ? 0.4 : 0.0;
    }

    public static double getHermitArmorBonus(boolean active, boolean ascended, boolean isolated) {
        if (!active || !isolated) return 0.0;
        return ascended ? 8.0 : 2.0;
    }

    public static double getHermitArmorToughnessBonus(boolean active, boolean ascended, boolean isolated) {
        if (!active || !ascended || !isolated) return 0.0;
        return 4.0;
    }

    public static double getWheelLuckBonus(boolean active, boolean ascended) {
        if (!active) return 0.0;
        return ascended ? 3.0 : 0.5;
    }

    public static double getStarBlockReachBonus(boolean active, boolean ascended) {
        if (!active) return 0.0;
        return ascended ? 2.5 : 0.5;
    }

    public static double getStarEntityReachBonus(boolean active, boolean ascended) {
        return (active && ascended) ? 1.5 : 0.0;
    }

    public static double getSunMaxHealthBonus(boolean active, boolean ascended) {
        if (!active) return 0.0;
        return ascended ? 20.0 : 4.0;
    }

    // --- I - The Magician ---

    public static double getMagicianDurabilitySaveChance(boolean active, boolean ascended) {
        if (!active) return 0.0;
        return ascended ? 0.50 : 0.125;
    }

    // --- II - The High Priestess ---

    public static int computeDiscountedAnvilCost(int originalCost, boolean active, boolean ascended) {
        if (!active || originalCost <= 0) return Math.max(0, originalCost);
        double multiplier = ascended ? 0.50 : 0.85;
        return Math.max(1, (int) Math.floor(originalCost * multiplier));
    }

    public static float computeHighPriestessEnchantedDamage(float baseDamage, boolean active, boolean ascended, boolean hasEnchantments) {
        if (!active || !ascended || !hasEnchantments || baseDamage <= 0f) return Math.max(0f, baseDamage);
        return baseDamage * 1.15f;
    }

    // --- III - The Empress ---

    public static int computeEmpressParentBreedingCooldown(int vanillaCooldownTicks, boolean active, boolean ascended) {
        if (!active || vanillaCooldownTicks <= 0) return Math.max(0, vanillaCooldownTicks);
        double multiplier = ascended ? 0.25 : 0.75;
        return (int) Math.round(vanillaCooldownTicks * multiplier);
    }

    public static int computeEmpressBabyStartingAge(int vanillaBabyAgeTicks, boolean active, boolean ascended) {
        if (!active) return vanillaBabyAgeTicks;
        if (ascended) return 0; // Instant adult
        return (int) Math.round(vanillaBabyAgeTicks * 0.75); // 25% closer to adulthood (e.g. -18000 instead of -24000)
    }

    // --- V - The Hierophant ---

    public static int computeHierophantBonusXp(int baseOrbXp, boolean active, boolean ascended) {
        if (!active || baseOrbXp <= 0) return 0;
        double bonusMultiplier = ascended ? 1.50 : 0.25;
        return Math.max(1, (int) Math.round(baseOrbXp * bonusMultiplier));
    }

    public static float computeHierophantHeal(int baseOrbXp, boolean active, boolean ascended) {
        if (!active || !ascended || baseOrbXp <= 0) return 0.0f;
        return baseOrbXp * 0.05f; // 1.0 HP (0.5 heart) per 20 XP
    }

    // --- VI - The Lovers ---

    public static float computeLoversPetOutgoingDamage(float baseDamage, boolean active, boolean ascended) {
        if (!active || !ascended || baseDamage <= 0f) return Math.max(0f, baseDamage);
        return baseDamage * 1.50f;
    }

    public static float computeLoversPetIncomingDamage(float baseDamage, boolean active, boolean ascended) {
        if (!active || !ascended || baseDamage <= 0f) return Math.max(0f, baseDamage);
        return baseDamage * 0.50f;
    }

    // --- VIII - Strength ---

    public static float computeStrengthMeleeDamage(float baseDamage, double targetArmor, boolean active, boolean ascended) {
        if (!active || baseDamage <= 0f) return Math.max(0f, baseDamage);
        float multiplied = baseDamage * (ascended ? 1.50f : 1.15f);
        if (ascended && targetArmor > 0.0) {
            // Bypass 30% of target armor mitigation (each armor point reduces up to 4% in vanilla; add proportional bonus)
            double armorFactor = Math.min(20.0, targetArmor) * 0.04 * 0.30;
            multiplied = (float) (multiplied * (1.0 + armorFactor));
        }
        return multiplied;
    }

    // --- XI - Justice ---

    public static float computeJusticeIncomingDamage(float incomingDamage, boolean active, boolean ascended) {
        if (!active || incomingDamage <= 0f) return Math.max(0f, incomingDamage);
        return ascended ? incomingDamage * 0.85f : incomingDamage;
    }

    public static float computeJusticeReflectedDamage(float preMitigationDamage, boolean active, boolean ascended) {
        if (!active || preMitigationDamage <= 0f) return 0.0f;
        return preMitigationDamage * (ascended ? 0.75f : 0.15f);
    }

    // --- XII - The Hanged Man ---

    public static int computeHangedManXp(float damageTaken, boolean active, boolean ascended) {
        if (!active || damageTaken <= 0f) return 0;
        double ratio = ascended ? 0.50 : 0.10;
        return Math.max(1, (int) Math.round(damageTaken * ratio));
    }

    public static boolean isHangedManLowHealthActive(float currentHealth, float maxHealth, boolean active, boolean ascended) {
        if (!active || !ascended || maxHealth <= 0f) return false;
        return currentHealth <= (maxHealth * 0.30f);
    }

    public static float computeHangedManOutgoingDamage(float baseDamage, float currentHealth, float maxHealth, boolean active, boolean ascended) {
        if (baseDamage <= 0f) return 0f;
        if (isHangedManLowHealthActive(currentHealth, maxHealth, active, ascended)) {
            return baseDamage * 1.30f;
        }
        return baseDamage;
    }

    // --- XIII - Death ---

    public static float computeDeathOutgoingDamage(float baseDamage, boolean isUndead, boolean active, boolean ascended) {
        if (!active || baseDamage <= 0f) return Math.max(0f, baseDamage);
        if (ascended) {
            return baseDamage * 1.50f;
        }
        return isUndead ? baseDamage * 1.15f : baseDamage;
    }

    public static boolean shouldExecuteTarget(float targetCurrentHealth, float targetMaxHealth, float incomingDamage,
                                              boolean isBossOrPlayer, boolean active, boolean ascended) {
        if (!active || !ascended || isBossOrPlayer || targetMaxHealth <= 0f) return false;
        float remainingHealth = targetCurrentHealth - incomingDamage;
        return remainingHealth > 0f && remainingHealth <= (targetMaxHealth * 0.15f);
    }

    public static float computeDeathLifesteal(float inflictedDamage, boolean active, boolean ascended) {
        if (!active || !ascended || inflictedDamage <= 0f) return 0.0f;
        return inflictedDamage * 0.15f;
    }

    // --- XIV - Temperance ---

    public static float computeTemperanceExhaustionReduction(float exhaustionDelta, boolean active, boolean ascended) {
        if (!active || exhaustionDelta <= 0f) return 0.0f;
        return exhaustionDelta * (ascended ? 0.85f : 0.25f);
    }

    // --- XV - The Devil ---

    public static int getDevilFireSeconds(boolean active, boolean ascended) {
        if (!active) return 0;
        return ascended ? 6 : 3;
    }

    public static int getDevilWitherAmplifier(boolean active, boolean ascended) {
        if (!active) return -1;
        return ascended ? 2 : 0; // Wither III (amp 2) vs Wither I (amp 0)
    }

    public static float computeDevilOutgoingDamage(float baseDamage, boolean targetBurningOrDebuffed, boolean active, boolean ascended) {
        if (baseDamage <= 0f) return 0f;
        if (active && ascended && targetBurningOrDebuffed) {
            return baseDamage * 1.35f;
        }
        return baseDamage;
    }

    // --- XX - Judgement ---

    public static double getJudgementCritChance(boolean active, boolean ascended) {
        if (!active) return 0.0;
        return ascended ? 0.30 : 0.10;
    }

    public static float computeJudgementCritDamage(float baseDamage, boolean active, boolean ascended) {
        if (!active || baseDamage <= 0f) return Math.max(0f, baseDamage);
        return baseDamage * (ascended ? 2.50f : 2.00f);
    }

    public static boolean isJudgementTotemReady(Long lastUsedTimestampMs, long nowTimestampMs, int cooldownSeconds) {
        if (lastUsedTimestampMs == null) return true;
        long cooldownMs = Math.max(0L, (long) cooldownSeconds * 1000L);
        return (nowTimestampMs - lastUsedTimestampMs) >= cooldownMs;
    }
}
