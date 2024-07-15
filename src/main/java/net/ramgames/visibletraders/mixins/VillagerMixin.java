package net.ramgames.visibletraders.mixins;

import com.mojang.serialization.Codec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ReputationEventHandler;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerDataHolder;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.ramgames.visibletraders.VillagerDuck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(Villager.class)
public abstract class VillagerMixin extends AbstractVillager implements ReputationEventHandler, VillagerDataHolder, VillagerDuck {

    @Shadow public abstract VillagerData getVillagerData();

    @Shadow public abstract void setVillagerData(VillagerData villagerData);

    @Shadow public abstract void updateTrades();

    @Shadow public abstract boolean isClientSide();

    @Unique
    private static final Logger VisibleTradersLogger = LoggerFactory.getLogger("Visible Traders");

    @Unique
    private List<MerchantOffers> lockedOffers = null;

    @Unique
    private int prevLevel = 0;

    public VillagerMixin(EntityType<? extends AbstractVillager> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("HEAD"))
    private void writeOfferingLevel(CompoundTag compoundTag, CallbackInfo ci) {
        if(this.lockedOffers != null)
            compoundTag.put("LockedOffers", Codec.list(MerchantOffers.CODEC).encodeStart(this.registryAccess().createSerializationContext(NbtOps.INSTANCE), this.lockedOffers).getOrThrow());
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void updateLockedTradesOnTick(CallbackInfo ci) {
        if(this.isClientSide()) return;

        if(this.offers == null) {
            this.lockedOffers = null;
            return;
        }
        if(this.lockedOffers == null) this.lockedOffers = new ArrayList<>();
        int size = this.lockedOffers.size();
        int level = this.getVillagerData().getLevel();
        prevLevel = level;
        if(size + level == 5) return;
        if(size > 0 && size + level > 5) {
            this.lockedOffers.removeFirst();
            return;
        }
        VillagerData data = this.getVillagerData();
        this.setVillagerData(data.setLevel(data.getLevel()+size + 1));
        int prev = this.offers.size();
        this.updateTrades();
        int dif = this.offers.size() - prev;
        MerchantOffers newOffers = new MerchantOffers();
        for(int i = 0; i < dif; i++) newOffers.add(this.offers.removeLast());
        int lockedSize = this.lockedOffers.size();
        this.lockedOffers.add(newOffers);
        if(this.lockedOffers.size() == lockedSize) System.out.println("no change");
        this.setVillagerData(data);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void readOfferingLevel(CompoundTag compoundTag, CallbackInfo ci) {
        if(compoundTag.contains("LockedOffers")) Codec.list(MerchantOffers.CODEC).parse(this.registryAccess().createSerializationContext(NbtOps.INSTANCE), compoundTag.get("LockedOffers")).resultOrPartial(VisibleTradersLogger::error).ifPresent((lockedOffers) -> this.lockedOffers = new ArrayList<>(lockedOffers));
        else this.lockedOffers = new ArrayList<>();
    }

    @Inject(method = "updateTrades", at = @At("HEAD"), cancellable = true)
    private void preventAdditionalTradesOnRankIncrease(CallbackInfo ci) {
        if(this.offers == null) return;
        if(this.lockedOffers == null) return;
        if(prevLevel != this.getVillagerData().getLevel()) return;
        if(!this.lockedOffers.isEmpty() && this.lockedOffers.size() + this.getVillagerData().getLevel() > 5) {
            MerchantOffers newOffers = lockedOffers.removeFirst();
            this.offers.addAll(newOffers);
            ci.cancel();
        }
    }

    @Inject(method = "increaseMerchantCareer", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/npc/Villager;updateTrades()V", shift = At.Shift.BEFORE))
    private void updateLastKnownLevelOnCareerIncrease(CallbackInfo ci) {
        this.prevLevel = this.getVillagerData().getLevel();
    }

    @Override
    public int visibleTraders$getAvailableOffersCount() {
        if(this.offers == null) return 0;
        return this.offers.size();
    }

    @Override
    public MerchantOffers visibleTraders$getLockedOffers() {
        if(this.lockedOffers == null) return new MerchantOffers();
        MerchantOffers lockedOffers = new MerchantOffers();
        for(MerchantOffers listOffers : this.lockedOffers) lockedOffers.addAll(listOffers);
        return lockedOffers;
    }
}
