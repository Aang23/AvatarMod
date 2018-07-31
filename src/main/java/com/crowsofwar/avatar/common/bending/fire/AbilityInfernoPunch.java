package com.crowsofwar.avatar.common.bending.fire;

import com.crowsofwar.avatar.common.bending.Ability;
import com.crowsofwar.avatar.common.bending.BendingAi;
import com.crowsofwar.avatar.common.data.*;
import com.crowsofwar.avatar.common.data.ctx.AbilityContext;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;

import static com.crowsofwar.avatar.common.bending.StatusControl.INFERNO_PUNCH;
import static com.crowsofwar.avatar.common.config.ConfigStats.STATS_CONFIG;
import static com.crowsofwar.avatar.common.data.TickHandler.INFERNO_PARTICLE_SPAWNER;

public class AbilityInfernoPunch extends Ability {
	public AbilityInfernoPunch() {
		super(Firebending.ID, "inferno_punch");
	}

	@Override
	public void execute(AbilityContext ctx) {
		EntityLivingBase entity = ctx.getBenderEntity();
		BendingData data = ctx.getData();
		Bender bender = ctx.getBender();
		if (data.hasStatusControl(INFERNO_PUNCH)) return;

		float chi = STATS_CONFIG.chiInfernoPunch;
		if (ctx.getLevel() >= 1) {
			chi = STATS_CONFIG.chiInfernoPunch * 4/3;
			//4
		}
		if (ctx.getLevel() >= 2) {
			chi = STATS_CONFIG.chiInfernoPunch * 5/3;
			//5
		}
		if (ctx.isMasterLevel(AbilityData.AbilityTreePath.FIRST)) {
			chi = STATS_CONFIG.chiLargeInfernoPunch;
			//6
		}
		if (ctx.isMasterLevel(AbilityData.AbilityTreePath.SECOND)) {
			chi = STATS_CONFIG.chiSmallInfernoPunch * 2F;
			//4
		}

		if (bender.consumeChi(chi)) {
			data.addStatusControl(INFERNO_PUNCH);
			data.addTickHandler(INFERNO_PARTICLE_SPAWNER);
		}
	}

	@Override
	public BendingAi getAi(EntityLiving entity, Bender bender) {
		return super.getAi(entity, bender);
	}

	@Override
	public int getCooldown(AbilityContext ctx) {
		EntityLivingBase entity = ctx.getBenderEntity();

		int coolDown = 150;

		if (entity instanceof EntityPlayer && ((EntityPlayer) entity).isCreative()) {
			coolDown = 0;
		}

		if (ctx.getLevel() == 1) {
			coolDown = 130;
		}
		if (ctx.getLevel() == 2) {
			coolDown = 110;
		}
		if (ctx.isMasterLevel(AbilityData.AbilityTreePath.FIRST)) {
			coolDown = 100;
		}
		if (ctx.isMasterLevel(AbilityData.AbilityTreePath.SECOND)) {
			coolDown = 60;
		}
		return coolDown;
	}
}
