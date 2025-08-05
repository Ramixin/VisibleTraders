package net.ramixin.visibletraders;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.ramixin.visibletraders.threading.SerializableListing;
import net.ramixin.visibletraders.threading.TradeWorker;
import org.slf4j.Logger;

import java.util.function.Function;

public class VisibleTraders implements ModInitializer {

    public static final String MOD_NAME = "Visible Traders";
    public static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(MOD_NAME);

    public static final TradeWorker TRADE_WORKER = new TradeWorker();

    private static final ResourceKey<Registry<MapCodec<? extends SerializableListing>>> LISTINGS_REGISTRY_KEY = ResourceKey.createRegistryKey(ResourceLocation.parse("visibletraders:listings"));
    public static final Registry<MapCodec<? extends SerializableListing>> LISTINGS_REGISTRY = FabricRegistryBuilder.createSimple(LISTINGS_REGISTRY_KEY).buildAndRegister();

    public static final Codec<SerializableListing> CODEC = LISTINGS_REGISTRY.byNameCodec().dispatch(SerializableListing::visibleTrades$getCodec, Function.identity());

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing (1/1)");

        ServerLifecycleEvents.SERVER_STARTED.register(server -> startTradeWorker());
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> TRADE_WORKER.stop());

        Registry.register(LISTINGS_REGISTRY, ResourceLocation.fromNamespaceAndPath("minecraft", "treasure_map_for_emeralds"), ListingCodecs.NORMALIZED_TREASURE_MAP_FOR_EMERALDS_CODEC);
    }

    private static void startTradeWorker() {
        TRADE_WORKER.reset();
        Thread thread = new Thread(TRADE_WORKER);
        thread.setName("Visible Traders Trade Thread");
        thread.start();
    }
}
