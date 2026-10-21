package com.tarotcardsrevamp.deck;

import com.tarotcardsrevamp.card.TarotCardType;
import java.util.Arrays;
import java.util.Objects;

/**
 * Immutable data structure representing the 22-slot container contents of a Tarot Deck.
 * Each slot corresponds strictly to one TarotCardType (ordinal 0 to 21).
 */
public class DeckDataComponent {

    public static final int TOTAL_SLOTS = 22;

    public record DeckSlotEntry(boolean present, boolean active, int questProgress, boolean ascended) {
        public static final DeckSlotEntry EMPTY = new DeckSlotEntry(false, true, 0, false);
    }

    private final DeckSlotEntry[] slots;

    public DeckDataComponent() {
        this.slots = new DeckSlotEntry[TOTAL_SLOTS];
        Arrays.fill(this.slots, DeckSlotEntry.EMPTY);
    }

    public DeckDataComponent(DeckSlotEntry[] slots) {
        this.slots = new DeckSlotEntry[TOTAL_SLOTS];
        for (int i = 0; i < TOTAL_SLOTS; i++) {
            if (slots != null && i < slots.length && slots[i] != null) {
                this.slots[i] = slots[i];
            } else {
                this.slots[i] = DeckSlotEntry.EMPTY;
            }
        }
    }

    public DeckSlotEntry getSlot(int index) {
        if (index < 0 || index >= TOTAL_SLOTS) {
            return DeckSlotEntry.EMPTY;
        }
        return slots[index];
    }

    public DeckSlotEntry getSlot(TarotCardType type) {
        if (type == null) {
            return DeckSlotEntry.EMPTY;
        }
        return getSlot(type.ordinal());
    }

    public DeckDataComponent withSlot(int index, DeckSlotEntry entry) {
        if (index < 0 || index >= TOTAL_SLOTS) {
            return this;
        }
        DeckSlotEntry[] copy = Arrays.copyOf(this.slots, TOTAL_SLOTS);
        copy[index] = entry != null ? entry : DeckSlotEntry.EMPTY;
        return new DeckDataComponent(copy);
    }

    public DeckDataComponent toggleSlotActive(int index) {
        if (index < 0 || index >= TOTAL_SLOTS) {
            return this;
        }
        DeckSlotEntry current = getSlot(index);
        if (!current.present()) {
            return this;
        }
        DeckSlotEntry updated = new DeckSlotEntry(true, !current.active(), current.questProgress(), current.ascended());
        return withSlot(index, updated);
    }

    public int getCardCount() {
        int count = 0;
        for (DeckSlotEntry entry : slots) {
            if (entry != null && entry.present()) {
                count++;
            }
        }
        return count;
    }

    public int getAscendedCount() {
        int count = 0;
        for (DeckSlotEntry entry : slots) {
            if (entry != null && entry.present() && entry.ascended()) {
                count++;
            }
        }
        return count;
    }

    public boolean contains(TarotCardType type) {
        if (type == null) return false;
        DeckSlotEntry entry = getSlot(type.ordinal());
        return entry != null && entry.present();
    }
}
