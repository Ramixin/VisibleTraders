package net.ramixin.visibletraders.mixins.client;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffers;
import net.ramixin.visibletraders.ducks.ClientMerchantMenuDuck;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.Optional;

@Mixin(MerchantMenu.class)
public abstract class MerchantMenuMixin implements ClientMerchantMenuDuck {

    @Shadow
    @Final
    private Merchant trader;

    @Unique
    private final Mutable<MerchantOffers> combinedOffers = new MutableObject<>();

    @Unique
    private boolean useCombinedOffers = false;

    @Unique
    private int combinedOffersScopeDepth = 0;

    @Override
    public void visibleTraders$setLockedTradeOffers(Optional<MerchantOffers> maybeOffers) {
        MerchantOffers combined = new MerchantOffers();
        combined.addAll(this.trader.getOffers());
        maybeOffers.ifPresent(combined::addAll);
        combinedOffers.setValue(combined);
    }

    @Override
    public void visibleTraders$enableCombinedOffers() {
        useCombinedOffers = true;
    }

    @Override
    public void visibleTraders$beginCombinedOffersScope() {
        combinedOffersScopeDepth++;
    }

    @Override
    public void visibleTraders$endCombinedOffersScope() {
        combinedOffersScopeDepth = Math.max(0, combinedOffersScopeDepth - 1);
    }

    @WrapMethod(method = "getOffers")
    private MerchantOffers useCombinedOffersIfEnabled(Operation<MerchantOffers> original) {
        if(combinedOffersScopeDepth > 0 && combinedOffers.get() != null) {
            return combinedOffers.get();
        }
        if(useCombinedOffers && combinedOffers.get() != null) {
            useCombinedOffers = false;
            return combinedOffers.get();
        }
        return original.call();
    }
}
