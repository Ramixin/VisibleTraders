package net.ramixin.visibletraders.mixins;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.monster.zombie.ZombieVillager;
import net.minecraft.world.entity.npc.villager.Villager;
import net.ramixin.visibletraders.ducks.VillagerDuck;
import net.ramixin.visibletraders.ducks.ZombieVillagerDuck;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Zombie.class)
public class ZombieMixin {

    @Inject(method = "lambda$convertVillagerToZombieVillager$0", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/zombie/ZombieVillager;setVillagerXp(I)V"))
    private void transferTradesToZombieVillager(ServerLevel level, Villager villager, ZombieVillager zombie, CallbackInfo ci) {
        VillagerDuck.of(villager).visibleTraders$getLockedTradeData().ifPresent(data -> ZombieVillagerDuck.of(zombie).visibleTraders$setLockedTradeData(data));
    }

}
