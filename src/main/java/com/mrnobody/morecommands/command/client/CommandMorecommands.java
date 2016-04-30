package com.mrnobody.morecommands.command.client;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import com.mrnobody.morecommands.command.ClientCommandProperties;
import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.util.Reference;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

@Command(
		name = "morecommands",
		description = "command.morecommands.description",
		example = "command.morecommands.example",
		syntax = "command.morecommands.syntax",
		videoURL = "command.morecommands.videoURL"
		)
public class CommandMorecommands extends StandardCommand implements ClientCommandProperties {

	@Override
	public String getCommandName() {
		return "morecommands";
	}

	@Override
	public String getCommandUsage() {
		return "command.morecommands.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		
		TextComponentString textModid = new TextComponentString("MODID:             "); textModid.setChatStyle(new Style().setColor(TextFormatting.DARK_AQUA));
		TextComponentString modid = new TextComponentString(Reference.MODID); modid.setChatStyle(new Style().setColor(TextFormatting.GRAY));
		
		TextComponentString textVersion = new TextComponentString("VERSION:          "); textVersion.setChatStyle(new Style().setColor(TextFormatting.DARK_AQUA));
		TextComponentString version = new TextComponentString(Reference.VERSION); version.setChatStyle(new Style().setColor(TextFormatting.GRAY));
	
		TextComponentString textName = new TextComponentString("NAME:              "); textName.setChatStyle(new Style().setColor(TextFormatting.DARK_AQUA));
		TextComponentString name = new TextComponentString(Reference.NAME); name.setChatStyle(new Style().setColor(TextFormatting.GRAY));
	
		TextComponentString textModDir = new TextComponentString("MOD_DIR:          "); textModDir.setChatStyle(new Style().setColor(TextFormatting.DARK_AQUA));
		TextComponentString modDir = new TextComponentString(Reference.getModDir().getPath()); modDir.setChatStyle(new Style().setColor(TextFormatting.GRAY));
	
		TextComponentString textBuildDate = new TextComponentString("BUILD_DATE:     "); textBuildDate.setChatStyle(new Style().setColor(TextFormatting.DARK_AQUA));
		TextComponentString builDate = new TextComponentString(df.format(Reference.BUILD)); builDate.setChatStyle(new Style().setColor(TextFormatting.GRAY));
	
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
	public boolean registerIfServerModded() {
		return false;
	}
	
	@Override
	public int getDefaultPermissionLevel() {
		return 0;
	}
}
