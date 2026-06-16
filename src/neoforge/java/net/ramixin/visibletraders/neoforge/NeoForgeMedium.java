package net.ramixin.visibletraders.neoforge;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.ramixin.visibletraders.commonapi.LoaderMedium;
import net.ramixin.visibletraders.networking.ClientboundLockedTradesPayload;

public class NeoForgeMedium implements LoaderMedium {
    @Override
    public void sendPayload(ServerPlayer serverPlayer, ClientboundLockedTradesPayload packet) {
        PacketDistributor.sendToPlayer(serverPlayer, packet);
    }
}
