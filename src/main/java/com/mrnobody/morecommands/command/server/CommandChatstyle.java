package com.mrnobody.morecommands.command.server;

import java.util.Arrays;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandException;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.CommandSender;
import com.mrnobody.morecommands.command.MultipleCommands;
import com.mrnobody.morecommands.command.ServerCommandProperties;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.event.EventHandler;
import com.mrnobody.morecommands.event.Listeners.EventListener;
import com.mrnobody.morecommands.settings.ServerPlayerSettings;
import com.mrnobody.morecommands.util.ObfuscatedNames.ObfuscatedField;
import com.mrnobody.morecommands.util.ReflectionHelper;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.event.ServerChatEvent;

@Command.MultipleCommand(
		name = {"chatstyle", "chatstyle_global"},
		description = {"command.chatstyle.description", "command.chatstyle.global.description"},
		example = {"command.chatstyle.example", "command.chatstyle.global.example"},
		syntax = {"command.chatstyle.syntax", "command.chatstyle.global.syntax"},
		videoURL = {"command.chatstyle.videoURL", "command.chatstyle.global.videoURL"}
		)
public class CommandChatstyle extends MultipleCommands implements ServerCommandProperties, EventListener<ServerChatEvent> {
	public CommandChatstyle() {
		super();
		EventHandler.SERVER_CHAT.register(this, true);
	}
	
	public CommandChatstyle(int typeIndex) {
		super(typeIndex);
	}
	
	@Override
	public String[] getCommandNames() {
		return new String[] {"chatstyle", "chatstyle_global"};
	}

	@Override
	public String[] getCommandUsages() {
		return new String[] {"command.chatstyle.syntax", "command.chatstyle.global.syntax"};
	}
	
	@Override
	public void onEvent(ServerChatEvent event) {
		ChatComponentTranslation component = (ChatComponentTranslation) event.getComponent();
		
		if (!component.getKey().equals("chat.type.text") || component.getFormatArgs().length != 2 ||
			!(component.getFormatArgs()[0] instanceof IChatComponent) ||
			!(component.getFormatArgs()[1] instanceof IChatComponent)) return;
		
		ServerPlayerSettings settings = getPlayerSettings(event.player);
		
		if (settings.nameStyle != null) 
			((IChatComponent) component.getFormatArgs()[0]).getChatStyle().setParentStyle(settings.nameStyle);
		
		if (settings.textStyle != null) 
			((IChatComponent) component.getFormatArgs()[1]).getChatStyle().setParentStyle(settings.textStyle);
	}
	
	@Override
	public String execute(String commandName, CommandSender sender, String[] params) throws CommandException {
		boolean global = commandName.endsWith("global");
		
		if (params.length > (global ? 0 : 1)) {
			String type = global ? null : params[0];
			if (!global) params = Arrays.copyOfRange(params, 1, params.length);
			ChatStyle style = new ChatStyle();
			
			if (params[0].equalsIgnoreCase("reset")) style = null;
			else {
				for (String formatting : params) {
					String[] split = formatting.split(":");
					if (split.length != 2) throw new CommandException("command.chatstyle.invalidArg", sender, formatting);
					
					if (formatting.startsWith("color")) {
						EnumChatFormatting color = EnumChatFormatting.getValueByName(split[1]);
						if (color == null || !color.isColor()) throw new CommandException("command.chatstyle.noColor", sender, split[1]);
						style.setColor(color);
					}
					else if (split[0].equalsIgnoreCase("bold")) style.setBold(split[1].equalsIgnoreCase("true") || split[1].equals("1"));
					else if (split[0].equalsIgnoreCase("italic")) style.setItalic(split[1].equalsIgnoreCase("true") || split[1].equals("1"));
					else if (split[0].equalsIgnoreCase("underlined")) style.setUnderlined(split[1].equalsIgnoreCase("true") || split[1].equals("1"));
					else if (split[0].equalsIgnoreCase("strikethrough")) style.setStrikethrough(split[1].equalsIgnoreCase("true") || split[1].equals("1"));
					else if (split[0].equalsIgnoreCase("obfuscated")) style.setObfuscated(split[1].equalsIgnoreCase("true") || split[1].equals("1"));
					else throw new CommandException("command.chatstyle.invalidArg", sender, formatting);
				}
			}
			
			if (global) ReflectionHelper.set(ObfuscatedField.ChatStyle_defaultChatStyle, null, style);
			else {
				ServerPlayerSettings settings = getPlayerSettings(getSenderAsEntity(sender.getMinecraftISender(), EntityPlayerMP.class));
				
				if (type.equals("name")) settings.nameStyle = style;
				else if (type.equals("text")) settings.textStyle = style;
				else if (type.equals("both")) settings.nameStyle = settings.textStyle = style;
			}
		}
		
		return null;
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
		return commandName.endsWith("global") ? true : isSenderOfEntityType(sender, EntityPlayerMP.class);
	}
}