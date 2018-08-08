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
package com.crowsofwar.avatar.common.entity;

import com.crowsofwar.avatar.common.bending.StatusControl;
import com.crowsofwar.avatar.common.bending.fire.AbilityFireball;
import com.crowsofwar.avatar.common.data.AbilityData;
import com.crowsofwar.avatar.common.data.AbilityData.AbilityTreePath;
import com.crowsofwar.avatar.common.data.Bender;
import com.crowsofwar.avatar.common.data.BendingData;
import com.crowsofwar.avatar.common.entity.data.Behavior;
import com.crowsofwar.avatar.common.entity.data.FireballBehavior;
import com.crowsofwar.avatar.common.world.AvatarFireExplosion;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;

import static com.crowsofwar.avatar.common.config.ConfigStats.STATS_CONFIG;

/**
 * @author CrowsOfWar
 */
public class EntityFireball extends AvatarEntity {

	private static final DataParameter<FireballBehavior> SYNC_BEHAVIOR = EntityDataManager
			.createKey(EntityFireball.class, FireballBehavior.DATA_SERIALIZER);

	public static final DataParameter<Integer> SYNC_SIZE = EntityDataManager.createKey(EntityFireball.class,
			DataSerializers.VARINT);

	private AxisAlignedBB expandedHitbox;

	private float damage;
	private float explosionStrength;

	public void setExplosionStrength(float strength) {
		this.explosionStrength = strength;
	}


	/**
	 * @param world
	 */
	public EntityFireball(World world) {
		super(world);
		setSize(.8f, .8f);
		this.explosionStrength = 0.75f;
	}

	@Override
	public void entityInit() {
		super.entityInit();
		dataManager.register(SYNC_BEHAVIOR, new FireballBehavior.Idle());
		dataManager.register(SYNC_SIZE, 30);
	}

	@Override
	public void setDead() {
		super.setDead();
		removeStatCtrl();
	}

	@Override
	public void onUpdate() {

		super.onUpdate();
		setBehavior((FireballBehavior) getBehavior().onUpdate(this));


		if (ticksExisted % 30 == 0) {
			world.playSound(null, posX, posY, posZ, SoundEvents.BLOCK_FIRE_AMBIENT, SoundCategory.BLOCKS, 6, 0.8F);
		}

		// Add hook or something
		if (getOwner() == null) {
			setDead();
			removeStatCtrl();
		}
		
		if (getOwner() != null) {
			EntityFireball ball = AvatarEntity.lookupControlledEntity(world, EntityFireball.class, getOwner());
			BendingData bD = BendingData.get(getOwner());
			if (ball == null && bD.hasStatusControl(StatusControl.THROW_FIREBALL)) {
				bD.removeStatusControl(StatusControl.THROW_FIREBALL);
			}
			if (ball != null && ball.getBehavior() instanceof FireballBehavior.PlayerControlled && !(bD.hasStatusControl(StatusControl.THROW_FIREBALL))) {
				bD.addStatusControl(StatusControl.THROW_FIREBALL);
			}

		}
	}


	@Override
	public boolean onMajorWaterContact() {
		spawnExtinguishIndicators();
		if (getBehavior() instanceof FireballBehavior.PlayerControlled) {
			removeStatCtrl();
		}
		setDead();
		return true;
	}

	@Override
	public boolean onMinorWaterContact() {
		spawnExtinguishIndicators();
		return false;
	}

	public FireballBehavior getBehavior() {
		return dataManager.get(SYNC_BEHAVIOR);
	}

	public void setBehavior(FireballBehavior behavior) {
		dataManager.set(SYNC_BEHAVIOR, behavior);
	}

	@Override
	public EntityLivingBase getController() {
		return getBehavior() instanceof FireballBehavior.PlayerControlled ? getOwner() : null;
	}

	public float getDamage() {
		return damage;
	}

	public void setDamage(float damage) {
		this.damage = damage;
	}

	public int getSize() {
		return dataManager.get(SYNC_SIZE);
	}

	public void setSize(int size) {
		dataManager.set(SYNC_SIZE, size);
	}

	@Override
	protected void onCollideWithEntity(Entity entity) {
		if (entity instanceof AvatarEntity) {
			((AvatarEntity) entity).onFireContact();
		}
		if (canCollideWith(entity) && entity != getOwner() && getBehavior() instanceof FireballBehavior.Thrown) {
			float explosionSize = STATS_CONFIG.fireballSettings.explosionSize;

			explosionSize *= getSize() / 15f;
			explosionSize += getPowerRating() * 2.0 / 100;

			AvatarFireExplosion fireExplosion = new AvatarFireExplosion(world, this, posX, posY, posZ, explosionSize * this.explosionStrength,
					!world.isRemote, STATS_CONFIG.fireballSettings.damageBlocks);

			if (!ForgeEventFactory.onExplosionStart(world, fireExplosion)) {
				fireExplosion.doExplosionA();
				fireExplosion.doExplosionB(true);

			}
		}
	}

	@Override
	public boolean onCollideWithSolid() {


		if (getBehavior() instanceof FireballBehavior.Thrown) {
			float explosionSize = STATS_CONFIG.fireballSettings.explosionSize;

			explosionSize *= getSize() / 15f;
			explosionSize += getPowerRating() * 2.0 / 100;
			boolean destroyObsidian = false;

			if (getOwner() != null && !world.isRemote) {
				if (getAbility() instanceof AbilityFireball) {
					AbilityData abilityData = BendingData.get(getOwner())
							.getAbilityData("fireball");
					if (abilityData.isMasterPath(AbilityTreePath.FIRST)) {
						destroyObsidian = true;
					}
				}

			}

			AvatarFireExplosion fireExplosion = new AvatarFireExplosion(world, this, posX, posY, posZ, explosionSize * this.explosionStrength,
					!world.isRemote, STATS_CONFIG.fireballSettings.damageBlocks);

			if (!ForgeEventFactory.onExplosionStart(world, fireExplosion)) {
				fireExplosion.doExplosionA();
				fireExplosion.doExplosionB(true);

			}

			if (destroyObsidian) {
				for (EnumFacing dir : EnumFacing.values()) {
					BlockPos pos = getPosition().offset(dir);
					if (world.getBlockState(pos).getBlock() == Blocks.OBSIDIAN) {
						world.destroyBlock(pos, true);
					}
				}
			}


			setDead();
			removeStatCtrl();

		}
		return true;

	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbt) {
		super.readEntityFromNBT(nbt);
		setDamage(nbt.getFloat("Damage"));
		setBehavior((FireballBehavior) Behavior.lookup(nbt.getInteger("Behavior"), this));
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nbt) {
		super.writeEntityToNBT(nbt);
		nbt.setFloat("Damage", getDamage());
		nbt.setInteger("Behavior", getBehavior().getId());
	}

	@Override
	public int getBrightnessForRender() {
		return 150;
	}


	public AxisAlignedBB getExpandedHitbox() {
		return this.expandedHitbox;
	}

	@Override
	public void setEntityBoundingBox(AxisAlignedBB bb) {
		super.setEntityBoundingBox(bb);
		expandedHitbox = bb.grow(0.35, 0.35, 0.35);
	}

	@Override
	public boolean shouldRenderInPass(int pass) {
		return true;
	}
	//Mostly fixes a glitch where the entity turns invisible

	private void removeStatCtrl() {
		if (getOwner() != null) {
			BendingData data = Bender.get(getOwner()).getData();
			if (data != null) {
				data.removeStatusControl(StatusControl.THROW_FIREBALL);
			}
		}
	}

	@Override
	public boolean isProjectile() {
		return true;
	}

}
