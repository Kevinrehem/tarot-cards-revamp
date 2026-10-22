package com.tarotcardsrevamp;

import com.tarotcardsrevamp.advancement.PlayerTarotTracker;
import com.tarotcardsrevamp.card.CardDataComponent;
import com.tarotcardsrevamp.card.TarotCardType;
import com.tarotcardsrevamp.deck.DeckDataComponent;
import com.tarotcardsrevamp.effect.ActiveCardResolver;
import com.tarotcardsrevamp.effect.ActiveCardStatus;
import com.tarotcardsrevamp.loot.SmartCardSelector;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
}
