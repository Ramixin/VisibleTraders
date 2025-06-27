package net.ramixin.visibletraders.ducks;

import net.minecraft.world.item.trading.MerchantOffers;
import org.spongepowered.asm.mixin.Unique;

public interface ClientSideMerchantDuck {


    @Unique
    MerchantOffers visibleTraders$getClientUnlockedTrades();

    @Unique
    void visibleTraders$setClientUnlockedTrades(MerchantOffers offers);

}
