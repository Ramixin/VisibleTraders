package net.ramixin.visibletraders.neoforge;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.ramixin.visibletraders.VisibleTradersCommon;
import net.ramixin.visibletraders.client.VisibleTradersCommonClient;
import net.ramixin.visibletraders.networking.ClientboundLockedTradesPayload;

@Mod("visibletraders")
public class VisibleTraders {

    public VisibleTraders(IEventBus modBus) {
        modBus.addListener(this::commonSetup);
        modBus.addListener(this::registerPayloads);
    }


    private void commonSetup(FMLCommonSetupEvent event) {
        VisibleTradersCommon.onInitialize(new NeoForgeMedium());
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(ClientboundLockedTradesPayload.PACKET_ID, ClientboundLockedTradesPayload.PACKET_CODEC);
    }

    @EventBusSubscriber(modid = "visibletraders", value = Dist.CLIENT)
    public static class VisibleTradersClient {

        @SubscribeEvent
        private static void onClientSetup(RegisterClientPayloadHandlersEvent event) {
            event.register(ClientboundLockedTradesPayload.PACKET_ID, (payload, _) -> {
                if(payload instanceof ClientboundLockedTradesPayload lockedTradesPayload)
                    VisibleTradersCommonClient.handleClientboundTradesPayload(lockedTradesPayload);
            });
        }

    }

}
