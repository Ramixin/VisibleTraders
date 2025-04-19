package net.ramgames.visibletraders.ducks;

import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.trading.MerchantOffers;
import net.ramgames.visibletraders.LockedTradeData;

public interface VillagerDuck {

    static VillagerDuck of(Villager villager) {
        return (VillagerDuck)villager;
    }

    int visibleTraders$getAvailableOffersCount();

    MerchantOffers visibleTraders$getLockedOffers();

    @SuppressWarnings("unused")
    void visibleTraders$forceTradeGeneration();

    void visibleTraders$setLockedTradeData(LockedTradeData data);

    LockedTradeData visibleTraders$getLockedTradeData();
}
