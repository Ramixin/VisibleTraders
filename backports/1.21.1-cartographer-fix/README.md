# Visible Traders 1.21.1 cartographer AIR trade fix

This directory contains a small backport patch for the published Fabric `1.3.2` build on Minecraft `1.21.1`.

## Bug

Cartographer explorer-map trades are generated asynchronously through `FutureMerchantOffer`. While pending, the placeholder offer uses `Items.AIR` as its input cost. Under some timing / null-result cases the placeholder can be promoted from locked trades into the villager's real vanilla `offers` list. The next entity save then fails because Minecraft 1.21.1 refuses to encode an AIR item cost:

```
java.lang.IllegalStateException: Item must not be minecraft:air
```

The issue reproduces by trading/levelling a cartographer and then saving/pausing the world.

## Fix

The patch keeps the existing asynchronous generation model but enforces two invariants:

1. A future has a separate completion state, so a legitimate completed `null` explorer-map result is distinguishable from a still-pending lookup.
2. `FutureMerchantOffer` placeholders are never promoted into vanilla villager offers. Completed futures are unwrapped to ordinary `MerchantOffer`s; completed-null results are omitted; pending futures keep the trade set locked.

The completion flag is `volatile` so the server thread observes the resolved offer after the worker thread publishes completion.

## Validation

Built against:

- Minecraft `1.21.1`
- Fabric Loader `0.18.4`
- Fabric API `0.116.7+1.21.1`
- published Visible Traders `1.3.2` (`nEDGMR8b`)

The resulting patched jar was tested in-game with the cartographer reproduction case and no longer crashes on save.
