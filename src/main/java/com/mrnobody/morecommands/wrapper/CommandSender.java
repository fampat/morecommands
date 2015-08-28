package com.mrnobody.morecommands.wrapper;

import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.util.LanguageManager;
import com.mrnobody.morecommands.util.ServerPlayerSettings;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

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
		if (!(sender instanceof EntityPlayerMP)) {if (CommandSender.output) sender.addChatMessage(component);}
		else if (CommandSender.output && !ServerPlayerSettings.containsSettingsForPlayer((EntityPlayerMP) sender)) sender.addChatMessage(component);
		else if (CommandSender.output && ServerPlayerSettings.getPlayerSettings((EntityPlayerMP) sender).output) sender.addChatMessage(component);
	}
	
	public void sendStringMessage(String message, ChatStyle stye) {
		ChatComponentText text = new ChatComponentText(message);
		text.setChatStyle(stye);
		this.sendChatComponent(text);
	}
	
	public void sendStringMessage(String message, EnumChatFormatting color) {
		ChatComponentText text = new ChatComponentText(message);
		text.getChatStyle().setColor(color);
		this.sendChatComponent(text);
	}
	
	public void sendStringMessage(String message) {
		this.sendChatComponent(new ChatComponentText(message));
	}
	
	public void sendLangfileMessage(String LangFileEntry, Object... formatArgs) {
		String text = LanguageManager.getTranslation(MoreCommands.getMoreCommands().getCurrentLang(this.sender), LangFileEntry, formatArgs);
		this.sendStringMessage(text);
	}
	
	public void sendLangfileMessage(String LangFileEntry, ChatStyle style , Object... formatArgs) {
		String text = LanguageManager.getTranslation(MoreCommands.getMoreCommands().getCurrentLang(this.sender), LangFileEntry, formatArgs);
		this.sendStringMessage(text, style);
	}
	
	public void sendLangfileMessage(String LangFileEntry, EnumChatFormatting color , Object... formatArgs) {
		String text = LanguageManager.getTranslation(MoreCommands.getMoreCommands().getCurrentLang(this.sender), LangFileEntry, formatArgs);
		this.sendStringMessage(text, color);
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
