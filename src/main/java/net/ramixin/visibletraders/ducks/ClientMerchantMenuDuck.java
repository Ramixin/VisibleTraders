package net.ramixin.visibletraders.ducks;

import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.trading.MerchantOffers;

import java.util.Optional;

public interface ClientMerchantMenuDuck {

    void visibleTraders$setLockedTradeOffers(Optional<MerchantOffers> maybeOffers);

    void visibleTraders$enableCombinedOffers();

    static ClientMerchantMenuDuck of(MerchantMenu screen) {
        return (ClientMerchantMenuDuck) screen;
    }
}
