package net.ramixin.visibletraders.mixins;

import net.minecraft.world.inventory.MerchantContainer;
import net.minecraft.world.item.trading.Merchant;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(MerchantContainer.class)
public class MerchantContainerMixin {

    @Shadow @Final private Merchant merchant;

    @Inject(method = "updateSellItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z", ordinal = 0, shift = At.Shift.BEFORE), cancellable = true)
    private void removeTradeAutoFillIfLocked(CallbackInfo ci) {
        if(this.merchant.isClientSide()) ci.cancel();
    }

}
