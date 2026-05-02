package net.ramixin.visibletraders.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ReputationEventHandler;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerData;
import net.minecraft.world.entity.npc.villager.VillagerDataHolder;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.ramixin.visibletraders.LockedTradeData;
import net.ramixin.visibletraders.ducks.VillagerDuck;
import net.ramixin.visibletraders.networking.ClientboundLockedTradesPayload;
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
import java.util.concurrent.atomic.AtomicBoolean;
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
        LockedTradeData val = lockedTradeData.get();
        if(val == null) return;
        consumer.accept(val);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("HEAD"))
    private void saveLockedTradeData(ValueOutput output, CallbackInfo ci) {
        ifPresent(data -> data.write(output));
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void readLockedTradeData(ValueInput input, CallbackInfo ci) {
        lockedTradeData.setValue(LockedTradeData.constructOrNull(input, this));
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void removeLockedTradeDataIfNoOffers(CallbackInfo ci) {
        if(this.offers == null)
            this.lockedTradeData.setValue(null);
        ifPresent(data -> data.tick((Villager) (Object) this, this::appendLockedOffer));
    }

    @Inject(method = "updateTrades", at = @At("HEAD"), cancellable = true)
    private void preventAdditionalTradesOnRankIncrease(CallbackInfo ci) {
        if(this.offers == null || this.offers.isEmpty()) {
            this.lockedTradeData.setValue(null);
            return;
        }
        if(appendLockedOffer()) ci.cancel();
    }

    @Unique
    private boolean appendLockedOffer() {
        if(this.offers == null) return false;
        AtomicBoolean result = new AtomicBoolean(false);
        ifPresent(data -> {
            Optional<MerchantOffers> dismissedTrades = data.popTradeSet();
            if(dismissedTrades.isPresent()) {
                this.offers.addAll(dismissedTrades.get());
                result.set(true);
            }
        });
        return result.get();
    }

    @WrapOperation(method = "customServerAiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/npc/villager/Villager;isTrading()Z", ordinal = 0))
    private boolean preventUpgradeIfStillGeneratingTrades(Villager instance, Operation<Boolean> original) {
        boolean originalResult = original.call(instance);
        if(originalResult) return true;
        if(lockedTradeData.get() == null) return false;
        LockedTradeData data = lockedTradeData.get();
        return data.isGenerating();
    }


    @Override
    public void visibleTraders$setLockedTradeData(LockedTradeData data) {
        this.lockedTradeData.setValue(data);
    }

    @Override
    public Optional<LockedTradeData> visibleTraders$getLockedTradeData() {
        return Optional.ofNullable(lockedTradeData.get());
    }

    @Override
    public void visibleTrades$regenerateTrades() {
        this.lockedTradeData.setValue(new LockedTradeData((Villager) (Object) this));
    }

    @Override
    public Optional<MerchantOffers> visibleTraders$getCondensedOffers() {
        LockedTradeData data = lockedTradeData.get();
        if(data == null) {
            visibleTrades$regenerateTrades();
            return Optional.empty();
        }
        MerchantOffers offers = data.condense();
        if(offers.isEmpty()) return Optional.empty();
        return Optional.of(offers);
    }

    @Override
    public void visibleTraders$requestOffers(ServerPlayer player) {
        ServerPlayNetworking.send(player, new ClientboundLockedTradesPayload(Optional.empty()));
        ifPresent(data -> data.requestOffers(player));
    }
}
