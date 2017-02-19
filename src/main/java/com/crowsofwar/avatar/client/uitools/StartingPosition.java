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
package com.crowsofwar.avatar.client.uitools;

import static com.crowsofwar.avatar.client.uitools.ScreenInfo.screenWidth;

/**
 * 
 * 
 * @author CrowsOfWar
 */
public class StartingPosition {
	
	public static StartingPosition TOP_LEFT = new StartingPosition(0, 0, 0, 0);
	public static StartingPosition TOP_RIGHT = new StartingPosition(screenWidth(), 0, 1, 0);
	
	private float x, y, minusWidth, minusHeight;
	
	private StartingPosition(float x, float y, float minusWidth, float minusHeight) {
		this.x = x;
		this.y = y;
		this.minusWidth = minusWidth;
		this.minusHeight = minusHeight;
	}
	
	public float getX() {
		return x;
	}
	
	public float getY() {
		return y;
	}
	
	/**
	 * To achieve the desired x coordinate, the width times this number should
	 * be subtracted from the x position.
	 */
	public float getMinusWidth() {
		return minusWidth;
	}
	
	/**
	 * To achieve the desired y coordinate, the height times this number should
	 * be subtracted from the y position.
	 */
	public float getMinusHeight() {
		return minusHeight;
	}
	
	public static StartingPosition custom(float x, float y) {
		return new StartingPosition(x, y, .5f, .5f);
	}
	
}
