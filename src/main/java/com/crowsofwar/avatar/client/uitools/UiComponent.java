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

import static com.crowsofwar.avatar.client.uitools.ScreenInfo.scaleFactor;
import static net.minecraft.client.renderer.GlStateManager.*;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;

/**
 * Components are a part of a ui. They can be text, images, etc. They have a
 * manipulable {@link UiTransform transformation} that allows repositioning,
 * scaling, etc.
 * 
 * @author CrowsOfWar
 */
public abstract class UiComponent extends Gui {
	
	protected final Minecraft mc;
	private UiTransform transform;
	
	public UiComponent() {
		this.mc = Minecraft.getMinecraft();
		this.transform = new UiTransformBasic(this);
	}
	
	public UiTransform transform() {
		return transform;
	}
	
	public void setTransform(UiTransform transform) {
		this.transform = transform;
	}
	
	protected abstract float componentWidth();
	
	protected abstract float componentHeight();
	
	/**
	 * Get the actual width that is seen on-screen, in pixels
	 */
	public float width() {
		return componentWidth() * scale() * scaleFactor();
	}
	
	/**
	 * Get the actual height that is seen on-screen, in pixels
	 */
	public float height() {
		return componentHeight() * scale() * scaleFactor();
	}
	
	public void draw(float partialTicks) {
		
		transform.update(partialTicks);
		color(1, 1, 1, 1);
		
		//@formatter:off
		pushMatrix();
		
			GlStateManager.translate(coordinates().xInPixels() / scaleFactor(), coordinates().yInPixels() / scaleFactor(), 0);
			GlStateManager.scale(scale(), scale(), 1f); // unfortunately needed due to shadowing
			componentDraw(partialTicks);
			
		popMatrix();
		//@formatter:on
		
	}
	
	/**
	 * Actually draw the component. It is already translated and scaled to the
	 * correct position.
	 */
	protected abstract void componentDraw(float partialTicks);
	
	// Delegates to transform
	
	public Measurement coordinates() {
		return transform.coordinates();
	}
	
	public StartingPosition position() {
		return transform.position();
	}
	
	public void setPosition(StartingPosition position) {
		transform.setPosition(position);
	}
	
	public Measurement offset() {
		return transform.offset();
	}
	
	public void setOffset(Measurement offset) {
		transform.setOffset(offset);
	}
	
	public float offsetScale() {
		return transform.offsetScale();
	}
	
	public void setOffsetScale(float scale) {
		transform.setOffsetScale(scale);
	}
	
	public float scale() {
		return transform.scale();
	}
	
	public void setScale(float scale) {
		transform.setScale(scale);
	}
	
	public Frame getFrame() {
		return transform.getFrame();
	}
	
	public void setFrame(Frame frame) {
		transform.setFrame(frame);
	}
	
}
