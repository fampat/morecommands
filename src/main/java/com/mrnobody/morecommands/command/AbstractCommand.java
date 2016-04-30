package com.mrnobody.morecommands.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.mrnobody.morecommands.core.AppliedPatches.PlayerPatches;
import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.util.ClientPlayerSettings;
import com.mrnobody.morecommands.util.LanguageManager;
import com.mrnobody.morecommands.util.PlayerSettings;
import com.mrnobody.morecommands.util.ServerPlayerSettings;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

import net.minecraft.block.Block;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.command.ICommandSender;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Abstract base class for commands
 * 
 * @author MrNobody98
 */
public abstract class AbstractCommand extends net.minecraft.command.CommandBase {
	/**
	 * @return whether the command sender can use this command
	 */
	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
		if (this.getRequiredPermissionLevel() == 0) return true;
		else return sender.canCommandSenderUseCommand(this.getRequiredPermissionLevel(), this.getCommandName());
	}
	
	/**
	 * @return The required permission level
	 */
	@Override
    public int getRequiredPermissionLevel() {return this.getDefaultPermissionLevel();}
    
	/**
	 * @return The command usage
	 */
    public final String getCommandUsage(ICommandSender sender) {return this.getCommandUsage();}
    
	/**
	 * processes the command
	 */
    public abstract void execute(MinecraftServer server, ICommandSender sender, String[] params) throws net.minecraft.command.CommandException;
    
	/**
	 * @return The command name
	 */
    public abstract String getCommandName();
    
	/**
	 * @return The command usage
	 */
    public abstract String getCommandUsage();
    
	/**
	 * Executes the command
	 * 
	 * @param sender the command sender
	 * @param params the command parameters
	 * @throws CommandException if the command couldn't be processed for some reason
	 */
    public abstract void execute(CommandSender sender, String[] params) throws CommandException;
    
	/**
	 * @return The requirements for a command to be executed
	 */
    public abstract CommandRequirement[] getRequirements();
    
	/**
	 * @return The Server Type on which this command can be executed
	 */
    public abstract ServerType getAllowedServerType();
    
	/**
	 * @return The default permission level
	 */
    public abstract int getDefaultPermissionLevel();
    
	/**
	 * Checks the command sender whether he can use this command with the given parameters
	 * 
	 * @param sender the command sender
	 * @param params the command parameters
	 * @param side the side on which this command is executed
	 * 
	 * @return whether the command requirements are satisfied
	 */
    public boolean checkRequirements(ICommandSender sender, String[] params, Side side) {
    	String lang = MoreCommands.INSTANCE.getCurrentLang(sender);
    	
    	if (!(this.getAllowedServerType() == ServerType.ALL || this.getAllowedServerType() == MoreCommands.getServerType())) {
    		if (this.getAllowedServerType() == ServerType.INTEGRATED)
    			sendChatMsg(sender, LanguageManager.translate(lang, "command.generic.notIntegrated"));
    		if (this.getAllowedServerType() == ServerType.DEDICATED) 
    			sendChatMsg(sender, LanguageManager.translate(lang, "command.generic.notDedicated"));
    		return false;
    	}
    	
    	PlayerPatches clientInfo = isSenderOfEntityType(sender, EntityPlayerMP.class) ? 
    	getSenderAsEntity(sender, EntityPlayerMP.class).getCapability(PlayerPatches.PATCHES_CAPABILITY, null) : null;
    	
    	CommandRequirement[] requierements = this.getRequirements();
    	if (clientInfo == null) {
    		clientInfo = PlayerPatches.PATCHES_CAPABILITY.getDefaultInstance();
    		
    		if (isSenderOfEntityType(sender, EntityPlayerMP.class))
    			clientInfo.setServerPlayHandlerPatched(getSenderAsEntity(sender, EntityPlayerMP.class).
    					playerNetServerHandler instanceof com.mrnobody.morecommands.patch.NetHandlerPlayServer);
    	}
    	
    	for (CommandRequirement requierement : requierements) {
    		if (!requierement.isSatisfied(sender, clientInfo, side)) {
    			sendChatMsg(sender, LanguageManager.translate(lang, requierement.getLangfileMsg(side)));
    			return false;
    		}
    	}
    	
    	return true;
    }
    
    private final void sendChatMsg(ICommandSender sender, String msg) {
    	TextComponentString text = new TextComponentString(msg);
    	text.getChatStyle().setColor(TextFormatting.RED);
    	sender.addChatMessage(text);
    }
    
    @Override
    public boolean equals(Object o) {
    	if (o == this) return true;
    	else if (!(o instanceof AbstractCommand)) return false;
    	else return ((AbstractCommand) o).getCommandName() != null && ((AbstractCommand) o).getCommandName().equals(this.getCommandName());
    }
    
    @Override
    public int hashCode() {
    	return this.getCommandName() == null ? 0 : this.getCommandName().hashCode();
    }
    
    private static final Joiner spaceJoiner = Joiner.on(" ");
    
    /**
     * Rejoins the space-splitted parameters to a string
     * 
     * @param params
     * @return the rejoined parameters
     */
    public static final String rejoinParams(String[] params) {
    	return spaceJoiner.join(params);
    }
    
    /**
     * returns true if the value of <b>params[index]</b> is <b>"enable"</b>, <b>"on"</b>, <b>"1"</b> or <b>"true"</b>,<br>
     * false if this value is <b>"disable"</b>, <b>"off"</b>, <b>"0"</b> or <b>"false"</b>.<br>
     * If the index does not exist, it will return <b>default_</b> NEGATED.
     * 
     * @param params the command arguments
     * @param index the index to be parsed
     * @param default_ the negated default value
     * @return the parsed boolean argument
     * 
     * @throws IllegalArgumentException if <b>params[index]</b> is none of the values named above
     */
    public static boolean parseTrueFalse(String[] params, int index, boolean default_) throws IllegalArgumentException {
        if (params.length > index) {
        	if (params[index].equalsIgnoreCase("enable") || params[index].equalsIgnoreCase("1")
            	|| params[index].equalsIgnoreCase("on") || params[index].equalsIgnoreCase("true")) {
        		return true;
            }
            else if (params[index].equalsIgnoreCase("disable") || params[index].equalsIgnoreCase("0")
            		|| params[index].equalsIgnoreCase("off") || params[index].equalsIgnoreCase("false")) {
            	return false;
            }
            else throw new IllegalArgumentException("Invalid Argument");
        }
        else return !default_;
    }
    
    /**
     * The default opening <-> closing char mapping. Contains:<br>
     * '{' -> '}'<br>
     * '[' -> ']'<br>
     */
    public static final ImmutableMap<Character, Character> DEFAULT_JSON_OPENING_CLOSING_CHARS = ImmutableMap.of('{', '}', '[', ']');
    
    /**
     * Searches a splitted string from a starting index which contains an opening char<br>
     * for the closing index which contains the respective closing char. If the opening char occurs<br>
     * again, the next closing char is NOT considered as the final closing char and the<br>
     * search continues. <b>Note that the opening and closing char must be the first/last<br>
     * char of their respective index!</b>
     * 
     * @param params the the splitted string array to be searched
     * @param openingIndex the index from where the closing index should be searched
     * @param openingClosingChars the opening char <-> closing char map
     * 
     * @return <b>-1</b> if the first char of <b>openingIndex is not any of openingClosingChars.keySet()</b><br>
     * or if the <b>closing index does not exist</b> or if the <b>last char of the closing index<br>
     * is not the closing char</b>. Otherwise the <b>closing index</b> is returned
     * 
     * @throws ArrayIndexOutOfBoundsException if openingIndex >= params.length
     */
    public static int getClosingIndex(String[] params, int openingIndex, Map<Character, Character> openingClosingChars) {
    	if (openingIndex >= params.length) throw new ArrayIndexOutOfBoundsException("openingIndex out of bounds");
    	
    	if (params[openingIndex].isEmpty() || !startsWithAnyChar(params[openingIndex], 
    		openingClosingChars.keySet().toArray(new Character[openingClosingChars.size()]))) return -1;
    	
    	char openingChar = params[openingIndex].charAt(0), closingChar = openingClosingChars.get(openingChar);
    	int depthLevel = 0; int index = openingIndex;
    	
    	for (String param : Arrays.copyOfRange(params, openingIndex, params.length)) {
    		char[] chars = param.toCharArray();
    		
    		for (char ch : chars) {
    			if (ch == openingChar) depthLevel++;
    			else if (ch == closingChar) depthLevel--;
    		}
    		
    		if (depthLevel == 0) 
    			return chars[chars.length - 1] == closingChar ? index : -1;
    		
    		index++;
    	}
    	
    	return -1;
    }
    
    private static boolean startsWithAnyChar(String s, Character[] chars) {
    	char ch = s.charAt(0);
    	
    	for (char character : chars)
    		if (character == ch) return true;
    	
    	return false;
    }
    
    private static final Pattern isTargetSeletorWithArguments = Pattern.compile("^@[pareb]\\[");
    
    /**
     * Reparses the command parameters to get NBT arguments and target selectors<br>
     * in <b>ONLY ONE index</b> while <b>space characters</b> are allowed, e.g.<br>
     * <b>"{@literal @}p[name = Test, type = Pig]" is NOT allowed in vanilla commands</b> because<br>
     * it splits at every space character, result would be <b>["@p[name", "=", "Test,", "type", "=", "Pig]"]</b>.<br>
     * For NBT data, spaces are allowed because they are always appearing only once per command<br>
     * and they are always the last parameter so they can easily be concatenated.<br>
     * A command String which would not work in vanilla, e.g.<br>
     * <b>"{@literal @}p[name = Test, type = Pig] param1 {somejson : {key : [value1, value2]}} param2"</b><br>
     * can be parsed with this method. The Vanilla result is:<br>
     * <b>["@p[name", "=", "Test,", "type", "=", "Pig]", "param1", "{somejson", ":", "{key", ":", "[value1,", "value2]}}", "param2"]</b><br>
     * The result of this method is:<br>
     * <b>["@p[name = Test, type = Pig]", "param1", "{somejson : {key : [value1, value2]}}", "param2"]</b>
     * 
     * @param params the space-splitted parameters passed to the command
     * @return the reparsed parameters
     */
    public static String[] reparseParamsWithNBTData(String[] params) {
    	List<String> newParams = new ArrayList<String>(); int endIndex;
    	
    	for (int index = 0; index < params.length; index++) {
    		String param = params[index];
    		boolean isTargetSelector = param.length() >= 3 && isTargetSeletorWithArguments.matcher(param.substring(0, 3)).matches();
    		
    		if (isTargetSelector) params[index] = param.substring(2, param.length());
    		endIndex = getClosingIndex(params, index, DEFAULT_JSON_OPENING_CLOSING_CHARS);
    		
    		if (endIndex != -1) {
    			newParams.add((isTargetSelector ? param.substring(0, 2) : "") + rejoinParams(Arrays.copyOfRange(params, index, endIndex + 1)));
    			index = endIndex;
    		}
    		else newParams.add(param);
    	}
    	
    	return newParams.toArray(new String[newParams.size()]);
    }
    
    /**
     * @param param the command argument to be tested
     * @return whether this argument represents a nbt parameter
     */
    public static boolean isNBTParam(String param) {
    	return param.length() >= 2 && startsWithAnyChar(param, DEFAULT_JSON_OPENING_CLOSING_CHARS.keySet().toArray(new Character[DEFAULT_JSON_OPENING_CLOSING_CHARS.size()])) &&
    			param.charAt(param.length() - 1) == DEFAULT_JSON_OPENING_CLOSING_CHARS.get(param.charAt(0));
    }
    
    /**
     * @param param the command argument to be tested
     * @return whether this argument is a target selector
     */
    public static boolean isTargetSelector(String param) {
    	return param.startsWith("@");
    }
    
    /**
     * @param param the command argument to be tested
     * @return whether this argument is a target selector with arguments
     */
    public static boolean isTargetSelectorWithArguments(String param) {
    	return param.length() > 3 && isTargetSeletorWithArguments.matcher(param.substring(0, 3)).matches() && param.endsWith("]");
    }
    
    /**
     * @param param the command argument to be tested
     * @return whether this argument represents the nbt contain mode EQUAL_LISTS
     * @see com.mrnobody.morecommands.util.TargetSelector#nbtContains(NBTBase, NBTBase, boolean)
     */
    public static boolean isEqualLists(String param) {
    	return param.equalsIgnoreCase("EQUAL");
    }
    
    /**
     * @param param the command argument to be tested
     * @return whether this argument represents the nbt merge mode MERGE_LISTS
     * @see com.mrnobody.morecommands.util.TargetSelector#nbtMerge(NBTBase, NBTBase, boolean)
     */
    public static boolean isMergeLists(String param) {
    	return param.equalsIgnoreCase("MERGE");
    }
    
    /**
     * Parses a command argument into a {@link NBTBase}
     * 
     * @param param the command argument to parsed into a NBTBase
     * @param sender the command sender
     * @return the parsed NBTBase
     */
    public static NBTTagCompound getNBTFromParam(String param, ICommandSender sender) {
    	if (isNBTParam(param)) {
    		NBTBase nbt = null;
    		
    		try {return JsonToNBT.getTagFromJson(param);}
    		catch (NBTException ex) {return null;}
    	}
    	else return null;
    }
    
    /**
     * Parses three indices starting at params[index]<br>
     * into a {@link Coordinate}
     * 
     * @param sender the command sender
     * @param params the command parameters
     * @param index the index from where the coordinate shall be parsed
     * @return the parsed Coordinate
     * @throws NumberFormatException if any of the coordinate components (x, y, z) is not a number
     * @throws ArrayIndexOutOfBoundsException if params.length < index + 3
     */
    public static BlockPos getCoordFromParams(ICommandSender sender, String[] params, int index) throws NumberFormatException {
    	if (params.length < index + 3) throw new ArrayIndexOutOfBoundsException(params.length);
    	
		boolean relative;
		
		relative = params[index].startsWith("~"); if (relative && params[index].length() <= 1) params[index] = "~0";
		int x = relative ? sender.getPosition().getX() + Integer.parseInt(params[index].substring(1)) : Integer.parseInt(params[index]);
		
		relative = params[index + 1].startsWith("~"); if (relative && params[index + 1].length() <= 1) params[index + 1] = "~0";
		int y = relative ? sender.getPosition().getY() + Integer.parseInt(params[index + 1].substring(1)) : Integer.parseInt(params[index + 1]);
		
		relative = params[index + 2].startsWith("~"); if (relative && params[index + 2].length() <= 1) params[index + 2] = "~0";
		int z = relative ? sender.getPosition().getZ() + Integer.parseInt(params[index + 2].substring(1)) : Integer.parseInt(params[index + 2]);
		
		return new BlockPos(x, y, z);
    }
    
    /**
     * Parses a command argument into an {@link Item}
     * 
     * @param the command argument which represents the item
     * @return the item if it was found else null
     */
    public static Item getItem(String param) {
		return Item.getByNameOrId(param);
    }
    
    /**
     * Parses a command argument into a {@link Block}
     * 
     * @param the command argument which represents the block
     * @return the block if it was found else null
     */
    public static Block getBlock(String param) {
		return Block.getBlockFromName(param);
    }
    
    /**
     * Parses a command argument into a {@link Potion}
     * 
     * @param the command argument which represents the potion
     * @return the potion if it was found else null
     */
    public static Potion getPotion(String param) {
		Potion potion = Potion.getPotionFromResourceLocation(param);
		
		if (potion == null) {
			try {potion = Potion.getPotionById(Integer.parseInt(param));}
			catch (NumberFormatException e) {}
		}
		
		return potion;
    }
    
    /**
     * Parses a command argument into a {@link Enchantment}
     * 
     * @param the command argument which represents the enchantment
     * @return the enchantment if it was found else null
     */
    public static Enchantment getEnchantment(String param) {
		Enchantment ench = Enchantment.getEnchantmentByLocation(param);
		
		if (ench == null) {
			try {ench = Enchantment.getEnchantmentByID(Integer.parseInt(param));}
			catch (NumberFormatException e) {}
		}
		
		return ench;
    }
    
    /**
     * Get's a player by its name
     * 
     * @return the player or null if it wasn't found
     */
    public static EntityPlayerMP getPlayer(MinecraftServer server, String param) {
    	return server.getPlayerList().getPlayerByUsername(param);
    }
    
    /**
     * checks whether the command sender is of a certain entity type
     * 
     * @param sender the command sender
     * @param entityType the entity type class
     * @return whether the sender is of the type <b>entityType</b>
     */
    public static <T extends Entity> boolean isSenderOfEntityType(ICommandSender sender, Class<T> entityType) {
    	return entityType.isInstance(sender) ? true : entityType.isInstance(sender.getCommandSenderEntity());
    }
    
    /**
     * Gets the sender as an entity. {@link #isSenderOfEntityType(ICommandSender, Class)} must be true!
     * 
     * @param sender the command sender
     * @param entityType the entity type class
     * @return the sender as entity type <b>entityType</b> or null if  {@link #isSenderOfEntityType(ICommandSender, Class)} is false
     */
    public static <T extends Entity> T getSenderAsEntity(ICommandSender sender, Class<T> entityType) {
    	if (!isSenderOfEntityType(sender, entityType)) return null;
    	else return entityType.isInstance(sender) ? entityType.cast(sender) : entityType.cast(sender.getCommandSenderEntity());
    }
    
    /**
     * Gets the {@link ServerPlayerSettings} corresponding to the given {@link EntityPlayerMP}
     * 
     * @param player the player
     * @return the player settings
     */
    public static ServerPlayerSettings getPlayerSettings(EntityPlayerMP player) {
    	ServerPlayerSettings settings = player.getCapability(PlayerSettings.SETTINGS_CAP_SERVER, null);
    	if (settings == null) settings = PlayerSettings.SETTINGS_CAP_SERVER.getDefaultInstance();
    	return settings;
    }
    
    /**
     * Gets the {@link ClientPlayerSettings} corresponding to the given {@link EntityPlayerSP}<br>
     * <b>IMPORTANT: Available only on client side!!!</b>
     * 
     * @param player the player
     * @return the player settings
     */
    @SideOnly(Side.CLIENT)
    public static ClientPlayerSettings getPlayerSettings(EntityPlayerSP player) {
    	ClientPlayerSettings settings = player.getCapability(PlayerSettings.SETTINGS_CAP_CLIENT, null);
    	if (settings == null) settings = PlayerSettings.SETTINGS_CAP_CLIENT.getDefaultInstance();
    	return settings;
    }
}