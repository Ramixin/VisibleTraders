package net.ramixin.visibletraders.ducks;

import net.minecraft.world.entity.monster.zombie.ZombieVillager;
import net.ramixin.visibletraders.LockedTradeData;

public interface ZombieVillagerDuck {

    static ZombieVillagerDuck of(ZombieVillager zombieVillager) {
        return (ZombieVillagerDuck)zombieVillager;
    }

    void visibleTraders$setLockedTradeData(LockedTradeData data);

}
