package com.mrnobody.morecommands.wrapper;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;

import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.util.LanguageManager;
import com.mrnobody.morecommands.util.ServerPlayerSettings;

/**
 * A wrapper for the {@link ICommandSender} interface
 * 
 * @author MrNobody98
 */
public class CommandSender {
	public static boolean output = true;
	
	private final ICommandSender sender;
	
	public CommandSender(ICommandSender sender) {
		this.sender = sender;
	}
	
	public CommandSender(Player player) {
		this(player.getMinecraftPlayer());
	}
	
	public String getSenderName() {
		return sender.getCommandSenderName();
	}
	
	public boolean canUseCommand(String command) {
		return sender.canCommandSenderUseCommand(4, command);
	}
	
	public void sendChatComponentToPlayer(IChatComponent component) {
		if (CommandSender.output && !ServerPlayerSettings.playerSettingsMapping.containsKey(sender)) sender.addChatMessage(component);
		else if (CommandSender.output && ServerPlayerSettings.playerSettingsMapping.get(sender).output) sender.addChatMessage(component);
	}
	
	public void sendStringMessageToPlayer(String message) {
		if (CommandSender.output && !ServerPlayerSettings.playerSettingsMapping.containsKey(sender)) sender.addChatMessage(new ChatComponentText(message));
		else if (CommandSender.output && ServerPlayerSettings.playerSettingsMapping.get(sender).output) sender.addChatMessage(new ChatComponentText(message));
	}
	
	public void sendLangfileMessageToPlayer(String LangFileEntry, Object... formatArgs) {
		String text = LanguageManager.getTranslation(MoreCommands.getCurrentLang(this.sender), LangFileEntry, formatArgs);
		if (CommandSender.output && !ServerPlayerSettings.playerSettingsMapping.containsKey(sender)) sender.addChatMessage(new ChatComponentText(text));
		else if (CommandSender.output && ServerPlayerSettings.playerSettingsMapping.get(sender).output) sender.addChatMessage(new ChatComponentText(text));
	}
	
	public ICommandSender getMinecraftISender() {
		return sender;
	}
	
	public boolean isPlayer() {
		return sender instanceof EntityPlayerMP;
	}
	
	public Player toPlayer() {
		return new Player(CommandBase.getCommandSenderAsPlayer(sender));
	}
}
