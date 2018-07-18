package com.crowsofwar.avatar.common.bending.earth;

import com.crowsofwar.avatar.common.data.AbilityData;
import com.crowsofwar.avatar.common.data.BendingData;
import com.crowsofwar.avatar.common.data.TickHandler;
import com.crowsofwar.avatar.common.data.ctx.BendingContext;
import com.crowsofwar.avatar.common.entity.AvatarEntity;
import com.crowsofwar.avatar.common.entity.EntityEarthspike;
import com.crowsofwar.avatar.common.entity.EntityEarthspikeSpawner;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;

import static com.crowsofwar.avatar.common.config.ConfigStats.STATS_CONFIG;

public class SpawnEarthspikesHandler extends TickHandler {
	@Override
	public boolean tick(BendingContext ctx) {
		World world = ctx.getWorld();
		EntityLivingBase owner = ctx.getBenderEntity();
		AbilityData abilityData = AbilityData.get(owner, "earthspike");
		BendingData data = ctx.getData();

		float frequency = STATS_CONFIG.earthspikeSettings.frequency;
		//8 (by default)
		double damage = STATS_CONFIG.earthspikeSettings.damage;
		//3 (by default)


		if (abilityData.getLevel() == 1) {
			frequency *= 0.75;
			//6
			damage *= (4 / 3);
			//4
		}

		if (abilityData.getLevel() == 2) {
			frequency *= 0.5;
			//4
			damage *= 5 / 3;
			//5
		}

		if (abilityData.isMasterPath(AbilityData.AbilityTreePath.FIRST)) {
			frequency *= (5 / 8);
			//5
			damage = STATS_CONFIG.earthspikeSettings.damage;
			//1.5
		}

		if (abilityData.isMasterPath(AbilityData.AbilityTreePath.SECOND)) {
			frequency *= 0.25;
			//2
			damage *= 2;
			//6

		}
		EntityEarthspikeSpawner entity = AvatarEntity.lookupControlledEntity(world, EntityEarthspikeSpawner.class, owner);

		if (abilityData.isMasterPath(AbilityData.AbilityTreePath.SECOND)) {
			for (int i = 0; i < 8; i++) {
				EntityEarthspikeSpawner entity1 = AvatarEntity.lookupControlledEntity(world, EntityEarthspikeSpawner.class, owner);
				if (entity1 != null) {
					if (data.getTickHandlerDuration(this) % frequency == 0) {
						EntityEarthspike earthspike = new EntityEarthspike(world);
						earthspike.posX = entity1.posX;
						earthspike.posY = entity1.posY;
						earthspike.posZ = entity1.posZ;
						earthspike.setAbility(abilityData.getAbility());
						earthspike.setDamage(damage);
						earthspike.setOwner(owner);
						world.spawnEntity(earthspike);
					}
					return false;
				}
			}
		} else {

			if (entity != null) {
				if (data.getTickHandlerDuration(this) % frequency == 0) {
					EntityEarthspike earthspike = new EntityEarthspike(world);
					earthspike.posX = entity.posX;
					earthspike.posY = entity.posY;
					earthspike.posZ = entity.posZ;
					earthspike.setAbility(abilityData.getAbility());
					earthspike.setDamage(damage);
					earthspike.setOwner(owner);
					world.spawnEntity(earthspike);

				}
				return false;
			}
		}
		return entity == null;
	}
}
