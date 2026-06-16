package net.ramixin.visibletraders;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerData;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.item.trading.TradeSet;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.ramixin.visibletraders.networking.ClientboundLockedTradesPayload;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

public class LockedTradeData {

    private @NotNull List<MerchantOffers> lockedOffers = new ArrayList<>();
    private final Queue<Consumer<MerchantOffers>> requestCallbacks = new LinkedList<>();
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
            VisibleTradersCommon.LOGGER.error("Error generating locked trades", e);
        }
        activelyGenerating = false;
    }

    public void write(ValueOutput output) {
        output.store("LockedOffers", MerchantOffers.CODEC.listOf(), this.lockedOffers);
    }

    public boolean hasNoOffers() {
        return this.lockedOffers.isEmpty();
    }

    public Optional<MerchantOffers> popTradeSet() {
        if(hasNoOffers()) return Optional.empty();
        return Optional.of(this.lockedOffers.removeFirst());
    }

    public MerchantOffers condense() {
        MerchantOffers lockedOffers = new MerchantOffers();
        for(MerchantOffers listOffers : this.lockedOffers) lockedOffers.addAll(listOffers);
        return lockedOffers;
    }

    public void tick(Villager villager, Runnable popCallback) {
        int requiredSets = 5 - villager.getVillagerData().level();
        while(requiredSets < this.lockedOffers.size()) popCallback.run();
        if(requiredSets > this.lockedOffers.size() && !activelyGenerating) {
            VisibleTradersCommon.LOGGER.error("detected missing locked trade sets. Rebuilding locked offers");
            generateTrades(villager);
        }
        while(!activelyGenerating && !requestCallbacks.isEmpty()) {
            Objects.requireNonNull(requestCallbacks.poll()).accept(condense());
        }
    }

    public void requestOffers(ServerPlayer player) {
        requestCallbacks.add((offers) -> VisibleTradersCommon.getMedium().sendPayload(player, new ClientboundLockedTradesPayload(offers)));
    }

    public boolean isGenerating() {
        return activelyGenerating;
    }
}
