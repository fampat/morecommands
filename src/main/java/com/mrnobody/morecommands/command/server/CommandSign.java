package com.mrnobody.morecommands.command.server;

import java.util.Arrays;

import net.minecraft.block.BlockStandingSign;
import net.minecraft.block.BlockWallSign;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.network.Packet;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;

import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;
import com.mrnobody.morecommands.wrapper.Player;

@Command(
		description = "command.sign.description",
		example = "command.sign.example",
		name = "sign",
		syntax = "command.sign.syntax",
		videoURL = "command.sign.videoURL"
		)
public class CommandSign extends ServerCommand {
	@Override
	public String getName() {
		return "sign";
	}

	@Override
	public String getUsage() {
		return "command.sign.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params)throws CommandException {
		Player player = new Player((EntityPlayerMP) sender.getMinecraftISender());
		MovingObjectPosition hit = player.rayTraceBlock(128.0D, 1.0F);

		if (hit != null && params.length > 1) {
			BlockPos trace = hit.getBlockPos();
			String[] givenLines = Arrays.copyOfRange(params, 1, params.length);
			String args = "";
			for (String param : givenLines) args += " " + param;
			args = args.trim();
			
			if (!args.startsWith("\"") && !args.endsWith("\""))
				throw new CommandException("command.sign.invalidUsage", sender);
			args = args.substring(1, args.length() - 1);
			
			String[] lines = args.split("\" \"");
			IChatComponent[] newLines = new IChatComponent[] {new ChatComponentText(""), new ChatComponentText(""), new ChatComponentText(""), new ChatComponentText("")};
			
			for (int i = 0; i < newLines.length && i < lines.length; i++) newLines[i] = new ChatComponentText(lines[i]);
			
			if (params[0].equalsIgnoreCase("edit") && player.getWorld().getTileEntity(trace) instanceof TileEntitySign) {
				TileEntitySign sign = (TileEntitySign) player.getWorld().getTileEntity(trace);
				
				sign.signText[0] = newLines[0];
				sign.signText[1] = newLines[1];
				sign.signText[2] = newLines[2];
				sign.signText[3] = newLines[3];
				Packet update = sign.getDescriptionPacket();
				((EntityPlayerMP) sender.getMinecraftISender()).playerNetServerHandler.sendPacket(update);
			
				sender.sendLangfileMessage("command.sign.editsuccess");
			}
			else if (params[0].equalsIgnoreCase("add")) {
				if (hit.sideHit == EnumFacing.DOWN) throw new CommandException("command.sign.bottom", sender);
				
				int x = trace.getX();
				int y = trace.getY();
				int z = trace.getZ();
				
				if (hit.sideHit == EnumFacing.UP) y += 1;
				else if (hit.sideHit == EnumFacing.EAST) x -= 1;
				else if (hit.sideHit == EnumFacing.WEST) x += 1;
				else if (hit.sideHit == EnumFacing.SOUTH) z += 1;
				else if (hit.sideHit == EnumFacing.NORTH) z -= 1;
				
				if (hit.sideHit == EnumFacing.UP) {
					int i = MathHelper.floor_double((double)((((EntityPlayerMP) player.getMinecraftPlayer()).rotationYaw + 180.0F) * 16.0F / 360.0F) + 0.5D) & 15;
					player.getWorld().getMinecraftWorld().setBlockState(new BlockPos(x, y, z), Blocks.standing_sign.getDefaultState().withProperty(BlockStandingSign.ROTATION, Integer.valueOf(i)), 3);
				}
				else {
					player.getWorld().getMinecraftWorld().setBlockState(new BlockPos(x, y, z), Blocks.wall_sign.getDefaultState().withProperty(BlockWallSign.FACING, hit.sideHit), 3);
				}
				
				if (player.getWorld().getTileEntity(x, y, z) instanceof TileEntitySign) {
					TileEntitySign sign = (TileEntitySign) player.getWorld().getTileEntity(x, y, z);
					
					sign.signText[0] = newLines[0];
					sign.signText[1] = newLines[1];
					sign.signText[2] = newLines[2];
					sign.signText[3] = newLines[3];
				
					sender.sendLangfileMessage("command.sign.addsuccess");
				}
			}
			else throw new CommandException("command.sign.invalidUsage", sender);
		}
		else throw new CommandException("command.sign.noBlockInSight", sender);
	}

	@Override
	public Requirement[] getRequirements() {
		return new Requirement[0];
	}

	@Override
	public ServerType getAllowedServerType() {
		return ServerType.ALL;
	}

	@Override
	public int getPermissionLevel() {
		return 2;
	}

	@Override
	public boolean canSenderUse(ICommandSender sender) {
		return sender instanceof EntityPlayerMP;
	}
}
