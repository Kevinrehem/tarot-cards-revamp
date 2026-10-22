package com.tarotcardsrevamp.loot;

import com.tarotcardsrevamp.advancement.PlayerTarotTracker;
import com.tarotcardsrevamp.card.TarotCardItem;
import com.tarotcardsrevamp.card.TarotCardType;
import com.tarotcardsrevamp.deck.DeckDataComponent;
import com.tarotcardsrevamp.deck.TarotDeckItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class PlayerCardScanner {

    public static PlayerTarotTracker scanPlayerCards(Player player) {
        PlayerTarotTracker tracker = new PlayerTarotTracker();
        if (player == null) return tracker;

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() instanceof TarotCardItem cardItem) {
                tracker.discover(cardItem.getCardType());
            } else if (stack.getItem() instanceof TarotDeckItem) {
                DeckDataComponent deck = TarotDeckItem.getData(stack);
                for (int s = 0; s < DeckDataComponent.TOTAL_SLOTS; s++) {
                    var entry = deck.getSlot(s);
                    if (entry != null && entry.present()) {
                        tracker.discover(TarotCardType.values()[s]);
                    }
                }
            }
        }
        return tracker;
    }
}
