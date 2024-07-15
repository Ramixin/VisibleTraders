package net.ramgames.visibletraders.mixins;

import net.minecraft.world.inventory.MerchantMenu;
import net.ramgames.visibletraders.MerchantMenuDuck;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MerchantMenu.class)
public abstract class MerchantMenuMixin implements MerchantMenuDuck {

    @Shadow private int merchantLevel;
    @Unique
    private int unlockedTradeCount = 0;

    @Inject(method = "setMerchantLevel", at = @At("HEAD"), cancellable = true)
    private void readUnlockedTradeCountFromLevel(int i, CallbackInfo ci) {
        unlockedTradeCount = i >> 8;
        this.merchantLevel = i & 255;
        ci.cancel();
    }


    @Override
    public boolean visibleTraders$shouldAllowTrade(int i) {
        if(unlockedTradeCount == 0) return true;
        return i <= unlockedTradeCount-1;
    }
}
