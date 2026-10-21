package com.tarotcardsrevamp.card;

public enum TarotCardType {
    THE_FOOL("the_fool", 0, "The Fool", "O Louco", QuestType.FALL_DAMAGE_TAKEN, 200),
    THE_MAGICIAN("the_magician", 1, "The Magician", "O Mago", QuestType.DURABILITY_CONSUMED, 2000),
    THE_HIGH_PRIESTESS("the_high_priestess", 2, "The High Priestess", "A Sacerdotisa", QuestType.EXPERIENCE_LEVELS_GAINED, 50),
    THE_EMPRESS("the_empress", 3, "The Empress", "A Imperatriz", QuestType.ANIMALS_BRED, 50),
    THE_EMPEROR("the_emperor", 4, "The Emperor", "O Imperador", QuestType.VILLAGER_TRADES, 64),
    THE_HIEROPHANT("the_hierophant", 5, "The Hierophant", "O Hierofante", QuestType.RAW_EXPERIENCE_COLLECTED, 2500),
    THE_LOVERS("the_lovers", 6, "The Lovers", "Os Amantes", QuestType.ALLIES_HEALED, 100),
    THE_CHARIOT("the_chariot", 7, "The Chariot", "A Carruagem", QuestType.DISTANCE_SPRINTED_OR_MOUNTED, 10000),
    STRENGTH("strength", 8, "Strength", "A Forca", QuestType.MELEE_DAMAGE_DEALT, 1500),
    THE_HERMIT("the_hermit", 9, "The Hermit", "O Eremita", QuestType.SOLITARY_TICKS_SURVIVED, 72000), // 3 Minecraft days (72,000 ticks)
    WHEEL_OF_FORTUNE("wheel_of_fortune", 10, "Wheel of Fortune", "Roda da Fortuna", QuestType.LOOT_CHESTS_OPENED, 25),
    JUSTICE("justice", 11, "Justice", "A Justica", QuestType.COMBAT_DAMAGE_TAKEN, 300),
    THE_HANGED_MAN("the_hanged_man", 12, "The Hanged Man", "O Enforcado", QuestType.TOTAL_DAMAGE_TAKEN, 400),
    DEATH("death", 13, "Death", "A Morte", QuestType.HOSTILE_MOBS_KILLED, 150),
    TEMPERANCE("temperance", 14, "Temperance", "A Temperanca", QuestType.FOOD_EATEN, 64),
    THE_DEVIL("the_devil", 15, "The Devil", "O Diabo", QuestType.NETHER_OR_FIRE_KILLS, 100),
    THE_TOWER("the_tower", 16, "The Tower", "A Torre", QuestType.EXPLOSIONS_SURVIVED, 15),
    THE_STAR("the_star", 17, "The Star", "A Estrela", QuestType.BLOCKS_MINED_OR_PLACED, 2000),
    THE_MOON("the_moon", 18, "The Moon", "A Lua", QuestType.NIGHT_DISTANCE_TRAVELED, 3000),
    THE_SUN("the_sun", 19, "The Sun", "O Sol", QuestType.DAYLIGHT_DISTANCE_TRAVELED, 3000),
    JUDGEMENT("judgement", 20, "Judgement", "O Julgamento", QuestType.CRITICAL_HITS_DEALT, 100),
    THE_WORLD("the_world", 21, "The World", "O Mundo", QuestType.MOBS_KILLED_ACROSS_DIMENSIONS, 200);

    public enum QuestType {
        FALL_DAMAGE_TAKEN,
        DURABILITY_CONSUMED,
        EXPERIENCE_LEVELS_GAINED,
        ANIMALS_BRED,
        VILLAGER_TRADES,
        RAW_EXPERIENCE_COLLECTED,
        ALLIES_HEALED,
        DISTANCE_SPRINTED_OR_MOUNTED,
        MELEE_DAMAGE_DEALT,
        SOLITARY_TICKS_SURVIVED,
        LOOT_CHESTS_OPENED,
        COMBAT_DAMAGE_TAKEN,
        TOTAL_DAMAGE_TAKEN,
        HOSTILE_MOBS_KILLED,
        FOOD_EATEN,
        NETHER_OR_FIRE_KILLS,
        EXPLOSIONS_SURVIVED,
        BLOCKS_MINED_OR_PLACED,
        NIGHT_DISTANCE_TRAVELED,
        DAYLIGHT_DISTANCE_TRAVELED,
        CRITICAL_HITS_DEALT,
        MOBS_KILLED_ACROSS_DIMENSIONS
    }

    private final String id;
    private final int romanNumber;
    private final String englishName;
    private final String portugueseName;
    private final QuestType questType;
    private final int defaultGoal;

    TarotCardType(String id, int romanNumber, String englishName, String portugueseName, QuestType questType, int defaultGoal) {
        this.id = id;
        this.romanNumber = romanNumber;
        this.englishName = englishName;
        this.portugueseName = portugueseName;
        this.questType = questType;
        this.defaultGoal = defaultGoal;
    }

    public String getId() {
        return id;
    }

    public int getRomanNumber() {
        return romanNumber;
    }

    public String getEnglishName() {
        return englishName;
    }

    public String getPortugueseName() {
        return portugueseName;
    }

    public QuestType getQuestType() {
        return questType;
    }

    public int getDefaultGoal() {
        return defaultGoal;
    }

    public static TarotCardType byOrdinalSlot(int slot) {
        TarotCardType[] values = values();
        if (slot >= 0 && slot < values.length) {
            return values[slot];
        }
        return null;
    }

    public static TarotCardType byId(String id) {
        for (TarotCardType card : values()) {
            if (card.getId().equalsIgnoreCase(id)) {
                return card;
            }
        }
        return null;
    }
}
