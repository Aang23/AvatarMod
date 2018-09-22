package com.crowsofwar.avatar.client.render;

import com.crowsofwar.avatar.common.entity.ControlPoint;
import com.crowsofwar.avatar.common.entity.EntityArc;
import com.crowsofwar.avatar.common.entity.EntityWaterCannon;
import com.crowsofwar.avatar.common.particle.ClientParticleSpawner;
import com.crowsofwar.avatar.common.particle.ParticleSpawner;
import com.crowsofwar.gorecore.util.Vector;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;

public class RenderWaterCannon extends RenderArc {
	private static final ResourceLocation TEXTURE = new ResourceLocation("minecraft",
			"textures/blocks/water_overlay.png");

	private final ParticleSpawner particleSpawner;

	public RenderWaterCannon(RenderManager renderManager) {
		super(renderManager, true);
		particleSpawner = new ClientParticleSpawner();
	}

	@Override
	public void doRender(Entity entity, double xx, double yy, double zz, float p_76986_8_,
						 float partialTicks) {

		EntityWaterCannon cannon = (EntityWaterCannon) entity;
		renderArc(cannon, partialTicks, 3f, 3f * cannon.getSizeMultiplier());
		/*Vector vector = cannon.velocity().normalize();
		//double yaw = Math.atan(vector.x()/(-vector.y()));
		//double pitch = Math.atan(Math.sqrt(vector.x() * vector.x() + vector.y() * vector.y())/vector.z());
		for (int degree = 0; degree < 360; degree++) {
			double radians = Math.toRadians(degree);
			double x = Math.cos(radians);
			double z = Math.sin(radians);
			//double y = cannon.posY - cannon.getSizeMultiplier();
			/*if (y >= cannon.posY + cannon.getSizeMultiplier()) {
				y -= 1;
			}
			else if (y<= cannon.posY - cannon.getSizeMultiplier()) {
				y -= 1;
			}
			cannon.world.spawnParticle(EnumParticleTypes.WATER_SPLASH, x + cannon.getOwner().posX, cannon.getOwner().posY,
					z + cannon.getOwner().posZ, 0, 0, 0);

		}**/

	}

	@Override
	protected void onDrawSegment(EntityArc arc, ControlPoint first, ControlPoint second) {
		// Parametric equation

		Vector from = new Vector(0, 0, 0);
		Vector to = second.position().minus(first.position());
		Vector diff = to.minus(from);
		Vector offset = first.position();
		Vector direction = diff.normalize();
		Vector spawnAt = offset.plus(direction.times(Math.random()));
		Vector velocity = first.velocity();
		arc.world.spawnParticle(EnumParticleTypes.WATER_SPLASH, spawnAt.x(), spawnAt.y(), spawnAt.z(),
				velocity.x(), velocity.y(), velocity.z());
	}

	@Override
	protected ResourceLocation getTexture() {
		return TEXTURE;
	}

}


