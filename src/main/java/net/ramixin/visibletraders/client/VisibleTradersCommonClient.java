package net.ramixin.visibletraders.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.client.player.LocalPlayer;
import net.ramixin.visibletraders.ducks.ClientMerchantMenuDuck;
import net.ramixin.visibletraders.networking.ClientboundLockedTradesPayload;

public class VisibleTradersCommonClient {

    public static void handleClientboundTradesPayload(ClientboundLockedTradesPayload payload) {
        LocalPlayer player = Minecraft.getInstance().player;
        if(player == null) return;
        if(!(Minecraft.getInstance().screen instanceof MerchantScreen merchantScreen)) return;
        ClientMerchantMenuDuck duck = ClientMerchantMenuDuck.of(merchantScreen.getMenu());
        duck.visibleTraders$setLockedTradeOffers(payload.offers());
    }

}
