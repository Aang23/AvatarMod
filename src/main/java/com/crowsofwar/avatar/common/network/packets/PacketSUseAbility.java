package com.crowsofwar.avatar.common.network.packets;

import com.crowsofwar.avatar.common.AvatarAbility;
import com.crowsofwar.avatar.common.controls.AvatarControl;
import com.crowsofwar.avatar.common.network.IAvatarPacket;
import com.crowsofwar.avatar.common.network.PacketRedirector;
import com.crowsofwar.avatar.common.util.AvBlockPos;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Packet which tells the server that the client pressed a control. The control is given to the
 * player's active bending controller.
 * 
 * @see AvatarControl
 *
 */
public class PacketSUseAbility implements IAvatarPacket<PacketSUseAbility> {
	
	private AvatarAbility ability;
	private AvBlockPos target;
	/** ID of EnumFacing of the side of the block player is looking at */
	private EnumFacing side;
	
	public PacketSUseAbility() {}
	
	public PacketSUseAbility(AvatarAbility ability, AvBlockPos target, EnumFacing side) {
		this.ability = ability;
		this.target = target;
		this.side = side;
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		ability = AvatarAbility.fromId(buf.readInt());
		target = buf.readBoolean() ? AvBlockPos.fromBytes(buf) : null;
		side = EnumFacing.getFront(buf.readInt());
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(ability.getId());
		buf.writeBoolean(target != null);
		if (target != null) {
			target.toBytes(buf);
		}
		buf.writeInt(side == null ? -1 : side.ordinal());
	}
	
	@Override
	public IMessage onMessage(PacketSUseAbility message, MessageContext ctx) {
		return PacketRedirector.redirectMessage(message, ctx);
	}
	
	@Override
	public Side getRecievedSide() {
		return Side.SERVER;
	}
	
	public AvatarAbility getAbility() {
		return ability;
	}
	
	public AvBlockPos getTargetPos() {
		return target;
	}
	
	public EnumFacing getSideHit() {
		return side;
	}
	
}
