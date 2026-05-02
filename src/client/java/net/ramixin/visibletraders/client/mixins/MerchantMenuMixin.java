package net.ramixin.visibletraders.client.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffers;
import net.ramixin.visibletraders.client.ClientMerchantMenuDuck;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

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

    @ModifyReturnValue(method = "getOffers", at = @At("RETURN"))
    private MerchantOffers useCombinedOffersIfEnabled(MerchantOffers original) {
        if(useCombinedOffers && combinedOffers.get() != null) {
            useCombinedOffers = false;
            return combinedOffers.get();
        }
        return original;
    }
}
