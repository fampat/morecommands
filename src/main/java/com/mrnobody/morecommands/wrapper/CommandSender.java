package com.mrnobody.morecommands.wrapper;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
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
		return sender.getName();
	}
	
	public boolean canUseCommand(String command) {
		return sender.canUseCommand(4, command);
	}
	
	public void sendChatComponent(IChatComponent component) {
		if (CommandSender.output && !ServerPlayerSettings.playerSettingsMapping.containsKey(sender)) sender.addChatMessage(component);
		else if (CommandSender.output && ServerPlayerSettings.playerSettingsMapping.get(sender).output) sender.addChatMessage(component);
	}
	
	public void sendStringMessage(String message) {
		if (CommandSender.output && !ServerPlayerSettings.playerSettingsMapping.containsKey(sender)) sender.addChatMessage(new ChatComponentText(message));
		else if (CommandSender.output && ServerPlayerSettings.playerSettingsMapping.get(sender).output) sender.addChatMessage(new ChatComponentText(message));
	}
	
	public void sendLangfileMessage(String LangFileEntry, Object... formatArgs) {
		String text = LanguageManager.getTranslation(MoreCommands.getMoreCommands().getCurrentLang(this.sender), LangFileEntry, formatArgs);
		if (CommandSender.output && !ServerPlayerSettings.playerSettingsMapping.containsKey(sender)) sender.addChatMessage(new ChatComponentText(text));
		else if (CommandSender.output && ServerPlayerSettings.playerSettingsMapping.get(sender).output) sender.addChatMessage(new ChatComponentText(text));
	}
	
	public BlockPos getPosition() {
		return sender.getPosition();
	}
	
	public World getWorld() {
		return new World(sender.getEntityWorld());
	}
	
	public ICommandSender getMinecraftISender() {
		return sender;
	}
}
