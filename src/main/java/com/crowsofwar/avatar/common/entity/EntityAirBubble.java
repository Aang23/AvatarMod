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
import com.crowsofwar.avatar.common.data.Bender;
import com.crowsofwar.avatar.common.data.BendingData;
import com.crowsofwar.avatar.common.util.AvatarUtils;
import com.crowsofwar.gorecore.util.Vector;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;
import java.util.UUID;

import static com.crowsofwar.avatar.common.config.ConfigSkills.SKILLS_CONFIG;
import static com.crowsofwar.avatar.common.config.ConfigStats.STATS_CONFIG;
import static com.crowsofwar.gorecore.util.Vector.getEntityPos;

/**
 * @author CrowsOfWar
 */
public class EntityAirBubble extends EntityShield {

	public static final DataParameter<Integer> SYNC_DISSIPATE = EntityDataManager
			.createKey(EntityAirBubble.class, DataSerializers.VARINT);
	public static final DataParameter<Boolean> SYNC_HOVERING = EntityDataManager
			.createKey(EntityAirBubble.class, DataSerializers.BOOLEAN);
	public static final DataParameter<Float> SYNC_SIZE = EntityDataManager.createKey(EntityAirBubble.class,
			DataSerializers.FLOAT);
	public static final UUID SLOW_ATTR_ID = UUID.fromString("40354c68-6e88-4415-8a6b-e3ddc56d6f50");
	public static final AttributeModifier SLOW_ATTR = new AttributeModifier(SLOW_ATTR_ID,
			"airbubble_slowness", -.3, 2);

	private int airLeft;

	public EntityAirBubble(World world) {
		super(world);
		setSize(2.5f, 2.5f);
		//setSize(0, 0);

		this.airLeft = 600;
		this.putsOutFires = true;
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		dataManager.register(SYNC_DISSIPATE, 0);
		dataManager.register(SYNC_HOVERING, false);
		dataManager.register(SYNC_SIZE, 2.5f);
	}

	@Override
	public EntityLivingBase getController() {
		return !isDissipating() ? getOwner() : null;
	}

	public boolean doesAllowHovering() {
		return dataManager.get(SYNC_HOVERING);
	}

	public void setAllowHovering(boolean floating) {
		dataManager.set(SYNC_HOVERING, floating);
	}

	public float getSize() {
		return dataManager.get(SYNC_SIZE);
	}

	public void setSize(float size) {
		dataManager.set(SYNC_SIZE, size);
	}


	@Override
	public void setVelocity(double x, double y, double z) {
		super.setVelocity(0, 0, 0);
	}

	@Override
	public void setPositionAndUpdate(double x, double y, double z) {
		if (getOwner() != null) {
			super.setPositionAndUpdate(getOwner().posX, getOwner().getEntityBoundingBox().minY, getOwner().posZ);

			this.posX = getOwner().posX;
			this.posY = getOwner().getEntityBoundingBox().minY;
			this.posZ = getOwner().posZ;

			this.motionX = this.motionY = this.motionZ = 0;

		}
	}

	@Override
	public boolean canBeCollidedWith() {
		return true;
	}

	@Override
	public void onUpdate() {
		//super.onUpdate();
		//Otherwise the entity glitches out


		EntityLivingBase owner = getOwner();

		if (owner == null && !isDissipating()) {
			this.setDead();
			removeStatCtrl();
		}

		if (owner == null) {
			return;
		}


		if (putsOutFires) {
			setFire(0);
			for (int x = 0; x <= 1; x++) {
				for (int z = 0; z <= 1; z++) {
					BlockPos pos = new BlockPos(posX + x * width, posY, posZ + z * width);
					if (world.getBlockState(pos).getBlock() == Blocks.FIRE) {
						world.setBlockToAir(pos);
						world.playSound(posX, posY, posZ, SoundEvents.BLOCK_FIRE_EXTINGUISH,
								SoundCategory.PLAYERS, 1, 1, false);
					}
				}
			}
			for (int x = 0; x >= -1; x--) {
				for (int z = 0; z >= -1; z--) {
					BlockPos pos = new BlockPos(posX + x * width, posY, posZ + z * width);
					if (world.getBlockState(pos).getBlock() == Blocks.FIRE) {
						world.setBlockToAir(pos);
						world.playSound(posX, posY, posZ, SoundEvents.BLOCK_FIRE_EXTINGUISH,
								SoundCategory.PLAYERS, 1, 1, false);
					}
				}
			}
		}

		if (owner.isDead) {
			dissipateSmall();
			removeStatCtrl();
			return;
		}
		for (int i = 0; i < 2; i++) {
			setPosition(owner.posX, owner.getEntityBoundingBox().minY, owner.posZ);

			this.motionX = this.motionY = this.motionZ = 0;

			if (!world.isRemote) {
				this.posX = owner.posX;
				this.posY = owner.getEntityBoundingBox().minY;
				this.posZ = owner.posZ;
			}

			this.motionX = this.motionY = this.motionZ = 0;
		}

		if (getOwner() != null) {
			EntityAirBubble bubble = AvatarEntity.lookupControlledEntity(world, EntityAirBubble.class, getOwner());
			BendingData bD = BendingData.get(getOwner());
			if (bubble == null && (bD.hasStatusControl(StatusControl.BUBBLE_CONTRACT) || bD.hasStatusControl(StatusControl.BUBBLE_EXPAND))) {
				bD.removeStatusControl(StatusControl.BUBBLE_CONTRACT);
				bD.removeStatusControl(StatusControl.BUBBLE_EXPAND);
			}
		}


		if (!world.isRemote && owner.isInsideOfMaterial(Material.WATER)) {
			owner.setAir(Math.min(airLeft, 300));
			airLeft--;
		}

		Bender ownerBender = Bender.get(getOwner());
		if (!world.isRemote && !ownerBender.consumeChi(STATS_CONFIG.chiAirBubbleOneSecond / 20f)) {

			dissipateSmall();

		}

		ItemStack chest = owner.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
		boolean elytraOk = (STATS_CONFIG.allowAirBubbleElytra || chest.getItem() != Items.ELYTRA);
		if (!elytraOk) {
			ownerBender.sendMessage("avatar.airBubbleElytra");
			dissipateSmall();
		}

		if (!isDissipating()) {
			IAttributeInstance attribute = owner.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
			if (attribute.getModifier(SLOW_ATTR_ID) == null) {
				attribute.applyModifier(SLOW_ATTR);
			}

			if (!owner.isInWater() && !ownerBender.isFlying() && chest.getItem() != Items.ELYTRA) {

				owner.motionY += 0.03;

				if (doesAllowHovering()) {

					if (doesAllowHovering() && !owner.isSneaking()) {
						handleHovering();
					} else {
						owner.motionY += 0.03;
					}
				}

			}

		}

		float size = getSize();

		if (isDissipatingLarge()) {
			setDissipateTime(getDissipateTime() + 1);
			float mult = 1 + getDissipateTime() / 10f;
			setSize(size * mult, size * mult);
			if (getDissipateTime() >= 10) {
				setDead();
			}
		} else if (isDissipatingSmall()) {
			setDissipateTime(getDissipateTime() - 1);
			float mult = 1 + getDissipateTime() / 40f;
			setSize(size * mult, size * mult);
			if (getDissipateTime() <= -10) {
				setDead();
			}
		} else {
			setSize(size, size);
		}

	}

	/**
	 * Handles hovering logic to make the owner hover. Preconditions (not in water, owner
	 * present, etc) are handled by the caller
	 */
	private void handleHovering() {

		if (getOwner() == null) {
			return;
		}
		if (getOwner() != null) {
			getOwner().fallDistance = 0;
		}

		// Min/max acceptable hovering distance
		// Hovering is allowed between these two values
		// Hover distance doesn't need to be EXACT
		final double minFloatHeight = 1.5;
		final double maxFloatHeight = 3;

		EntityLivingBase owner = getOwner();

		// Find whether there are blocks under the owner
		// Done by making a hitbox around the owner's feet and checking if there are blocks
		// colliding with that hitbox

		double x = owner.posX;
		double y = owner.posY;
		double z = owner.posZ;
		//Don't use setPosition; that makes it super duper ultra glitchy
		AxisAlignedBB hitbox = new AxisAlignedBB(x, y, z, x, y, z);
		hitbox = hitbox.grow(0.2, 0, 0.2);
		hitbox = hitbox.expand(0, -maxFloatHeight, 0);

		List<AxisAlignedBB> blockCollisions = world.getCollisionBoxes(null, hitbox);

		BlockPos pos = new BlockPos(getPosition().getX(), getPosition().getY() - maxFloatHeight, getPosition().getZ());
		BlockPos below = pos.offset(EnumFacing.DOWN);
		Block belowBlock = world.getBlockState(below).getBlock();

		if (!blockCollisions.isEmpty()) {

			// Calculate the top-of-ground ground y position
			// Performed by finding the maximum ypos of each collided block
			double groundPosition = Double.MIN_VALUE;
			for (AxisAlignedBB blockHitbox : blockCollisions) {
				if (blockHitbox.maxY > groundPosition) {
					groundPosition = blockHitbox.maxY;
				}
			}
			// Now calculate the distance from ground
			// and use that to determine whether owner should float
			double distanceFromGround = owner.posY - groundPosition;

			// Tweak motion based on distance to ground, and target distance
			// Minecraft gravity is 0.08 blocks/tick

			if (distanceFromGround < minFloatHeight) {
				owner.motionY += 0.11;
			}
			if (distanceFromGround >= minFloatHeight && distanceFromGround < maxFloatHeight) {
				owner.motionY *= 0.7;
			}
			if (distanceFromGround >= maxFloatHeight) {
				owner.motionY += 0.07;

				// Avoid falling at over 3 m/s
				if (owner.motionY < -3.0 / 20) {
					owner.motionY = 0;
				}
			}

		}
	}

	@Override
	public void setDead() {
		super.setDead();
		removeStatCtrl();
	}

	@Override
	public boolean isShield() {
		return true;
	}


	@Override
	public boolean canPush() {
		return false;
	}

	@Override
	protected void onCollideWithEntity(Entity entity) {
		if (canCollideWith(entity) && entity != getOwner()) {

			double mult = -2;
			if (isDissipatingLarge()) mult = -4;
			Vector vel = position().minus(getEntityPos(entity));
			vel = vel.normalize().times(mult).plusY(0.3f);

			entity.motionX = vel.x();
			entity.motionY = vel.y();
			entity.motionZ = vel.z();

			if (entity instanceof AvatarEntity) {
				AvatarEntity avent = (AvatarEntity) entity;
				avent.setVelocity(vel);
			}
			entity.isAirBorne = true;
			AvatarUtils.afterVelocityAdded(entity);
		}
	}

	@Override
	public boolean canCollideWith(Entity entity) {
		if (entity instanceof AvatarEntity && ((AvatarEntity) entity).getOwner() == getOwner()) {
			return false;
		} else return entity != getOwner() && !(entity instanceof EntityXPOrb) && !(entity instanceof EntityItem);
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbt) {
		super.readEntityFromNBT(nbt);
		setDissipateTime(nbt.getInteger("Dissipate"));
		setAllowHovering(nbt.getBoolean("AllowHovering"));
		setSize(nbt.getFloat("Size"));
		airLeft = nbt.getInteger("AirLeft");
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbt) {
		super.writeEntityToNBT(nbt);
		nbt.setInteger("Dissipate", getDissipateTime());
		nbt.setBoolean("AllowHovering", doesAllowHovering());
		nbt.setFloat("Size", getSize());
		nbt.setInteger("AirLeft", airLeft);
	}


	@Override
	protected float getChiDamageCost() {
		return STATS_CONFIG.chiAirBubbleTakeDamage;
	}

	@Override
	protected float getProtectionXp() {
		return SKILLS_CONFIG.airbubbleProtect;
	}

	@Override
	protected String getAbilityName() {
		return "air_bubble";
	}

	@Override
	protected void onDeath() {
		dissipateLarge();
	}

	@Override
	public boolean shouldRenderInPass(int pass) {
		return pass == 1;
	}

	public int getDissipateTime() {
		return dataManager.get(SYNC_DISSIPATE);
	}

	public void setDissipateTime(int dissipate) {
		dataManager.set(SYNC_DISSIPATE, dissipate);
	}

	public void dissipateLarge() {
		if (!isDissipating()) setDissipateTime(1);
		removeStatCtrl();
	}

	public void dissipateSmall() {
		if (!isDissipating()) setDissipateTime(-1);
		removeStatCtrl();
	}

	private boolean isDissipating() {
		return getDissipateTime() != 0;
	}

	public boolean isDissipatingLarge() {
		return getDissipateTime() > 0;
	}

	public boolean isDissipatingSmall() {
		return getDissipateTime() < 0;
	}

	private void removeStatCtrl() {
		if (getOwner() != null) {
			BendingData data = Bender.get(getOwner()).getData();
			data.removeStatusControl(StatusControl.BUBBLE_EXPAND);
			data.removeStatusControl(StatusControl.BUBBLE_CONTRACT);

			IAttributeInstance attribute = getOwner()
					.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
			if (attribute.getModifier(SLOW_ATTR_ID) != null) {
				attribute.removeModifier(SLOW_ATTR);
			}
		}
	}

}
