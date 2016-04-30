package com.mrnobody.morecommands.command.server;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.ServerCommandProperties;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.event.EventHandler;
import com.mrnobody.morecommands.event.Listeners.EventListener;
import com.mrnobody.morecommands.util.ObfuscatedNames.ObfuscatedField;
import com.mrnobody.morecommands.util.ObfuscatedNames.ObfuscatedMethod;
import com.mrnobody.morecommands.util.ReflectionHelper;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;
import com.mrnobody.morecommands.wrapper.Player;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;

@Command(
		name = "speed",
		description = "command.speed.description",
		example = "command.speed.example",
		syntax = "command.speed.syntax",
		videoURL = "command.speed.videoURL"
		)
public class CommandSpeed extends StandardCommand implements ServerCommandProperties, EventListener<EntityJoinWorldEvent> {
	private final float walkSpeedDefault = 0.1F;
	private final float flySpeedDefault = 0.05F;
	private final float railSpeedDefault = 0.4F;
	private final float minecartSpeedDefault = 1.2F;
	
	private float currentMinecartSpeed = this.minecartSpeedDefault;
	
	private final Field walkSpeed = ReflectionHelper.getField(ObfuscatedField.PlayerCapabilities_walkSpeed);
	private final Field flySpeed = ReflectionHelper.getField(ObfuscatedField.PlayerCapabilities_flySpeed);
	private final Field minecartSpeed = ReflectionHelper.getField(ObfuscatedField.EntityMinecart_currentSpeedRail);
	private final Method setRailSpeed = ReflectionHelper.getMethod(ObfuscatedMethod.BlockRailBase_setMaxRailSpeed);

	public CommandSpeed() {
		EventHandler.ENTITYJOIN.register(this);
	}
	
	@Override
	public void onEvent(EntityJoinWorldEvent event) {
		if (this.minecartSpeed != null && event.entity instanceof EntityMinecart)
			ReflectionHelper.set(ObfuscatedField.EntityMinecart_currentSpeedRail, this.minecartSpeed, (EntityMinecart) event.entity, this.currentMinecartSpeed);
	}
	
	@Override
	public String getName() {
		return "speed";
	}

	@Override
	public String getUsage() {
		return "command.speed.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		Player player = params.length > 0 && params[0].equalsIgnoreCase("minecart") ? null : new Player(getSenderAsEntity(sender.getMinecraftISender(), EntityPlayerMP.class));
		if (this.minecartSpeed == null || this.setRailSpeed == null || this.walkSpeed == null || this.flySpeed == null)
			throw new CommandException("command.speed.error", sender);
		
		if (params.length > 1) {
			if (params[0].equalsIgnoreCase("walk") || params[0].equalsIgnoreCase("fly") || params[0].equalsIgnoreCase("minecart")) {
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
						else if (params[0].equalsIgnoreCase("minecart")) {
							try {
								this.currentMinecartSpeed = speed / 10;
								this.setRailSpeed.invoke(null, speed / 10);
							}
							catch (Exception ex) {throw new CommandException("command.speed.error", sender);}
							sender.sendLangfileMessage("command.speed.minecartSet");
						}
						
						if (player != null)
							player.getMinecraftPlayer().sendPlayerAbilities();
					}
					else throw new CommandException("command.speed.noArg", sender);
				}
				else if (params[1].equalsIgnoreCase("get")) {
					if (params[0].equalsIgnoreCase("walk")) {sender.sendLangfileMessage("command.speed.getWalk", player.getMinecraftPlayer().capabilities.getWalkSpeed() * 10);}
					else if (params[0].equalsIgnoreCase("fly")) {sender.sendLangfileMessage("command.speed.getFly", player.getMinecraftPlayer().capabilities.getFlySpeed() * 10);}
					else if (params[0].equalsIgnoreCase("minecart")) {sender.sendLangfileMessage("command.speed.getMinecart", this.currentMinecartSpeed * 10);}
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
					else if (params[0].equalsIgnoreCase("minecart")) {
						try {
							this.currentMinecartSpeed = this.minecartSpeedDefault;
							this.setRailSpeed.invoke(null, this.railSpeedDefault);
						}
						catch (Exception ex) {throw new CommandException("command.speed.error", sender);}
						sender.sendLangfileMessage("command.speed.minecartReset");
					}
					
					if (player != null)
						player.getMinecraftPlayer().sendPlayerAbilities();
				}
				else throw new CommandException("command.generic.invalidUsage", sender, this.getName());
			}
			else throw new CommandException("command.generic.invalidUsage", sender, this.getName());
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
		return params.length > 0 && params[0].equalsIgnoreCase("minecart") ? true : isSenderOfEntityType(sender, EntityPlayerMP.class);
	}
}
