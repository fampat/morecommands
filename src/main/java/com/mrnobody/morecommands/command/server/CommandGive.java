package com.mrnobody.morecommands.command.server;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;
import com.mrnobody.morecommands.wrapper.Player;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;

@Command(
		name = "give",
		description = "command.give.description",
		example = "command.give.example",
		syntax = "command.give.syntax",
		videoURL = "command.give.videoURL"
		)
public class CommandGive extends ServerCommand {

	@Override
	public String getCommandName() {
		return "give";
	}

	@Override
	public String getUsage() {
		return "command.give.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params)throws CommandException {
		if (params.length > 0) {
			Player player = new Player((EntityPlayerMP) sender.getMinecraftISender());
			
			String modid = params[0].split(":").length > 1 ? params[0].split(":")[0] : "minecraft";
			String name = params[0].split(":").length > 1 ? params[0].split(":")[1] : params[0];
			Item item = GameRegistry.findItem(modid, name);
			
			if (item == null) {
				try {item = Item.getItemById(Integer.parseInt(params[0]));}
				catch (NumberFormatException e) {}
			}
			
			if (item != null) {
				if (params.length > 1) {
					if (params.length > 2) {
						if (item.getHasSubtypes()) {
							try {player.givePlayerItem(item, Integer.parseInt(params[1]), Integer.parseInt(params[2]));}
							catch(NumberFormatException e) {throw new CommandException("command.give.notFound", sender);}
						}
						else throw new CommandException("command.give.noMeta", sender);
					}
					else {
						try {player.givePlayerItem(item, Integer.parseInt(params[1])); sender.sendLangfileMessage("command.give.success");}
						catch (NumberFormatException e) {throw new CommandException("command.give.notFound", sender);}
					}
				}
				else {player.givePlayerItem(item); sender.sendLangfileMessage("command.give.success");}
			}
			else throw new CommandException("command.give.notFound", sender);
		}
		else throw new CommandException("command.give.invalidUsage", sender);
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
