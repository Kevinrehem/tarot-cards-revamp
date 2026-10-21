package com.tarotcardsrevamp.loot;

import com.tarotcardsrevamp.advancement.PlayerTarotTracker;
import com.tarotcardsrevamp.card.TarotCardType;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Smart card picker that selects loot with weighting towards cards the player has not yet discovered.
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
        if (tracker == null) {
            return all[random.nextInt(all.length)];
        }

        List<TarotCardType> missing = new ArrayList<>();
        for (TarotCardType card : all) {
            if (!tracker.isDiscovered(card)) {
                missing.add(card);
            }
        }

        if (missing.isEmpty()) {
            // Player already found all 22 cards, return random
            return all[random.nextInt(all.length)];
        }

        // 80% chance to prioritize undiscovered cards
        if (random.nextFloat() < 0.80f || missing.size() == all.length) {
            return missing.get(random.nextInt(missing.size()));
        }

        return all[random.nextInt(all.length)];
    }
}
