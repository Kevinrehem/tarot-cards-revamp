package com.tarotcardsrevamp.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Server and Common configuration for Tarot Cards Revamp.
 * Allows pack makers and server admins to tweak loot rates, quest goals, and buff values.
 */
public class TarotConfig {

    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.DoubleValue LOOT_CHEST_CHANCE;
    public static final ModConfigSpec.DoubleValue SMART_LOOT_BIAS;
    public static final ModConfigSpec.DoubleValue QUEST_GOAL_MULTIPLIER;
    public static final ModConfigSpec.BooleanValue ENABLE_ASCENDED_CHARIOT_FLUID_WALK;
    public static final ModConfigSpec.IntValue JUDGEMENT_TOTEM_COOLDOWN_SECONDS;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push("loot");
        LOOT_CHEST_CHANCE = builder
                .comment("Base probability (0.0 to 1.0) of finding a Tarot Card in structure loot chests.")
                .defineInRange("lootChestChance", 0.35, 0.0, 1.0);
        SMART_LOOT_BIAS = builder
                .comment("Probability (0.0 to 1.0) of prioritizing undiscovered cards when a chest generates tarot loot.")
                .defineInRange("smartLootBias", 0.80, 0.0, 1.0);
        builder.pop();

        builder.push("quests");
        QUEST_GOAL_MULTIPLIER = builder
                .comment("Global multiplier for quest completion requirements (e.g. 0.5 for half requirements, 2.0 for double).")
                .defineInRange("questGoalMultiplier", 1.0, 0.1, 100.0);
        builder.pop();

        builder.push("cards");
        ENABLE_ASCENDED_CHARIOT_FLUID_WALK = builder
                .comment("Whether Ascended The Chariot allows sprinting over water and lava surfaces.")
                .define("enableAscendedChariotFluidWalk", true);
        JUDGEMENT_TOTEM_COOLDOWN_SECONDS = builder
                .comment("Cooldown in seconds for the built-in Totem of Undying effect on Ascended Judgement.")
                .defineInRange("judgementTotemCooldownSeconds", 600, 30, 86400);
        builder.pop();

        SPEC = builder.build();
    }
}
