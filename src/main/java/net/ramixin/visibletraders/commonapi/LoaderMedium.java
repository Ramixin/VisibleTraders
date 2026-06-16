package net.ramixin.visibletraders.commonapi;

import net.minecraft.server.level.ServerPlayer;
import net.ramixin.visibletraders.networking.ClientboundLockedTradesPayload;

public interface LoaderMedium {

    void sendPayload(ServerPlayer serverPlayer, ClientboundLockedTradesPayload packet);

}
