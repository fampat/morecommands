package com.mrnobody.morecommands.command.server;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import net.minecraft.block.BlockRailBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.PlayerCapabilities;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;

import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.handler.EventHandler;
import com.mrnobody.morecommands.handler.Listeners.EventListener;
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
public class CommandSpeed extends ServerCommand implements EventListener<EntityJoinWorldEvent> {
	private final float walkSpeedDefault = 0.1F;
	private final float flySpeedDefault = 0.05F;
	private final float railSpeedDefault = 0.4F;
	private final float minecartSpeedDefault = 1.2F;
	
	private float currentMinecartSpeed = minecartSpeedDefault;
	
	private final Field walkSpeed = ReflectionHelper.getField(PlayerCapabilities.class, "walkSpeed");
	private final Field flySpeed = ReflectionHelper.getField(PlayerCapabilities.class, "flySpeed");
	private final Field minecartSpeed = ReflectionHelper.getField(EntityMinecart.class, "currentSpeedRail");
	private final Method setRailSpeed = ReflectionHelper.getMethod(BlockRailBase.class, "setMaxRailSpeed", float.class);

	public CommandSpeed() {
		EventHandler.ENTITYJOIN.getHandler().register(this);
	}
	
	@Override
	public void onEvent(EntityJoinWorldEvent event) {
		if (event.entity instanceof EntityMinecart) {
			try {this.minecartSpeed.setFloat(event.entity, this.currentMinecartSpeed);}
			catch (Exception ex) {}
		}
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
		Player player = new Player((EntityPlayerMP) sender.getMinecraftISender());
		if (this.walkSpeed == null || this.flySpeed == null)
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
