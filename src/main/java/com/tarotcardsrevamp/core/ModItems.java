package com.tarotcardsrevamp.core;

import com.tarotcardsrevamp.card.TarotCardItem;
import com.tarotcardsrevamp.card.TarotCardType;
import com.tarotcardsrevamp.card.UnidentifiedTarotCardItem;
import com.tarotcardsrevamp.deck.TarotDeckItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.EnumMap;
import java.util.Map;

public class ModItems {

    /**
     * {@link DeferredRegister.Items} injects the registry id into {@link Item.Properties} before the item is
     * constructed. Since Minecraft 1.21.2 the {@link Item} constructor throws "Item id not set" otherwise.
     */
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems("tarotcardsrevamp");

    public static final DeferredItem<TarotDeckItem> TAROT_DECK =
            ITEMS.registerItem("tarot_deck", TarotDeckItem::new);

    public static final DeferredItem<UnidentifiedTarotCardItem> UNIDENTIFIED_TAROT_CARD =
            ITEMS.registerItem("unidentified_tarot_card", UnidentifiedTarotCardItem::new);

    private static final Map<TarotCardType, DeferredItem<TarotCardItem>> CARD_ITEMS = new EnumMap<>(TarotCardType.class);

    static {
        for (TarotCardType cardType : TarotCardType.values()) {
            DeferredItem<TarotCardItem> holder = ITEMS.registerItem("tarot_card_" + cardType.getId(),
                    properties -> new TarotCardItem(cardType, properties));
            CARD_ITEMS.put(cardType, holder);
        }
    }

    public static TarotCardItem getCardItem(TarotCardType type) {
        DeferredItem<TarotCardItem> holder = CARD_ITEMS.get(type);
        return holder != null ? holder.get() : null;
    }
}
