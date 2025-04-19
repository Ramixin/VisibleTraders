package net.ramgames.visibletraders.ducks;

import net.minecraft.world.entity.monster.ZombieVillager;
import net.ramgames.visibletraders.LockedTradeData;

public interface ZombieVillagerDuck {

    static ZombieVillagerDuck of(ZombieVillager zombieVillager) {
        return (ZombieVillagerDuck)zombieVillager;
    }

    void visibleTraders$setLockedTradeData(LockedTradeData data);

}
