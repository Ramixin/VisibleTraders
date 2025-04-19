package net.ramgames.visibletraders.mixins;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ReputationEventHandler;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerDataHolder;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.ramgames.visibletraders.LockedTradeData;
import net.ramgames.visibletraders.ducks.VillagerDuck;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Villager.class)
public abstract class VillagerMixin extends AbstractVillager implements ReputationEventHandler, VillagerDataHolder, VillagerDuck {

    @Unique
    private LockedTradeData lockedTradeData = LockedTradeData.DEFAULT;

    public VillagerMixin(EntityType<? extends AbstractVillager> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("HEAD"))
    private void saveLockedTradeData(CompoundTag compoundTag, CallbackInfo ci) {
        lockedTradeData.write(compoundTag, this.registryAccess().createSerializationContext(NbtOps.INSTANCE));
    }



    @Inject(method = "tick", at = @At("HEAD"))
    private void updateLockedTradesOnTick(CallbackInfo ci) {
        if(this.isClientSide()) return;
        if(!this.level().hasChunk((int) (this.getX() / 16), (int) (this.getZ() / 16))) return;
        lockedTradeData.tickLockedTrades(offers, (Villager)(Object)this);
    }

    @SuppressWarnings("ConstantValue")
    @Override
    public void visibleTraders$forceTradeGeneration() {
        for(int i = 0; i < 5; i++)
            lockedTradeData.tickLockedTrades(offers, (Villager)(Object)this);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void readLockedTradeData(CompoundTag compoundTag, CallbackInfo ci) {
        lockedTradeData = new LockedTradeData(compoundTag, this.registryAccess().createSerializationContext(NbtOps.INSTANCE));
        if(this.offers != null && !this.offers.isEmpty()) lockedTradeData.setCachedTrade(this.offers.getFirst());
    }

    @Inject(method = "updateTrades", at = @At("HEAD"), cancellable = true)
    private void preventAdditionalTradesOnRankIncrease(CallbackInfo ci) {
        if(this.offers == null) return;
        MerchantOffers dismissedTrades = lockedTradeData.shouldDismissTrades(this.getVillagerData().level());
        if(dismissedTrades != null) {
            this.offers.addAll(dismissedTrades);
            ci.cancel();
        }
    }

    @Inject(method = "increaseMerchantCareer", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/npc/Villager;updateTrades()V"))
    private void updateLastKnownLevelOnCareerIncrease(CallbackInfo ci) {
        this.lockedTradeData.setPrevLevel(this.getVillagerData().level());
    }

    @Override
    public int visibleTraders$getAvailableOffersCount() {
        if(this.offers == null) return 0;
        return this.offers.size();
    }

    @Override
    public MerchantOffers visibleTraders$getLockedOffers() {
        return lockedTradeData.buildLockedOffers();
    }

    @Override
    public void visibleTraders$setLockedTradeData(LockedTradeData data) {
        this.lockedTradeData = data;
    }

    @Override
    public LockedTradeData visibleTraders$getLockedTradeData() {
        return lockedTradeData;
    }
}
