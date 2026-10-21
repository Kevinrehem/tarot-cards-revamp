package com.tarotcardsrevamp.effect;

import com.tarotcardsrevamp.card.TarotCardType;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/**
 * High-performance deduplicator and resolver for active Tarot cards.
 * Ensures that if multiple copies of the same TarotCardType are active across
 * the player's inventory, hands, and Tarot Deck, only a single copy provides buffs,
 * and an Ascended version unconditionally overrides a non-ascended version.
 */
public class ActiveCardResolver {

    private final Map<TarotCardType, ActiveCardStatus> resolved = new EnumMap<>(TarotCardType.class);

    public void offer(TarotCardType type, boolean active, boolean ascended, int questProgress) {
        if (type == null || !active) {
            return;
        }

        ActiveCardStatus existing = resolved.get(type);
        if (existing == null) {
            resolved.put(type, new ActiveCardStatus(type, ascended, questProgress));
        } else if (!existing.ascended() && ascended) {
            // Ascended takes precedence over base
            resolved.put(type, new ActiveCardStatus(type, true, questProgress));
        } else if (existing.ascended() == ascended && questProgress > existing.questProgress()) {
            // Pick the one with higher quest progress
            resolved.put(type, new ActiveCardStatus(type, ascended, questProgress));
        }
    }

    public boolean isActive(TarotCardType type) {
        return resolved.containsKey(type);
    }

    public boolean isAscended(TarotCardType type) {
        ActiveCardStatus status = resolved.get(type);
        return status != null && status.ascended();
    }

    public ActiveCardStatus getStatus(TarotCardType type) {
        return resolved.get(type);
    }

    public Collection<ActiveCardStatus> getAllActive() {
        return Collections.unmodifiableCollection(resolved.values());
    }

    public int getActiveCount() {
        return resolved.size();
    }

    public void clear() {
        resolved.clear();
    }
}
