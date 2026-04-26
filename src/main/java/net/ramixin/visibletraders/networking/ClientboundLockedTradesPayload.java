package net.ramixin.visibletraders.networking;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.trading.MerchantOffers;
import net.ramixin.visibletraders.VisibleTraders;
import org.jspecify.annotations.NonNull;

public record ClientboundLockedTradesPayload(MerchantOffers offers) implements CustomPacketPayload {

    public static final Type<ClientboundLockedTradesPayload> PACKET_ID = new Type<>(VisibleTraders.id("locked_trades"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundLockedTradesPayload> PACKET_CODEC = StreamCodec.of(
            (buf, payload) -> MerchantOffers.STREAM_CODEC.encode(buf, payload.offers()),
            buf -> new ClientboundLockedTradesPayload(MerchantOffers.STREAM_CODEC.decode(buf))
    );

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return PACKET_ID;
    }
}
