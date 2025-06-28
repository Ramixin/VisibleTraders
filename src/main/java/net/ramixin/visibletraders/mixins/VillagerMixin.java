package net.ramixin.visibletraders.mixins;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ReputationEventHandler;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerDataHolder;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.ramixin.visibletraders.LockedTradeData;
import net.ramixin.visibletraders.ducks.VillagerDuck;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import java.util.function.Consumer;

@Mixin(Villager.class)
public abstract class VillagerMixin extends AbstractVillager implements ReputationEventHandler, VillagerDataHolder, VillagerDuck {

    @Shadow public abstract @NotNull VillagerData getVillagerData();

    @Unique
    private final Mutable<LockedTradeData> lockedTradeData = new MutableObject<>();

    public VillagerMixin(EntityType<? extends AbstractVillager> entityType, Level level) {
        super(entityType, level);
    }

    @Unique
    private void ifPresent(Consumer<LockedTradeData> consumer) {
        LockedTradeData val = lockedTradeData.getValue();
        if(val == null) return;
        consumer.accept(val);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("HEAD"))
    private void saveLockedTradeData(ValueOutput valueOutput, CallbackInfo ci) {
        ifPresent(data -> data.write(valueOutput));
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void readLockedTradeData(ValueInput valueInput, CallbackInfo ci) {
        lockedTradeData.setValue(new LockedTradeData(valueInput, (Villager) (Object) this));
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void removeLockedTradeDataIfNoOffers(CallbackInfo ci) {
        if(this.offers == null)
            this.lockedTradeData.setValue(null);
    }

    @Inject(method = "updateTrades", at = @At("HEAD"), cancellable = true)
    private void preventAdditionalTradesOnRankIncrease(CallbackInfo ci) {
        if(this.offers == null || this.offers.isEmpty()) {
            this.lockedTradeData.setValue(null);
            return;
        }
        ifPresent(data -> {
            MerchantOffers dismissedTrades = data.popTradeSet();
            if(dismissedTrades != null) {
                this.offers.addAll(dismissedTrades);
                ci.cancel();
            }
        });
    }

    @Override
    public void visibleTraders$setLockedTradeData(LockedTradeData data) {
        this.lockedTradeData.setValue(data);
    }

    @Override
    public Optional<LockedTradeData> visibleTraders$getLockedTradeData() {
        return Optional.ofNullable(lockedTradeData.getValue());
    }

    @Override
    public void visibleTrades$regenerateTrades() {
        this.lockedTradeData.setValue(new LockedTradeData((Villager) (Object) this));
    }

    @Override
    public int visibleTraders$getShiftedLevel() {
        int level = getVillagerData().level();
        if(this.offers == null) return level;
        return level | (this.offers.size() << 8);
    }

    @Override
    public MerchantOffers visibleTraders$getCombinedOffers() {
        MerchantOffers offers = new MerchantOffers();
        offers.addAll(this.offers);
        if(lockedTradeData.getValue() == null)
            visibleTrades$regenerateTrades();
        ifPresent(data -> offers.addAll(data.buildLockedOffers()));
        return offers;
    }
}
