package net.ramixin.visibletraders;

import com.mojang.serialization.Codec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerData;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.ramixin.visibletraders.ducks.VillagerDuck;
import net.ramixin.visibletraders.threading.FutureMerchantOffer;
import net.ramixin.visibletraders.threading.SerializableListing;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class LockedTradeData {

    private List<MerchantOffers> lockedOffers;

    public LockedTradeData(Villager villager) {
        this.lockedOffers = generateTrades(villager);
    }

    private LockedTradeData(List<MerchantOffers> offers) {
        this.lockedOffers = new ArrayList<>(offers);
    }

    public static @Nullable LockedTradeData constructOrNull(ValueInput valueInput, Entity entity) {
        if(!(entity.level() instanceof ServerLevel level))
            return null;
        Optional<List<MerchantOffers>> maybeOffers = valueInput.read("LockedOffers", MerchantOffers.CODEC.listOf());
        if(maybeOffers.isEmpty()) return null;
        List<MerchantOffers> offers = maybeOffers.get();
        Optional<List<Long>> maybeIndices = valueInput.read("futureOfferIndices", Codec.LONG.listOf());
        if(maybeIndices.isPresent()) {
            List<int[]> indices = maybeIndices.get().stream().map(lung -> new int[]{(int) (lung >> 32), (int) (lung & 0xFF_FF_FF_FFL)}).toList();
            Optional<List<SerializableListing>> maybeListings = valueInput.read("futureOffers", VisibleTraders.CODEC.listOf());
            if(maybeListings.isEmpty()) return null;
            List<SerializableListing> listings = maybeListings.get();
            if(listings.size() != indices.size()) return null;
            for(int i = 0; i < listings.size(); i++) {
                SerializableListing listing = listings.get(i);
                int[] index = indices.get(i);
                if(index[0] >= offers.size()) return null;
                MerchantOffers offerSet = offers.get(index[0]);
                FutureMerchantOffer futureOffer = new FutureMerchantOffer(listing, () -> listing.visibleTrades$buildOffer(level, entity, entity.getRandom()));
                VisibleTraders.TRADE_WORKER.addOrder(futureOffer);
                offerSet.add(index[1], futureOffer);
            }
        }
        return new LockedTradeData(offers);
    }

    private static ArrayList<MerchantOffers> generateTrades(Villager villager) {
        MerchantOffers offers = villager.getOffers();
        VillagerData data = villager.getVillagerData();
        ArrayList<MerchantOffers> lockedOffers = new ArrayList<>();
        int level = data.level();
        while(level < 5) {
            villager.setVillagerData(data.withLevel(++level));
            int prev = offers.size();
            VillagerDuck.of(villager).visibleTraders$updateTrades();
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
        List<MerchantOffers> offersToWrite = new ArrayList<>();
        List<Long> futureOfferIndexes = new ArrayList<>();
        List<SerializableListing> futureOffers = new ArrayList<>();
        for (int i = 0; i < lockedOffers.size(); i++) {
            MerchantOffers offers = lockedOffers.get(i);
            MerchantOffers newOffers = new MerchantOffers();
            for (int j = 0; j < offers.size(); j++) {
                MerchantOffer offer = offers.get(j);
                if (!(offer instanceof FutureMerchantOffer futureOffer)) newOffers.add(offer);
                else {
                    if (futureOffer.fulfilled())
                        newOffers.add(Objects.requireNonNull(futureOffer.getFuture(), "Future offers was null although fulfilled"));
                    else {
                        futureOfferIndexes.add(((long)i) << 32 | j);
                        futureOffers.add(futureOffer.getListing());
                    }
                }
            }
            offersToWrite.add(newOffers);
        }
        valueOutput.store("LockedOffers", MerchantOffers.CODEC.listOf(), offersToWrite);
        valueOutput.store("futureOfferIndices", Codec.LONG.listOf(), futureOfferIndexes);
        valueOutput.store("futureOffers", VisibleTraders.CODEC.listOf(), futureOffers);
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
            if(offer.getResult().isEmpty() && !(offer instanceof FutureMerchantOffer)) {
                this.lockedOffers = new ArrayList<>();
                VisibleTraders.LOGGER.error("detected incomplete trade. Rebuilding locked offers");
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
            VisibleTraders.LOGGER.error("detected missing locked trade sets. Rebuilding locked offers");
            this.lockedOffers = generateTrades(villager);
        }
    }
}
