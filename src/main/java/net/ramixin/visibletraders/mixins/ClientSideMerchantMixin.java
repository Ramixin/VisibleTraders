package net.ramixin.visibletraders.mixins;

import net.minecraft.world.entity.npc.ClientSideMerchant;
import net.minecraft.world.item.trading.MerchantOffers;
import net.ramixin.visibletraders.ClientSideMerchantDuck;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ClientSideMerchant.class)
public class ClientSideMerchantMixin implements ClientSideMerchantDuck {

    @Unique
    private MerchantOffers visibleTraders_NeoForge$clientUnlockedTrades = null;


    @Unique
    @Override
    public MerchantOffers visibleTraders$getClientUnlockedTrades() {
        return visibleTraders_NeoForge$clientUnlockedTrades;
    }

    @Unique
    @Override
    public void visibleTraders$setClientUnlockedTrades(MerchantOffers offers) {
        this.visibleTraders_NeoForge$clientUnlockedTrades = offers;
    }

}
