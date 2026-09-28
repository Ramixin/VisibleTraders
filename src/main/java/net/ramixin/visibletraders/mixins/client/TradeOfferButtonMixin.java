package net.ramixin.visibletraders.mixins.client;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.ramixin.visibletraders.ducks.ClientMerchantMenuDuck;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(MerchantScreen.TradeOfferButton.class)
public class TradeOfferButtonMixin {

    @Shadow
    @Final
    private MerchantScreen this$0;

    @WrapMethod(method = "extractToolTip")
    private void useCombinedOffersWhileExtractingTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY, Operation<Void> original) {
        ClientMerchantMenuDuck duck = (ClientMerchantMenuDuck) this.this$0.getMenu();
        duck.visibleTraders$beginCombinedOffersScope();
        try {
            original.call(graphics, mouseX, mouseY);
        } finally {
            duck.visibleTraders$endCombinedOffersScope();
        }
    }


}
