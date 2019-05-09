package net.minecraft.entity.ai.tasks;

import net.minecraft.entity.Entity;
import net.minecraft.entity.VanillaEntity;

public class EntityAIWatchClosest2 extends EntityAIWatchClosest
{
    public EntityAIWatchClosest2(VanillaEntity entitylivingIn, Class <? extends Entity > watchTargetClass, float maxDistance, float chanceIn)
    {
        super(entitylivingIn, watchTargetClass, maxDistance, chanceIn);
        this.setMutexBits(3);
    }
}
