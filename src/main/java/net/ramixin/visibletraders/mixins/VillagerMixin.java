package net.ramixin.visibletraders.mixins;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ReputationEventHandler;
import net.minecraft.world.entity.ai.village.ReputationEventType;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerDataHolder;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.ramixin.visibletraders.VillagerDuck;
import org.jetbrains.annotations.NotNull;
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

    @Shadow public abstract @NotNull VillagerData getVillagerData();

    @Shadow public abstract void setVillagerData(@NotNull VillagerData villagerData);

    @Shadow public abstract void updateTrades();

    @Shadow public abstract boolean isClientSide();

    @Shadow public abstract void onReputationEventFrom(@NotNull ReputationEventType reputationEventType, @NotNull Entity entity);

    @Unique
    private static final Logger visibleTraders_NeoForge$visibleTradersLogger = LoggerFactory.getLogger("Visible Traders");

    @Unique
    private List<MerchantOffers> visibleTraders_NeoForge$lockedOffers = null;

    @Unique
    private MerchantOffer visibleTraders_NeoForge$cachedTrade = null;

    @Unique
    private int visibleTraders_NeoForge$prevLevel = 0;

    public VillagerMixin(EntityType<? extends AbstractVillager> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("HEAD"))
    private void writeOfferingLevel(CompoundTag compoundTag, CallbackInfo ci) {
        if(!this.isClientSide()) for(int i = 0; i < 5; i++) visibleTraders_NeoForge$lockedTradesTick();

        if(this.visibleTraders_NeoForge$lockedOffers == null) return;
        DataResult<Tag> val = Codec.list(MerchantOffers.CODEC).encodeStart(this.registryAccess().createSerializationContext(NbtOps.INSTANCE), this.visibleTraders_NeoForge$lockedOffers);
        if(val.isError()) //noinspection OptionalGetWithoutIsPresent
            visibleTraders_NeoForge$visibleTradersLogger.error(val.error().get().toString());
        else compoundTag.put("LockedOffers", val.getOrThrow());
    }

    @Unique
    private void visibleTraders_NeoForge$lockedTradesTick() {

        int level = this.getVillagerData().getLevel();
        visibleTraders_NeoForge$prevLevel = level;

        if(this.offers == null) {
            this.visibleTraders_NeoForge$lockedOffers = null;
            return;
        }

        if(visibleTraders_NeoForge$cachedTrade == null || this.offers.isEmpty()) {
            this.visibleTraders_NeoForge$lockedOffers = null;
            if(!this.offers.isEmpty()) this.visibleTraders_NeoForge$cachedTrade = this.offers.getFirst();
            return;
        }

        if(visibleTraders_NeoForge$cachedTrade != this.offers.getFirst()) {
            this.visibleTraders_NeoForge$lockedOffers = null;
            this.visibleTraders_NeoForge$cachedTrade = this.offers.getFirst();
            return;
        }

        if(this.visibleTraders_NeoForge$lockedOffers == null) this.visibleTraders_NeoForge$lockedOffers = new ArrayList<>();
        int size = this.visibleTraders_NeoForge$lockedOffers.size();

        if(size + level == 5) return;
        if(size > 0 && size + level > 5) {
            this.visibleTraders_NeoForge$lockedOffers.removeFirst();
            return;
        }
        VillagerData data = this.getVillagerData();
        this.setVillagerData(data.setLevel(data.getLevel()+size + 1));
        int prev = this.offers.size();
        this.updateTrades();
        int dif = this.offers.size() - prev;
        MerchantOffers newOffers = new MerchantOffers();
        for(int i = 0; i < dif; i++) newOffers.add(this.offers.removeLast());
        this.visibleTraders_NeoForge$lockedOffers.add(newOffers);
        this.setVillagerData(data);

    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void updateLockedTradesOnTick(CallbackInfo ci) {
        if(this.isClientSide()) return;

        visibleTraders_NeoForge$lockedTradesTick();
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void readOfferingLevel(CompoundTag compoundTag, CallbackInfo ci) {
        if(compoundTag.contains("LockedOffers")) Codec.list(MerchantOffers.CODEC).parse(this.registryAccess().createSerializationContext(NbtOps.INSTANCE), compoundTag.get("LockedOffers")).resultOrPartial(msg -> {
            this.visibleTraders_NeoForge$lockedOffers = new ArrayList<>();
            visibleTraders_NeoForge$visibleTradersLogger.error(msg);
        }).ifPresent((lockedOffers) -> this.visibleTraders_NeoForge$lockedOffers = new ArrayList<>(lockedOffers));
        else this.visibleTraders_NeoForge$lockedOffers = new ArrayList<>();
        if(this.offers != null && !this.offers.isEmpty()) visibleTraders_NeoForge$cachedTrade = this.offers.getFirst();
    }

    @Inject(method = "updateTrades", at = @At("HEAD"), cancellable = true)
    private void preventAdditionalTradesOnRankIncrease(CallbackInfo ci) {
        if(this.offers == null) return;
        if(this.visibleTraders_NeoForge$lockedOffers == null) return;
        if(visibleTraders_NeoForge$prevLevel != this.getVillagerData().getLevel()) return;
        if(!this.visibleTraders_NeoForge$lockedOffers.isEmpty() && this.visibleTraders_NeoForge$lockedOffers.size() + this.getVillagerData().getLevel() > 5) {
            MerchantOffers newOffers = visibleTraders_NeoForge$lockedOffers.removeFirst();
            this.offers.addAll(newOffers);
            ci.cancel();
        }
    }

    @Inject(method = "increaseMerchantCareer", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/npc/Villager;updateTrades()V"))
    private void updateLastKnownLevelOnCareerIncrease(CallbackInfo ci) {
        this.visibleTraders_NeoForge$prevLevel = this.getVillagerData().getLevel();
    }

    @Override
    public int visibleTraders$getAvailableOffersCount() {
        if(this.offers == null) return 0;
        return this.offers.size();
    }

    @Override
    public MerchantOffers visibleTraders$getLockedOffers() {
        if(this.visibleTraders_NeoForge$lockedOffers == null) return new MerchantOffers();
        MerchantOffers lockedOffers = new MerchantOffers();
        for(MerchantOffers listOffers : List.copyOf(this.visibleTraders_NeoForge$lockedOffers)) for(MerchantOffer offer : listOffers) {
            if(offer.getResult().isEmpty()) {
                this.visibleTraders_NeoForge$lockedOffers = new ArrayList<>();
                visibleTraders_NeoForge$visibleTradersLogger.error("detected incomplete trade. Rebuilding locked offers");
                return new MerchantOffers();
            }
            lockedOffers.add(offer);
        }
        return lockedOffers;
    }
}
