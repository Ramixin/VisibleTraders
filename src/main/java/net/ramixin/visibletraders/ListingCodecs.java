package net.ramixin.visibletraders;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.npc.villager.VillagerTrades;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import net.ramixin.visibletraders.ducks.TreasureMapForEmeraldsDuck;
import net.ramixin.visibletraders.threading.SerializableListing;

public interface ListingCodecs {

    MapCodec<VillagerTrades.TreasureMapForEmeralds> TREASURE_MAP_FOR_EMERALDS_CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(
            Codec.INT.fieldOf("emeraldCost").forGetter(listing -> TreasureMapForEmeraldsDuck.get(listing).visibleTrades$getEmeraldCost()),
            TagKey.codec(Registries.STRUCTURE).fieldOf("destination").forGetter(listing -> TreasureMapForEmeraldsDuck.get(listing).visibleTrades$getDestination()),
            Codec.STRING.fieldOf("displayName").forGetter(listing -> TreasureMapForEmeraldsDuck.get(listing).visibleTrades$getDisplayName()),
            MapDecorationType.CODEC.fieldOf("destinationType").forGetter(listing -> TreasureMapForEmeraldsDuck.get(listing).visibleTrades$getDestinationType()),
            Codec.INT.fieldOf("maxUses").forGetter(listing -> TreasureMapForEmeraldsDuck.get(listing).visibleTrades$getMaxUses()),
            Codec.INT.fieldOf("villagerXp").forGetter(listing -> TreasureMapForEmeraldsDuck.get(listing).visibleTrades$getVillagerXp())
    ).apply(instance, VillagerTrades.TreasureMapForEmeralds::new));

    MapCodec<SerializableListing> NORMALIZED_TREASURE_MAP_FOR_EMERALDS_CODEC = normalize(TREASURE_MAP_FOR_EMERALDS_CODEC);


    static <T> MapCodec<SerializableListing> normalize(MapCodec<T> codec) {
        //noinspection unchecked
        return codec.xmap(t -> {
            if (t instanceof SerializableListing listing) return listing;
            else return null;
        }, serializableListing -> (T) serializableListing);
    }

}
