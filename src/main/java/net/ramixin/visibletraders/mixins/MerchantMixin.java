package net.ramixin.visibletraders.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffers;
import net.ramixin.visibletraders.ducks.VillagerDuck;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Merchant.class)
public interface MerchantMixin {

    @WrapOperation(method = "openTradingScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;sendMerchantOffers(ILnet/minecraft/world/item/trading/MerchantOffers;IIZZ)V"))
    private void sendLockedOffersWithNormalOffersOnScreenOpen(Player instance, int syncId, MerchantOffers merchantOffers, int j, int k, boolean bl, boolean bl2, Operation<Void> original) {
        if(!(((Merchant)this) instanceof Villager villager)) original.call(instance, syncId, merchantOffers, j, k, bl, bl2);
        else {
            VillagerDuck duck = VillagerDuck.of(villager);
            original.call(instance, syncId, duck.visibleTraders$getCombinedOffers(), duck.visibleTraders$getShiftedLevel(), k, bl, bl2);
        }
    }

}
