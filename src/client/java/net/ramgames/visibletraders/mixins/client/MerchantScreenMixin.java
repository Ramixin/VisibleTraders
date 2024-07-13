package net.ramgames.visibletraders.mixins.client;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MerchantMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MerchantScreen.class)
public abstract class MerchantScreenMixin extends AbstractContainerScreen<MerchantMenu> {

    @Shadow int scrollOff;

    public MerchantScreenMixin(MerchantMenu abstractContainerMenu, Inventory inventory, Component component) {
        super(abstractContainerMenu, inventory, component);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/trading/MerchantOffers;size()I", ordinal = 1, shift = At.Shift.BEFORE))
    private void disableOptionIfOutOfLevelRange(GuiGraphics guiGraphics, int i, int j, float f, CallbackInfo ci, @Local MerchantScreen.TradeOfferButton tradeOfferButton) {
        tradeOfferButton.active = ((tradeOfferButton.getIndex()+this.scrollOff) / 2) <= (this.menu.getTraderLevel()-1);
    }
}
