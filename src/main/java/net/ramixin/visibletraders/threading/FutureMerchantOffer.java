package net.ramixin.visibletraders.threading;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    public boolean fulfilled() {
        return offer.getValue() != null;
    }

    public @Nullable MerchantOffer getFuture() {
        return offer.getValue();
    }

    @Override
    public @NotNull ItemStack getBaseCostA() {
        return offer.getValue() != null ? offer.getValue().getBaseCostA() : super.getBaseCostA();
    }

    @Override
    public @NotNull ItemStack getCostA() {
        return offer.getValue() != null ? offer.getValue().getCostA() : super.getCostA();
    }

    @Override
    public @NotNull ItemStack getCostB() {
        return offer.getValue() != null ? offer.getValue().getCostB() : super.getCostB();
    }

    @Override
    public @NotNull ItemCost getItemCostA() {
        return offer.getValue() != null ? offer.getValue().getItemCostA() : super.getItemCostA();
    }

    @Override
    public @NotNull Optional<ItemCost> getItemCostB() {
        return offer.getValue() != null ? offer.getValue().getItemCostB() : super.getItemCostB();
    }

    @Override
    public @NotNull ItemStack getResult() {
        return offer.getValue() != null ? offer.getValue().getResult() : super.getResult();
    }

    @Override
    public void updateDemand() {
        if (offer.getValue() != null) offer.getValue().updateDemand();
        else super.updateDemand();
    }

    @Override
    public @NotNull ItemStack assemble() {
        return offer.getValue() != null ? offer.getValue().assemble() : super.assemble();
    }

    @Override
    public int getUses() {
        return offer.getValue() != null ? offer.getValue().getUses() : super.getUses();
    }

    @Override
    public void resetUses() {
        if (offer.getValue() != null) offer.getValue().resetUses();
        else super.resetUses();
    }

    @Override
    public int getMaxUses() {
        return offer.getValue() != null ? offer.getValue().getMaxUses() : super.getMaxUses();
    }

    @Override
    public void increaseUses() {
        if (offer.getValue() != null) offer.getValue().increaseUses();
        else super.increaseUses();
    }

    @Override
    public int getDemand() {
        return offer.getValue() != null ? offer.getValue().getDemand() : super.getDemand();
    }

    @Override
    public void addToSpecialPriceDiff(int i) {
        if (offer.getValue() != null) offer.getValue().addToSpecialPriceDiff(i);
        else super.addToSpecialPriceDiff(i);
    }

    @Override
    public void resetSpecialPriceDiff() {
        if (offer.getValue() != null) offer.getValue().resetSpecialPriceDiff();
        else super.resetSpecialPriceDiff();
    }

    @Override
    public int getSpecialPriceDiff() {
        return offer.getValue() != null ? offer.getValue().getSpecialPriceDiff() : super.getSpecialPriceDiff();
    }

    @Override
    public void setSpecialPriceDiff(int i) {
        if (offer.getValue() != null) offer.getValue().setSpecialPriceDiff(i);
        else super.setSpecialPriceDiff(i);
    }

    @Override
    public float getPriceMultiplier() {
        return offer.getValue() != null ? offer.getValue().getPriceMultiplier() : super.getPriceMultiplier();
    }

    @Override
    public int getXp() {
        return offer.getValue() != null ? offer.getValue().getXp() : super.getXp();
    }

    @Override
    public boolean isOutOfStock() {
        return offer.getValue() != null ? offer.getValue().isOutOfStock() : super.isOutOfStock();
    }

    @Override
    public void setToOutOfStock() {
        if (offer.getValue() != null) offer.getValue().setToOutOfStock();
        else super.setToOutOfStock();
    }

    @Override
    public boolean needsRestock() {
        return offer.getValue() != null ? offer.getValue().needsRestock() : super.needsRestock();
    }

    @Override
    public boolean shouldRewardExp() {
        return offer.getValue() != null ? offer.getValue().shouldRewardExp() : super.shouldRewardExp();
    }

    @Override
    public boolean satisfiedBy(ItemStack a, ItemStack b) {
        return offer.getValue() != null ? offer.getValue().satisfiedBy(a, b) : super.satisfiedBy(a, b);
    }

    @Override
    public boolean take(ItemStack a, ItemStack b) {
        return offer.getValue() != null ? offer.getValue().take(a, b) : super.take(a, b);
    }

    @Override
    public @NotNull MerchantOffer copy() {
        return offer.getValue() != null ? offer.getValue().copy() : super.copy();
    }
}
