package net.ramixin.visibletraders.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffers;
import net.ramixin.visibletraders.VisibleTradersCommon;
import net.ramixin.visibletraders.ducks.VillagerDuck;
import net.ramixin.visibletraders.networking.ClientboundLockedTradesPayload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(Merchant.class)
public interface MerchantMixin {

    @WrapOperation(method = "openTradingScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;sendMerchantOffers(ILnet/minecraft/world/item/trading/MerchantOffers;IIZZ)V"))
    private void sendLockedOffersWithNormalOffersOnScreenOpen(Player instance, int containerId, MerchantOffers offers, int merchantLevel, int merchantXp, boolean showProgressBar, boolean canRestock, Operation<Void> original) {
        original.call(instance, containerId, offers, merchantLevel, merchantXp, showProgressBar, canRestock);
        if(!(((Merchant)this) instanceof Villager villager)) return;
        if(!(instance instanceof ServerPlayer serverPlayer)) return;
        VillagerDuck duck = VillagerDuck.of(villager);
        Optional<MerchantOffers> maybeOffers = duck.visibleTraders$getCondensedOffers();
        if(maybeOffers.isPresent())
            VisibleTradersCommon.getMedium().sendPayload(serverPlayer, new ClientboundLockedTradesPayload(maybeOffers));
        else
            duck.visibleTraders$requestOffers(serverPlayer);
    }

}
