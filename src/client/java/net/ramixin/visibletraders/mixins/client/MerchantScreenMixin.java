package net.ramixin.visibletraders.mixins.client;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.ramixin.visibletraders.VisibleTraders;
import net.ramixin.visibletraders.ducks.MerchantMenuDuck;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MerchantScreen.class)
public abstract class MerchantScreenMixin extends AbstractContainerScreen<MerchantMenu> {

    @Shadow int scrollOff;
    @Shadow @Final private MerchantScreen.TradeOfferButton[] tradeOfferButtons;

    public MerchantScreenMixin(MerchantMenu abstractContainerMenu, Inventory inventory, Component component) {
        super(abstractContainerMenu, inventory, component);
    }

    // UPDATED: Injected into `renderContents` now, inside the loop that updates the buttons' state.
    @Inject(method = "renderContents", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/MerchantScreen$TradeOfferButton;isHoveredOrFocused()Z"))
    private void disableOptionIfOutOfLevelRange(GuiGraphics guiGraphics, int i, int j, float f, CallbackInfo ci, @Local(ordinal=0) MerchantScreen.TradeOfferButton tradeOfferButton) {
        tradeOfferButton.active = ((MerchantMenuDuck) this.menu).visibleTraders$shouldAllowTrade(tradeOfferButton.getIndex() + scrollOff);
    }

    // UPDATED: Changed method to `renderContents`. The target for WrapOperation is still valid.
    @WrapOperation(method = "renderContents", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/MerchantScreen$TradeOfferButton;renderToolTip(Lnet/minecraft/client/gui/GuiGraphics;II)V"))
    private void ifStillGeneratingChangeTooltip(MerchantScreen.TradeOfferButton instance, GuiGraphics guiGraphics, int i, int j, Operation<Void> original) {
        if (!instance.isHoveredOrFocused()) { // Mojang changed this from isHovered()
            original.call(instance, guiGraphics, i, j);
            return;
        }
        VisibleTraders.LOGGER.debug("offers: {}", this.menu.getOffers());
        MerchantOffer offer = this.menu.getOffers().get(instance.getIndex() + this.scrollOff);
        if (!offer.getCostA().isEmpty()) original.call(instance, guiGraphics, i, j);
        else if (!offer.getResult().is(Items.BARRIER)) original.call(instance, guiGraphics, i, j);
        else {
            guiGraphics.setTooltipForNextFrame(this.font, Component.translatable("menu.trading.generating_full"), i, j);
        }
    }

    // BEHOLD: THE LEAST SKETCHY WAY TO RENDER TEXT INSTEAD OF ITEMS

    // UPDATED: Replaced WrapOperation on Iterator.next() with an Inject at the start of the new for-each loop.
    @Inject(method = "renderContents", at = @At("HEAD"))
    private void decideIfTradeIsStillGenerating_HEAD(GuiGraphics guiGraphics, int i, int j, float f, CallbackInfo ci, @Share("visibletraders:isFuture") LocalBooleanRef isFuture) {
        // Reset the flag at the start of the render method.
        isFuture.set(false);
    }

    // This mixin captures the current offer in the loop.
    @Inject(method = "renderContents", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/trading/MerchantOffer;getBaseCostA()Lnet/minecraft/world/item/ItemStack;"))
    private void decideIfTradeIsStillGenerating_BODY(GuiGraphics guiGraphics, int i, int j, float f, CallbackInfo ci, @Local MerchantOffer merchantOffer, @Share("visibletraders:isFuture") LocalBooleanRef isFuture) {
        if (!merchantOffer.getCostA().isEmpty()) return;
        if (!merchantOffer.getResult().is(Items.BARRIER)) return;
        isFuture.set(true);
    }


    // UPDATED: Changed method to `renderContents`. The target is still valid.
    @ModifyExpressionValue(method = "renderContents", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/MerchantScreen;canScroll(I)Z"))
    private boolean preventRenderingIfFuture(boolean original, @Share("visibletraders:isFuture") LocalBooleanRef isFuture) {
        if (isFuture.get()) return true;
        return original;
    }


    // UPDATED: Changed method to `renderContents`. The MixinExtras expression is still valid.
    @Definition(id = "scrollOff", field = "Lnet/minecraft/client/gui/screens/inventory/MerchantScreen;scrollOff:I")
    @Expression("? < 7 + this.scrollOff")
    @ModifyExpressionValue(method = "renderContents", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean continuePreventingRenderingIfFutureAndRender(boolean original, @Share("visibletraders:isFuture") LocalBooleanRef isFuture, @Local(argsOnly = true) GuiGraphics graphics, @Local(ordinal = 2) int k, @Local(ordinal = 4) LocalIntRef m) {
        if (!isFuture.get() || !original) return original;
        graphics.drawCenteredString(this.font, Component.translatable("menu.trading.generating"), k + 5 + 5 + 44, m.get() + 2 + 7, 0xFF_FF_FF_FF); // Centered the text
        m.set(m.get() + 20);
        isFuture.set(false);
        return false;
    }
}
