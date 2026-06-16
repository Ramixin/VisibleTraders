package net.ramixin.visibletraders.fabric.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.ramixin.visibletraders.client.VisibleTradersCommonClient;
import net.ramixin.visibletraders.networking.ClientboundLockedTradesPayload;

public class VisibleTradersClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(ClientboundLockedTradesPayload.PACKET_ID,
                (payload, _) -> VisibleTradersCommonClient.handleClientboundTradesPayload(payload)
        );
    }
}
