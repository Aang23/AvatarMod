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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.crowsofwar.avatar.common.bending.BendingAbility;
import com.crowsofwar.avatar.common.bending.BendingController;
import com.crowsofwar.avatar.common.bending.BendingManager;
import com.crowsofwar.avatar.common.bending.BendingType;
import com.crowsofwar.avatar.common.bending.StatusControl;

/**
 * 
 * 
 * @author CrowsOfWar
 */
public abstract class AbstractBendingData implements BendingData {
	
	private final Set<BendingController> bendings;
	private final Set<StatusControl> statusControls;
	private final Map<BendingAbility, AbilityData> abilityData;
	private Chi chi;
	
	private float fallAbsorption;
	private boolean skating;
	private int timeInAir;
	private int abilityCooldown;
	private boolean wallJumping;
	
	public AbstractBendingData() {
		bendings = new HashSet<>();
		statusControls = new HashSet<>();
		abilityData = new HashMap<>();
		chi = new Chi(this);
	}
	
	// ================================================================================
	// BENDINGS METHODS
	// ================================================================================
	
	/**
	 * Check if the player has that bending controller
	 */
	@Override
	public boolean hasBending(BendingController bending) {
		return bendings.contains(bending);
	}
	
	/**
	 * Check if the player has that type of bending
	 */
	@Override
	public boolean hasBending(BendingType type) {
		return hasBending(BendingManager.getBending(type));
	}
	
	/**
	 * If the bending controller is not already present, adds the bending
	 * controller.
	 * <p>
	 * Also adds the state if it isn't present.
	 */
	@Override
	public void addBending(BendingController bending) {
		if (bendings.add(bending)) {
			save(DataCategory.BENDING);
		}
	}
	
	/**
	 * If the bending controller is not already present, adds the bending
	 * controller.
	 */
	@Override
	public void addBending(BendingType type) {
		addBending(BendingManager.getBending(type));
	}
	
	/**
	 * Remove the specified bending controller and its associated state. Please
	 * note, this will be saved, so is permanent (unless another bending
	 * controller is added).
	 */
	@Override
	public void removeBending(BendingController bending) {
		if (bendings.remove(bending)) {
			save(DataCategory.BENDING);
		}
	}
	
	/**
	 * Remove the bending controller and its state with that type.
	 * 
	 * @see #removeBending(BendingController)
	 */
	@Override
	public void removeBending(BendingType type) {
		removeBending(BendingManager.getBending(type));
	}
	
	@Override
	public List<BendingController> getAllBending() {
		return new ArrayList<>(bendings);
	}
	
	// ================================================================================
	// STATUS CONTROLS
	// ================================================================================
	
	@Override
	public boolean hasStatusControl(StatusControl control) {
		return statusControls.contains(control);
	}
	
	@Override
	public void addStatusControl(StatusControl control) {
		if (statusControls.add(control)) {
			save(DataCategory.STATUS_CONTROLS);
		}
	}
	
	@Override
	public void removeStatusControl(StatusControl control) {
		if (statusControls.remove(control)) {
			save(DataCategory.STATUS_CONTROLS);
		}
	}
	
	@Override
	public List<StatusControl> getAllStatusControls() {
		return new ArrayList<>(statusControls);
	}
	
	@Override
	public void clearStatusControls() {
		statusControls.clear();
	}
	
	// ================================================================================
	// ABILITY DATA
	// ================================================================================
	
	@Override
	public boolean hasAbilityData(BendingAbility ability) {
		return abilityData.get(ability) != null;
	}
	
	/**
	 * Retrieves data about the given ability. Will create data if necessary.
	 */
	@Override
	public AbilityData getAbilityData(BendingAbility ability) {
		AbilityData data = abilityData.get(ability);
		if (data == null) {
			data = new AbilityData(this, ability);
			abilityData.put(ability, data);
			save(DataCategory.BENDING);
		}
		
		return data;
	}
	
	@Override
	public void setAbilityData(BendingAbility ability, AbilityData data) {
		abilityData.put(ability, data);
	}
	
	/**
	 * Gets a list of all ability data contained in this player data.
	 */
	@Override
	public List<AbilityData> getAllAbilityData() {
		return new ArrayList<>(abilityData.values());
	}
	
	/**
	 * Removes all ability data associations
	 */
	@Override
	public void clearAbilityData() {
		abilityData.clear();
	}
	
	// ================================================================================
	// CHI
	// ================================================================================
	
	/**
	 * Gets the chi information about the bender
	 */
	@Override
	public Chi chi() {
		return chi;
	}
	
	@Override
	public void setChi(Chi chi) {
		this.chi = chi;
		save(DataCategory.CHI);
	}
	
	// ================================================================================
	// MISC
	// ================================================================================
	
	@Override
	public boolean isSkating() {
		return skating;
	}
	
	@Override
	public void setSkating(boolean skating) {
		this.skating = skating;
	}
	
	@Override
	public float getFallAbsorption() {
		return fallAbsorption;
	}
	
	@Override
	public void setFallAbsorption(float fallAbsorption) {
		if (fallAbsorption == 0 || fallAbsorption > this.fallAbsorption) this.fallAbsorption = fallAbsorption;
	}
	
	@Override
	public int getTimeInAir() {
		return timeInAir;
	}
	
	@Override
	public void setTimeInAir(int time) {
		this.timeInAir = time;
	}
	
	@Override
	public int getAbilityCooldown() {
		return abilityCooldown;
	}
	
	@Override
	public void setAbilityCooldown(int cooldown) {
		if (cooldown < 0) cooldown = 0;
		this.abilityCooldown = cooldown;
		save(DataCategory.MISC);
	}
	
	@Override
	public void decrementCooldown() {
		if (abilityCooldown > 0) {
			abilityCooldown--;
			save(DataCategory.MISC);
		}
	}
	
	@Override
	public boolean isWallJumping() {
		return wallJumping;
	}
	
	@Override
	public void setWallJumping(boolean wallJumping) {
		this.wallJumping = wallJumping;
	}
	
	/**
	 * Save this BendingData
	 */
	@Override
	public abstract void save(DataCategory category, DataCategory... addditionalCategories);
	
	public enum DataCategory {
		
		BENDING,
		STATUS_CONTROLS,
		ABILITY_DATA,
		CHI,
		MISC
		
		// BENDING(data -> data.getAllBending()),
		// STATUS_CONTROLS(data -> data.getAllStatusControls()),
		// ABILITY_DATA(data -> data.getAllAbilityData()),
		// CHI(data -> data.getChi()),
		// MISC(data -> null);
		//
		// private final Networker.Property<?> property;
		// private final Function<BendingData, Object> getter;
		//
		// private DataCategory(Function<BendingData, Object> getter) {
		// property = new Networker.Property<>(ordinal() + 1);
		// this.getter = getter;
		// }
		//
		// public Networker.Property<?> property() {
		// return property;
		// }
		//
		// public Object get(BendingData data) {
		// return getter.apply(data);
		// }
		
	}
	
}
