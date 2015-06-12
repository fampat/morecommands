package com.mrnobody.morecommands.command.server;

import java.util.Arrays;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.network.Packet;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;
import com.mrnobody.morecommands.wrapper.Coordinate;
import com.mrnobody.morecommands.wrapper.Entity;
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
	public void unregisterFromHandler() {}

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
		Player player = new Player((EntityPlayerMP) sender.getMinecraftISender());
		MovingObjectPosition hit = player.rayTraceBlock(128.0D, 1.0F);

		if (hit != null && params.length > 1) {
			Coordinate trace = new Coordinate(hit.blockX, hit.blockY, hit.blockZ);
			String[] givenLines = Arrays.copyOfRange(params, 1, params.length);
			String args = "";
			for (String param : givenLines) args += " " + param;
			args = args.trim();
			
			if (!args.startsWith("\"") && !args.endsWith("\""))
				{sender.sendLangfileMessage("command.sign.invalidUsage", new Object[0]); return;}
			args = args.substring(1, args.length() - 1);
			
			String[] lines = args.split("\" \"");
			String[] newLines = new String[] {"", "", "", ""};
			
			for (int i = 0; i < newLines.length && i < lines.length; i++) newLines[i] = lines[i];
			
			if (params[0].equalsIgnoreCase("edit") && player.getWorld().getTileEntity(trace) instanceof TileEntitySign) {
				TileEntitySign sign = (TileEntitySign) player.getWorld().getTileEntity(trace);
				
				sign.signText[0] = newLines[0];
				sign.signText[1] = newLines[1];
				sign.signText[2] = newLines[2];
				sign.signText[3] = newLines[3];
				Packet update = sign.getDescriptionPacket();
				((EntityPlayerMP) sender.getMinecraftISender()).playerNetServerHandler.sendPacket(update);
			
				sender.sendLangfileMessage("command.sign.editsuccess", new Object[0]);
			}
			else if (params[0].equalsIgnoreCase("add")) {
				if (hit.sideHit == 0) {sender.sendLangfileMessage("command.sign.bottom", new Object[0]); return;}
				
				if (hit.sideHit == 1) hit.blockY += 1;
				else if (hit.sideHit == 4) hit.blockX -= 1;
				else if (hit.sideHit == 5) hit.blockX += 1;
				else if (hit.sideHit == 3) hit.blockZ += 1;
				else if (hit.sideHit == 2) hit.blockZ -= 1;
				
				if (hit.sideHit == 1) {
					int i1 = MathHelper.floor_double((double)((((EntityPlayerMP) player.getMinecraftPlayer()).rotationYaw + 180.0F) * 16.0F / 360.0F) + 0.5D) & 15;
					player.getWorld().getMinecraftWorld().setBlock(hit.blockX, hit.blockY, hit.blockZ, Blocks.standing_sign, i1, 3);
				}
                else player.getWorld().getMinecraftWorld().setBlock(hit.blockX, hit.blockY, hit.blockZ, Blocks.wall_sign, hit.sideHit, 3);
				
				if (player.getWorld().getTileEntity(hit.blockX, hit.blockY, hit.blockZ) instanceof TileEntitySign) {
					TileEntitySign sign = (TileEntitySign) player.getWorld().getTileEntity(hit.blockX, hit.blockY, hit.blockZ);
					
					sign.signText[0] = newLines[0];
					sign.signText[1] = newLines[1];
					sign.signText[2] = newLines[2];
					sign.signText[3] = newLines[3];
				
					sender.sendLangfileMessage("command.sign.addsuccess", new Object[0]);
				}
			}
			else sender.sendLangfileMessage("command.sign.invalidUsage", new Object[0]);
		}
		else sender.sendLangfileMessage("command.sign.noBlockInSight", new Object[0]);
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
