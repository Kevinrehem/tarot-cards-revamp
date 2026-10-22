package com.tarotcardsrevamp.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Server and Common configuration for Tarot Cards Revamp.
 * Allows pack makers and server admins to tweak loot rates, quest goals, and buff values.
 */
public class TarotConfig {

    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.DoubleValue LOOT_CHEST_CHANCE;
    public static final ModConfigSpec.DoubleValue CHANCE_1_CARD;
    public static final ModConfigSpec.DoubleValue CHANCE_2_CARDS;
    public static final ModConfigSpec.DoubleValue CHANCE_3_CARDS;
    public static final ModConfigSpec.DoubleValue SMART_LOOT_BIAS;
    public static final ModConfigSpec.DoubleValue QUEST_GOAL_MULTIPLIER;
    public static final ModConfigSpec.BooleanValue ENABLE_ASCENDED_FOOL_DOUBLE_JUMP;
    public static final ModConfigSpec.BooleanValue ENABLE_ASCENDED_CHARIOT_FLUID_WALK;
    public static final ModConfigSpec.IntValue JUDGEMENT_TOTEM_COOLDOWN_SECONDS;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push("loot");
        LOOT_CHEST_CHANCE = builder
                .comment("Legacy base probability (0.0 to 1.0) of finding a Tarot Card in structure loot chests.")
                .defineInRange("lootChestChance", 0.35, 0.0, 1.0);
        CHANCE_1_CARD = builder
                .comment("Probability (0.0 to 1.0) of finding 1 Unidentified Tarot Card in a chest (default 25%).")
                .defineInRange("chance1Card", 0.25, 0.0, 1.0);
        CHANCE_2_CARDS = builder
                .comment("Probability (0.0 to 1.0) of finding 2 Unidentified Tarot Cards in a chest (default 10%).")
                .defineInRange("chance2Cards", 0.10, 0.0, 1.0);
        CHANCE_3_CARDS = builder
                .comment("Probability (0.0 to 1.0) of finding 3 Unidentified Tarot Cards in a chest (default 1%).")
                .defineInRange("chance3Cards", 0.01, 0.0, 1.0);
        SMART_LOOT_BIAS = builder
                .comment("Probability (0.0 to 1.0) of prioritizing undiscovered cards (default 0.0, uniform equal chance for all 22 cards).")
                .defineInRange("smartLootBias", 0.0, 0.0, 1.0);
        builder.pop();

        builder.push("quests");
        QUEST_GOAL_MULTIPLIER = builder
                .comment("Global multiplier for quest completion requirements (e.g. 0.5 for half requirements, 2.0 for double).")
                .defineInRange("questGoalMultiplier", 1.0, 0.1, 100.0);
        builder.pop();

        builder.push("cards");
        ENABLE_ASCENDED_FOOL_DOUBLE_JUMP = builder
                .comment("Whether Ascended The Fool grants mid-air Double Jump.")
                .define("enableAscendedFoolDoubleJump", true);
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
