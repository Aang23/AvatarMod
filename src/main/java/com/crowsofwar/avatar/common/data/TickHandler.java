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

import com.crowsofwar.avatar.common.data.ctx.BendingContext;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.FMLLog;

import java.util.HashMap;
import java.util.Map;

/**
 * @author CrowsOfWar
 */
public abstract class TickHandler {
	private final int id;
	//static Map<Integer, TickHandler> allHandlers = new HashMap<>();

	public TickHandler(int id) {
		this.id = id;
		TickHandlerController.allHandlers.put(id, this);

	}

	/**
	 * Ticks and returns whether to remove (false to stay)
	 */
	public abstract boolean tick(BendingContext ctx);

	public int id() {
		return id;
	}

	public void toBytes(ByteBuf buf) {
		buf.writeInt(id);
	}
/*
	public static TickHandler fromId(int id) {
		FMLLog.info("allHandlers = %s", allHandlers);
		return allHandlers.get(id);
	}**/

	/*public static TickHandler fromBytes(ByteBuf buf) {
		return fromId(buf.readInt());
	}**/

}
