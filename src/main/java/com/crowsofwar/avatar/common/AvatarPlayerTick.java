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

package com.crowsofwar.avatar.common;

import static com.crowsofwar.avatar.common.config.ConfigChi.CHI_CONFIG;

import com.crowsofwar.avatar.common.data.AvatarPlayerData;
import com.crowsofwar.avatar.common.data.Chi;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;

public class AvatarPlayerTick {
	
	@SubscribeEvent
	public void onPlayerTick(PlayerTickEvent e) {
		// Also forces loading of data on client
		AvatarPlayerData data = AvatarPlayerData.fetcher().fetch(e.player);
		if (data != null) {
			
			data.decrementCooldown();
			if (!e.player.worldObj.isRemote) {
				Chi chi = data.chi();
				chi.changeTotalChi(CHI_CONFIG.regenPerSecond / 20f);
				
				if (chi.getAvailableChi() < chi.getMaxChi() * CHI_CONFIG.availableThreshold) {
					chi.changeAvailableChi(CHI_CONFIG.availablePerSecond / 20f);
				}
				
			}
		}
		
	}
	
}
