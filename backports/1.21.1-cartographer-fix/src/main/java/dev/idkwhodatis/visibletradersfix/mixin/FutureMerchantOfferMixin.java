package dev.idkwhodatis.visibletradersfix.mixin;

import dev.idkwhodatis.visibletradersfix.FutureMerchantOfferState;
import net.minecraft.world.item.trading.MerchantOffer;
import net.ramixin.visibletraders.VisibleTraders;
import net.ramixin.visibletraders.threading.FutureMerchantOffer;
import org.apache.commons.lang3.mutable.Mutable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(value = FutureMerchantOffer.class, remap = false)
public abstract class FutureMerchantOfferMixin implements FutureMerchantOfferState {

    @Shadow @Final private Mutable<MerchantOffer> offer;
    @Shadow @Final private Supplier<MerchantOffer> future;

    @Unique
    private volatile boolean visibleTradersFix$completed;

    @Inject(method = "fulfillFuture", at = @At("HEAD"), cancellable = true, remap = false)
    private void visibleTradersFix$fulfillSafely(CallbackInfo ci) {
        try {
            this.offer.setValue(this.future.get());
        } catch (Throwable throwable) {
            VisibleTraders.LOGGER.error("Failed to generate async villager trade; dropping the trade instead of leaving an invalid placeholder", throwable);
            this.offer.setValue(null);
        } finally {
            // Volatile write publishes the resolved offer (including a legitimate null)
            // to the server thread before it is allowed to unwrap the future.
            this.visibleTradersFix$completed = true;
        }
        ci.cancel();
    }

    @Override
    public boolean visibleTradersFix$isCompleted() {
        return this.visibleTradersFix$completed;
    }
}
