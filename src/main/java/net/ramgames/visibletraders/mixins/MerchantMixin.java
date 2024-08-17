package net.ramgames.visibletraders.mixins;

import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffers;
import net.ramgames.visibletraders.VillagerDuck;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(Merchant.class)
public interface MerchantMixin {

    @ModifyArg(method = "openTradingScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;sendMerchantOffers(ILnet/minecraft/world/item/trading/MerchantOffers;IIZZ)V"), index = 2)
    private int mergeUnlockedTradeCountWithLevel(int i) {
        if(!((Merchant) this instanceof Villager villager)) return i;
        return i | (((VillagerDuck)villager).visibleTraders$getAvailableOffersCount() << 8);
    }

    @ModifyArg(method = "openTradingScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;sendMerchantOffers(ILnet/minecraft/world/item/trading/MerchantOffers;IIZZ)V"), index = 1)
    private MerchantOffers appendLockedTradesToOffers(MerchantOffers merchantOffers) {
        if(!((Merchant) this instanceof Villager villager)) return merchantOffers;
        MerchantOffers offersCopy = merchantOffers.copy();
        offersCopy.addAll(((VillagerDuck) villager).visibleTraders$getLockedOffers());
        return offersCopy;
    }

}
