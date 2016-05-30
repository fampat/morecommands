package com.mrnobody.morecommands.command.server;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.ServerCommandProperties;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.event.EventHandler;
import com.mrnobody.morecommands.event.Listeners.EventListener;
import com.mrnobody.morecommands.util.ServerPlayerSettings;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;
import com.mrnobody.morecommands.wrapper.Player;

import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraftforge.fml.common.gameevent.TickEvent;

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
	public String getUsage() {
		return "command.path.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
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
	}   
		
	@Override
	public void onEvent(TickEvent e) {
		if (e instanceof TickEvent.PlayerTickEvent && ((TickEvent.PlayerTickEvent) e).player instanceof EntityPlayerMP) {
			TickEvent.PlayerTickEvent event = (TickEvent.PlayerTickEvent) e;
			
			if (event.player instanceof EntityPlayerMP) {
				int[] plrData = getPlayerSettings((EntityPlayerMP) event.player).pathData;
				this.makePath(new Player((EntityPlayerMP) event.player), plrData);
			}
			else return;
		}
	}
	
	private void makePath(Player player, int[] data) {
		if (data[0] > 0) {
			BlockPos position = player.getPosition();
			int x = MathHelper.floor_double(position.getX());
			int y = MathHelper.floor_double(position.getY());
			int z = MathHelper.floor_double(position.getZ());
			if (x != data[3] || y != data[4] || z != data[5]) {
				int start = data[2] * -1 + 1;

				for (int i = start; i < data[2]; i++) {
					for (int j = -1; j < data[2]; j++) {
						for (int k = start; k < data[2]; k++) {
							if (j == -1) {
								this.setBlock(player, x + i, y + j, z + k, data[0], data[1]);
							} else {
								this.setBlock(player, x + i, y + j, z + k, 0, 0);
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
	
	private void setBlock(Player player, int i, int j, int k, int id, int meta) {
		if (meta > 0) player.getWorld().setBlockWithMeta(new BlockPos(i, j, k), Block.getBlockById(id), meta);
		else player.getWorld().setBlock(new BlockPos(i, j, k), Block.getBlockById(id));
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
