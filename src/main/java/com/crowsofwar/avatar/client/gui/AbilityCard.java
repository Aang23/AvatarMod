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

package com.crowsofwar.avatar.client.gui;

import static com.crowsofwar.avatar.common.config.ConfigClient.CLIENT_CONFIG;
import static net.minecraft.client.renderer.GlStateManager.*;

import com.crowsofwar.avatar.common.bending.BendingAbility;
import com.crowsofwar.avatar.common.data.AvatarPlayerData;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.ResourceLocation;

/**
 * 
 * 
 * @author CrowsOfWar
 */
public class AbilityCard extends Gui {
	
	private final BendingAbility ability;
	private final AvatarPlayerData data;
	private final Minecraft mc;
	
	private boolean editing;
	private KeyBinding conflict;
	
	public AbilityCard(BendingAbility ability) {
		this.mc = Minecraft.getMinecraft();
		this.ability = ability;
		this.data = AvatarPlayerData.fetcher().fetch(mc.thePlayer);
		this.editing = false;
	}
	
	public BendingAbility getAbility() {
		return ability;
	}
	
	/**
	 * @return right most X position of the card
	 */
	// @formatter:off
	public float render(ScaledResolution res, int index, int scroll) {
		
		// NOTE! Minecraft has automatic icon scaling; can be found via res.getScaleFactor()
		// To counteract this, normally you would use
		//   GlStateManager.scale(1f / res.getScaleFactor, 1f / res.getScaleFactor(), 1)
		// HOWEVER, since this is calculating scale already, I don't need to use that
		
		// There are 2 types of pixels here.
		// SCREEN PIXELS - The actual pixels of the screen. Requires resolution to make sure everything is proportioned.
		// CARD PIXELS   - Using scaling seen below, the card is now 100px x ??px (height depends on resolution).
		
		GlStateManager.enableBlend();
		
		float spacing = res.getScaledWidth() / 8.5f; // Spacing between each card
		float actualWidth = res.getScaledWidth() / 7f;  // Width of each card;  1/10 of total width
		float height = res.getScaledHeight() * 0.6f; // Height of each card; about 1/2 of total height
		float scaledWidth = 100;
		
		float scale = actualWidth / scaledWidth;
		
		float minX = (int) (index * (actualWidth + spacing)) + (float) scroll / res.getScaleFactor();
		float minY = (res.getScaledHeight() - height) / 2;
		float maxX = minX + actualWidth;
		float maxY = minY + height;
		float midX = (minX + maxX) / 2;
		float midY = (minY + maxY) / 2;
		
		float padding = 10;
		float leftX = minX + padding;
		float rightX = maxX - padding;
		float innerWidth = scaledWidth - 2 * padding;
		
		float iconY = 25;
		float iconSize = 180;
		
		float textMinX = 5;
		float textMaxX = 95;
		float textY = 123;
		
		float progressY = 120;
		float keybindingY = 180;
		float keybindingEditY = 200;
		
		// Draw card background
		pushMatrix();
			translate(minX, minY, 0);
			scale(actualWidth, height, 1);
			renderImage(AvatarUiTextures.skillsGui, 0, 0, 1, 1);
		popMatrix();
		
		pushMatrix();
			translate(minX, minY, 0);
			scale(scale, scale, 1);
			// Now is translated & scaled to size of 100px width (height is variable)
			
			// draw icon
			pushMatrix();
//				translate(padding, iconY, 0);
				translate((scaledWidth - iconSize) / 2 , -20, 0);
				scale(iconSize / 256, iconSize / 256, 1);
				renderImage(AvatarUiTextures.getAbilityTexture(ability), 0, 0, 256, 256);
			popMatrix();
			
			// draw progress bar
			pushMatrix();
				translate(10, progressY, 0);
				scale(2, 2, 1);
				renderImage(AvatarUiTextures.skillsGui, 0, 1, 40, 13);
				renderImage(AvatarUiTextures.skillsGui, 0, 14, (int) (data.getAbilityData(ability).getTotalXp() / scaledWidth * 40), 13);
			popMatrix();
			
			// draw keybinding
			pushMatrix();
				int color = conflict != null ? 0xff0000 : (editing ? 0xFF5962 : 0xffffff);
				
				String key;
				if (editing) {
					key = "editing";
				} else if (conflict != null) {
					key = "conflict";
				} else if (CLIENT_CONFIG.keymappings.get(ability) != null) {
					key = "set";
				} else {
					key = "none";
				}
				
				String boundTo = CLIENT_CONFIG.keymappings.get(ability) != null ? GameSettings.getKeyDisplayString(CLIENT_CONFIG.keymappings.get(ability)) : "no key";
				String conflictStr = conflict == null ? "no conflict" : I18n.format(conflict.getKeyDescription());
				String firstMsg = I18n.format("avatar.key." + key + "1", boundTo);
				String secondMsg = I18n.format("avatar.key." + key + "2", conflictStr);
				
				renderCenteredString(firstMsg, keybindingY, 1.5f, color);
				renderCenteredString(secondMsg, keybindingEditY, 1.25f, color);
			popMatrix();
			
		popMatrix();
		
		pushMatrix();
			
			String draw = ((int) data.getAbilityData(ability).getTotalXp()) + "%";
			
			translate(minX, minY, 0);
			scale(scale, scale, 1);
			
			renderCenteredString(draw, textY, 2.5f);
			renderCenteredString(I18n.format("avatar.ability." + ability.getName()), 10, 1.5f);
			
		popMatrix();
		
		return maxX;
		
	}
	// @formatter:on
	
	public boolean isEditing() {
		return editing;
	}
	
	public void setEditing(boolean editing) {
		this.editing = editing;
	}
	
	public void setConflict(KeyBinding conflict) {
		this.conflict = conflict;
	}
	
	/**
	 * Draws the image. Any transformations (e.g. transformation) should be
	 * performed with OpenGL functions.
	 * 
	 * @param texture
	 *            The texture to draw
	 * @param u
	 *            Leftmost U coordinate
	 * @param v
	 *            Uppermost V coordinate
	 * @param width
	 *            Width in pixels from texture
	 * @param height
	 *            Height in pixels from texture
	 */
	private void renderImage(ResourceLocation texture, int u, int v, int width, int height) {
		mc.renderEngine.bindTexture(texture);
		drawTexturedModalRect(0, 0, u, v, width, height);
	}
	
	/**
	 * Draws a centered string at the given y-position. Assumes that has already
	 * been transformed to the top-left corner of card (without padding), and
	 * the card is 100px wide. Padding is 10px.
	 * 
	 * @param str
	 *            String to draw
	 * @param y
	 *            Y position to draw at
	 * @param scale
	 *            Scale of text
	 * @param color
	 *            Color of the text
	 */
	private void renderCenteredString(String str, float y, float scale, int color) {
		pushMatrix();
		// assume padding is 10, innerWidth is 80
		translate(10 + (80 - mc.fontRendererObj.getStringWidth(str) * scale) / 2, y, 0);
		scale(scale, scale, 1);
		drawString(mc.fontRendererObj, str, 0, 0, color);
		popMatrix();
	}
	
	private void renderCenteredString(String str, float y, float scale) {
		renderCenteredString(str, y, scale, 0xffffff);
	}
	
}
