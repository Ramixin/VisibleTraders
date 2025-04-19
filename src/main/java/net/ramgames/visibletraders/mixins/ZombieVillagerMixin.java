package net.ramgames.visibletraders.mixins;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.ramgames.visibletraders.LockedTradeData;
import net.ramgames.visibletraders.ducks.VillagerDuck;
import net.ramgames.visibletraders.ducks.ZombieVillagerDuck;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ZombieVillager.class)
public abstract class ZombieVillagerMixin extends Zombie implements ZombieVillagerDuck {

    @Shadow @Nullable private MerchantOffers tradeOffers;

    @Unique
    private LockedTradeData lockedTradeData = null;

    public ZombieVillagerMixin(EntityType<? extends Zombie> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("HEAD"))
    private void saveLockedTradeData(CompoundTag compoundTag, CallbackInfo ci) {
        if(lockedTradeData == null) return;
        lockedTradeData.write(compoundTag, this.registryAccess().createSerializationContext(NbtOps.INSTANCE));
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void readLockedTradeData(CompoundTag compoundTag, CallbackInfo ci) {
        lockedTradeData = new LockedTradeData(compoundTag, this.registryAccess().createSerializationContext(NbtOps.INSTANCE));
        if(this.tradeOffers != null && !this.tradeOffers.isEmpty()) lockedTradeData.setCachedTrade(this.tradeOffers.getFirst());
    }

    @Inject(method = "method_63659", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/npc/Villager;setVillagerXp(I)V"))
    private void transferTradesToVillager(ServerLevel serverLevel, Villager villager, CallbackInfo ci) {
        VillagerDuck.of(villager).visibleTraders$setLockedTradeData(lockedTradeData);
    }

    @Override
    public void visibleTraders$setLockedTradeData(LockedTradeData data) {
        this.lockedTradeData = data;
    }
}
