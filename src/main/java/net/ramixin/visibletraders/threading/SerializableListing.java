package net.ramixin.visibletraders.threading;

import com.mojang.serialization.MapCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.trading.MerchantOffer;

public interface SerializableListing {

    MapCodec<? extends SerializableListing> visibleTrades$getCodec();

    MerchantOffer visibleTrades$buildOffer(ServerLevel level, Entity entity, RandomSource randomSource);
}
