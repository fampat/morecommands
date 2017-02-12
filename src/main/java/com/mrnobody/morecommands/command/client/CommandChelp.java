package com.mrnobody.morecommands.command.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.mrnobody.morecommands.command.ClientCommand;
import com.mrnobody.morecommands.command.ClientCommandProperties;
import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandException;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.CommandSender;
import com.mrnobody.morecommands.command.MultipleCommands;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.util.DummyCommand;
import com.mrnobody.morecommands.util.LanguageManager;

import net.minecraft.client.Minecraft;
import net.minecraft.command.ICommand;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.event.ClickEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.client.ClientCommandHandler;

@Command(
		name = "chelp",
		description = "command.chelp.description",
		example = "command.chelp.example",
		syntax = "command.chelp.syntax",
		videoURL = "command.chelp.videoURL"
		)
public class CommandChelp extends StandardCommand implements ClientCommandProperties {
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
		return "chelp";
	}

	@Override
	public String getCommandUsage() {
		return "command.chelp.syntax";
	}

	@Override
	public String execute(CommandSender sender, String[] params) throws CommandException {
		String langCode = Minecraft.getMinecraft().getLanguageManager().getCurrentLanguage().getLanguageCode();
		this.resetMessages(langCode);
		
		Map<String, ICommand> commands = ClientCommandHandler.instance.getCommands();
		String show = "generalhelp";
		
		List<String> names = new ArrayList<String>(commands.keySet());
		List<String> remove = new ArrayList<String>();
		
		for (String name : names) {
			if (ClientCommandHandler.instance.getCommands().get(name) instanceof DummyCommand) remove.add(name);
		}
		for (String rem : remove) names.remove(rem);
		
		Collections.sort(names);
		
		byte maxEntries = 7;
		int totalPages = (names.size() - 1) / maxEntries;
		int page = 0;
			
		try {
			page = params.length == 0 ? 0 : parseInt(params[0], 1, totalPages + 1) - 1;
		}
		catch (NumberInvalidException numberinvalidexception){
			if (!names.contains(params[0])) throw new CommandException("command.generic.notFound", sender);
			else show = "commandhelp";
		}
		
		if (show.equals("generalhelp")) {
			IChatComponent text = new ChatComponentText(LanguageManager.translate(langCode, "command.generic.help.pageheader", Integer.valueOf(page + 1), Integer.valueOf(totalPages + 1)));
			this.MESSAGE_PAGEHEADING = text.setChatStyle((new ChatStyle()).setColor(EnumChatFormatting.GREEN));
			sender.sendChatComponent(this.MESSAGE_PAGEHEADING);
			
			int max = Math.min((page + 1) * maxEntries, names.size());
			
			for (int index = page * maxEntries; index < max; ++index)
				sender.sendStringMessage("/" + names.get(index));
			
			sender.sendChatComponent(this.MESSAGE_INFO);
			sender.sendChatComponent(this.MESSAGE_FOOTER);
		}
		else if (show.equals("commandhelp")) {
			String[] info = commands.get(params[0]) instanceof ClientCommand<?> ? getInfo((ClientCommand<?>) commands.get(params[0])) : null;
			
			if (info != null) {
				sender.sendChatComponent(this.MESSAGE_COMMANDHEADING);
				
				sender.sendChatComponent(this.MESSAGE_NAME.appendSibling((new ChatComponentText(info[0])).setChatStyle((new ChatStyle()).setColor(EnumChatFormatting.WHITE))));
				sender.sendChatComponent(this.MESSAGE_DESCRIPTION.appendSibling((new ChatComponentText(LanguageManager.translate(langCode, info[1]))).setChatStyle((new ChatStyle()).setColor(EnumChatFormatting.WHITE))));
				sender.sendChatComponent(this.MESSAGE_SYNTAX.appendSibling((new ChatComponentText(LanguageManager.translate(langCode, info[2]))).setChatStyle((new ChatStyle()).setColor(EnumChatFormatting.WHITE))));
				sender.sendChatComponent(this.MESSAGE_EXAMPLE.appendSibling((new ChatComponentText(LanguageManager.translate(langCode, info[3]))).setChatStyle((new ChatStyle()).setColor(EnumChatFormatting.WHITE))));
				sender.sendChatComponent(this.MESSAGE_VIDEO.appendSibling((new ChatComponentText(LanguageManager.translate(langCode, info[4]))).setChatStyle((new ChatStyle()).setColor(EnumChatFormatting.WHITE).setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "http://" + LanguageManager.translate(langCode, info[4]))))));
				
				sender.sendChatComponent(this.MESSAGE_FOOTER);
			}
			else if (!(commands.get(params[0]) instanceof DummyCommand)){
				ICommand command = commands.get(params[0]);
				
				sender.sendChatComponent(this.MESSAGE_COMMANDHEADING);
				
				sender.sendChatComponent(this.MESSAGE_NAME.appendSibling((new ChatComponentText(command.getCommandName())).setChatStyle((new ChatStyle()).setColor(EnumChatFormatting.WHITE))));
				sender.sendChatComponent(this.MESSAGE_DESCRIPTION.appendSibling((new ChatComponentText(LanguageManager.translate(langCode, "command.generic.help.noDescription"))).setChatStyle((new ChatStyle()).setColor(EnumChatFormatting.WHITE))));
				sender.sendChatComponent(this.MESSAGE_SYNTAX.appendSibling((new ChatComponentTranslation(command.getCommandUsage(sender.getMinecraftISender()))).setChatStyle((new ChatStyle()).setColor(EnumChatFormatting.WHITE))));
				sender.sendChatComponent(this.MESSAGE_EXAMPLE.appendSibling((new ChatComponentText(LanguageManager.translate(langCode, "command.generic.help.noExample"))).setChatStyle((new ChatStyle()).setColor(EnumChatFormatting.WHITE))));
				sender.sendChatComponent(this.MESSAGE_VIDEO.appendSibling((new ChatComponentText(LanguageManager.translate(langCode, "command.generic.help.noVideo"))).setChatStyle((new ChatStyle()).setColor(EnumChatFormatting.WHITE))));
				
				sender.sendChatComponent(this.MESSAGE_FOOTER);
			}
		}
		
		return null;
	}
	
	private String[] getInfo(ClientCommand<?> cmd) {
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
	
	private void resetMessages(String langCode) {
		this.MESSAGE_COMMANDHEADING = (new ChatComponentText(LanguageManager.translate(langCode, "command.generic.help.commandheader"))).setChatStyle((new ChatStyle()).setColor(EnumChatFormatting.GREEN));
		this.MESSAGE_FOOTER = (new ChatComponentText(LanguageManager.translate(langCode, "command.generic.help.footer"))).setChatStyle((new ChatStyle()).setColor(EnumChatFormatting.GREEN));
		this.MESSAGE_NAME = (new ChatComponentText(LanguageManager.translate(langCode, "command.generic.help.name"))).setChatStyle((new ChatStyle()).setColor(EnumChatFormatting.GREEN));
		this.MESSAGE_DESCRIPTION = (new ChatComponentText(LanguageManager.translate(langCode, "command.generic.help.description"))).setChatStyle((new ChatStyle()).setColor(EnumChatFormatting.GREEN));
		this.MESSAGE_SYNTAX = (new ChatComponentText(LanguageManager.translate(langCode, "command.generic.help.syntax"))).setChatStyle((new ChatStyle()).setColor(EnumChatFormatting.GREEN));
		this.MESSAGE_EXAMPLE = (new ChatComponentText(LanguageManager.translate(langCode, "command.generic.help.example"))).setChatStyle((new ChatStyle()).setColor(EnumChatFormatting.GREEN));
		this.MESSAGE_VIDEO = (new ChatComponentText(LanguageManager.translate(langCode, "command.generic.help.video"))).setChatStyle((new ChatStyle()).setColor(EnumChatFormatting.GREEN));
		this.MESSAGE_INFO = (new ChatComponentText(LanguageManager.translate(langCode, "command.generic.help.moreinfo"))).setChatStyle((new ChatStyle()).setColor(EnumChatFormatting.AQUA));
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
		return true;
	}
	
	@Override
	public int getDefaultPermissionLevel(String[] args) {
		return 0;
	}
}
