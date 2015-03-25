package com.mrnobody.morecommands.command.server;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.handler.EventHandler;
import com.mrnobody.morecommands.handler.Listener;
import com.mrnobody.morecommands.util.ServerPlayerSettings;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;
import com.mrnobody.morecommands.wrapper.Coordinate;
import com.mrnobody.morecommands.wrapper.Player;

import cpw.mods.fml.common.gameevent.TickEvent;

@Command(
		name = "path",
		description = "command.path.description",
		example = "command.path.example",
		syntax = "command.path.syntax",
		videoURL = "command.path.videoURL"
		)
public class CommandPath extends ServerCommand implements Listener<TickEvent> {
	public CommandPath() {EventHandler.TICK.getHandler().register(this);}

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
		EntityPlayer playerEntity = sender.toPlayer().getMinecraftPlayer();
		
		if (params.length > 0) {
			if (params[0].toLowerCase().startsWith("minecraft:")) params[0] = params[0].substring("minecraft:".length());
			
			String blockID = params[0].split(":")[0];
			String metaID = null;
			try {metaID = params[0].split(":")[1];} catch (ArrayIndexOutOfBoundsException e) {}
			Block block = null;
			int meta = -1;
			
			try {
				block = (Block) Block.blockRegistry.getObjectById(Integer.parseInt(blockID));
			}
			catch (NumberFormatException nfe) {
				if (Block.getBlockFromName("minecraft:" + blockID) != null) 
					block = Block.getBlockFromName("minecraft:" + blockID);
				else {
					sender.sendLangfileMessageToPlayer("command.path.unknownBlock", new Object[] {block});
					return;
				}
			}
			
			if (metaID != null) {
				try {meta = Integer.parseInt(metaID);} 
				catch (NumberFormatException nfe) {
					sender.sendLangfileMessageToPlayer("command.path.invalidMeta", new Object[0]);
					return;
				}
			}
			
			if (block == null || block instanceof BlockAir) {
				sender.sendLangfileMessageToPlayer("command.path.unknownBlock", new Object[] {block.getUnlocalizedName()});
				return;
			}
			
			int size = 1;
			
			if (params.length > 1)  {
				try {
					if (Integer.parseInt(params[1]) > 0 && Integer.parseInt(params[1]) <= 50) size = Integer.parseInt(params[1]);
					else sender.sendLangfileMessageToPlayer("command.path.invalidRadius", new Object[0]);}
				catch (NumberFormatException nfe) {
					sender.sendLangfileMessageToPlayer("command.path.invalidRadius", new Object[0]);
					return;
				}
			}
			
			if (ServerPlayerSettings.playerSettingsMapping.containsKey(playerEntity)) {
				ServerPlayerSettings settings = ServerPlayerSettings.playerSettingsMapping.get(playerEntity);
				int[] plrData = settings.pathData;
				if(plrData[0] == Block.getIdFromBlock(block) && plrData[1] == meta && plrData[2] == size) {
					sender.sendLangfileMessageToPlayer("command.path.noChange", new Object[0]);
					return;
				}
				sender.sendLangfileMessageToPlayer("command.path.enabled", new Object[0]);
				settings.pathData = new int[] {Block.getIdFromBlock(block), meta, size, -1, -1, -1};
			}
		} else if (ServerPlayerSettings.playerSettingsMapping.containsKey(playerEntity) && ServerPlayerSettings.playerSettingsMapping.get(playerEntity).pathData[0] > -1) {
			sender.sendLangfileMessageToPlayer("command.path.disabled", new Object[0]);
			ServerPlayerSettings.playerSettingsMapping.get(playerEntity).pathData[0] = -1;
		} else {
			sender.sendLangfileMessageToPlayer("command.path.invalidUsage", new Object[0]);
			return;
		}
	}   
		
	@Override
	public void onEvent(TickEvent e) {
		if (e instanceof TickEvent.PlayerTickEvent) {
			TickEvent.PlayerTickEvent event = (TickEvent.PlayerTickEvent) e;
			
			if (ServerPlayerSettings.playerSettingsMapping.containsKey(event.player)) {
				int[] plrData = ServerPlayerSettings.playerSettingsMapping.get(event.player).pathData;
				this.makePath(new Player(event.player), plrData);
			} else {
			return;
			}
		}
	}
	
	private void makePath(Player player, int[] data) {
		if (data[0] >= 0) {
			Coordinate position = player.getPosition();
			int x = MathHelper.floor_double(position.getX());
			int y = MathHelper.floor_double(position.getY());
			int z = MathHelper.floor_double(position.getZ());
			if (x != data[3] || y != data[4] || z != data[5]) {
				int start = data[2] * -1 + 1;

				for (int i = start; i < data[2]; i++) {
					for (int j = -1; j < data[2]; j++) {
						for (int k = start; k < data[2]; k++) {
							if (j == -1) {
								this.setBlock(player, x + i, y + j - 1, z + k, data[0], data[1]);
							} else {
								this.setBlock(player, x + i, y + j - 1, z + k, 0, 0);
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
		if (meta > 0) player.getWorld().setBlockWithMeta(new Coordinate(i, j, k), Block.getBlockById(id), meta);
		else player.getWorld().setBlock(new Coordinate(i, j, k), Block.getBlockById(id));
	}
	
	@Override
	public Requirement[] getRequirements() {
		return new Requirement[0];
	}
	
	@Override
	public void unregisterFromHandler() {
		EventHandler.TICK.getHandler().unregister(this);
	}

	@Override
	public ServerType getAllowedServerType() {
		return ServerType.ALL;
	}
	
	@Override
	public int getPermissionLevel() {
		return 2;
	}
}
