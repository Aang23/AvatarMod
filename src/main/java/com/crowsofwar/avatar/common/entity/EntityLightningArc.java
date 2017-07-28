package com.crowsofwar.avatar.common.entity;

import com.crowsofwar.avatar.common.util.AvatarDataSerializers;
import com.crowsofwar.gorecore.util.Vector;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.world.World;
import org.joml.Matrix4d;
import org.joml.Vector4d;

/**
 * @author CrowsOfWar
 */
public class EntityLightningArc extends EntityArc {

	private static final DataParameter<Vector> SYNC_ENDPOS = EntityDataManager.createKey
			(EntityLightningArc.class, AvatarDataSerializers.SERIALIZER_VECTOR);

	public EntityLightningArc(World world) {
		super(world);
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		dataManager.register(SYNC_ENDPOS, Vector.ZERO);
	}

	@Override
	public int getAmountOfControlPoints() {
		return 6;
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		if (getOwner() != null) {
			Vector ownerPosition = Vector.getEyePos(getOwner());
			Vector endPosition = getEndPos();
			Vector position = ownerPosition;

			// position slightly below eye height
			position = position.minusY(0.3);
			// position slightly away from owner
			position = position.plus(endPosition.minus(position).dividedBy(10));

			setPosition(position);

			Vector newRotations = Vector.getRotationTo(position(), getEndPos());
			rotationYaw = (float) Math.toDegrees(newRotations.y());
			rotationPitch = (float) Math.toDegrees(newRotations.x());
		}
		if (ticksExisted > 40) {
			setDead();
		}
	}

	@Override
	protected void updateCpBehavior() {

		if (ticksExisted % 3 == 1) {

			for (int i = 0; i < getControlPoints().size(); i++) {

				ControlPoint controlPoint = getControlPoint(i);
				double targetDist = position().dist(getEndPos()) / getControlPoints().size();
				Vector dir = Vector.getLookRectangular(this);

				Vector normalPosition = position().plus(dir.times(targetDist).times(i));

				Vector randomize = Vector.ZERO;

				if (i != getControlPoints().size() - 1) {
					Matrix4d matrix = new Matrix4d();
					matrix.rotate(Math.toRadians(rotationYaw), 0, 1, 0);
					matrix.rotate(Math.toRadians(rotationPitch), 1, 0, 0);
					Vector4d randomJoml = new Vector4d(rand.nextGaussian(), rand.nextGaussian(), 0, 1);
					randomJoml.mul(matrix);

					randomize = new Vector(randomJoml.x, randomJoml.y, randomJoml.z);
				}

				controlPoint.setPosition(normalPosition.plus(randomize));

			}
		}
	}

	public Vector getEndPos() {
		return dataManager.get(SYNC_ENDPOS);
	}

	public void setEndPos(Vector endPos) {
		dataManager.set(SYNC_ENDPOS, endPos);
	}

}
