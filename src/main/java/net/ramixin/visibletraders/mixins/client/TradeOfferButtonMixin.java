package net.ramixin.visibletraders.mixins.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.trading.MerchantOffers;
import net.ramixin.visibletraders.ducks.ClientMerchantMenuDuck;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MerchantScreen.TradeOfferButton.class)
public class TradeOfferButtonMixin {

    @WrapOperation(method = "extractToolTip", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/MerchantMenu;getOffers()Lnet/minecraft/world/item/trading/MerchantOffers;"))
    private MerchantOffers enabledCombinedOffersForMouseDragged(MerchantMenu instance, Operation<MerchantOffers> original) {
        ClientMerchantMenuDuck duck = (ClientMerchantMenuDuck) instance;
        duck.visibleTraders$enableCombinedOffers();
        return original.call(instance);
    }


}
