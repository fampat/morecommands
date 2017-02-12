package com.mrnobody.morecommands.command.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandException;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.CommandSender;
import com.mrnobody.morecommands.command.MultipleCommands;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.command.ServerCommandProperties;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.util.DummyCommand;
import com.mrnobody.morecommands.util.LanguageManager;

import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;

@Command(
		name = "help",
		description = "command.helpSideServer.description",
		example = "command.helpSideServer.example",
		syntax = "command.helpSideServer.syntax",
		videoURL = "command.helpSideServer.videoURL"
		)
public class CommandHelp extends StandardCommand implements ServerCommandProperties {
	@Override
	public String getCommandName() {
		return "help";
	}

	@Override
	public String getCommandUsage() {
		return "command.helpSideServer.syntax";
	}

	@Override
	public String execute(CommandSender sender, String[] params) throws CommandException {
		String langCode = MoreCommands.getProxy().getLang(sender.getMinecraftISender());
		
		final ITextComponent HEADING = new TextComponentString(LanguageManager.translate(langCode, "command.generic.help.commandheader")).setChatStyle(new Style().setColor(TextFormatting.GREEN));
		final ITextComponent FOOTER = new TextComponentString(LanguageManager.translate(langCode, "command.generic.help.footer")).setChatStyle(new Style().setColor(TextFormatting.GREEN));
		final ITextComponent NAME = new TextComponentString(LanguageManager.translate(langCode, "command.generic.help.name")).setChatStyle(new Style().setColor(TextFormatting.GREEN));
		final ITextComponent DESCRIPTION = new TextComponentString(LanguageManager.translate(langCode, "command.generic.help.description")).setChatStyle(new Style().setColor(TextFormatting.GREEN));
		final ITextComponent SYNTAX = new TextComponentString(LanguageManager.translate(langCode, "command.generic.help.syntax")).setChatStyle(new Style().setColor(TextFormatting.GREEN));
		final ITextComponent EXAMPLE = new TextComponentString(LanguageManager.translate(langCode, "command.generic.help.example")).setChatStyle(new Style().setColor(TextFormatting.GREEN));
		final ITextComponent VIDEO = new TextComponentString(LanguageManager.translate(langCode, "command.generic.help.video")).setChatStyle(new Style().setColor(TextFormatting.GREEN));
		final ITextComponent INFO = new TextComponentString(LanguageManager.translate(langCode, "command.generic.help.moreinfo")).setChatStyle(new Style().setColor(TextFormatting.AQUA));
		
		Map<String, ICommand> commands = sender.getServer().getCommandManager().getCommands();
		String show = "generalhelp";
		
		List<String> names = new ArrayList<String>(commands.keySet());
		List<String> remove = new ArrayList<String>();
		
		for (String name : names) {
			if (sender.getServer().getCommandManager().getCommands().get(name) instanceof DummyCommand) remove.add(name);
		}
		for (String rem : remove) names.remove(rem);
		
		Collections.sort(names);
			
		byte maxEntries = 7;
		int totalPages = (names.size() - 1) / maxEntries;
		int page = 0;
		
		try {
			page = params.length == 0 ? 0 : this.parseInt(params[0], 1, totalPages + 1) - 1;
		}
		catch (NumberInvalidException numberinvalidexception){
			if (!names.contains(params[0])) throw new CommandException("command.generic.notFound", sender);
			else show = "commandhelp";
		}
		
		if (show.equals("generalhelp")) {
			final ITextComponent HEADER = new TextComponentString(LanguageManager.translate(langCode, "command.generic.help.commandheader", page + 1, totalPages + 1)).setChatStyle(new Style().setColor(TextFormatting.GREEN));
			
			sender.sendChatComponent(HEADER);
			int max = Math.min((page + 1) * maxEntries, names.size());
			
			for (int index = page * maxEntries; index < max; ++index)
				sender.sendStringMessage("/" + names.get(index));
			
			sender.sendChatComponent(INFO);
			sender.sendChatComponent(FOOTER);
		}
		else if (show.equals("commandhelp")) {
			String[] info = commands.get(params[0]) instanceof ServerCommand<?> ? getInfo((ServerCommand<?>) commands.get(params[0])) : null;
			
			if (info != null) {
				sender.sendChatComponent(HEADING);
				
				sender.sendChatComponent(NAME.appendSibling(new TextComponentString(info[0]).setChatStyle(new Style().setColor(TextFormatting.WHITE))));
				sender.sendChatComponent(DESCRIPTION.appendSibling(new TextComponentString(LanguageManager.translate(langCode, info[1])).setChatStyle(new Style().setColor(TextFormatting.WHITE))));
				sender.sendChatComponent(SYNTAX.appendSibling(new TextComponentString(LanguageManager.translate(langCode, info[2])).setChatStyle(new Style().setColor(TextFormatting.WHITE))));
				sender.sendChatComponent(EXAMPLE.appendSibling(new TextComponentString(LanguageManager.translate(langCode, info[3])).setChatStyle(new Style().setColor(TextFormatting.WHITE))));
				sender.sendChatComponent(VIDEO.appendSibling(new TextComponentString(LanguageManager.translate(langCode, info[4])).setChatStyle(new Style().setColor(TextFormatting.WHITE).setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "http://" + LanguageManager.translate(langCode, info[4]))))));
				
				sender.sendChatComponent(FOOTER);
			}
			else if (!(commands.get(params[0]) instanceof DummyCommand)){
				ICommand command = commands.get(params[0]);
				
				sender.sendChatComponent(HEADING);
				
				sender.sendChatComponent(NAME.appendSibling(new TextComponentString(command.getCommandName()).setChatStyle(new Style().setColor(TextFormatting.WHITE))));
				sender.sendChatComponent(DESCRIPTION.appendSibling(new TextComponentString(LanguageManager.translate(langCode, "command.generic.help.noDescription")).setChatStyle(new Style().setColor(TextFormatting.WHITE))));
				sender.sendChatComponent(SYNTAX.appendSibling(new TextComponentTranslation(command.getCommandUsage(sender.getMinecraftISender())).setChatStyle(new Style().setColor(TextFormatting.WHITE))));
				sender.sendChatComponent(EXAMPLE.appendSibling(new TextComponentString(LanguageManager.translate(langCode, "command.generic.help.noExample")).setChatStyle(new Style().setColor(TextFormatting.WHITE))));
				sender.sendChatComponent(VIDEO.appendSibling(new TextComponentString(LanguageManager.translate(langCode, "command.generic.help.noVideo")).setChatStyle(new Style().setColor(TextFormatting.WHITE))));
				
				sender.sendChatComponent(FOOTER);
			}
		}
		
		return null;
	}
	
	private String[] getInfo(ServerCommand<?> cmd) {
		StandardCommand delegate = cmd.getDelegate();
		
		if (delegate instanceof MultipleCommands && delegate.getClass().isAnnotationPresent(Command.MultipleCommand.class)) {
			MultipleCommands command = (MultipleCommands) delegate;
			Command.MultipleCommand info = delegate.getClass().getAnnotation(Command.MultipleCommand.class);
			
			try {return new String[] {info.name()[command.getTypeIndex()], info.description()[command.getTypeIndex()],
				info.syntax()[command.getTypeIndex()], info.example()[command.getTypeIndex()], info.videoURL()[command.getTypeIndex()]};}
			catch (ArrayIndexOutOfBoundsException ex) {return null;}
		}
		else if (delegate.getClass().isAnnotationPresent(Command.class)) {
			Command info = delegate.getClass().getAnnotation(Command.class);
			return new String[] {info.name(), info.description(), info.syntax(), info.example(), info.videoURL()};
		}
		else return null;
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
