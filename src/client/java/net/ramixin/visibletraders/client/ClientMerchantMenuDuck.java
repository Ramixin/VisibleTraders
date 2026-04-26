package net.ramixin.visibletraders.client;

import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.trading.MerchantOffers;

public interface ClientMerchantMenuDuck {

    void visibleTraders$setLockedTradeOffers(MerchantOffers offers);

    void visibleTraders$enableCombinedOffers();

    static ClientMerchantMenuDuck of(MerchantMenu screen) {
        return (ClientMerchantMenuDuck) screen;
    }
}
