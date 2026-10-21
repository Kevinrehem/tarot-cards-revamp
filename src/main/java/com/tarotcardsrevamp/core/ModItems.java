package com.tarotcardsrevamp.core;

import com.tarotcardsrevamp.card.TarotCardItem;
import com.tarotcardsrevamp.card.TarotCardType;
import com.tarotcardsrevamp.card.UnidentifiedTarotCardItem;
import com.tarotcardsrevamp.deck.TarotDeckItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.EnumMap;
import java.util.Map;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, "tarotcardsrevamp");

    public static final DeferredHolder<Item, TarotDeckItem> TAROT_DECK =
            ITEMS.register("tarot_deck", () -> new TarotDeckItem(new Item.Properties()));

    public static final DeferredHolder<Item, UnidentifiedTarotCardItem> UNIDENTIFIED_TAROT_CARD =
            ITEMS.register("unidentified_tarot_card", () -> new UnidentifiedTarotCardItem(new Item.Properties()));

    private static final Map<TarotCardType, DeferredHolder<Item, TarotCardItem>> CARD_ITEMS = new EnumMap<>(TarotCardType.class);

    static {
        for (TarotCardType cardType : TarotCardType.values()) {
            DeferredHolder<Item, TarotCardItem> holder = ITEMS.register("tarot_card_" + cardType.getId(),
                    () -> new TarotCardItem(cardType, new Item.Properties()));
            CARD_ITEMS.put(cardType, holder);
        }
    }

    public static TarotCardItem getCardItem(TarotCardType type) {
        DeferredHolder<Item, TarotCardItem> holder = CARD_ITEMS.get(type);
        return holder != null ? holder.get() : null;
    }
}
