package com.tarotcardsrevamp;

import com.tarotcardsrevamp.command.TarotCommand;
import com.tarotcardsrevamp.config.TarotConfig;
import com.tarotcardsrevamp.core.ModCreativeTabs;
import com.tarotcardsrevamp.core.ModDataComponents;
import com.tarotcardsrevamp.core.ModItems;
import com.tarotcardsrevamp.core.ModMenuTypes;
import com.tarotcardsrevamp.effect.TarotEffectHandler;
import com.tarotcardsrevamp.effect.TarotQuestHandler;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@Mod("tarotcardsrevamp")
public class TarotCardsRevampMod {

    public static final String MOD_ID = "tarotcardsrevamp";

    public TarotCardsRevampMod(IEventBus modEventBus, ModContainer modContainer) {
        // Register configs
        modContainer.registerConfig(ModConfig.Type.SYNCED, TarotConfig.SPEC);

        // Register registries to mod event bus
        com.tarotcardsrevamp.core.ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModDataComponents.DATA_COMPONENT_TYPES.register(modEventBus);
        ModMenuTypes.MENU_TYPES.register(modEventBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);
        com.tarotcardsrevamp.core.ModLootModifiers.GLOBAL_LOOT_MODIFIER_SERIALIZERS.register(modEventBus);

        // Network packets
        modEventBus.addListener(com.tarotcardsrevamp.network.TarotNetwork::register);

        // Client events
        modEventBus.addListener(com.tarotcardsrevamp.client.ClientModEvents::onRegisterMenuScreens);

        // Register gameplay event listeners to NeoForge game event bus
        NeoForge.EVENT_BUS.register(TarotEffectHandler.class);
        NeoForge.EVENT_BUS.register(TarotQuestHandler.class);
        NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        TarotCommand.register(event.getDispatcher());
    }
}
