package com.tarotcardsrevamp.core;

import com.tarotcardsrevamp.deck.TarotDeckMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMenuTypes {

    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(Registries.MENU, "tarotcardsrevamp");

    public static final DeferredHolder<MenuType<?>, MenuType<TarotDeckMenu>> TAROT_DECK_MENU =
            MENU_TYPES.register("tarot_deck_menu", () -> new MenuType<>(TarotDeckMenu::new, FeatureFlags.DEFAULT_FLAGS));
}
