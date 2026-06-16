package net.ramixin.visibletraders.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.ramixin.visibletraders.VisibleTradersCommon;
import net.ramixin.visibletraders.networking.ClientboundLockedTradesPayload;

public class VisibleTraders implements ModInitializer {

    @Override
    public void onInitialize() {
        VisibleTradersCommon.onInitialize(new FabricMedium());
        PayloadTypeRegistry.clientboundPlay().register(ClientboundLockedTradesPayload.PACKET_ID, ClientboundLockedTradesPayload.PACKET_CODEC);
    }
}
