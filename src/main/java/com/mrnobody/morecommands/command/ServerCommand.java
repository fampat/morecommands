package com.mrnobody.morecommands.command;

import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.util.GlobalSettings;
import com.mrnobody.morecommands.util.LanguageManager;

import cpw.mods.fml.relauncher.Side;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

/**
 * A wrapper class for commands which are intended to be used
 * on server side. Delegates all method calls to the wrapped command.
 * The wrapped command must be of type {@link StandardCommand} and {@link ServerCommandProperties}
 * 
 * @author MrNobody98
 */
public final class ServerCommand<T extends StandardCommand & ServerCommandProperties> extends CommandBase implements ServerCommandProperties {	
	/**
	 * Checks if an object <i>o</i> is of type {@link StandardCommand} and {@link ServerCommandProperties}.<br>
	 * Returns null if not, else a generic type with those two types as bounds.
	 * 
	 * @param o the object to check/cast
	 * @return a generic type having those two types as bounds
	 * @throws IllegalArgumentException if the <i>o</i> is not of type {@link StandardCommand} and {@link ServerCommandProperties}
	 */
	//possible to do with "(A & B) obj" multiple bounds cast in java 8, not used in order to be able to use older java versions
	public static final <T extends StandardCommand & ServerCommandProperties> T upcast(Object o) throws IllegalArgumentException {
		if (o instanceof StandardCommand && o instanceof ServerCommandProperties) return (T) o;
		else throw new IllegalArgumentException("argument is not of type StandardCommand & ServerCommandProperties");
	}
	
	private final ServerCommandProperties delegate;
	private int permissionLevel;
	
	public ServerCommand(T delegate) {
		super(delegate);
		this.delegate = delegate;
		refreshPermissionLevel();
	}
	
	/**
	 * Refreshes the permission level required to be able to use this command
	 */
	public final void refreshPermissionLevel() {
		Integer level = GlobalSettings.permissionMapping.get(this.getCommandName());
		this.permissionLevel = level == null ? this.getDefaultPermissionLevel() : level;
	}
	
	/**
	 * Sets the permission level required to use this command
	 * 
	 * @param level the permission level
	 */
	public final void setPermissionLevel(int level) {
		this.permissionLevel = level;
	}
	
	@Override
	public int getRequiredPermissionLevel() {
		return this.permissionLevel;
	}
    
    @Override
    public boolean checkRequirements(ICommandSender sender, String[] params, Side side) {
    	String lang = MoreCommands.INSTANCE.getCurrentLang(sender);
    	
    	if (!this.canSenderUse(this.getCommandName(), sender, params)) {
        	ChatComponentText text = new ChatComponentText(LanguageManager.translate(lang, "command.generic.cantUse"));
        	text.getChatStyle().setColor(EnumChatFormatting.RED);
        	sender.addChatMessage(text);
        	return false;
    	}
    	
    	return super.checkRequirements(sender, params, side);
    }
    
	@Override
	public boolean canSenderUse(String commandName, ICommandSender sender, String[] params) {
		return this.delegate.canSenderUse(commandName, sender, params);
	}
	
	@Override
	public Side getSide() {
		return Side.SERVER;
	}
}
