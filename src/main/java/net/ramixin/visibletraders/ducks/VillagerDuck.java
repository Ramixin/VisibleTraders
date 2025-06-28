package net.ramixin.visibletraders.ducks;

import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.trading.MerchantOffers;
import net.ramixin.visibletraders.LockedTradeData;

import java.util.Optional;

public interface VillagerDuck {

    static VillagerDuck of(Villager villager) {
        return (VillagerDuck)villager;
    }

    void visibleTraders$setLockedTradeData(LockedTradeData data);

    Optional<LockedTradeData> visibleTraders$getLockedTradeData();

    void visibleTrades$regenerateTrades();

    MerchantOffers visibleTraders$getCombinedOffers();

    int visibleTraders$getShiftedLevel();
}
