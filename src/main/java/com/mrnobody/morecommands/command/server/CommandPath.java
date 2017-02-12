package com.mrnobody.morecommands.command.server;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandException;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.CommandSender;
import com.mrnobody.morecommands.command.ServerCommandProperties;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.event.EventHandler;
import com.mrnobody.morecommands.event.Listeners.EventListener;
import com.mrnobody.morecommands.settings.ServerPlayerSettings;
import com.mrnobody.morecommands.util.Coordinate;
import com.mrnobody.morecommands.util.EntityUtils;
import com.mrnobody.morecommands.util.WorldUtils;

import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

@Command(
		name = "path",
		description = "command.path.description",
		example = "command.path.example",
		syntax = "command.path.syntax",
		videoURL = "command.path.videoURL"
		)
public class CommandPath extends StandardCommand implements ServerCommandProperties, EventListener<TickEvent> {
	public CommandPath() {EventHandler.TICK.register(this);}

	@Override
	public String getCommandName() {
		return "path";
	}

	@Override
	public String getCommandUsage() {
		return "command.path.syntax";
	}

	@Override
	public String execute(CommandSender sender, String[] params) throws CommandException {
		EntityPlayerMP playerEntity = getSenderAsEntity(sender.getMinecraftISender(), EntityPlayerMP.class);
		
		if (params.length > 0) {
			Block block = getBlock(params[0]);
			int meta = -1;
			
			if (block == null)
				throw new CommandException("command.path.unknownBlock", sender, params[0]);
			
			if (params.length > 1) {
				try {meta = Integer.parseInt(params[1]);} 
				catch (NumberFormatException nfe) {throw new CommandException("command.path.invalidMeta", sender);}
			}
			
			if (block == null || block instanceof BlockAir)
				throw new CommandException("command.path.unknownBlock", sender, params[0]);
			
			int size = 1;
			
			if (params.length > 2)  {
				try {
					size = Integer.parseInt(params[2]);
					if (size <= 0 || size > 50) throw new CommandException("command.path.invalidRadius", sender);
				} catch (NumberFormatException nfe) {throw new CommandException("command.path.invalidRadius", sender);}
			}
			
			ServerPlayerSettings settings = getPlayerSettings(playerEntity);
			int[] plrData = settings.pathData;
			if(plrData[0] == Block.getIdFromBlock(block) && plrData[1] == meta && plrData[2] == size)
				throw new CommandException("command.path.noChange", sender);
			sender.sendLangfileMessage("command.path.enabled");
			settings.pathData = new int[] {Block.getIdFromBlock(block), meta, size, -1, -1, -1};
		} else if (getPlayerSettings(playerEntity).pathData[0] > -1) {
			sender.sendLangfileMessage("command.path.disabled");
			getPlayerSettings(playerEntity).pathData[0] = -1;
		} else throw new CommandException("command.generic.invalidUsage", sender, this.getCommandName());
		
		return null;
	}   
		
	@Override
	public void onEvent(TickEvent e) {
		if (e instanceof TickEvent.PlayerTickEvent && ((TickEvent.PlayerTickEvent) e).player instanceof EntityPlayerMP) {
			TickEvent.PlayerTickEvent event = (TickEvent.PlayerTickEvent) e;
			
			if (event.player instanceof EntityPlayerMP) {
				int[] plrData = getPlayerSettings((EntityPlayerMP) event.player).pathData;
				this.makePath((EntityPlayerMP) event.player, plrData);
			}
			else return;
		}
	}
	
	private void makePath(EntityPlayerMP player, int[] data) {
		if (data[0] > 0) {
			Coordinate position = EntityUtils.getPosition(player);
			int x = MathHelper.floor_double(position.getX());
			int y = MathHelper.floor_double(position.getY());
			int z = MathHelper.floor_double(position.getZ());
			if (x != data[3] || y != data[4] || z != data[5]) {
				int start = data[2] * -1 + 1;

				for (int i = start; i < data[2]; i++) {
					for (int j = -1; j < data[2]; j++) {
						for (int k = start; k < data[2]; k++) {
							if (j == -1) {
								this.setBlock(player.worldObj, x + i, y + j, z + k, data[0], data[1]);
							} else {
								this.setBlock(player.worldObj, x + i, y + j, z + k, 0, 0);
							}
						}
					}
				}

				data[3] = x;
				data[4] = y;
				data[5] = z;
			}
		}
	}
	
	private void setBlock(World world, int i, int j, int k, int id, int meta) {
		if (meta > 0) WorldUtils.setBlockWithMeta(world, new Coordinate(i, j, k), Block.getBlockById(id), meta);
		else WorldUtils.setBlock(world, new Coordinate(i, j, k), Block.getBlockById(id));
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
	public int getDefaultPermissionLevel(String[] args) {
		return 2;
	}
	
	@Override
	public boolean canSenderUse(String commandName, ICommandSender sender, String[] params) {
		return isSenderOfEntityType(sender, EntityPlayerMP.class);
	}
}
