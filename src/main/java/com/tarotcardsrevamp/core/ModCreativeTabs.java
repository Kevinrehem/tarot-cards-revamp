package com.tarotcardsrevamp.core;

import com.tarotcardsrevamp.card.TarotCardType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, "tarotcardsrevamp");

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAROT_TAB =
            CREATIVE_MODE_TABS.register("tarot_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.tarotcardsrevamp"))
                    .icon(() -> new ItemStack(ModItems.TAROT_DECK.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.TAROT_DECK.get());
                        output.accept(ModItems.UNIDENTIFIED_TAROT_CARD.get());
                        for (TarotCardType type : TarotCardType.values()) {
                            output.accept(ModItems.getCardItem(type));
                        }
                    })
                    .build());
}
