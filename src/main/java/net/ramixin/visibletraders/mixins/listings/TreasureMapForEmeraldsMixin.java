package net.ramixin.visibletraders.mixins.listings;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.villager.VillagerTrades;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import net.ramixin.visibletraders.ListingCodecs;
import net.ramixin.visibletraders.ducks.TreasureMapForEmeraldsDuck;
import net.ramixin.visibletraders.threading.SerializableListing;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(VillagerTrades.TreasureMapForEmeralds.class)
public abstract class TreasureMapForEmeraldsMixin implements TreasureMapForEmeraldsDuck, SerializableListing {

    @Shadow @Final private int emeraldCost;
    @Shadow @Final private TagKey<Structure> destination;
    @Shadow @Final private String displayName;
    @Shadow @Final private Holder<MapDecorationType> destinationType;
    @Shadow @Final private int maxUses;
    @Shadow @Final private int villagerXp;

    @Shadow
    public abstract @Nullable MerchantOffer getOffer(ServerLevel serverLevel, Entity entity, RandomSource randomSource);

    @Override
    public int visibleTrades$getEmeraldCost() {
        return emeraldCost;
    }

    @Override
    public TagKey<Structure> visibleTrades$getDestination() {
        return destination;
    }

    @Override
    public String visibleTrades$getDisplayName() {
        return displayName;
    }

    @Override
    public Holder<MapDecorationType> visibleTrades$getDestinationType() {
        return destinationType;
    }

    @Override
    public int visibleTrades$getMaxUses() {
        return maxUses;
    }

    @Override
    public int visibleTrades$getVillagerXp() {
        return villagerXp;
    }

    @Override
    public MapCodec<? extends SerializableListing> visibleTrades$getCodec() {
        return ListingCodecs.NORMALIZED_TREASURE_MAP_FOR_EMERALDS_CODEC;
    }

    @Override
    public MerchantOffer visibleTrades$buildOffer(ServerLevel serverLevel, Entity entity, RandomSource randomSource) {
        return getOffer(serverLevel,entity, randomSource);
    }
}
