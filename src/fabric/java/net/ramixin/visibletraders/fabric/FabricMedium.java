package net.ramixin.visibletraders.fabric;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.ramixin.visibletraders.commonapi.LoaderMedium;
import net.ramixin.visibletraders.networking.ClientboundLockedTradesPayload;

public class FabricMedium implements LoaderMedium {
    @Override
    public void sendPayload(ServerPlayer serverPlayer, ClientboundLockedTradesPayload packet) {
        ServerPlayNetworking.send(serverPlayer, packet);
    }
}
