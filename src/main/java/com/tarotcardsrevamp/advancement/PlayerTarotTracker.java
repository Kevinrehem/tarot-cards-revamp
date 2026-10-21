package com.tarotcardsrevamp.advancement;

import com.tarotcardsrevamp.card.TarotCardType;
import java.util.EnumSet;
import java.util.Set;

/**
 * Tracks unique cards discovered and ascended by a player for advancement progression.
 */
public class PlayerTarotTracker {

    private final Set<TarotCardType> discoveredCards = EnumSet.noneOf(TarotCardType.class);
    private final Set<TarotCardType> ascendedCards = EnumSet.noneOf(TarotCardType.class);

    public boolean discover(TarotCardType type) {
        if (type == null) return false;
        return discoveredCards.add(type);
    }

    public boolean markAscended(TarotCardType type) {
        if (type == null) return false;
        discoveredCards.add(type); // Ascended implies discovered
        return ascendedCards.add(type);
    }

    public boolean isDiscovered(TarotCardType type) {
        return discoveredCards.contains(type);
    }

    public boolean isAscended(TarotCardType type) {
        return ascendedCards.contains(type);
    }

    public int getDiscoveredCount() {
        return discoveredCards.size();
    }

    public int getAscendedCount() {
        return ascendedCards.size();
    }

    public boolean hasReachedDiscoveryMilestone(int count) {
        return discoveredCards.size() >= count;
    }

    public boolean hasReachedAscensionMilestone(int count) {
        return ascendedCards.size() >= count;
    }
}
