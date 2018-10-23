/* 
  This file is part of AvatarMod.
    
  AvatarMod is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.
  
  AvatarMod is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
  
  You should have received a copy of the GNU General Public License
  along with AvatarMod. If not, see <http://www.gnu.org/licenses/>.
*/
package com.crowsofwar.avatar.common.bending.fire;

import net.minecraft.entity.*;

import com.crowsofwar.avatar.common.bending.*;
import com.crowsofwar.avatar.common.data.*;
import com.crowsofwar.avatar.common.entity.*;
import com.crowsofwar.avatar.common.entity.data.FireballBehavior;
import com.crowsofwar.gorecore.util.Vector;

import static com.crowsofwar.gorecore.util.Vector.*;
import static java.lang.Math.toDegrees;

/**
 * @author CrowsOfWar
 */
public class AiFireball extends BendingAi {

	private int timeExecuting;

	/**
	 * @param ability
	 * @param entity
	 * @param bender
	 */
	protected AiFireball(Ability ability, EntityLiving entity, Bender bender) {
		super(ability, entity, bender);
		timeExecuting = 0;
		setMutexBits(2);
	}

	@Override
	protected void startExec() {
		BendingData data = bender.getData();
		execAbility();
		data.getMiscData().setAbilityCooldown(120);
	}

	@Override
	public boolean shouldContinueExecuting() {

		if (entity.getAttackTarget() == null) return false;

		Vector rotations = getRotationTo(getEntityPos(entity), getEntityPos(entity.getAttackTarget()));
		entity.rotationYaw = (float) toDegrees(rotations.y());
		entity.rotationPitch = (float) toDegrees(rotations.x());

		if (timeExecuting >= 15) {
			execStatusControl(StatusControl.THROW_FIREBALL);
			timeExecuting = 0;
			return false;
		} else {
			return true;
		}

	}

	@Override
	protected boolean shouldExec() {
		EntityLivingBase target = entity.getAttackTarget();
		return target != null && entity.getDistance(target) > 4 && bender.getData().getMiscData().getAbilityCooldown() == 0 && entity.getRNG()
						.nextBoolean();
	}

	@Override
	public void updateTask() {
		timeExecuting++;
	}

	@Override
	public void resetTask() {

		EntityFireball fireball = AvatarEntity.lookupEntity(entity.world, EntityFireball.class, //
															fire -> fire.getBehavior() instanceof FireballBehavior.PlayerControlled
																			&& fire.getOwner() == entity);

		if (fireball != null) {
			fireball.setDead();
			bender.getData().removeStatusControl(StatusControl.THROW_FIREBALL);
		}

	}

}
