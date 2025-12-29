package net.ramixin.visibletraders.threading;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.Optional;
import java.util.function.Supplier;

public class FutureMerchantOffer extends MerchantOffer {

    private final Mutable<MerchantOffer> offer = new MutableObject<>();
    private final SerializableListing listing;
    private final Supplier<MerchantOffer> future;


    public FutureMerchantOffer(SerializableListing listing, Supplier<MerchantOffer> future) {
        super(new ItemCost(Items.AIR), Items.BARRIER.getDefaultInstance(), 0, 0, 0f);
        this.listing = listing;
        this.future = future;
    }

    public void fulfillFuture() {
        offer.setValue(future.get());
    }

    public SerializableListing getListing() {
        return listing;
    }

    public boolean isFulfilled() {
        return offer.get() != null;
    }

    public @Nullable MerchantOffer getFuture() {
        return offer.get();
    }

    @Override
    public @NotNull ItemStack getBaseCostA() {
        return offer.get() != null ? offer.get().getBaseCostA() : super.getBaseCostA();
    }

    @Override
    public @NotNull ItemStack getCostA() {
        return offer.get() != null ? offer.get().getCostA() : super.getCostA();
    }

    @Override
    public @NotNull ItemStack getCostB() {
        return offer.get() != null ? offer.get().getCostB() : super.getCostB();
    }

    @Override
    public @NotNull ItemCost getItemCostA() {
        return offer.get() != null ? offer.get().getItemCostA() : super.getItemCostA();
    }

    @Override
    public @NotNull Optional<ItemCost> getItemCostB() {
        return offer.get() != null ? offer.get().getItemCostB() : super.getItemCostB();
    }

    @Override
    public @NotNull ItemStack getResult() {
        return offer.get() != null ? offer.get().getResult() : super.getResult();
    }

    @Override
    public void updateDemand() {
        if (offer.get() != null) offer.get().updateDemand();
        else super.updateDemand();
    }

    @Override
    public @NotNull ItemStack assemble() {
        return offer.get() != null ? offer.get().assemble() : super.assemble();
    }

    @Override
    public int getUses() {
        return offer.get() != null ? offer.get().getUses() : super.getUses();
    }

    @Override
    public void resetUses() {
        if (offer.get() != null) offer.get().resetUses();
        else super.resetUses();
    }

    @Override
    public int getMaxUses() {
        return offer.get() != null ? offer.get().getMaxUses() : super.getMaxUses();
    }

    @Override
    public void increaseUses() {
        if (offer.get() != null) offer.get().increaseUses();
        else super.increaseUses();
    }

    @Override
    public int getDemand() {
        return offer.get() != null ? offer.get().getDemand() : super.getDemand();
    }

    @Override
    public void addToSpecialPriceDiff(int i) {
        if (offer.get() != null) offer.get().addToSpecialPriceDiff(i);
        else super.addToSpecialPriceDiff(i);
    }

    @Override
    public void resetSpecialPriceDiff() {
        if (offer.get() != null) offer.get().resetSpecialPriceDiff();
        else super.resetSpecialPriceDiff();
    }

    @Override
    public int getSpecialPriceDiff() {
        return offer.get() != null ? offer.get().getSpecialPriceDiff() : super.getSpecialPriceDiff();
    }

    @Override
    public void setSpecialPriceDiff(int i) {
        if (offer.get() != null) offer.get().setSpecialPriceDiff(i);
        else super.setSpecialPriceDiff(i);
    }

    @Override
    public float getPriceMultiplier() {
        return offer.get() != null ? offer.get().getPriceMultiplier() : super.getPriceMultiplier();
    }

    @Override
    public int getXp() {
        return offer.get() != null ? offer.get().getXp() : super.getXp();
    }

    @Override
    public boolean isOutOfStock() {
        return offer.get() != null ? offer.get().isOutOfStock() : super.isOutOfStock();
    }

    @Override
    public void setToOutOfStock() {
        if (offer.get() != null) offer.get().setToOutOfStock();
        else super.setToOutOfStock();
    }

    @Override
    public boolean needsRestock() {
        return offer.get() != null ? offer.get().needsRestock() : super.needsRestock();
    }

    @Override
    public boolean shouldRewardExp() {
        return offer.get() != null ? offer.get().shouldRewardExp() : super.shouldRewardExp();
    }

    @Override
    public boolean satisfiedBy(@NonNull ItemStack a, @NonNull ItemStack b) {
        return offer.get() != null ? offer.get().satisfiedBy(a, b) : super.satisfiedBy(a, b);
    }

    @Override
    public boolean take(@NonNull ItemStack a, @NonNull ItemStack b) {
        return offer.get() != null ? offer.get().take(a, b) : super.take(a, b);
    }

    @Override
    public @NotNull MerchantOffer copy() {
        return offer.get() != null ? offer.get().copy() : super.copy();
    }
}
