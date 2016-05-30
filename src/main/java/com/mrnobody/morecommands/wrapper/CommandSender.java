package com.mrnobody.morecommands.wrapper;

import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.util.LanguageManager;
import com.mrnobody.morecommands.util.PlayerSettings;
import com.mrnobody.morecommands.util.ServerPlayerSettings;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.FMLCommonHandler;

/**
 * A wrapper for the {@link ICommandSender} interface
 * 
 * @author MrNobody98
 */
public final class CommandSender {	
	/** Whether to allow chat output by any of {@link CommandSender}s sendXXXMessage methods */
	public static boolean output = true;
	
	/** The {@link ICommandSender} this {@link CommandSender} wraps */
	private final ICommandSender sender;
	
	/**
	 * Constructs a new {@link CommandSender} with a {@link ICommandSender}
	 * 
	 * @param sender the {@link ICommandSender}
	 */
	public CommandSender(ICommandSender sender) {
		this.sender = sender;
	}
	
	/**
	 * Constructs a new {@link CommandSender} with a {@link Player}
	 * 
	 * @param player the {@link Player}
	 */
	public CommandSender(Player player) {
		this(player.getMinecraftPlayer());
	}
	
	/**
	 * @return the command sender's name
	 */
	public String getSenderName() {
		return sender.getName();
	}
	
	/**
	 * Whether the command sender can use a command
	 * 
	 * @param permLevel the permission level required for the command
	 * @param command the name of the command to be checked
	 * @return whether this sender can use the given command
	 */
	public boolean canUseCommand(int permLevel, String command) {
		return this.sender.canCommandSenderUseCommand(permLevel, command);
	}
	
	/**
	 * Sends an {@link IChatComponent} message to the command sender
	 * 
	 * @param component the {@link IChatComponent} to send
	 */
	public void sendChatComponent(ITextComponent component) {
		if (!(this.sender instanceof EntityPlayerMP)) {if (CommandSender.output) this.sender.addChatMessage(component);}
		else if (CommandSender.output) {
			ServerPlayerSettings settings = ((EntityPlayerMP) this.sender).getCapability(PlayerSettings.SETTINGS_CAP_SERVER, null);
			if (settings == null) this.sender.addChatMessage(component);
			else if (settings.output) this.sender.addChatMessage(component);
		}
	}
	
	/**
	 * Sends a literal string message to the command sender
	 * 
	 * @param message the message to send
	 * @param style the chat style to use
	 */
	public void sendStringMessage(String message, Style style) {
		TextComponentString text = new TextComponentString(message);
		text.setStyle(style);
		this.sendChatComponent(text);
	}
	
	/**
	 * Sends a literal string message to the command sender
	 * 
	 * @param message the message to send
	 * @param formatting the chat formatting to use
	 */
	public void sendStringMessage(String message, TextFormatting formatting) {
		TextComponentString text = new TextComponentString(message);
		text.getStyle().setColor(formatting);
		this.sendChatComponent(text);
	}
	
	/**
	 * Sends a literal string message to the command sender
	 * 
	 * @param message the message to send
	 */
	public void sendStringMessage(String message) {
		this.sendChatComponent(new TextComponentString(message));
	}
	
	/**
	 * Sends a translated message to the command sender
	 * 
	 * @param langFileEntry the language file key
	 * @param formatArgs the translation format arguments (used for {@link String#format(String, Object...)})
	 */
	public void sendLangfileMessage(String langFileEntry, Object... formatArgs) {
		String text = LanguageManager.translate(MoreCommands.INSTANCE.getCurrentLang(this.sender), langFileEntry, formatArgs);
		this.sendStringMessage(text);
	}
	
	/**
	 * Sends a translated message to the command sender
	 * 
	 * @param langFileEntry the language file key
	 * @param style that chat style to use
	 * @param formatArgs the translation format arguments (used for {@link String#format(String, Object...)})
	 */
	public void sendLangfileMessage(String langFileEntry, Style style , Object... formatArgs) {
		String text = LanguageManager.translate(MoreCommands.INSTANCE.getCurrentLang(this.sender), langFileEntry, formatArgs);
		this.sendStringMessage(text, style);
	}
	
	/**
	 * Sends a translated message to the command sender
	 * 
	 * @param langFileEntry the language file key
	 * @param formatting the chat formatting to use
	 * @param formatArgs the translation format arguments (used for {@link String#format(String, Object...)})
	 */
	public void sendLangfileMessage(String langFileEntry, TextFormatting formatting , Object... formatArgs) {
		String text = LanguageManager.translate(MoreCommands.INSTANCE.getCurrentLang(this.sender), langFileEntry, formatArgs);
		this.sendStringMessage(text, formatting);
	}
	
	/**
	 * @return the {@link ICommandSender} this {@link CommandSender} wraps
	 */
	public ICommandSender getMinecraftISender() {
		return this.sender;
	}
	
	/**
	 * @return the current position of this command sender
	 */
	public BlockPos getPosition() {
		return this.sender.getPosition();
	}
	
	/**
	 * @return the command sender's world
	 */
	public World getWorld() {
		return new World(this.sender.getEntityWorld());
	}
	
	/**
	 * @return the minecraft server
	 */
	public MinecraftServer getServer() {
		return this.sender.getServer() == null ? FMLCommonHandler.instance().getMinecraftServerInstance() : this.sender.getServer();
	}
}
