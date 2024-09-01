package net.ramgames.visibletraders.mixins;

import net.minecraft.world.entity.npc.ClientSideMerchant;
import net.minecraft.world.item.trading.MerchantOffers;
import net.ramgames.visibletraders.ClientSideMerchantDuck;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ClientSideMerchant.class)
public class ClientSideMerchantMixin implements ClientSideMerchantDuck {

    @Unique
    private MerchantOffers clientUnlockedTrades = null;


    @Unique
    @Override
    public MerchantOffers visibleTraders$getClientUnlockedTrades() {
        return clientUnlockedTrades;
    }

    @Unique
    @Override
    public void visibleTraders$setClientUnlockedTrades(MerchantOffers offers) {
        this.clientUnlockedTrades = offers;
    }

}
