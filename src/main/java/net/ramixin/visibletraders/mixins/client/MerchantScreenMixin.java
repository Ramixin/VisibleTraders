package net.ramixin.visibletraders.mixins.client;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MerchantMenu;
import net.ramixin.visibletraders.ducks.ClientMerchantMenuDuck;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = MerchantScreen.class)
public abstract class MerchantScreenMixin extends AbstractContainerScreen<MerchantMenu> {

    @Shadow
    private int scrollOff;

    public MerchantScreenMixin(MerchantMenu abstractContainerMenu, Inventory inventory, Component component) {
        super(abstractContainerMenu, inventory, component);
    }

    @Inject(method = "extractContents", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/MerchantMenu;getOffers()Lnet/minecraft/world/item/trading/MerchantOffers;"))
    private void combineOffersBeforeRender(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, CallbackInfo ci) {
        ClientMerchantMenuDuck duck = (ClientMerchantMenuDuck) this.menu;
        duck.visibleTraders$enableCombinedOffers();
    }

    @Inject(method = "extractContents", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/trading/MerchantOffers;size()I", ordinal = 1))
    private void disableOptionIfOutOfLevelRange(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, CallbackInfo ci, @Local(name = "button") MerchantScreen.TradeOfferButton button) {
        button.active = button.getIndex() - this.menu.getOffers().size() + scrollOff < 0;
    }

    @Inject(method = "mouseClicked", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/MerchantMenu;getOffers()Lnet/minecraft/world/item/trading/MerchantOffers;"))
    private void enabledCombinedOffersForMouseClicked(MouseButtonEvent event, boolean doubleClick, CallbackInfoReturnable<Boolean> cir) {
        ClientMerchantMenuDuck duck = (ClientMerchantMenuDuck) this.menu;
        duck.visibleTraders$enableCombinedOffers();
    }

    @Inject(method = "mouseScrolled", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/MerchantMenu;getOffers()Lnet/minecraft/world/item/trading/MerchantOffers;"))
    private void enabledCombinedOffersForMouseScrolled(double x, double y, double scrollX, double scrollY, CallbackInfoReturnable<Boolean> cir) {
        ClientMerchantMenuDuck duck = (ClientMerchantMenuDuck) this.menu;
        duck.visibleTraders$enableCombinedOffers();
    }

    @Inject(method = "mouseDragged", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/MerchantMenu;getOffers()Lnet/minecraft/world/item/trading/MerchantOffers;"))
    private void enabledCombinedOffersForMouseDragged(MouseButtonEvent event, double dx, double dy, CallbackInfoReturnable<Boolean> cir) {
        ClientMerchantMenuDuck duck = (ClientMerchantMenuDuck) this.menu;
        duck.visibleTraders$enableCombinedOffers();
    }

}
