package net.ramgames.visibletraders.mixins;

import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.ramgames.visibletraders.ClientSideMerchantDuck;
import net.ramgames.visibletraders.MerchantMenuDuck;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(MerchantMenu.class)
public abstract class MerchantMenuMixin implements MerchantMenuDuck {

    @Shadow private int merchantLevel;
    @Shadow @Final private Merchant trader;

    @Shadow public abstract MerchantOffers getOffers();

    @Unique
    private int unlockedTradeCount = 0;

    @Inject(method = "setMerchantLevel", at = @At("HEAD"), cancellable = true)
    private void readUnlockedTradeCountFromLevel(int i, CallbackInfo ci) {
        unlockedTradeCount = i >> 8;
        if(this.trader instanceof ClientSideMerchantDuck duck) {
            List<MerchantOffer> list = getOffers().subList(0, unlockedTradeCount);
            MerchantOffers offers = new MerchantOffers();
            offers.addAll(list);
            duck.visibleTraders$setClientUnlockedTrades(offers);
        }
        this.merchantLevel = i & 255;
        ci.cancel();
    }


    @Override
    public boolean visibleTraders$shouldAllowTrade(int i) {
        if(unlockedTradeCount == 0) return true;
        return i <= unlockedTradeCount-1;
    }
}
