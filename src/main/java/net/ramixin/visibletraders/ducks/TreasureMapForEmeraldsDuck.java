package net.ramixin.visibletraders.ducks;

import net.minecraft.core.Holder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;

public interface TreasureMapForEmeraldsDuck {

    int visibleTrades$getEmeraldCost();

    TagKey<Structure> visibleTrades$getDestination();

    String visibleTrades$getDisplayName();

    Holder<MapDecorationType> visibleTrades$getDestinationType();

    int visibleTrades$getMaxUses();

    int visibleTrades$getVillagerXp();

    static TreasureMapForEmeraldsDuck get(VillagerTrades.TreasureMapForEmeralds listing) {
        return (TreasureMapForEmeraldsDuck) listing;
    }
}
