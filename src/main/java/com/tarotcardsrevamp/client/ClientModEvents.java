package com.tarotcardsrevamp.client;

import com.tarotcardsrevamp.client.gui.TarotDeckScreen;
import com.tarotcardsrevamp.core.ModMenuTypes;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public class ClientModEvents {

    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.TAROT_DECK_MENU.get(), TarotDeckScreen::new);
    }
}
