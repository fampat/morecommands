package com.mrnobody.morecommands.command.server;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.ServerCommandProperties;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.util.Reference;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;

@Command(
		name = "morecommands",
		description = "command.morecommands.description",
		example = "command.morecommands.example",
		syntax = "command.morecommands.syntax",
		videoURL = "command.morecommands.videoURL"
		)
public class CommandMorecommands extends StandardCommand implements ServerCommandProperties {

	@Override
	public String getName() {
		return "morecommands";
	}

	@Override
	public String getUsage() {
		return "command.morecommands.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		
		ChatComponentText textModid = new ChatComponentText("MODID:             "); textModid.setChatStyle(new ChatStyle().setColor(EnumChatFormatting.DARK_AQUA));
		ChatComponentText modid = new ChatComponentText(Reference.MODID); modid.setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GRAY));
		
		ChatComponentText textVersion = new ChatComponentText("VERSION:          "); textVersion.setChatStyle(new ChatStyle().setColor(EnumChatFormatting.DARK_AQUA));
		ChatComponentText version = new ChatComponentText(Reference.VERSION); version.setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GRAY));
	
		ChatComponentText textName = new ChatComponentText("NAME:              "); textName.setChatStyle(new ChatStyle().setColor(EnumChatFormatting.DARK_AQUA));
		ChatComponentText name = new ChatComponentText(Reference.NAME); name.setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GRAY));
	
		ChatComponentText textModDir = new ChatComponentText("MOD_DIR:          "); textModDir.setChatStyle(new ChatStyle().setColor(EnumChatFormatting.DARK_AQUA));
		ChatComponentText modDir = new ChatComponentText(Reference.getModDir().getPath()); modDir.setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GRAY));
	
		ChatComponentText textBuildDate = new ChatComponentText("BUILD_DATE:     "); textBuildDate.setChatStyle(new ChatStyle().setColor(EnumChatFormatting.DARK_AQUA));
		ChatComponentText builDate = new ChatComponentText(df.format(Reference.BUILD)); builDate.setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GRAY));
	
		sender.sendChatComponent(textModid.appendSibling(modid));
		sender.sendChatComponent(textVersion.appendSibling(version));
		sender.sendChatComponent(textName.appendSibling(name));
		sender.sendChatComponent(textModDir.appendSibling(modDir));
		sender.sendChatComponent(textBuildDate.appendSibling(builDate));
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
		return 0;
	}
	
	@Override
	public boolean canSenderUse(String commandName, ICommandSender sender, String[] params) {
		return true;
	}
}
