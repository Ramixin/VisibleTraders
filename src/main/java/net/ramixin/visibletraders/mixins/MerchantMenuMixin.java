package net.ramixin.visibletraders.mixins;

import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.ramixin.visibletraders.ClientSideMerchantDuck;
import net.ramixin.visibletraders.MerchantMenuDuck;
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
    @Shadow @Final
    private Merchant trader;

    @Shadow public abstract MerchantOffers getOffers();

    @Unique
    private int visibleTraders_NeoForge$unlockedTradeCount = 0;

    @Inject(method = "setMerchantLevel", at = @At("HEAD"), cancellable = true)
    private void readUnlockedTradeCountFromLevel(int i, CallbackInfo ci) {
        visibleTraders_NeoForge$unlockedTradeCount = i >> 8;
        if(this.trader instanceof ClientSideMerchantDuck duck) {
            List<MerchantOffer> list = getOffers().subList(0, visibleTraders_NeoForge$unlockedTradeCount);
            MerchantOffers offers = new MerchantOffers();
            offers.addAll(list);
            duck.visibleTraders$setClientUnlockedTrades(offers);
        }
        this.merchantLevel = i & 255;
        ci.cancel();
    }


    @Override
    public boolean visibleTraders$shouldAllowTrade(int i) {
        if(visibleTraders_NeoForge$unlockedTradeCount == 0) return true;
        return i <= visibleTraders_NeoForge$unlockedTradeCount -1;
    }
}
