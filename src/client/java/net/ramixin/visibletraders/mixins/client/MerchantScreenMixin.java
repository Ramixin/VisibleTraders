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
import net.ramixin.visibletraders.ducks.MerchantMenuDuck;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Iterator;

@Mixin(MerchantScreen.class)
public abstract class MerchantScreenMixin extends AbstractContainerScreen<MerchantMenu> {

    @Shadow int scrollOff;

    public MerchantScreenMixin(MerchantMenu abstractContainerMenu, Inventory inventory, Component component) {
        super(abstractContainerMenu, inventory, component);
    }

    @Inject(method = "renderContents", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/trading/MerchantOffers;size()I", ordinal = 1))
    private void disableOptionIfOutOfLevelRange(GuiGraphics guiGraphics, int i, int j, float f, CallbackInfo ci, @Local MerchantScreen.TradeOfferButton tradeOfferButton) {
        tradeOfferButton.active = ((MerchantMenuDuck) this.menu).visibleTraders$shouldAllowTrade(tradeOfferButton.getIndex() + scrollOff);
    }

    @WrapOperation(method = "renderContents", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/MerchantScreen$TradeOfferButton;renderToolTip(Lnet/minecraft/client/gui/GuiGraphics;II)V"))
    private void ifStillGeneratingChangeTooltip(MerchantScreen.TradeOfferButton instance, GuiGraphics guiGraphics, int i, int j, Operation<Void> original) {
        if(!instance.isHovered()) {
            original.call(instance, guiGraphics, i, j);
            return;
        }
        int indexer = instance.getIndex() + this.scrollOff;
        if(indexer >= this.menu.getOffers().size()) return;
        MerchantOffer offer = this.menu.getOffers().get(indexer);
        if(!offer.getCostA().isEmpty()) original.call(instance, guiGraphics, i, j);
        else if(!offer.getResult().is(Items.BARRIER)) original.call(instance, guiGraphics, i, j);
        else {
            guiGraphics.setTooltipForNextFrame(this.font, Component.translatable("menu.trading.generating_full"), i, j);
        }
    }

    // BEHOLD: THE LEAST SKETCHY WAY TO RENDER TEXT INSTEAD OF ITEMS

    @WrapOperation(method = "renderContents", at = @At(value = "INVOKE", target = "Ljava/util/Iterator;next()Ljava/lang/Object;"))
    private <E> E decideIfTradeIsStillGenerating(Iterator<E> instance, Operation<E> original, @Share("visibletraders:isFuture") LocalBooleanRef isFuture) {
        E value = original.call(instance);
        if(!(value instanceof MerchantOffer offer)) return value;
        if(!offer.getCostA().isEmpty()) return value;
        if(!offer.getResult().is(Items.BARRIER)) return value;
        isFuture.set(true);
        return value;
    }

    @ModifyExpressionValue(method = "renderContents", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/MerchantScreen;canScroll(I)Z"))
    private boolean preventRenderingIfFuture(boolean original, @Share("visibletraders:isFuture") LocalBooleanRef isFuture) {
        if(isFuture.get()) return true;
        return original;
    }

    @SuppressWarnings("LocalMayBeArgsOnly")
    @Definition(id = "scrollOff", field = "Lnet/minecraft/client/gui/screens/inventory/MerchantScreen;scrollOff:I")
    @Expression("? < 7 + this.scrollOff")
    @ModifyExpressionValue(method = "renderContents", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean continuePreventingRenderingIfFutureAndRender(boolean original, @Share("visibletraders:isFuture") LocalBooleanRef isFuture, @Local(argsOnly = true) GuiGraphics graphics, @Local(ordinal = 2) int k, @Local(ordinal = 4) LocalIntRef m) {
        if(!isFuture.get() || !original) return original;
        graphics.drawCenteredString(this.font, Component.translatable("menu.trading.generating"), k + 50, m.get() + 7, 0xFF_FF_FF_FF);
        m.set(m.get() + 20);
        isFuture.set(false);
        return false;
    }
}
