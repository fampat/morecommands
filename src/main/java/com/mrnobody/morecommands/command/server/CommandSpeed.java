package com.mrnobody.morecommands.command.server;

import java.lang.reflect.Field;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.PlayerCapabilities;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.util.ReflectionHelper;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;
import com.mrnobody.morecommands.wrapper.Player;

@Command(
		name = "speed",
		description = "command.speed.description",
		example = "command.speed.example",
		syntax = "command.speed.syntax",
		videoURL = "command.speed.videoURL"
		)
public class CommandSpeed extends ServerCommand {
	private final float walkSpeedDefault = 0.1F;
	private final float flySpeedDefault = 0.05F;
	
	private final Field walkSpeed = ReflectionHelper.getField(PlayerCapabilities.class, "walkSpeed");
	private final Field flySpeed = ReflectionHelper.getField(PlayerCapabilities.class, "flySpeed");

	@Override
	public String getCommandName() {
		return "speed";
	}

	@Override
	public String getUsage() {
		return "command.speed.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		Player player = new Player((EntityPlayerMP) sender.getMinecraftISender());
		if (this.walkSpeed == null || this.flySpeed == null)
			throw new CommandException("command.speed.error", sender);
		
		if (params.length > 1) {
			if (params[0].equalsIgnoreCase("walk") || params[0].equalsIgnoreCase("fly")) {
				if (params[1].equalsIgnoreCase("set")) {
					if (params.length > 2) {
						float speed;
						
						try {speed = Float.parseFloat(params[2]);}
						catch (NumberFormatException nfe) {throw new CommandException("command.speed.NAN", sender);}
						
						if (params[0].equalsIgnoreCase("walk")) {
							try {this.walkSpeed.setFloat(player.getMinecraftPlayer().capabilities, speed / 10);}
							catch (Exception ex) {throw new CommandException("command.speed.error", sender);}
							sender.sendLangfileMessage("command.speed.walkSet");
						}
						else if (params[0].equalsIgnoreCase("fly")) {
							try {this.flySpeed.setFloat(player.getMinecraftPlayer().capabilities, speed / 10);}
							catch (Exception ex) {throw new CommandException("command.speed.error", sender);}
							sender.sendLangfileMessage("command.speed.flySet");
						}
						
						player.getMinecraftPlayer().sendPlayerAbilities();
					}
					else throw new CommandException("command.speed.noArg", sender);
				}
				else if (params[1].equalsIgnoreCase("get")) {
					if (params[0].equalsIgnoreCase("walk")) {sender.sendLangfileMessage("command.speed.getWalk", player.getMinecraftPlayer().capabilities.getWalkSpeed() * 10);}
					else if (params[0].equalsIgnoreCase("fly")) {sender.sendLangfileMessage("command.speed.getFly", player.getMinecraftPlayer().capabilities.getFlySpeed() * 10);}
				}
				else if (params[1].equalsIgnoreCase("reset")) {
					if (params[0].equalsIgnoreCase("walk")) {
						try {this.walkSpeed.setFloat(player.getMinecraftPlayer().capabilities, this.walkSpeedDefault);}
						catch (Exception ex) {throw new CommandException("command.speed.error", sender);}
						sender.sendLangfileMessage("command.speed.walkReset");
					}
					else if (params[0].equalsIgnoreCase("fly")) {
						try {this.flySpeed.setFloat(player.getMinecraftPlayer().capabilities, this.flySpeedDefault);}
						catch (Exception ex) {throw new CommandException("command.speed.error", sender);}
						sender.sendLangfileMessage("command.speed.flyReset");
					}
					
					player.getMinecraftPlayer().sendPlayerAbilities();
				}
				else throw new CommandException("command.speed.invalidUsage", sender);
			}
			else throw new CommandException("command.speed.invalidUsage", sender);
		}
		else throw new CommandException("command.speed.invalidUsage", sender);
	}
	
	@Override
	public Requirement[] getRequirements() {
		return new Requirement[0];
	}
	
	@Override
	public void unregisterFromHandler() {}

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
