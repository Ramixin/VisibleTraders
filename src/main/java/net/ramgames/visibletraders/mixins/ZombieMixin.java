package net.ramgames.visibletraders.mixins;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.npc.Villager;
import net.ramgames.visibletraders.ducks.VillagerDuck;
import net.ramgames.visibletraders.ducks.ZombieVillagerDuck;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Zombie.class)
public class ZombieMixin {

    @Inject(method = "method_63655", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/ZombieVillager;setVillagerXp(I)V"))
    private void transferTradesToZombieVillager(ServerLevel serverLevel, Villager villager, ZombieVillager zombieVillager, CallbackInfo ci) {
        ZombieVillagerDuck.of(zombieVillager).visibleTraders$setLockedTradeData(VillagerDuck.of(villager).visibleTraders$getLockedTradeData());
    }

}
