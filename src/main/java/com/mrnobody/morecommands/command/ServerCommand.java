package com.mrnobody.morecommands.command;

import org.apache.commons.lang3.tuple.MutablePair;

import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.settings.MoreCommandsConfig;
import com.mrnobody.morecommands.util.LanguageManager;

import gnu.trove.map.TObjectIntMap;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;

/**
 * A wrapper class for commands which are intended to be used
 * on server side. Delegates all method calls to the wrapped command.
 * The wrapped command must be of type {@link StandardCommand} and {@link ServerCommandProperties}
 * 
 * @author MrNobody98
 */
public final class ServerCommand<T extends StandardCommand & ServerCommandProperties> extends CommandBase<T> implements ServerCommandProperties {	
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
	private int basePermLevel;
	private TObjectIntMap<String> actionPermLevels = null;
	
	public ServerCommand(T delegate) {
		super(delegate);
		this.delegate = delegate;
		refreshPermissionLevel();
	}
	
	/**
	 * Refreshes the permission level required to be able to use this command
	 */
	public final void refreshPermissionLevel() {
		MutablePair<Integer, TObjectIntMap<String>> level = MoreCommandsConfig.permissionMapping.get(this.getCommandName());
		
		this.basePermLevel = level == null ? -1 : level.getLeft();
		this.actionPermLevels = level == null ? null : level.getRight();
	}

	@Override
	public int getRequiredPermissionLevel() {
		return this.basePermLevel < 0 ? getDefaultPermissionLevel(null) : this.basePermLevel;
	}
	
	@Override
	public int getRequiredPermissionLevel(String[] args) {
		int level = args.length == 0 || this.actionPermLevels == null || !this.actionPermLevels.containsKey(args[0]) ? 
					this.basePermLevel : this.actionPermLevels.get(args[0]);
		
		if (level < 0) return getDefaultPermissionLevel(args);
		else return level;
	}
    
    @Override
    public ITextComponent checkRequirements(ICommandSender sender, String[] params, Side side) {
    	String lang = MoreCommands.INSTANCE.getCurrentLang(sender);

    	if (!checkPermLevel(sender, params))
    		return new TextComponentTranslation("commands.generic.permission").setStyle(new Style().setColor(TextFormatting.RED));
    	
    	if (!this.canSenderUse(this.getCommandName(), sender, params))
        	return makeChatMsg(LanguageManager.translate(lang, "command.generic.cantUse"));
    	
    	return super.checkRequirements(sender, params, side);
    }
    
    private boolean checkPermLevel(ICommandSender sender, String[] params) {
		if (this.getRequiredPermissionLevel(params) == 0) return true;
		else return sender.canCommandSenderUseCommand(this.getRequiredPermissionLevel(params), this.getCommandName());
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
