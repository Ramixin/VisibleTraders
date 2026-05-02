package net.ramixin.visibletraders;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.resources.Identifier;
import net.ramixin.visibletraders.networking.ClientboundLockedTradesPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VisibleTraders implements ModInitializer {

    public static final String MOD_ID = "visibletraders";
    public static final String MOD_NAME = "Visible Traders";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);


    @Override
    public void onInitialize() {
        LOGGER.info("Initializing (1/1)");
        PayloadTypeRegistry.clientboundPlay().register(ClientboundLockedTradesPayload.PACKET_ID, ClientboundLockedTradesPayload.PACKET_CODEC);
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
