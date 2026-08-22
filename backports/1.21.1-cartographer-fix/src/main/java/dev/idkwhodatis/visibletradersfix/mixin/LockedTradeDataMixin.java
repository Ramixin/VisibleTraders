package dev.idkwhodatis.visibletradersfix.mixin;

import dev.idkwhodatis.visibletradersfix.FutureMerchantOfferState;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.ramixin.visibletraders.LockedTradeData;
import net.ramixin.visibletraders.VisibleTraders;
import net.ramixin.visibletraders.threading.FutureMerchantOffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Mixin(value = LockedTradeData.class, remap = false)
public abstract class LockedTradeDataMixin {

    @Shadow private List<MerchantOffers> lockedOffers;

    @Shadow
    private static ArrayList<MerchantOffers> generateTrades(Villager villager) {
        throw new AssertionError();
    }

    @Unique
    private MerchantOffers visibleTradersFix$normalize(MerchantOffers source, boolean keepPending) {
        MerchantOffers normalized = new MerchantOffers();
        for (MerchantOffer offer : source) {
            if (!(offer instanceof FutureMerchantOffer futureOffer)) {
                normalized.add(offer);
                continue;
            }

            FutureMerchantOfferState state = (FutureMerchantOfferState) (Object) futureOffer;
            if (!state.visibleTradersFix$isCompleted()) {
                if (keepPending) {
                    normalized.add(futureOffer);
                    continue;
                }
                return null;
            }

            MerchantOffer resolved = futureOffer.getFuture();
            if (resolved != null) normalized.add(resolved);
            // A completed null result is valid for explorer-map lookups: simply omit it.
        }
        return normalized;
    }

    @Inject(method = "popTradeSet", at = @At("HEAD"), cancellable = true, remap = false)
    private void visibleTradersFix$popOnlyResolved(CallbackInfoReturnable<MerchantOffers> cir) {
        if (this.lockedOffers == null || this.lockedOffers.isEmpty()) {
            cir.setReturnValue(null);
            return;
        }

        MerchantOffers normalized = this.visibleTradersFix$normalize(this.lockedOffers.getFirst(), false);
        if (normalized == null) {
            // The next level still contains an async placeholder. Do not remove or expose it.
            cir.setReturnValue(null);
            return;
        }

        this.lockedOffers.removeFirst();
        cir.setReturnValue(normalized);
    }

    @Inject(method = "peekTradeSet", at = @At("HEAD"), cancellable = true, remap = false)
    private void visibleTradersFix$peekNormalized(CallbackInfoReturnable<Optional<MerchantOffers>> cir) {
        if (this.lockedOffers == null || this.lockedOffers.isEmpty()) {
            cir.setReturnValue(Optional.empty());
            return;
        }
        cir.setReturnValue(Optional.of(this.visibleTradersFix$normalize(this.lockedOffers.getFirst(), true)));
    }

    @Inject(method = "buildLockedOffers", at = @At("HEAD"), cancellable = true, remap = false)
    private void visibleTradersFix$buildNormalizedOffers(CallbackInfoReturnable<MerchantOffers> cir) {
        MerchantOffers combined = new MerchantOffers();
        if (this.lockedOffers == null) {
            cir.setReturnValue(combined);
            return;
        }

        for (MerchantOffers set : this.lockedOffers) {
            combined.addAll(this.visibleTradersFix$normalize(set, true));
        }
        cir.setReturnValue(combined);
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true, remap = false)
    private void visibleTradersFix$avoidUnresolvedPopLoop(Villager villager, Runnable popCallback, CallbackInfo ci) {
        if (this.lockedOffers == null) {
            ci.cancel();
            return;
        }

        int requiredSets = 5 - villager.getVillagerData().getLevel();
        while (requiredSets < this.lockedOffers.size()) {
            int before = this.lockedOffers.size();
            popCallback.run();
            if (this.lockedOffers.size() == before) {
                // popTradeSet refused because the first set is still generating.
                break;
            }
        }

        if (requiredSets > this.lockedOffers.size()) {
            VisibleTraders.LOGGER.error("detected missing locked trade sets. Rebuilding locked offers");
            this.lockedOffers = generateTrades(villager);
        }
        ci.cancel();
    }
}
