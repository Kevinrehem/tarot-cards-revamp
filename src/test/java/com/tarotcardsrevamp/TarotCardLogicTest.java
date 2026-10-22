package com.tarotcardsrevamp;

import com.tarotcardsrevamp.advancement.PlayerTarotTracker;
import com.tarotcardsrevamp.card.CardDataComponent;
import com.tarotcardsrevamp.card.TarotCardType;
import com.tarotcardsrevamp.deck.DeckDataComponent;
import com.tarotcardsrevamp.effect.ActiveCardResolver;
import com.tarotcardsrevamp.effect.ActiveCardStatus;
import com.tarotcardsrevamp.effect.TarotEffectCalculator;
import com.tarotcardsrevamp.loot.SmartCardSelector;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class TarotCardLogicTest {

    @Test
    @DisplayName("TP-01: Quest progress increments on active cards and triggers ascended = true at 100% target")
    void testQuestProgressAndAutoAscension() {
        int goal = 200;
        CardDataComponent initial = CardDataComponent.DEFAULT;
        assertFalse(initial.ascended());
        assertEquals(0, initial.questProgress());

        // Increment partially
        CardDataComponent step1 = initial.addProgress(50, goal);
        assertEquals(50, step1.questProgress());
        assertFalse(step1.ascended());
        assertEquals(25, step1.getPercent(goal));

        // Reach 100%
        CardDataComponent step2 = step1.addProgress(150, goal);
        assertEquals(200, step2.questProgress());
        assertTrue(step2.ascended(), "Card must automatically ascend when progress reaches or exceeds goal");
        assertEquals(100, step2.getPercent(goal));

        // Adding more progress after ascension does not alter ascended state
        CardDataComponent step3 = step2.addProgress(50, goal);
        assertTrue(step3.ascended());
    }

    @Test
    @DisplayName("TP-02: Active card resolver deduplicates multiple copies of the same card and prioritizes Ascended over Base")
    void testActiveCardResolverDeduplicationAndPrecedence() {
        ActiveCardResolver resolver = new ActiveCardResolver();

        // Add Base The Fool
        resolver.offer(TarotCardType.THE_FOOL, true, false, 50);
        assertTrue(resolver.isActive(TarotCardType.THE_FOOL));
        assertFalse(resolver.isAscended(TarotCardType.THE_FOOL));
        assertEquals(1, resolver.getActiveCount());

        // Add second Base The Fool with higher progress
        resolver.offer(TarotCardType.THE_FOOL, true, false, 80);
        assertEquals(1, resolver.getActiveCount(), "Duplicate cards of same type must not stack");
        assertEquals(80, resolver.getStatus(TarotCardType.THE_FOOL).questProgress());

        // Add Ascended The Fool
        resolver.offer(TarotCardType.THE_FOOL, true, true, 200);
        assertEquals(1, resolver.getActiveCount());
        assertTrue(resolver.isAscended(TarotCardType.THE_FOOL), "Ascended copy must override Base copy");

        // Offer lower Base copy afterwards - Ascended should remain intact
        resolver.offer(TarotCardType.THE_FOOL, true, false, 100);
        assertTrue(resolver.isAscended(TarotCardType.THE_FOOL));
    }

    @Test
    @DisplayName("TP-03: Tarot Deck 22-slot validation accepts each TarotCardType in its designated slot and auto-collects")
    void testTarotDeckSlotsMapping() {
        DeckDataComponent deck = new DeckDataComponent();
        assertEquals(0, deck.getCardCount());

        // Put The Fool in slot 0
        DeckDataComponent.DeckSlotEntry foolEntry = new DeckDataComponent.DeckSlotEntry(true, true, 10, false);
        deck = deck.withSlot(TarotCardType.THE_FOOL.ordinal(), foolEntry);

        assertEquals(1, deck.getCardCount());
        assertTrue(deck.contains(TarotCardType.THE_FOOL));
        assertTrue(deck.getSlot(TarotCardType.THE_FOOL).present());
        assertTrue(deck.getSlot(TarotCardType.THE_FOOL).active());

        // Put The World (slot 21) as ascended
        DeckDataComponent.DeckSlotEntry worldEntry = new DeckDataComponent.DeckSlotEntry(true, true, 200, true);
        deck = deck.withSlot(TarotCardType.THE_WORLD.ordinal(), worldEntry);

        assertEquals(2, deck.getCardCount());
        assertEquals(1, deck.getAscendedCount());
        assertTrue(deck.contains(TarotCardType.THE_WORLD));
        assertTrue(deck.getSlot(TarotCardType.THE_WORLD).ascended());

        // Toggle slot
        deck = deck.toggleSlotActive(TarotCardType.THE_FOOL.ordinal());
        assertFalse(deck.getSlot(TarotCardType.THE_FOOL).active());
    }

    @Test
    @DisplayName("TP-04: PlayerTarotTracker triggers discovery and ascension milestones at 1, 10, 17, and 22 unique cards")
    void testMilestoneAdvancementTracking() {
        PlayerTarotTracker tracker = new PlayerTarotTracker();
        assertFalse(tracker.hasReachedDiscoveryMilestone(1));

        tracker.discover(TarotCardType.THE_FOOL);
        assertTrue(tracker.hasReachedDiscoveryMilestone(1));
        assertFalse(tracker.hasReachedDiscoveryMilestone(10));

        TarotCardType[] all = TarotCardType.values();
        for (int i = 1; i < 10; i++) {
            tracker.discover(all[i]);
        }
        assertTrue(tracker.hasReachedDiscoveryMilestone(10));
        assertFalse(tracker.hasReachedDiscoveryMilestone(17));

        for (int i = 10; i < 17; i++) {
            tracker.discover(all[i]);
        }
        assertTrue(tracker.hasReachedDiscoveryMilestone(17));
        assertFalse(tracker.hasReachedDiscoveryMilestone(22));

        for (int i = 17; i < 22; i++) {
            tracker.discover(all[i]);
        }
        assertTrue(tracker.hasReachedDiscoveryMilestone(22));
        assertEquals(22, tracker.getDiscoveredCount());

        // Ascension tracking
        assertFalse(tracker.hasReachedAscensionMilestone(22));
        for (TarotCardType card : all) {
            tracker.markAscended(card);
        }
        assertTrue(tracker.hasReachedAscensionMilestone(22));
        assertEquals(22, tracker.getAscendedCount());
    }

    @Test
    @DisplayName("TE-01: CardDataComponent clamps negative or excessive progress values safely")
    void testProgressDefensiveClamping() {
        int goal = 100;
        CardDataComponent neg = new CardDataComponent(true, -50, false);
        assertEquals(0, neg.questProgress(), "Negative progress must be clamped to 0");

        CardDataComponent clamped = neg.withProgress(-10, goal);
        assertEquals(0, clamped.questProgress());

        CardDataComponent overflow = neg.withProgress(99999, goal);
        assertEquals(100, overflow.questProgress());
        assertTrue(overflow.ascended(), "Overflowing progress safely completes ascension");
    }

    @Test
    @DisplayName("TE-02: Toggled-off (active = false) cards neither grant buffs nor gain quest progress")
    void testInactiveCardIgnored() {
        ActiveCardResolver resolver = new ActiveCardResolver();
        resolver.offer(TarotCardType.THE_MAGICIAN, false, false, 100);

        assertFalse(resolver.isActive(TarotCardType.THE_MAGICIAN), "Inactive cards must not be active in resolver");
        assertEquals(0, resolver.getActiveCount());
    }

    @Test
    @DisplayName("TE-03: Smart card selector falls back cleanly when all 22 cards are already discovered")
    void testSmartCardSelectorAllDiscoveredFallback() {
        PlayerTarotTracker tracker = new PlayerTarotTracker();
        for (TarotCardType card : TarotCardType.values()) {
            tracker.discover(card);
        }

        SmartCardSelector selector = new SmartCardSelector(new Random(42));
        TarotCardType picked = selector.pickCard(tracker);
        assertNotNull(picked, "Must cleanly return a card even when all 22 are already discovered");
    }

    @Test
    @DisplayName("TE-04: Out-of-bounds slot index in Tarot Deck operations is rejected safely")
    void testOutOfBoundsDeckSlotOperations() {
        DeckDataComponent deck = new DeckDataComponent();
        DeckDataComponent modifiedNeg = deck.withSlot(-1, new DeckDataComponent.DeckSlotEntry(true, true, 0, false));
        assertSame(deck, modifiedNeg, "Negative index must return unchanged instance");

        DeckDataComponent modifiedHigh = deck.withSlot(999, new DeckDataComponent.DeckSlotEntry(true, true, 0, false));
        assertSame(deck, modifiedHigh, "High index must return unchanged instance");

        assertEquals(DeckDataComponent.DeckSlotEntry.EMPTY, deck.getSlot(-1));
        assertEquals(DeckDataComponent.DeckSlotEntry.EMPTY, deck.getSlot(50));
    }

    @Test
    @DisplayName("TP-05: Tarot Deck counts cards and upgrades Base slot to Ascended slot cleanly")
    void testDeckSlotUpgradeAndCount() {
        DeckDataComponent deck = new DeckDataComponent();
        assertEquals(0, deck.getCardCount());
        assertEquals(0, deck.getAscendedCount());

        // Insert Base Chariot
        deck = deck.withSlot(TarotCardType.THE_CHARIOT.ordinal(),
                new DeckDataComponent.DeckSlotEntry(true, true, 200, false));
        assertEquals(1, deck.getCardCount());
        assertEquals(0, deck.getAscendedCount());
        assertTrue(deck.contains(TarotCardType.THE_CHARIOT));

        // Upgrade to Ascended Chariot
        deck = deck.withSlot(TarotCardType.THE_CHARIOT.ordinal(),
                new DeckDataComponent.DeckSlotEntry(true, true, 10000, true));
        assertEquals(1, deck.getCardCount());
        assertEquals(1, deck.getAscendedCount());
        assertTrue(deck.getSlot(TarotCardType.THE_CHARIOT).ascended());

        // Test offering into ActiveCardResolver
        ActiveCardResolver resolver = new ActiveCardResolver();
        for (int s = 0; s < DeckDataComponent.TOTAL_SLOTS; s++) {
            var entry = deck.getSlot(s);
            if (entry.present()) {
                resolver.offer(TarotCardType.values()[s], entry.active(), entry.ascended(), entry.questProgress());
            }
        }
        assertTrue(resolver.isActive(TarotCardType.THE_CHARIOT));
        assertTrue(resolver.isAscended(TarotCardType.THE_CHARIOT));
    }

    @Test
    @DisplayName("TP-06: LootProbabilityHelper calculates exact tiered probability boundaries")
    void testLootModifierTieredProbabilityBoundaries() {
        double c1 = 0.25;
        double c2 = 0.10;
        double c3 = 0.01;

        // 3 cards (< 0.01)
        assertEquals(3, com.tarotcardsrevamp.loot.LootProbabilityHelper.calculateCardCount(0.0000, c1, c2, c3));
        assertEquals(3, com.tarotcardsrevamp.loot.LootProbabilityHelper.calculateCardCount(0.0099, c1, c2, c3));

        // 2 cards (0.01 <= roll < 0.11)
        assertEquals(2, com.tarotcardsrevamp.loot.LootProbabilityHelper.calculateCardCount(0.0100, c1, c2, c3));
        assertEquals(2, com.tarotcardsrevamp.loot.LootProbabilityHelper.calculateCardCount(0.1099, c1, c2, c3));

        // 1 card (0.11 <= roll < 0.36)
        assertEquals(1, com.tarotcardsrevamp.loot.LootProbabilityHelper.calculateCardCount(0.1100, c1, c2, c3));
        assertEquals(1, com.tarotcardsrevamp.loot.LootProbabilityHelper.calculateCardCount(0.3599, c1, c2, c3));

        // 0 cards (roll >= 0.36)
        assertEquals(0, com.tarotcardsrevamp.loot.LootProbabilityHelper.calculateCardCount(0.3600, c1, c2, c3));
        assertEquals(0, com.tarotcardsrevamp.loot.LootProbabilityHelper.calculateCardCount(0.9999, c1, c2, c3));
    }

    @Test
    @DisplayName("TP-07: LootProbabilityHelper Monte Carlo simulation matches requested 25% / 10% / 1% distribution")
    void testLootModifierMonteCarloDistribution() {
        double c1 = 0.25;
        double c2 = 0.10;
        double c3 = 0.01;
        int trials = 100_000;
        int count3 = 0;
        int count2 = 0;
        int count1 = 0;
        int count0 = 0;

        Random random = new Random(42);
        for (int i = 0; i < trials; i++) {
            float roll = random.nextFloat();
            int count = com.tarotcardsrevamp.loot.LootProbabilityHelper.calculateCardCount(roll, c1, c2, c3);
            switch (count) {
                case 3 -> count3++;
                case 2 -> count2++;
                case 1 -> count1++;
                case 0 -> count0++;
                default -> fail("Unexpected card count: " + count);
            }
        }

        double pct3 = (double) count3 / trials;
        double pct2 = (double) count2 / trials;
        double pct1 = (double) count1 / trials;
        double pct0 = (double) count0 / trials;

        assertEquals(0.01, pct3, 0.005, "3 cards must occur with ~1% frequency");
        assertEquals(0.10, pct2, 0.005, "2 cards must occur with ~10% frequency");
        assertEquals(0.25, pct1, 0.005, "1 card must occur with ~25% frequency");
        assertEquals(0.64, pct0, 0.005, "0 cards must occur with ~64% frequency");
    }

    @Test
    @DisplayName("TP-08: DeckDataComponent.addProgress increments progress on present cards and triggers ascension at goal")
    void testDeckDataComponentAddProgressAndAscension() {
        int chariotGoal = TarotCardType.THE_CHARIOT.getDefaultGoal();
        assertEquals(10000, chariotGoal);

        DeckDataComponent deck = new DeckDataComponent();
        // Insert The Chariot (slot 7) with 0 progress
        deck = deck.withSlot(TarotCardType.THE_CHARIOT.ordinal(),
                new DeckDataComponent.DeckSlotEntry(true, true, 0, false));

        assertTrue(deck.contains(TarotCardType.THE_CHARIOT));
        assertEquals(0, deck.getSlot(TarotCardType.THE_CHARIOT).questProgress());
        assertFalse(deck.getSlot(TarotCardType.THE_CHARIOT).ascended());

        // Add 500 sprint blocks
        deck = deck.addProgress(TarotCardType.THE_CHARIOT, 500, chariotGoal);
        assertEquals(500, deck.getSlot(TarotCardType.THE_CHARIOT).questProgress());
        assertFalse(deck.getSlot(TarotCardType.THE_CHARIOT).ascended());

        // Add 9,500 more sprint blocks to reach 10,000
        deck = deck.addProgress(TarotCardType.THE_CHARIOT, 9500, chariotGoal);
        assertEquals(10000, deck.getSlot(TarotCardType.THE_CHARIOT).questProgress());
        assertTrue(deck.getSlot(TarotCardType.THE_CHARIOT).ascended(), "The Chariot must ascend when reaching 10,000 blocks in deck");
        assertEquals(1, deck.getAscendedCount());

        // Excess progress beyond goal does not exceed goal or break ascension
        deck = deck.addProgress(TarotCardType.THE_CHARIOT, 500, chariotGoal);
        assertEquals(10000, deck.getSlot(TarotCardType.THE_CHARIOT).questProgress());
        assertTrue(deck.getSlot(TarotCardType.THE_CHARIOT).ascended());
    }

    @Test
    @DisplayName("TP-09: DeckDataComponent.addProgress respects boundary conditions: inactive, empty, or invalid slots")
    void testDeckDataComponentAddProgressEdgeCases() {
        DeckDataComponent deck = new DeckDataComponent();

        // 1. Empty slot should not gain progress
        deck = deck.addProgress(TarotCardType.THE_FOOL, 100, 200);
        assertFalse(deck.getSlot(TarotCardType.THE_FOOL).present());
        assertEquals(0, deck.getSlot(TarotCardType.THE_FOOL).questProgress());

        // 2. Inactive card should not gain progress
        deck = deck.withSlot(TarotCardType.THE_FOOL.ordinal(),
                new DeckDataComponent.DeckSlotEntry(true, false, 20, false));
        deck = deck.addProgress(TarotCardType.THE_FOOL, 50, 200);
        assertEquals(20, deck.getSlot(TarotCardType.THE_FOOL).questProgress());

        // 3. Zero or negative progress amount should not alter state
        deck = deck.toggleSlotActive(TarotCardType.THE_FOOL.ordinal()); // make active
        assertTrue(deck.getSlot(TarotCardType.THE_FOOL).active());
        deck = deck.addProgress(TarotCardType.THE_FOOL, 0, 200);
        assertEquals(20, deck.getSlot(TarotCardType.THE_FOOL).questProgress());
        deck = deck.addProgress(TarotCardType.THE_FOOL, -10, 200);
        assertEquals(20, deck.getSlot(TarotCardType.THE_FOOL).questProgress());

        // 4. Out of bounds index
        DeckDataComponent untouched = deck.addProgress(-1, 100, 200);
        assertSame(deck, untouched);
        untouched = deck.addProgress(99, 100, 200);
        assertSame(deck, untouched);
    }

    @Test
    @DisplayName("TP-10: All 22 TarotCardTypes have valid quest translation keys, quest types, and positive default goals")
    void testQuestTranslationKeysAndGoals() {
        assertEquals(22, TarotCardType.values().length);
        for (TarotCardType type : TarotCardType.values()) {
            assertNotNull(type.getQuestTranslationKey());
            assertEquals("tarotcardsrevamp.card." + type.getId() + ".quest", type.getQuestTranslationKey());
            assertTrue(type.getDefaultGoal() > 0, "Default goal must be greater than zero for " + type.getId());
            assertNotNull(type.getQuestType(), "QuestType must not be null for " + type.getId());
        }
    }

    @Test
    @DisplayName("TP-11: Localization files (en_us and pt_br) contain all 22 quest translations and tooltip labels")
    void testQuestLocalizationKeysExistInLangFiles() throws IOException {
        Path enUsPath = Path.of("src/main/resources/assets/tarotcardsrevamp/lang/en_us.json");
        Path ptBrPath = Path.of("src/main/resources/assets/tarotcardsrevamp/lang/pt_br.json");

        assertTrue(Files.exists(enUsPath), "en_us.json must exist");
        assertTrue(Files.exists(ptBrPath), "pt_br.json must exist");

        String enUs = Files.readString(enUsPath);
        String ptBr = Files.readString(ptBrPath);

        String[] requiredCommonKeys = new String[]{
                "tarotcardsrevamp.tooltip.quest_objective_label",
                "tarotcardsrevamp.tooltip.quest_progress",
                "tarotcardsrevamp.tooltip.quest_completed_label"
        };

        for (String key : requiredCommonKeys) {
            assertTrue(enUs.contains("\"" + key + "\""), "en_us.json missing key: " + key);
            assertTrue(ptBr.contains("\"" + key + "\""), "pt_br.json missing key: " + key);
        }

        for (TarotCardType card : TarotCardType.values()) {
            String questKey = card.getQuestTranslationKey();
            assertTrue(enUs.contains("\"" + questKey + "\""), "en_us.json missing quest key for " + card.getId());
            assertTrue(ptBr.contains("\"" + questKey + "\""), "pt_br.json missing quest key for " + card.getId());
        }
    }

    @Test
    @DisplayName("TP-12: Attribute modifier deltas for Chariot, Hermit, Wheel of Fortune, Star, and Sun")
    void testAttributeModifierDeltas() {
        // VII - The Chariot Step Height (+0.4 Ascended -> 1.0 block total)
        assertEquals(0.0, TarotEffectCalculator.getChariotStepHeightBonus(false, false));
        assertEquals(0.0, TarotEffectCalculator.getChariotStepHeightBonus(true, false));
        assertEquals(0.4, TarotEffectCalculator.getChariotStepHeightBonus(true, true), 0.0001);

        // IX - The Hermit Armor & Toughness when isolated
        assertEquals(0.0, TarotEffectCalculator.getHermitArmorBonus(true, true, false), "No armor bonus when not isolated");
        assertEquals(2.0, TarotEffectCalculator.getHermitArmorBonus(true, false, true));
        assertEquals(8.0, TarotEffectCalculator.getHermitArmorBonus(true, true, true));
        assertEquals(0.0, TarotEffectCalculator.getHermitArmorToughnessBonus(true, false, true));
        assertEquals(4.0, TarotEffectCalculator.getHermitArmorToughnessBonus(true, true, true));

        // X - Wheel of Fortune Luck
        assertEquals(0.0, TarotEffectCalculator.getWheelLuckBonus(false, true));
        assertEquals(0.5, TarotEffectCalculator.getWheelLuckBonus(true, false));
        assertEquals(3.0, TarotEffectCalculator.getWheelLuckBonus(true, true));

        // XVII - The Star Block & Entity Reach
        assertEquals(0.5, TarotEffectCalculator.getStarBlockReachBonus(true, false));
        assertEquals(2.5, TarotEffectCalculator.getStarBlockReachBonus(true, true));
        assertEquals(0.0, TarotEffectCalculator.getStarEntityReachBonus(true, false));
        assertEquals(1.5, TarotEffectCalculator.getStarEntityReachBonus(true, true));

        // XIX - The Sun Max Health
        assertEquals(4.0, TarotEffectCalculator.getSunMaxHealthBonus(true, false));
        assertEquals(20.0, TarotEffectCalculator.getSunMaxHealthBonus(true, true));
    }

    @Test
    @DisplayName("TP-13: Outgoing combat calculations for Strength, Death, Devil, High Priestess, and Hanged Man")
    void testOutgoingCombatEffectCalculations() {
        // VIII - Strength (+15% Base, +50% + armor bypass Ascended)
        assertEquals(11.5f, TarotEffectCalculator.computeStrengthMeleeDamage(10.0f, 0.0, true, false), 0.001f);
        float ascendedStrengthNoArmor = TarotEffectCalculator.computeStrengthMeleeDamage(10.0f, 0.0, true, true);
        float ascendedStrengthWithArmor = TarotEffectCalculator.computeStrengthMeleeDamage(10.0f, 20.0, true, true);
        assertEquals(15.0f, ascendedStrengthNoArmor, 0.001f);
        assertTrue(ascendedStrengthWithArmor > ascendedStrengthNoArmor, "Ascended Strength must deal bonus armor-bypassing damage against armored targets");

        // XIII - Death (+15% Undead Base, +50% All Ascended, 15% Lifesteal, <15% HP Execute)
        assertEquals(11.5f, TarotEffectCalculator.computeDeathOutgoingDamage(10.0f, true, true, false), 0.001f);
        assertEquals(10.0f, TarotEffectCalculator.computeDeathOutgoingDamage(10.0f, false, true, false), 0.001f);
        assertEquals(15.0f, TarotEffectCalculator.computeDeathOutgoingDamage(10.0f, false, true, true), 0.001f);
        assertEquals(3.0f, TarotEffectCalculator.computeDeathLifesteal(20.0f, true, true), 0.001f);
        assertTrue(TarotEffectCalculator.shouldExecuteTarget(14.0f, 100.0f, 2.0f, false, true, true));

        // XV - The Devil (Fire 3s/6s, Wither I/III, +35% vs burning/debuffed)
        assertEquals(3, TarotEffectCalculator.getDevilFireSeconds(true, false));
        assertEquals(6, TarotEffectCalculator.getDevilFireSeconds(true, true));
        assertEquals(0, TarotEffectCalculator.getDevilWitherAmplifier(true, false));
        assertEquals(2, TarotEffectCalculator.getDevilWitherAmplifier(true, true));
        assertEquals(13.5f, TarotEffectCalculator.computeDevilOutgoingDamage(10.0f, true, true, true), 0.001f);
        assertEquals(10.0f, TarotEffectCalculator.computeDevilOutgoingDamage(10.0f, false, true, true), 0.001f);

        // II - The High Priestess (+15% enchanted weapon damage when Ascended)
        assertEquals(11.5f, TarotEffectCalculator.computeHighPriestessEnchantedDamage(10.0f, true, true, true), 0.001f);
        assertEquals(10.0f, TarotEffectCalculator.computeHighPriestessEnchantedDamage(10.0f, true, true, false), 0.001f);

        // XII - The Hanged Man (+30% damage when <= 30% HP)
        assertTrue(TarotEffectCalculator.isHangedManLowHealthActive(6.0f, 20.0f, true, true));
        assertFalse(TarotEffectCalculator.isHangedManLowHealthActive(10.0f, 20.0f, true, true));
        assertEquals(13.0f, TarotEffectCalculator.computeHangedManOutgoingDamage(10.0f, 5.0f, 20.0f, true, true), 0.001f);
    }

    @Test
    @DisplayName("TP-14: Incoming combat calculations for Justice reflection, Hanged Man XP conversion, and Lovers pet scaling")
    void testIncomingAndPetCombatCalculations() {
        // XI - Justice (Base: 0% mitigation, 15% reflected; Ascended: 15% mitigation, 75% reflected)
        assertEquals(20.0f, TarotEffectCalculator.computeJusticeIncomingDamage(20.0f, true, false), 0.001f);
        assertEquals(17.0f, TarotEffectCalculator.computeJusticeIncomingDamage(20.0f, true, true), 0.001f);
        assertEquals(3.0f, TarotEffectCalculator.computeJusticeReflectedDamage(20.0f, true, false), 0.001f);
        assertEquals(15.0f, TarotEffectCalculator.computeJusticeReflectedDamage(20.0f, true, true), 0.001f);

        // XII - The Hanged Man XP conversion (10% Base, 50% Ascended)
        assertEquals(2, TarotEffectCalculator.computeHangedManXp(20.0f, true, false));
        assertEquals(10, TarotEffectCalculator.computeHangedManXp(20.0f, true, true));

        // VI - The Lovers Pet scaling (+50% outgoing, -50% incoming when Ascended)
        assertEquals(15.0f, TarotEffectCalculator.computeLoversPetOutgoingDamage(10.0f, true, true), 0.001f);
        assertEquals(5.0f, TarotEffectCalculator.computeLoversPetIncomingDamage(10.0f, true, true), 0.001f);
    }

    @Test
    @DisplayName("TP-15: Utility calculations for Magician, High Priestess Anvil, Empress Breeding, Hierophant XP, and Temperance")
    void testUtilityAndEconomyCalculations() {
        // I - The Magician durability preservation chance
        assertEquals(0.125, TarotEffectCalculator.getMagicianDurabilitySaveChance(true, false));
        assertEquals(0.50, TarotEffectCalculator.getMagicianDurabilitySaveChance(true, true));

        // II - The High Priestess Anvil XP discount (-15% Base, -50% Ascended, clamped >= 1)
        assertEquals(17, TarotEffectCalculator.computeDiscountedAnvilCost(20, true, false));
        assertEquals(10, TarotEffectCalculator.computeDiscountedAnvilCost(20, true, true));
        assertEquals(1, TarotEffectCalculator.computeDiscountedAnvilCost(1, true, true), "Anvil cost must never drop to 0 when original > 0");

        // III - The Empress breeding cooldown & baby age
        assertEquals(4500, TarotEffectCalculator.computeEmpressParentBreedingCooldown(6000, true, false));
        assertEquals(1500, TarotEffectCalculator.computeEmpressParentBreedingCooldown(6000, true, true));
        assertEquals(-18000, TarotEffectCalculator.computeEmpressBabyStartingAge(-24000, true, false));
        assertEquals(0, TarotEffectCalculator.computeEmpressBabyStartingAge(-24000, true, true));

        // V - The Hierophant XP bonus (+25% Base, +150% Ascended) and heal (0.05 HP per XP)
        assertEquals(5, TarotEffectCalculator.computeHierophantBonusXp(20, true, false));
        assertEquals(30, TarotEffectCalculator.computeHierophantBonusXp(20, true, true));
        assertEquals(1.0f, TarotEffectCalculator.computeHierophantHeal(20, true, true), 0.001f);

        // XIV - Temperance hunger exhaustion reduction (25% Base, 85% Ascended)
        assertEquals(0.25f, TarotEffectCalculator.computeTemperanceExhaustionReduction(1.0f, true, false), 0.001f);
        assertEquals(0.85f, TarotEffectCalculator.computeTemperanceExhaustionReduction(1.0f, true, true), 0.001f);
    }

    @Test
    @DisplayName("TE-05: Bosses and players are excluded from Death execute, and negative inputs are safely clamped")
    void testDeathExecuteSafetyAndNegativeClamping() {
        // Bosses/players must never be executed even if remaining HP <= 15%
        assertFalse(TarotEffectCalculator.shouldExecuteTarget(10.0f, 300.0f, 5.0f, true, true, true));
        // Base tier must never execute
        assertFalse(TarotEffectCalculator.shouldExecuteTarget(10.0f, 100.0f, 5.0f, false, true, false));
        // Negative damage inputs clamp safely to 0
        assertEquals(0.0f, TarotEffectCalculator.computeJusticeReflectedDamage(-10.0f, true, true));
        assertEquals(0, TarotEffectCalculator.computeHangedManXp(-5.0f, true, true));
    }

    @Test
    @DisplayName("TE-06: Judgement Totem of Undying cooldown enforcement across timestamps")
    void testJudgementTotemCooldownEnforcement() {
        long t0 = 1_000_000L;
        int cooldownSec = 600; // 10 minutes = 600,000 ms

        assertTrue(TarotEffectCalculator.isJudgementTotemReady(null, t0, cooldownSec), "Ready when never used");
        assertFalse(TarotEffectCalculator.isJudgementTotemReady(t0, t0 + 100_000L, cooldownSec), "Blocked while on cooldown");
        assertFalse(TarotEffectCalculator.isJudgementTotemReady(t0, t0 + 599_999L, cooldownSec), "Blocked 1ms before expiry");
        assertTrue(TarotEffectCalculator.isJudgementTotemReady(t0, t0 + 600_000L, cooldownSec), "Ready exactly at cooldown expiry");
    }

}
