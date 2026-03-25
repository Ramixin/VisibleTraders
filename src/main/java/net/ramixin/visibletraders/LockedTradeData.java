package net.ramixin.visibletraders;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerData;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.item.trading.TradeSet;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LockedTradeData {

    private List<MerchantOffers> lockedOffers = new ArrayList<>();
    private boolean activelyGenerating = false;

    public LockedTradeData(Villager villager) {
        generateTrades(villager);
    }

    private LockedTradeData(List<MerchantOffers> offers) {
        this.lockedOffers = new ArrayList<>(offers);
    }

    public static @Nullable LockedTradeData constructOrNull(ValueInput input, Entity entity) {
        if(!(entity.level() instanceof ServerLevel))
            return null;
        Optional<List<MerchantOffers>> maybeOffers = input.read("LockedOffers", MerchantOffers.CODEC.listOf());
        if(maybeOffers.isEmpty()) return null;
        List<MerchantOffers> offers = maybeOffers.get();
        return new LockedTradeData(offers);
    }

    private void generateTrades(Villager villager) {
        if(!(villager.level() instanceof ServerLevel serverLevel)) return;
        activelyGenerating = true;
        Thread generationThread = new Thread(() -> this.generationThreadMethod(villager, serverLevel));
        generationThread.start();
    }

    private void generationThreadMethod(Villager villager, ServerLevel serverLevel) {
        try {
            ArrayList<MerchantOffers> lockedOffers = new ArrayList<>();
            int level = villager.getVillagerData().level();
            while(level++ < 5) {
                MerchantOffers offers = new MerchantOffers();
                VillagerData data = villager.getVillagerData().withLevel(level);
                VillagerProfession profession = data.profession().value();
                ResourceKey<TradeSet> trades = profession.getTrades(data.level());
                if(trades == null) continue;
                villager.addOffersFromTradeSet(serverLevel, offers, trades);
                lockedOffers.add(offers);
            }

            this.lockedOffers = lockedOffers;
        } catch (Exception e) {
            VisibleTraders.LOGGER.error("Error generating locked trades", e);
        }
        activelyGenerating = false;
    }

    public void write(ValueOutput output) {
        if(this.lockedOffers == null) return;
        output.store("LockedOffers", MerchantOffers.CODEC.listOf(), this.lockedOffers);
    }

    public boolean hasNoOffers() {
        return this.lockedOffers.isEmpty();
    }

    public MerchantOffers popTradeSet() {
        if(this.lockedOffers == null || hasNoOffers()) return null;
        return this.lockedOffers.removeFirst();
    }

    public Optional<MerchantOffers> peekTradeSet() {
        if(this.lockedOffers.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(this.lockedOffers.getFirst());
    }

    public MerchantOffers buildLockedOffers() {
        MerchantOffers lockedOffers = new MerchantOffers();
        if(this.lockedOffers == null) return lockedOffers;
        for(MerchantOffers listOffers : this.lockedOffers) lockedOffers.addAll(listOffers);
        return lockedOffers;
    }

    public void tick(Villager villager, Runnable popCallback) {
        if(this.lockedOffers == null) return;
        int requiredSets = 5 - villager.getVillagerData().level();
        while(requiredSets < this.lockedOffers.size()) popCallback.run();
        if(requiredSets > this.lockedOffers.size() && !activelyGenerating) {
            VisibleTraders.LOGGER.error("detected missing locked trade sets. Rebuilding locked offers");
            generateTrades(villager);
        }
    }
}
