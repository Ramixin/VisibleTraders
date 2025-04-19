package net.ramgames.visibletraders.ducks;

import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffers;

public interface ServerPlayerDuck {

    void visibleTraders$wrapAndSendMerchantOffers(Merchant merchant, int syncId, MerchantOffers merchantOffers, int levelProgress, int experience, boolean leveled, boolean refreshable);

}
