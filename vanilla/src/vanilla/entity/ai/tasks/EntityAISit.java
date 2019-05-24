package vanilla.entity.ai.tasks;

import net.minecraft.entity.EntityLivingBase;
import vanilla.entity.passive.EntityTameable;

public class EntityAISit extends EntityAIBase
{
    private EntityTameable theEntity;

    /** If the EntityTameable is sitting. */
    private boolean isSitting;

    public EntityAISit(EntityTameable entityIn)
    {
        this.theEntity = entityIn;
        this.setMutexBits(5);
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute()
    {
        if (!this.theEntity.isTamed())
        {
            return false;
        }
		if (this.theEntity.isInWater())
		{
			return false;
		}
		if (!this.theEntity.onGround)
		{
			return false;
		}
		EntityLivingBase entitylivingbase = this.theEntity.getOwner();
		return entitylivingbase == null || (!(this.theEntity.getDistanceSqToEntity(entitylivingbase) < 144.0D) || entitylivingbase.getAITarget() == null) && this.isSitting;
	}

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void startExecuting()
    {
        this.theEntity.getNavigator().clearPathEntity();
        this.theEntity.setSitting(true);
    }

    /**
     * Resets the task
     */
    public void resetTask()
    {
        this.theEntity.setSitting(false);
    }

    /**
     * Sets the sitting flag.
     */
    public void setSitting(boolean sitting)
    {
        this.isSitting = sitting;
    }
}