package com.mrnobody.morecommands.command.server;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandException;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.CommandSender;
import com.mrnobody.morecommands.command.ServerCommandProperties;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.util.Reference;

import net.minecraft.command.ICommandSender;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;

@Command(
		name = "morecommands",
		description = "command.morecommands.description",
		example = "command.morecommands.example",
		syntax = "command.morecommands.syntax",
		videoURL = "command.morecommands.videoURL"
		)
public class CommandMorecommands extends StandardCommand implements ServerCommandProperties {

	@Override
	public String getCommandName() {
		return "morecommands";
	}

	@Override
	public String getCommandUsage() {
		return "command.morecommands.syntax";
	}

	@Override
	public String execute(CommandSender sender, String[] params) throws CommandException {
		DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		
		ITextComponent textModid = new TextComponentString("MODID:             ").setChatStyle(new Style().setColor(TextFormatting.DARK_AQUA));
		ITextComponent modid = new TextComponentString(Reference.MODID).setChatStyle(new Style().setColor(TextFormatting.GREEN));
		
		ITextComponent textVersion = new TextComponentString("VERSION:          ").setChatStyle(new Style().setColor(TextFormatting.DARK_AQUA));
		ITextComponent version = new TextComponentString(Reference.VERSION).setChatStyle(new Style().setColor(TextFormatting.GREEN));
	
		ITextComponent textName = new TextComponentString("NAME:              ").setChatStyle(new Style().setColor(TextFormatting.DARK_AQUA));
		ITextComponent name = new TextComponentString(Reference.NAME).setChatStyle(new Style().setColor(TextFormatting.GREEN));
	
		ITextComponent textModDir = new TextComponentString("MOD_DIR:          ").setChatStyle(new Style().setColor(TextFormatting.DARK_AQUA));
		ITextComponent modDir = new TextComponentString(Reference.getModDir().getPath()).setChatStyle(new Style().setColor(TextFormatting.GREEN));
	
		ITextComponent textBuildDate = new TextComponentString("BUILD_DATE:     ").setChatStyle(new Style().setColor(TextFormatting.DARK_AQUA));
		ITextComponent builDate = new TextComponentString(df.format(Reference.BUILD)).setChatStyle(new Style().setColor(TextFormatting.GREEN));

		ITextComponent textWebsite = new TextComponentString("WEBSITE         ").setChatStyle(new Style().setColor(TextFormatting.DARK_AQUA));
		ITextComponent website = new TextComponentString(Reference.WEBSITE).setChatStyle(new Style().setColor(TextFormatting.GREEN).setUnderlined(true).setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, Reference.WEBSITE)));
		
		sender.sendChatComponent(textModid.appendSibling(modid));
		sender.sendChatComponent(textVersion.appendSibling(version));
		sender.sendChatComponent(textName.appendSibling(name));
		sender.sendChatComponent(textModDir.appendSibling(modDir));
		sender.sendChatComponent(textBuildDate.appendSibling(builDate));
		sender.sendChatComponent(textWebsite.appendSibling(website));
		
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
		return 0;
	}
	
	@Override
	public boolean canSenderUse(String commandName, ICommandSender sender, String[] params) {
		return true;
	}
}
