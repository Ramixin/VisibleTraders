package net.ramixin.visibletraders.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.client.player.LocalPlayer;
import net.ramixin.visibletraders.networking.ClientboundLockedTradesPayload;

public class VisibleTradersClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(ClientboundLockedTradesPayload.PACKET_ID, (payload, _) -> {
            LocalPlayer player = Minecraft.getInstance().player;
            if(player == null) return;
            if(!(Minecraft.getInstance().screen instanceof MerchantScreen merchantScreen)) return;
            ClientMerchantMenuDuck duck = ClientMerchantMenuDuck.of(merchantScreen.getMenu());
            duck.visibleTraders$setLockedTradeOffers(payload.offers());
        });
    }
}
