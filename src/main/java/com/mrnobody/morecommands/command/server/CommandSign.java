package com.mrnobody.morecommands.command.server;

import java.util.Arrays;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.ServerCommandProperties;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;
import com.mrnobody.morecommands.wrapper.Coordinate;
import com.mrnobody.morecommands.wrapper.Player;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;

@Command(
		description = "command.sign.description",
		example = "command.sign.example",
		name = "sign",
		syntax = "command.sign.syntax",
		videoURL = "command.sign.videoURL"
		)
public class CommandSign extends StandardCommand implements ServerCommandProperties {
	@Override
	public String getCommandName() {
		return "sign";
	}

	@Override
	public String getUsage() {
		return "command.sign.syntax";
	}
	
	@Override
	public void execute(CommandSender sender, String[] params)throws CommandException {
		Player player = new Player(getSenderAsEntity(sender.getMinecraftISender(), EntityPlayerMP.class));
		MovingObjectPosition hit = player.rayTraceBlock(128.0D, 1.0F);
		
		if (hit != null && params.length > 1) {
			Coordinate trace = new Coordinate(hit.blockX, hit.blockY, hit.blockZ);
			
			String[] lines = reparseParamsWithNBTData(Arrays.copyOfRange(params, 1, params.length));
			String[] newLines = new String[] {"", "", "", ""};
			
			for (int i = 0; i < newLines.length && i < lines.length; i++)
				newLines[i] = isNBTParam(lines[i]) ? lines[i].substring(1, lines[i].length() - 1) : lines[i];
			
			if (params[0].equalsIgnoreCase("edit") && player.getWorld().getTileEntity(trace) instanceof TileEntitySign) {
				TileEntitySign sign = (TileEntitySign) player.getWorld().getTileEntity(trace);
				
				sign.signText[0] = newLines[0];
				sign.signText[1] = newLines[1];
				sign.signText[2] = newLines[2];
				sign.signText[3] = newLines[3];
				MinecraftServer.getServer().getConfigurationManager().sendPacketToAllPlayers(sign.getDescriptionPacket());
				
				sender.sendLangfileMessage("command.sign.editsuccess");
			}
			else if (params[0].equalsIgnoreCase("add")) {
				if (hit.sideHit == 0) throw new CommandException("command.sign.bottom", sender);
				
				if (hit.sideHit == 1) hit.blockY += 1;
				else if (hit.sideHit == 4) hit.blockX -= 1;
				else if (hit.sideHit == 5) hit.blockX += 1;
				else if (hit.sideHit == 3) hit.blockZ += 1;
				else if (hit.sideHit == 2) hit.blockZ -= 1;
				
				if (hit.sideHit == 1) {
					int i1 = MathHelper.floor_double((double) ((player.getYaw() + 180.0F) * 16.0F / 360.0F) + 0.5D) & 15;
					player.getWorld().getMinecraftWorld().setBlock(hit.blockX, hit.blockY, hit.blockZ, Blocks.standing_sign, i1, 3);
				}
                else player.getWorld().getMinecraftWorld().setBlock(hit.blockX, hit.blockY, hit.blockZ, Blocks.wall_sign, hit.sideHit, 3);
				
				if (player.getWorld().getTileEntity(hit.blockX, hit.blockY, hit.blockZ) instanceof TileEntitySign) {
					TileEntitySign sign = (TileEntitySign) player.getWorld().getTileEntity(hit.blockX, hit.blockY, hit.blockZ);
					
					sign.signText[0] = newLines[0];
					sign.signText[1] = newLines[1];
					sign.signText[2] = newLines[2];
					sign.signText[3] = newLines[3];
				
					sender.sendLangfileMessage("command.sign.addsuccess");
				}
			}
			else throw new CommandException("command.generic.invalidUsage", sender, this.getCommandName());
		}
		else throw new CommandException("command.sign.noBlockInSight", sender);
	}

	@Override
	public CommandRequirement[] getRequirements() {
		return new CommandRequirement[0];
	}

	@Override
	public ServerType getAllowedServerType() {
		return ServerType.ALL;
	}

	@Override
	public int getDefaultPermissionLevel() {
		return 2;
	}

	@Override
	public boolean canSenderUse(String commandName, ICommandSender sender, String[] params) {
		return isSenderOfEntityType(sender, EntityPlayerMP.class);
	}
}
