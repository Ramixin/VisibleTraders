package net.ramgames.visibletraders.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffers;
import net.ramgames.visibletraders.ServerPlayerDuck;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Merchant.class)
public interface MerchantMixin {

    @WrapOperation(method = "openTradingScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;sendMerchantOffers(ILnet/minecraft/world/item/trading/MerchantOffers;IIZZ)V"))
    private void sendLockedOffersWithNormalOffersOnScreenOpen(Player instance, int i, MerchantOffers merchantOffers, int j, int k, boolean bl, boolean bl2, Operation<Void> original) {
        if(!(instance instanceof ServerPlayer serverPlayer)) original.call(instance, i, merchantOffers, j, k, bl, bl2);
        else ((ServerPlayerDuck)serverPlayer).visibleTraders$wrapAndSendMerchantOffers((Merchant) this, i, merchantOffers, j, k, bl, bl2);
    }

}
