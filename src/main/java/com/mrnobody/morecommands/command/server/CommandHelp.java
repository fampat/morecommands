package com.mrnobody.morecommands.command.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.event.ClickEvent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.util.DummyCommand;
import com.mrnobody.morecommands.util.LanguageManager;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

@Command(
		name = "help",
		description = "command.helpSideServer.description",
		example = "command.helpSideServer.example",
		syntax = "command.helpSideServer.syntax",
		videoURL = "command.helpSideServer.videoURL"
		)
public class CommandHelp extends ServerCommand {
	private IChatComponent MESSAGE_PAGEHEADING;
	private IChatComponent MESSAGE_COMMANDHEADING;
	private IChatComponent MESSAGE_FOOTER;
	private IChatComponent MESSAGE_NAME;
	private IChatComponent MESSAGE_DESCRIPTION;
	private IChatComponent MESSAGE_SYNTAX;
	private IChatComponent MESSAGE_EXAMPLE;
	private IChatComponent MESSAGE_VIDEO;
	private IChatComponent MESSAGE_INFO;
	
	@Override
	public String getCommandName() {
		return "help";
	}

	@Override
	public String getUsage() {
		return "command.helpSideServer.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		String langCode = "en_US";
		langCode = MoreCommands.getProxy().getLang(sender.getMinecraftISender());
		this.resetMessages(langCode);
		
		Map<String, ICommand> commands = MinecraftServer.getServer().getCommandManager().getCommands();
		String show = "generalhelp";
		
		List<String> names = new ArrayList<String>(commands.keySet());
		List<String> remove = new ArrayList<String>();
		
		for (String name : names) {
			if (MinecraftServer.getServer().getCommandManager().getCommands().get(name) instanceof DummyCommand) remove.add(name);
		}
		for (String rem : remove) names.remove(rem);
		
		Collections.sort(names);
			
		byte maxEntries = 7;
		int totalPages = (names.size() - 1) / maxEntries;
		int page = 0;
			
		try {
			page = params.length == 0 ? 0 : this.parseIntBounded(sender.getMinecraftISender(), params[0], 1, totalPages + 1) - 1;
		}
		catch (NumberInvalidException numberinvalidexception){
			if (!names.contains(params[0])) {sender.sendLangfileMessage("command.generic.notFound", new Object[0]); return;}
			else show = "commandhelp";
		}
		
		if (show.equals("generalhelp")) {
			IChatComponent text = new ChatComponentText(LanguageManager.getTranslation(langCode, "command.generic.help.pageheader", new Object[] {Integer.valueOf(page + 1), Integer.valueOf(totalPages + 1)}));
			this.MESSAGE_PAGEHEADING = text.setChatStyle((new ChatStyle()).setColor(EnumChatFormatting.GREEN));
			sender.sendChatComponent(this.MESSAGE_PAGEHEADING);
			int max = Math.min((page + 1) * maxEntries, names.size());
			
			for (int index = page * maxEntries; index < max; ++index)
				sender.sendStringMessage("/" + names.get(index));
			
			sender.sendChatComponent(this.MESSAGE_INFO);
			sender.sendChatComponent(this.MESSAGE_FOOTER);
		}
		else if (show.equals("commandhelp")) {
			if (commands.get(params[0]) instanceof ServerCommand && commands.get(params[0]).getClass().getAnnotation(Command.class) != null) {
				Command info = commands.get(params[0]).getClass().getAnnotation(Command.class);
				
				sender.sendChatComponent(this.MESSAGE_COMMANDHEADING);
				
				sender.sendChatComponent(this.MESSAGE_NAME.appendSibling((new ChatComponentText(info.name())).setChatStyle((new ChatStyle()).setColor(EnumChatFormatting.WHITE))));
				sender.sendChatComponent(this.MESSAGE_DESCRIPTION.appendSibling((new ChatComponentText(LanguageManager.getTranslation(langCode, info.description(), new Object[0]))).setChatStyle((new ChatStyle()).setColor(EnumChatFormatting.WHITE))));
				sender.sendChatComponent(this.MESSAGE_SYNTAX.appendSibling((new ChatComponentText(LanguageManager.getTranslation(langCode, info.syntax(), new Object[0]))).setChatStyle((new ChatStyle()).setColor(EnumChatFormatting.WHITE))));
				sender.sendChatComponent(this.MESSAGE_EXAMPLE.appendSibling((new ChatComponentText(LanguageManager.getTranslation(langCode, info.example(), new Object[0]))).setChatStyle((new ChatStyle()).setColor(EnumChatFormatting.WHITE))));
				sender.sendChatComponent(this.MESSAGE_VIDEO.appendSibling((new ChatComponentText(LanguageManager.getTranslation(langCode, info.videoURL(), new Object[0]))).setChatStyle((new ChatStyle()).setColor(EnumChatFormatting.WHITE).setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "http://" + LanguageManager.getTranslation(langCode, info.videoURL(), new Object[0]))))));
				
				sender.sendChatComponent(this.MESSAGE_FOOTER);
			}
			else if (!(commands.get(params[0]) instanceof DummyCommand)){
				ICommand command = commands.get(params[0]);
				
				sender.sendChatComponent(this.MESSAGE_COMMANDHEADING);
				
				sender.sendChatComponent(this.MESSAGE_NAME.appendSibling((new ChatComponentText(command.getCommandName())).setChatStyle((new ChatStyle()).setColor(EnumChatFormatting.WHITE))));
				sender.sendChatComponent(this.MESSAGE_DESCRIPTION.appendSibling((new ChatComponentText(LanguageManager.getTranslation(langCode, "command.generic.help.noDescription", new Object[0]))).setChatStyle((new ChatStyle()).setColor(EnumChatFormatting.WHITE))));
				sender.sendChatComponent(this.MESSAGE_SYNTAX.appendSibling((new ChatComponentTranslation(command.getCommandUsage(sender.getMinecraftISender()), new Object[0])).setChatStyle((new ChatStyle()).setColor(EnumChatFormatting.WHITE))));
				sender.sendChatComponent(this.MESSAGE_EXAMPLE.appendSibling((new ChatComponentText(LanguageManager.getTranslation(langCode, "command.generic.help.noExample", new Object[0]))).setChatStyle((new ChatStyle()).setColor(EnumChatFormatting.WHITE))));
				sender.sendChatComponent(this.MESSAGE_VIDEO.appendSibling((new ChatComponentText(LanguageManager.getTranslation(langCode, "command.generic.help.noVideo", new Object[0]))).setChatStyle((new ChatStyle()).setColor(EnumChatFormatting.WHITE))));
				
				sender.sendChatComponent(this.MESSAGE_FOOTER);
			}
		}
	}
	
	private void resetMessages(String langCode) {
		this.MESSAGE_COMMANDHEADING = (new ChatComponentText(LanguageManager.getTranslation(langCode, "command.generic.help.commandheader", new Object[0]))).setChatStyle((new ChatStyle()).setColor(EnumChatFormatting.GREEN));
		this.MESSAGE_FOOTER = (new ChatComponentText(LanguageManager.getTranslation(langCode, "command.generic.help.footer", new Object[0]))).setChatStyle((new ChatStyle()).setColor(EnumChatFormatting.GREEN));
		this.MESSAGE_NAME = (new ChatComponentText(LanguageManager.getTranslation(langCode, "command.generic.help.name", new Object[0]))).setChatStyle((new ChatStyle()).setColor(EnumChatFormatting.GREEN));
		this.MESSAGE_DESCRIPTION = (new ChatComponentText(LanguageManager.getTranslation(langCode, "command.generic.help.description", new Object[0]))).setChatStyle((new ChatStyle()).setColor(EnumChatFormatting.GREEN));
		this.MESSAGE_SYNTAX = (new ChatComponentText(LanguageManager.getTranslation(langCode, "command.generic.help.syntax", new Object[0]))).setChatStyle((new ChatStyle()).setColor(EnumChatFormatting.GREEN));
		this.MESSAGE_EXAMPLE = (new ChatComponentText(LanguageManager.getTranslation(langCode, "command.generic.help.example", new Object[0]))).setChatStyle((new ChatStyle()).setColor(EnumChatFormatting.GREEN));
		this.MESSAGE_VIDEO = (new ChatComponentText(LanguageManager.getTranslation(langCode, "command.generic.help.video", new Object[0]))).setChatStyle((new ChatStyle()).setColor(EnumChatFormatting.GREEN));
		this.MESSAGE_INFO = (new ChatComponentText(LanguageManager.getTranslation(langCode, "command.generic.help.moreinfo", new Object[0]))).setChatStyle((new ChatStyle()).setColor(EnumChatFormatting.AQUA));
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
	public void unregisterFromHandler() {}
	
	@Override
	public int getPermissionLevel() {
		return 0;
	}
	
	@Override
	public boolean canSenderUse(ICommandSender sender) {
		return true;
	}
}
