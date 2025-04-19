package net.ramgames.visibletraders;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class LockedTradeData {

    public static final LockedTradeData DEFAULT = new LockedTradeData(new CompoundTag(), null);

    private static final Logger visibleTradersLogger = LoggerFactory.getLogger("Visible Traders");

    private List<MerchantOffers> lockedOffers;

    private MerchantOffer cachedOffer = null;

    private int prevLevel = 0;

    public LockedTradeData(CompoundTag compoundTag, DynamicOps<Tag> serializationContext) {
        if(compoundTag.contains("LockedOffers"))
            Codec.list(MerchantOffers.CODEC)
                    .parse(serializationContext, compoundTag.get("LockedOffers"))
                    .ifSuccess(lockedOffers -> this.lockedOffers = new ArrayList<>(lockedOffers))
                    .ifError(msg -> {
                        this.lockedOffers = new ArrayList<>();
                        visibleTradersLogger.error("DECODE_FAILURE: {}", msg);
                    });
        else this.lockedOffers = new ArrayList<>();
    }

    public void write(CompoundTag compoundTag, DynamicOps<Tag> serializationContext) {
        if(this.lockedOffers == null) return;
        DataResult<Tag> val = Codec.list(MerchantOffers.CODEC).encodeStart(serializationContext, this.lockedOffers);
        if(val.isError()) //noinspection OptionalGetWithoutIsPresent
            visibleTradersLogger.error("ENCODE_FAILURE: {}", val.error().get());
        else compoundTag.put("LockedOffers", val.getOrThrow());
    }

    public void setCachedTrade(MerchantOffer offer) {
        this.cachedOffer = offer;
    }

    public void setPrevLevel(int level) {
        this.prevLevel = level;
    }

    public boolean hasNoOffers() {
        return this.lockedOffers == null || this.lockedOffers.isEmpty();
    }

    public MerchantOffers shouldDismissTrades(int villagerLevel) {
        if(prevLevel != villagerLevel) return null;
        if(hasNoOffers()) return null;
        if(this.lockedOffers.size() + villagerLevel <= 5) return null;
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

    public void tickLockedTrades(MerchantOffers offers, Villager villager) {
        prevLevel = villager.getVillagerData().level();

        if(offers == null) {
            lockedOffers = null;
            return;
        }

        if(cachedOffer == null || offers.isEmpty()) {
            lockedOffers = null;
            if(!offers.isEmpty()) cachedOffer = offers.getFirst();
            return;
        }

        if(cachedOffer != offers.getFirst()) {
            this.lockedOffers = null;
            cachedOffer = offers.getFirst();
            return;
        }

        if(this.lockedOffers == null) this.lockedOffers = new ArrayList<>();
        int size = this.lockedOffers.size();

        if(size + prevLevel == 5) return;
        if(size > 0 && size + prevLevel > 5) {
            this.lockedOffers.removeFirst();
            return;
        }
        VillagerData data = villager.getVillagerData();
        villager.setVillagerData(data.withLevel(data.level()+size + 1));
        int prev = offers.size();
        villager.updateTrades();
        int dif = offers.size() - prev;
        MerchantOffers newOffers = new MerchantOffers();
        for(int i = 0; i < dif; i++) newOffers.add(offers.removeLast());
        this.lockedOffers.add(newOffers);
        villager.setVillagerData(data);
    }
}
