package com.mrnobody.morecommands.command.client;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import com.mrnobody.morecommands.command.ClientCommandProperties;
import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandException;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.CommandSender;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.util.Reference;

import net.minecraft.event.ClickEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

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
	public String execute(CommandSender sender, String[] params) throws CommandException {
		DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		
		IChatComponent textModid = new ChatComponentText("MODID:             ").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.DARK_AQUA));
		IChatComponent modid = new ChatComponentText(Reference.MODID).setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GREEN));
		
		IChatComponent textVersion = new ChatComponentText("VERSION:          ").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.DARK_AQUA));
		IChatComponent version = new ChatComponentText(Reference.VERSION).setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GREEN));
	
		IChatComponent textName = new ChatComponentText("NAME:              ").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.DARK_AQUA));
		IChatComponent name = new ChatComponentText(Reference.NAME).setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GREEN));
	
		IChatComponent textModDir = new ChatComponentText("MOD_DIR:          ").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.DARK_AQUA));
		IChatComponent modDir = new ChatComponentText(Reference.getModDir().getPath()).setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GREEN));
	
		IChatComponent textBuildDate = new ChatComponentText("BUILD_DATE:     ").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.DARK_AQUA));
		IChatComponent builDate = new ChatComponentText(df.format(Reference.BUILD)).setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GREEN));

		IChatComponent textWebsite = new ChatComponentText("WEBSITE         ").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.DARK_AQUA));
		IChatComponent website = new ChatComponentText(Reference.WEBSITE).setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GREEN).setUnderlined(true).setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, Reference.WEBSITE)));
		
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
	public boolean registerIfServerModded() {
		return false;
	}
	
	@Override
	public int getDefaultPermissionLevel(String[] args) {
		return 0;
	}
}
