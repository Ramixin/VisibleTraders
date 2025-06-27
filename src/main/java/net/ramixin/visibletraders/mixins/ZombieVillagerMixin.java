package net.ramixin.visibletraders.mixins;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.ramixin.visibletraders.LockedTradeData;
import net.ramixin.visibletraders.ducks.VillagerDuck;
import net.ramixin.visibletraders.ducks.ZombieVillagerDuck;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(ZombieVillager.class)
public abstract class ZombieVillagerMixin extends Zombie implements ZombieVillagerDuck {

    @Unique
    private final Mutable<LockedTradeData> lockedTradeData = new MutableObject<>();

    public ZombieVillagerMixin(EntityType<? extends Zombie> entityType, Level level) {
        super(entityType, level);
    }

    @Unique
    private void ifPresent(Consumer<LockedTradeData> consumer) {
        LockedTradeData val = lockedTradeData.getValue();
        if(val == null) return;
        consumer.accept(val);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("HEAD"))
    private void saveLockedTradeData(ValueOutput valueOutput, CallbackInfo ci) {
        ifPresent(data -> data.write(valueOutput));
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void readLockedTradeData(ValueInput valueInput, CallbackInfo ci) {
        lockedTradeData.setValue(LockedTradeData.constructOrNull(valueInput));
    }

    @Inject(method = "method_63659", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/npc/Villager;setVillagerXp(I)V"))
    private void transferTradesToVillager(ServerLevel serverLevel, Villager villager, CallbackInfo ci) {
        VillagerDuck.of(villager).visibleTraders$setLockedTradeData(lockedTradeData.getValue());
    }

    @Override
    public void visibleTraders$setLockedTradeData(LockedTradeData data) {
        this.lockedTradeData.setValue(data);
    }
}
