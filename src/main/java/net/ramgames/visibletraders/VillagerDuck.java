package net.ramgames.visibletraders;

import net.minecraft.world.item.trading.MerchantOffers;

public interface VillagerDuck {

    int visibleTraders$getAvailableOffersCount();

    MerchantOffers visibleTraders$getLockedOffers();
}