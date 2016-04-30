package com.mrnobody.morecommands.command.server;

import java.util.Arrays;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.ServerCommandProperties;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;
import com.mrnobody.morecommands.wrapper.Player;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;

@Command(
		name = "give",
		description = "command.give.description",
		example = "command.give.example",
		syntax = "command.give.syntax",
		videoURL = "command.give.videoURL"
		)
public class CommandGive extends StandardCommand implements ServerCommandProperties {

	@Override
	public String getName() {
		return "give";
	}

	@Override
	public String getUsage() {
		return "command.give.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params)throws CommandException {
		if (params.length > 0) {
			Player player = new Player(getSenderAsEntity(sender.getMinecraftISender(), EntityPlayerMP.class));
			Item item = getItem(params[0]);
			
			if (item != null) {
				int meta = 0; NBTBase nbt = null; int quantity = 1;
				
				if (params.length > 1) {
					try {quantity = Integer.parseInt(params[1]);}
					catch(NumberFormatException e) {throw new CommandException("command.give.NAN", sender);}
				}
				
				if (params.length > 2) {
					try {meta = Integer.parseInt(params[2]);}
					catch(NumberFormatException e) {throw new CommandException("command.give.NAN", sender);}
					if (!item.getHasSubtypes() && meta != 0) throw new CommandException("command.give.noMeta", sender);
				}
				
				if (params.length > 3) {
					String nbtString = rejoinParams(Arrays.copyOfRange(params, 3, params.length));
					nbt = getNBTFromParam(params[3], sender.getMinecraftISender());
					if (!(nbt instanceof NBTTagCompound)) throw new CommandException("command.give.noCompound", sender);
				}
				
				player.givePlayerItem(item, quantity, meta, (NBTTagCompound) nbt);
			}
			else throw new CommandException("command.give.notFound", sender);
		}
		else throw new CommandException("command.generic.invalidUsage", sender, this.getName());
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
