package net.ramixin.visibletraders.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.inventory.MerchantContainer;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffers;
import net.ramixin.visibletraders.ClientSideMerchantDuck;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;


@Mixin(MerchantContainer.class)
public class MerchantContainerMixin {

    @WrapOperation(method = "updateSellItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/trading/Merchant;getOffers()Lnet/minecraft/world/item/trading/MerchantOffers;"))
    private MerchantOffers removeTradeAutoFillIfLocked(Merchant instance, Operation<MerchantOffers> original) {
        MerchantOffers offers = original.call(instance);
        if(instance instanceof ClientSideMerchantDuck duck) {
            MerchantOffers unlockedOffers = duck.visibleTraders$getClientUnlockedTrades();
            if(unlockedOffers == null) return offers;
            else return unlockedOffers;
        }
        return offers;
    }

}
