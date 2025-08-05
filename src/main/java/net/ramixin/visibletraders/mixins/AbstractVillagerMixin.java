package net.ramixin.visibletraders.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.trading.MerchantOffer;
import net.ramixin.visibletraders.VisibleTraders;
import net.ramixin.visibletraders.threading.FutureMerchantOffer;
import net.ramixin.visibletraders.threading.SerializableListing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractVillager.class)
public class AbstractVillagerMixin {

    @WrapOperation(method = "addOffersFromItemListings", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/npc/VillagerTrades$ItemListing;getOffer(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/util/RandomSource;)Lnet/minecraft/world/item/trading/MerchantOffer;"))
    private MerchantOffer makeMapTradesFutures(VillagerTrades.ItemListing instance, Entity entity, RandomSource randomSource, Operation<MerchantOffer> original) {
        if(!(instance instanceof SerializableListing listing)) return original.call(instance, entity, randomSource);
        FutureMerchantOffer futureOffer = new FutureMerchantOffer(listing, () -> original.call(instance, entity, randomSource));
        if(!VisibleTraders.TRADE_WORKER.isRunning()) {
            VisibleTraders.LOGGER.error("Trade thread is inactive. Restart to re-enable multithreaded trade generation");
            futureOffer.fulfillFuture();
        } else {
            VisibleTraders.TRADE_WORKER.addOrder(futureOffer);
        }
        return futureOffer;
    }

}
