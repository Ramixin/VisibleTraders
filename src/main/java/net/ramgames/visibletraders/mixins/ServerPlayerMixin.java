package net.ramgames.visibletraders.mixins;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffers;
import net.ramgames.visibletraders.ServerPlayerDuck;
import net.ramgames.visibletraders.VillagerDuck;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin implements ServerPlayerDuck {

    @Shadow public abstract void sendMerchantOffers(int i, MerchantOffers merchantOffers, int j, int k, boolean bl, boolean bl2);

    @Override
    public void visibleTraders$wrapAndSendMerchantOffers(Merchant merchant, int syncId, MerchantOffers merchantOffers, int levelProgress, int experience, boolean leveled, boolean refreshable) {
        if(!(merchant instanceof Villager villager)) {
            sendMerchantOffers(syncId, merchantOffers, levelProgress, experience, leveled, refreshable);
            return;
        }
        int level = levelProgress | (((VillagerDuck)villager).visibleTraders$getAvailableOffersCount() << 8);
        MerchantOffers offersCopy = merchantOffers.copy();
        offersCopy.addAll(((VillagerDuck) villager).visibleTraders$getLockedOffers());
        sendMerchantOffers(syncId, offersCopy, level, experience, leveled, refreshable);
    }
}
