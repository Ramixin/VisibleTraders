package net.ramixin.visibletraders.networking;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.trading.MerchantOffers;
import net.ramixin.visibletraders.VisibleTradersCommon;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public record ClientboundLockedTradesPayload(Optional<MerchantOffers> offers) implements CustomPacketPayload {

    public static final Type<ClientboundLockedTradesPayload> PACKET_ID = new Type<>(VisibleTradersCommon.id("locked_trades"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundLockedTradesPayload> PACKET_CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeBoolean(payload.offers().isPresent());
                if(payload.offers().isEmpty())
                    return;
                MerchantOffers.STREAM_CODEC.encode(buf, payload.offers().get());
            },
            buf -> {
                if(!buf.readBoolean())
                    return new ClientboundLockedTradesPayload(Optional.empty());
                return new ClientboundLockedTradesPayload(Optional.of(MerchantOffers.STREAM_CODEC.decode(buf)));
            }
    );

    public ClientboundLockedTradesPayload(@Nullable MerchantOffers offers) {
        this(Optional.ofNullable(offers));
    }

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return PACKET_ID;
    }
}
