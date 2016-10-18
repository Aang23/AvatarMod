package com.crowsofwar.avatar.common.entity;

import static com.crowsofwar.avatar.common.bending.BendingType.EARTHBENDING;
import static net.minecraft.network.datasync.EntityDataManager.createKey;

import java.util.List;
import java.util.Random;

import com.crowsofwar.avatar.common.bending.earth.EarthbendingState;
import com.crowsofwar.avatar.common.data.AvatarPlayerData;
import com.crowsofwar.avatar.common.entity.data.Behavior;
import com.crowsofwar.avatar.common.entity.data.FloatingBlockBehavior;
import com.crowsofwar.avatar.common.entity.data.OwnerAttribute;
import com.crowsofwar.avatar.common.util.AvatarDataSerializers;
import com.crowsofwar.gorecore.util.BackedVector;
import com.crowsofwar.gorecore.util.Vector;
import com.google.common.base.Optional;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockWall;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ReportedException;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityFloatingBlock extends AvatarEntity {
	
	public static final Block DEFAULT_BLOCK = Blocks.STONE;
	
	private static final DataParameter<Integer> SYNC_ENTITY_ID = createKey(EntityFloatingBlock.class,
			DataSerializers.VARINT);
	private static final DataParameter<Vector> SYNC_VELOCITY = createKey(EntityFloatingBlock.class,
			AvatarDataSerializers.SERIALIZER_VECTOR);
	private static final DataParameter<Float> SYNC_FRICTION = createKey(EntityFloatingBlock.class,
			DataSerializers.FLOAT);
	private static final DataParameter<Optional<IBlockState>> SYNC_BLOCK = createKey(
			EntityFloatingBlock.class, DataSerializers.OPTIONAL_BLOCK_STATE);
	
	private static final DataParameter<FloatingBlockBehavior> SYNC_BEHAVIOR = createKey(
			EntityFloatingBlock.class, FloatingBlockBehavior.DATA_SERIALIZER);
	
	private static final DataParameter<String> SYNC_OWNER_NAME = createKey(EntityFloatingBlock.class,
			DataSerializers.STRING);
	
	private static int nextBlockID = 0;
	
	/**
	 * Cached owner of this floating block. May not be accurate- use
	 * {@link #getOwner()} to use updated version.
	 */
	private EntityPlayer ownerCached;
	
	/**
	 * Whether or not to drop an ItemBlock when the floating block has been
	 * destroyed. Does not matter on client.
	 */
	private boolean enableItemDrops;
	
	/**
	 * The hitbox for this floating block, but slightly expanded to give more
	 * room for killing things with.
	 */
	private AxisAlignedBB expandedHitbox;
	
	private final OwnerAttribute ownerAttrib;
	
	public EntityFloatingBlock(World world) {
		super(world);
		// For some reason, Entity#moveEntity doesn't work properly on
		// client-side, when there is less than 1 size.
		// TODO Investigate... why?
		float size = !worldObj.isRemote ? 1 : 0.95f;
		setSize(size, size);
		if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER) {
			setID(nextBlockID++);
		}
		this.enableItemDrops = true;
		this.ownerAttrib = new OwnerAttribute(this, SYNC_OWNER_NAME, newOwner -> {
			EarthbendingState state = (EarthbendingState) AvatarPlayerData.fetcher()
					.fetchPerformance(newOwner).getBendingState(EARTHBENDING.id());
			if (state != null) state.setPickupBlock(this);
		});
		
	}
	
	public EntityFloatingBlock(World world, IBlockState blockState) {
		this(world);
		setBlockState(blockState);
	}
	
	public EntityFloatingBlock(World world, IBlockState blockState, EntityPlayer owner) {
		this(world, blockState);
		setOwner(owner);
	}
	
	@Override
	protected Vector createInternalVelocity() {
		//@formatter:off
		return new BackedVector(
				x -> dataManager.set(SYNC_VELOCITY, velocity().copy().setX(x)),
				y -> dataManager.set(SYNC_VELOCITY, velocity().copy().setY(y)),
				z -> dataManager.set(SYNC_VELOCITY, velocity().copy().setZ(z)),
				() -> dataManager.get(SYNC_VELOCITY).x(),
				() -> dataManager.get(SYNC_VELOCITY).y(),
				() -> dataManager.get(SYNC_VELOCITY).z());
		//@formatter:on
	}
	
	// Called from constructor of Entity class
	@Override
	protected void entityInit() {
		
		dataManager.register(SYNC_ENTITY_ID, 0);
		dataManager.register(SYNC_VELOCITY, Vector.ZERO);
		dataManager.register(SYNC_FRICTION, 1f);
		dataManager.register(SYNC_BLOCK, Optional.of(DEFAULT_BLOCK.getDefaultState()));
		dataManager.register(SYNC_BEHAVIOR, new FloatingBlockBehavior.DoNothing());
		
	}
	
	@Override
	protected void readEntityFromNBT(NBTTagCompound nbt) {
		setBlockState(
				Block.getBlockById(nbt.getInteger("BlockId")).getStateFromMeta(nbt.getInteger("Metadata")));
		setVelocity(nbt.getDouble("VelocityX"), nbt.getDouble("VelocityY"), nbt.getDouble("VelocityZ"));
		setFriction(nbt.getFloat("Friction"));
		setItemDropsEnabled(nbt.getBoolean("DropItems"));
		setBehavior((FloatingBlockBehavior) Behavior.lookup(nbt.getInteger("Behavior"), this));
		ownerAttrib.load(nbt);
	}
	
	@Override
	protected void writeEntityToNBT(NBTTagCompound nbt) {
		nbt.setInteger("BlockId", Block.getIdFromBlock(getBlock()));
		nbt.setInteger("Metadata", getBlock().getMetaFromState(getBlockState()));
		nbt.setDouble("VelocityX", velocity().x());
		nbt.setDouble("VelocityY", velocity().y());
		nbt.setDouble("VelocityZ", velocity().z());
		nbt.setFloat("Friction", getFriction());
		nbt.setBoolean("DropItems", areItemDropsEnabled());
		nbt.setInteger("Behavior", getBehavior().getId());
		ownerAttrib.save(nbt);
	}
	
	public Block getBlock() {
		return getBlockState().getBlock();
	}
	
	public void setBlock(Block block) {
		setBlockState(block.getDefaultState());
	}
	
	public IBlockState getBlockState() {
		Optional<IBlockState> obs = dataManager.get(SYNC_BLOCK);
		return obs.get();
	}
	
	public void setBlockState(IBlockState state) {
		dataManager.set(SYNC_BLOCK, Optional.of(state));
	}
	
	/**
	 * Get the ID of this floating block. Each instance has its own unique ID.
	 * Synced between client and server.
	 */
	public int getID() {
		return dataManager.get(SYNC_ENTITY_ID);
	}
	
	public void setID(int id) {
		if (!worldObj.isRemote) dataManager.set(SYNC_ENTITY_ID, id);
	}
	
	public static EntityFloatingBlock getFromID(World world, int id) {
		for (int i = 0; i < world.loadedEntityList.size(); i++) {
			Entity e = (Entity) world.loadedEntityList.get(i);
			if (e instanceof EntityFloatingBlock && ((EntityFloatingBlock) e).getID() == id)
				return (EntityFloatingBlock) e;
		}
		return null;
	}
	
	/**
	 * Returns whether the floating block drops the block as an item when it is
	 * destroyed. Only used on server-side. By default, is true.
	 */
	public boolean areItemDropsEnabled() {
		return enableItemDrops;
	}
	
	/**
	 * Set whether the block should be dropped when it is destroyed.
	 */
	public void setItemDropsEnabled(boolean enable) {
		this.enableItemDrops = enable;
	}
	
	/**
	 * Disable dropping an item when the floating block is destroyed.
	 */
	public void disableItemDrops() {
		setItemDropsEnabled(false);
	}
	
	private void spawnCrackParticle(double x, double y, double z, double mx, double my, double mz) {
		worldObj.spawnParticle(EnumParticleTypes.BLOCK_CRACK, x, y, z, mx, my, mz,
				Block.getStateId(getBlockState()));
	}
	
	@Override
	public void onUpdate() {
		
		if (ticksExisted == 1) {
			
			for (int i = 0; i < 10; i++) {
				double spawnX = posX + (rand.nextDouble() - 0.5);
				double spawnY = posY - 0;
				double spawnZ = posZ + (rand.nextDouble() - 0.5);
				spawnCrackParticle(spawnX, spawnY, spawnZ, 0, -0.1, 0);
			}
			
		}
		
		if (!worldObj.isRemote) velocity().mul(getFriction());
		
		prevPosX = posX;
		prevPosY = posY;
		prevPosZ = posZ;
		lastTickPosX = posX;
		lastTickPosY = posY;
		lastTickPosZ = posZ;
		motionX = velocity().x() / 20;
		motionY = velocity().y() / 20;
		motionZ = velocity().z() / 20;
		
		modifiedMoveEntity(velocity().x() / 20, velocity().y() / 20, velocity().z() / 20);
		
		getBehavior().setEntity(this);
		FloatingBlockBehavior nextBehavior = (FloatingBlockBehavior) getBehavior().onUpdate();
		if (nextBehavior != getBehavior()) setBehavior(nextBehavior);
		
	}
	
	private void modifiedMoveEntity(double x, double y, double z) {
		if (this.noClip) {
			this.setEntityBoundingBox(this.getEntityBoundingBox().offset(x, y, z));
			this.resetPositionToBB();
		} else {
			this.worldObj.theProfiler.startSection("move");
			double d0 = this.posX;
			double d1 = this.posY;
			double d2 = this.posZ;
			
			List<AxisAlignedBB> list1 = this.worldObj.getCollisionBoxes(this,
					this.getEntityBoundingBox().addCoord(x, y, z));
			AxisAlignedBB axisalignedbb = this.getEntityBoundingBox();
			int i = 0;
			
			for (int j = list1.size(); i < j; ++i) {
				y = ((AxisAlignedBB) list1.get(i)).calculateYOffset(this.getEntityBoundingBox(), y);
			}
			
			this.setEntityBoundingBox(this.getEntityBoundingBox().offset(0.0D, y, 0.0D));
			boolean i_ = this.onGround || y < 0.0D;
			int j4 = 0;
			
			for (int k = list1.size(); j4 < k; ++j4) {
				x = ((AxisAlignedBB) list1.get(j4)).calculateXOffset(this.getEntityBoundingBox(), x);
			}
			
			this.setEntityBoundingBox(this.getEntityBoundingBox().offset(x, 0.0D, 0.0D));
			j4 = 0;
			
			for (int k4 = list1.size(); j4 < k4; ++j4) {
				z = ((AxisAlignedBB) list1.get(j4)).calculateZOffset(this.getEntityBoundingBox(), z);
			}
			
			this.setEntityBoundingBox(this.getEntityBoundingBox().offset(0.0D, 0.0D, z));
			
			this.worldObj.theProfiler.endSection();
			this.worldObj.theProfiler.startSection("rest");
			this.resetPositionToBB();
			this.isCollidedHorizontally = x != x || z != z;
			this.isCollidedVertically = y != y;
			this.onGround = this.isCollidedVertically && y < 0.0D;
			this.isCollided = this.isCollidedHorizontally || this.isCollidedVertically;
			j4 = MathHelper.floor_double(this.posX);
			int l4 = MathHelper.floor_double(this.posY - 0.20000000298023224D);
			int i5 = MathHelper.floor_double(this.posZ);
			BlockPos blockpos = new BlockPos(j4, l4, i5);
			IBlockState iblockstate = this.worldObj.getBlockState(blockpos);
			
			if (iblockstate.getMaterial() == Material.AIR) {
				BlockPos blockpos1 = blockpos.down();
				IBlockState iblockstate1 = this.worldObj.getBlockState(blockpos1);
				Block block1 = iblockstate1.getBlock();
				
				if (block1 instanceof BlockFence || block1 instanceof BlockWall
						|| block1 instanceof BlockFenceGate) {
					iblockstate = iblockstate1;
					blockpos = blockpos1;
				}
			}
			
			this.updateFallState(y, this.onGround, iblockstate, blockpos);
			
			Block block = iblockstate.getBlock();
			
			try {
				this.doBlockCollisions();
			} catch (Throwable throwable) {
				CrashReport crashreport = CrashReport.makeCrashReport(throwable,
						"Checking entity block collision");
				CrashReportCategory crashreportcategory = crashreport
						.makeCategory("Entity being checked for collision");
				this.addEntityCrashInfo(crashreportcategory);
				throw new ReportedException(crashreport);
			}
			
			this.worldObj.theProfiler.endSection();
		}
	}
	
	/**
	 * Called when the block collides with the ground, but not other entities
	 */
	public void onCollision() {
		// Spawn particles
		Random random = new Random();
		for (int i = 0; i < 7; i++) {
			spawnCrackParticle(posX, posY + 0.3, posZ, random.nextGaussian() * 0.1,
					random.nextGaussian() * 0.1, random.nextGaussian() * 0.1);
		}
		
		if (!worldObj.isRemote && areItemDropsEnabled()) {
			List<ItemStack> drops = getBlock().getDrops(worldObj, new BlockPos(this), getBlockState(), 0);
			for (ItemStack is : drops) {
				EntityItem ei = new EntityItem(worldObj, posX, posY, posZ, is);
				worldObj.spawnEntityInWorld(ei);
			}
		}
	}
	
	public float getFriction() {
		return dataManager.get(SYNC_FRICTION);
	}
	
	public void setFriction(float friction) {
		if (!worldObj.isRemote) dataManager.set(SYNC_FRICTION, friction);
	}
	
	public void drop() {
		setBehavior(new FloatingBlockBehavior.Fall());
	}
	
	public EntityPlayer getOwner() {
		return ownerAttrib.getOwner();
	}
	
	public void setOwner(EntityPlayer owner) {
		ownerAttrib.setOwner(owner);
	}
	
	public FloatingBlockBehavior getBehavior() {
		return dataManager.get(SYNC_BEHAVIOR);
	}
	
	public void setBehavior(FloatingBlockBehavior behavior) {
		// FIXME research: why doesn't sync_Behavior cause an update to client?
		if (behavior == null) throw new IllegalArgumentException("Cannot have null behavior");
		dataManager.set(SYNC_BEHAVIOR, behavior);
	}
	
	public AxisAlignedBB getExpandedHitbox() {
		return this.expandedHitbox;
	}
	
	@Override
	public void setEntityBoundingBox(AxisAlignedBB bb) {
		super.setEntityBoundingBox(bb);
		expandedHitbox = bb.expand(0.35, 0.35, 0.35);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public boolean isInRangeToRenderDist(double d) {
		return true;
	}
	
}
