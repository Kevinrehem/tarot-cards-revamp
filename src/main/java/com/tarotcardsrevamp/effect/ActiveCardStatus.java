package com.tarotcardsrevamp.effect;

import com.tarotcardsrevamp.card.TarotCardType;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * Resolved active card entry for a player in a given tick.
 *
 * @param type The Major Arcana card type.
 * @param ascended True if the active card is Ascended. If both base and ascended
 *                 copies exist in inventory or deck, the Ascended version takes precedence.
 */
public record ActiveCardStatus(TarotCardType type, boolean ascended, int questProgress) {

    public ActiveCardStatus {
        Objects.requireNonNull(type, "TarotCardType cannot be null");
    }
}
