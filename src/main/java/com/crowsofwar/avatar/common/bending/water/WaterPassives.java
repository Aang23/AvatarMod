package com.crowsofwar.avatar.common.bending.water;

import com.crowsofwar.avatar.AvatarInfo;
import com.crowsofwar.avatar.common.data.Bender;
import com.crowsofwar.avatar.common.data.BendingData;
import com.crowsofwar.avatar.common.data.Chi;
import com.crowsofwar.avatar.common.entity.mob.EntityBender;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static com.crowsofwar.avatar.common.config.ConfigChi.CHI_CONFIG;
import static com.crowsofwar.avatar.common.config.ConfigStats.STATS_CONFIG;

@Mod.EventBusSubscriber(modid = AvatarInfo.MOD_ID)
public class WaterPassives {

	@SubscribeEvent
	public static void waterPassives(LivingEvent.LivingUpdateEvent event) {
		//TODO: Use configurable lists; they aren't working rn, I need to figure out why
		EntityLivingBase entity = (EntityLivingBase) event.getEntity();
		World world = entity.getEntityWorld();
		if (entity instanceof EntityBender || entity instanceof EntityPlayerMP) {
			Bender bender = Bender.get(entity);
			if (bender.getData() != null) {
				BendingData ctx = BendingData.get(entity);
				if (ctx.hasBendingId(Waterbending.ID)) {
					if (entity.isInWater()) {
						entity.addPotionEffect(new PotionEffect(MobEffects.WATER_BREATHING, 10));
						entity.addPotionEffect(new PotionEffect(MobEffects.HASTE, 10, 1));
						//entity.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 10, 0));
						//OP right now; will be implemented later with the skill tree
						}
					}
				}

			}
		}
	}
