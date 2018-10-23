package com.crowsofwar.avatar.common.bending.fire;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;

import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import com.crowsofwar.avatar.AvatarInfo;
import com.crowsofwar.avatar.common.data.*;
import com.crowsofwar.avatar.common.entity.mob.EntityBender;

@Mod.EventBusSubscriber(modid = AvatarInfo.MODID)
public class FirePassiveHandler {

	@SubscribeEvent
	public static void fireResistance(LivingEvent.LivingUpdateEvent event) {
		if (event.getEntity() instanceof EntityLivingBase) {
			EntityLivingBase entity = event.getEntityLiving();
			if (entity instanceof EntityBender || entity instanceof EntityPlayer) {
				Bender b = Bender.get(entity);
				if (b != null) {
					BendingData data = b.getData();
					if (data != null) {
						if (data.hasBendingId(Firebending.ID)) {
							if (entity.ticksExisted % 400 == 0) {
								if (entity.world.isDaytime()) {
									if (b.calcPowerRating(Firebending.ID) >= 35) {
										entity.addPotionEffect(new PotionEffect(MobEffects.FIRE_RESISTANCE, 400, -1));
									}
								}
							}
						}
					}
				}
			}
		}
	}
}
