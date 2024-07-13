package net.ramgames.visibletraders.mixins;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ReputationEventHandler;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerDataHolder;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Villager.class)
public abstract class VillagerMixin extends AbstractVillager implements ReputationEventHandler, VillagerDataHolder {

    @Shadow public abstract VillagerData getVillagerData();

    @Shadow public abstract void setVillagerData(VillagerData villagerData);

    @Unique
    private int offeringLevel = 1;

    public VillagerMixin(EntityType<? extends AbstractVillager> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void insertTradesIfNotAllPresent(CallbackInfo ci) {
        if(this.offers == null) return;
        if(this.getVillagerData() == null) return;
        int prev = this.offers.size();
        if(this.offeringLevel < 5) {
            VillagerData data = this.getVillagerData();
            this.setVillagerData(data.setLevel(offeringLevel+1));
            this.updateTrades();
            this.setVillagerData(data);
            if(prev != this.offers.size()) offeringLevel++;
        }
    }

    @Inject(method = "addAdditionalSaveData", at = @At("HEAD"))
    private void writeOfferingLevel(CompoundTag compoundTag, CallbackInfo ci) {
        compoundTag.putInt("offeringLevel", offeringLevel);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("HEAD"))
    private void readOfferingLevel(CompoundTag compoundTag, CallbackInfo ci) {
        offeringLevel = compoundTag.getInt("offeringLevel");
        if(offeringLevel == 0) offeringLevel = this.getVillagerData().getLevel();
    }
}
