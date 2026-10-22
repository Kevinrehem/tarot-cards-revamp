package com.tarotcardsrevamp.loot;

/**
 * Pure Java helper for calculating tiered card drop counts.
 * Keeps probability logic isolated from Minecraft classes for fast unit testing.
 */
public class LootProbabilityHelper {

    public static int calculateCardCount(double roll, double c1, double c2, double c3) {
        if (roll < c3) {
            return 3;
        } else if (roll < (c3 + c2)) {
            return 2;
        } else if (roll < (c3 + c2 + c1)) {
            return 1;
        } else {
            return 0;
        }
    }
}
