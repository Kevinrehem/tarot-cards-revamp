package com.tarotcardsrevamp.loot;

import com.tarotcardsrevamp.advancement.PlayerTarotTracker;
import com.tarotcardsrevamp.card.TarotCardType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

/**
 * Smart card picker that selects loot with weighting towards cards the player has not yet discovered.
 * Pure Java POJO to ensure fast, dependency-free unit testing.
 */
public class SmartCardSelector {

    private final Random random;

    public SmartCardSelector() {
        this(new Random());
    }

    public SmartCardSelector(Random random) {
        this.random = random;
    }

    /**
     * Chooses a TarotCardType.
     * If the player tracker has missing cards, there is an 80% chance to pick from missing cards.
     * Otherwise, picks uniformly from all 22 cards.
     */
    public TarotCardType pickCard(PlayerTarotTracker tracker) {
        TarotCardType[] all = TarotCardType.values();
        double bias = 0.0;
        try {
            if (com.tarotcardsrevamp.config.TarotConfig.SMART_LOOT_BIAS != null) {
                bias = com.tarotcardsrevamp.config.TarotConfig.SMART_LOOT_BIAS.get();
            }
        } catch (Throwable ignored) {
            // Unit tests without config spec loaded
        }

        if (bias <= 0.0 || tracker == null) {
            return all[random.nextInt(all.length)];
        }

        List<TarotCardType> missing = new ArrayList<>();
        for (TarotCardType card : all) {
            if (!tracker.isDiscovered(card)) {
                missing.add(card);
            }
        }

        if (missing.isEmpty() || random.nextDouble() >= bias) {
            return all[random.nextInt(all.length)];
        }

        return missing.get(random.nextInt(missing.size()));
    }

    /**
     * Overload taking a collection of already discovered cards.
     */
    public TarotCardType pickCardFromDiscovered(Collection<TarotCardType> discovered) {
        if (discovered == null || discovered.isEmpty()) {
            return pickCard((PlayerTarotTracker) null);
        }
        PlayerTarotTracker tracker = new PlayerTarotTracker();
        for (TarotCardType type : discovered) {
            tracker.discover(type);
        }
        return pickCard(tracker);
    }
}
