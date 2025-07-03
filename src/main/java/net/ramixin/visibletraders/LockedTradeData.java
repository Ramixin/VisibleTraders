package net.ramixin.visibletraders;

import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LockedTradeData {

    private static final Logger visibleTradersLogger = LoggerFactory.getLogger("Visible Traders");

    private List<MerchantOffers> lockedOffers;

    public LockedTradeData(Villager villager) {
        this.lockedOffers = generateTrades(villager);
    }

    public LockedTradeData(ValueInput valueInput) {
        Optional<List<MerchantOffers>> offers = valueInput.read("LockedOffers", MerchantOffers.CODEC.listOf());
        this.lockedOffers = offers.map(ArrayList::new).orElse(null);
    }

    private LockedTradeData(List<MerchantOffers> offers) {
        this.lockedOffers = new ArrayList<>(offers);
    }

    public static @Nullable LockedTradeData constructOrNull(ValueInput valueInput) {
        Optional<List<MerchantOffers>> offers = valueInput.read("LockedOffers", MerchantOffers.CODEC.listOf());
        return offers.map(LockedTradeData::new).orElse(null);
    }

    private static ArrayList<MerchantOffers> generateTrades(Villager villager) {
        MerchantOffers offers = villager.getOffers();
        VillagerData data = villager.getVillagerData();
        ArrayList<MerchantOffers> lockedOffers = new ArrayList<>();
        int level = data.level();
        while(level < 5) {
            villager.setVillagerData(data.withLevel(++level));
            int prev = offers.size();
            villager.updateTrades();
            int dif = offers.size() - prev;
            MerchantOffers newOffers = new MerchantOffers();
            for(int i = 0; i < dif; i++) newOffers.add(offers.removeLast());
            lockedOffers.add(newOffers);
        }
        villager.setVillagerData(data);
        return lockedOffers;
    }

    public void write(ValueOutput valueOutput) {
        if(this.lockedOffers == null) return;
        valueOutput.store("LockedOffers", MerchantOffers.CODEC.listOf(), lockedOffers);
    }

    public boolean hasNoOffers() {
        return this.lockedOffers.isEmpty();
    }

    public MerchantOffers popTradeSet() {
        if(this.lockedOffers == null || hasNoOffers()) return null;
        return this.lockedOffers.removeFirst();
    }

    public MerchantOffers buildLockedOffers() {
        MerchantOffers lockedOffers = new MerchantOffers();
        if(this.lockedOffers == null) return lockedOffers;
        for(MerchantOffers listOffers : this.lockedOffers) for(MerchantOffer offer : listOffers) {
            if(offer.getResult().isEmpty()) {
                this.lockedOffers = new ArrayList<>();
                visibleTradersLogger.error("detected incomplete trade. Rebuilding locked offers");
                return new MerchantOffers();
            }
            lockedOffers.add(offer);
        }
        return lockedOffers;
    }

    public void tick(Villager villager, Runnable popCallback) {
        if(this.lockedOffers == null) return;
        int requiredSets = 5 - villager.getVillagerData().level();
        while(requiredSets < this.lockedOffers.size()) popCallback.run();
        if(requiredSets > this.lockedOffers.size()) {
            visibleTradersLogger.error("detected missing locked trade sets. Rebuilding locked offers");
            this.lockedOffers = generateTrades(villager);
        }
    }
}
