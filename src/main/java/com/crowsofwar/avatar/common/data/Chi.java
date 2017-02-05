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
package com.crowsofwar.avatar.common.data;

import static com.crowsofwar.gorecore.util.GoreCoreNBTUtil.findNestedCompound;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Represents a bender's energy to use abilities. Chi is required to execute an
 * ability and also will regenerate over time.
 * <p>
 * Chi is somewhat simple; it is a bar with a current and maximum amount.
 * However, only a certain portion of the bar is usable at one time. This is
 * referred to as {@link #getAvailableChi() available chi}. The other chi can't
 * be used, until the available mark increases.
 * 
 * @author CrowsOfWar
 */
public class Chi {
	
	private final AvatarPlayerData data;
	
	// These fields are not for modification directly; use getters/setters
	private float max;
	private float total;
	private float availableMark;
	
	public Chi(AvatarPlayerData data) {
		this.data = data;
		
		// Default values for testing
		this.max = 20;
		this.total = 10;
		this.availableMark = 8;
		
	}
	
	/**
	 * Gets the current amount of chi. Some may not be usable.
	 * 
	 * @see #setTotalChi(float)
	 */
	public float getTotalChi() {
		return total;
	}
	
	/**
	 * Sets the current amount of chi. Some may not be usable.
	 * 
	 * @see #getTotalChi()
	 */
	public void setTotalChi(float total) {
		if (total > max) total = max;
		this.total = total;
		save();
	}
	
	/**
	 * Gets the maximum amount of chi possible. However, not all of this chi
	 * would be usable at one time
	 * 
	 * @see #setMaxChi(float)
	 */
	public float getMaxChi() {
		return max;
	}
	
	/**
	 * Sets the maximum amount of chi possible. However, not all of this chi
	 * would be usable at one time
	 * 
	 * @see #getMaxChi()
	 */
	public void setMaxChi(float max) {
		this.max = max;
		if (max < total) setTotalChi(max);
		save();
	}
	
	/**
	 * Gets the current available amount of chi.
	 * 
	 * @see #setAvailableChi(float)
	 */
	public float getAvailableChi() {
		return total - availableMark;
	}
	
	/**
	 * Moves the available chi mark so the amount of available chi is now at the
	 * requested value.
	 * 
	 * @see #getAvailableChi()
	 */
	public void setAvailableChi(float available) {
		if (available > total) available = total;
		float subtract = getAvailableChi() - available;
		this.total -= subtract;
		save();
	}
	
	/**
	 * Gets the maximum amount of available chi, at this available mark
	 */
	public float getAvailableMaxChi() {
		return max - availableMark;
	}
	
	private void save() {
		data.getNetworker().changeAndSync(AvatarPlayerData.KEY_CHI, this);
		data.saveChanges();
	}
	
	/**
	 * Reads the chi information to NBT. This creates a subcompound in the
	 * parameter
	 */
	public void readFromNBT(NBTTagCompound compound) {
		NBTTagCompound nbt = findNestedCompound(compound, "ChiData");
		this.max = nbt.getFloat("Max");
		this.total = nbt.getFloat("Current");
		this.availableMark = nbt.getFloat("AvailableMark");
	}
	
	/**
	 * Writes the chi information to NBT. This creates a subcompound in the
	 * parameter
	 */
	public void writeToNBT(NBTTagCompound compound) {
		NBTTagCompound nbt = findNestedCompound(compound, "ChiData");
		nbt.setFloat("Max", max);
		nbt.setFloat("Current", total);
		nbt.setFloat("AvailableMark", availableMark);
	}
	
	public void toBytes(ByteBuf buf) {
		buf.writeFloat(max);
		buf.writeFloat(total);
		buf.writeFloat(availableMark);
	}
	
	public void fromBytes(ByteBuf buf) {
		max = buf.readFloat();
		total = buf.readFloat();
		availableMark = buf.readFloat();
	}
	
}
