package com.tarotcardsrevamp.card;

import java.util.Objects;

/**
 * Immutable data component representing the runtime state of a Tarot Card ItemStack.
 *
 * @param active Whether the card's passive buff is toggled active.
 * @param questProgress Current progress towards ascension.
 * @param ascended Whether the card has been awakened to its ascended form.
 */
public record CardDataComponent(boolean active, int questProgress, boolean ascended) {

    public static final CardDataComponent DEFAULT = new CardDataComponent(true, 0, false);

    public CardDataComponent {
        // Enforce defensive bounds: progress cannot be negative
        if (questProgress < 0) {
            questProgress = 0;
        }
    }

    public CardDataComponent withActive(boolean newActive) {
        return new CardDataComponent(newActive, this.questProgress, this.ascended);
    }

    public CardDataComponent withProgress(int newProgress, int goal) {
        if (this.ascended) {
            return this;
        }
        int clamped = Math.max(0, Math.min(newProgress, goal));
        boolean becomesAscended = clamped >= goal && goal > 0;
        return new CardDataComponent(this.active, clamped, becomesAscended);
    }

    public CardDataComponent addProgress(int amount, int goal) {
        if (this.ascended || amount <= 0) {
            return this;
        }
        long sum = (long) this.questProgress + amount;
        int clamped = (int) Math.min(sum, goal);
        boolean becomesAscended = clamped >= goal && goal > 0;
        return new CardDataComponent(this.active, clamped, becomesAscended);
    }

    public CardDataComponent withAscended(boolean newAscended) {
        return new CardDataComponent(this.active, this.questProgress, newAscended);
    }

    public float getProgressRatio(int goal) {
        if (ascended || goal <= 0) {
            return 1.0f;
        }
        return Math.min(1.0f, (float) questProgress / (float) goal);
    }

    public int getPercent(int goal) {
        if (ascended || goal <= 0) {
            return 100;
        }
        return (int) (getProgressRatio(goal) * 100.0f);
    }
}
