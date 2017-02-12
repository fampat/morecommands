package com.mrnobody.morecommands.command;

import com.mrnobody.morecommands.command.AbstractCommand.ResultAcceptingCommandSender;
import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.settings.PlayerSettings;
import com.mrnobody.morecommands.settings.ServerPlayerSettings;
import com.mrnobody.morecommands.util.Coordinate;
import com.mrnobody.morecommands.util.LanguageManager;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

/**
 * A wrapper for the {@link ICommandSender} interface
 * 
 * @author MrNobody98
 */
public final class CommandSender {
	/**
	 * A wrapper class to make every kind of entity capable of being an {@link ICommandSender}
	 * 
	 * @author MrNobody98
	 */
	public static class EntityCommandSenderWrapper implements ResultAcceptingCommandSender {
		private final Entity entity;
		private final ICommandSender sender;
		private ChunkCoordinates pos;
		
		/**
		 * Constructs an new {@link EntityCommandSenderWrapper}
		 * 
		 * @param entity the entity to wrap
		 * @param sender the original command sender (e.g. {@link #addChatMessage(IChatComponent)} will delegate to this command sender)
		 * @param coord a fixed position that will be used by {@link #getPlayerCoordinates()}. May be null to use the entity's position
		 */
		public EntityCommandSenderWrapper(Entity entity, ICommandSender sender, Coordinate coord) {
			this.entity = entity;
			this.sender = sender;
			if (coord != null) this.pos = new ChunkCoordinates(coord.getBlockX(), coord.getBlockY(), coord.getBlockZ());
		}

		@Override
		public String getCommandSenderName() {
			return this.entity.getCommandSenderName();
		}
		
		@Override
		public IChatComponent func_145748_c_() {
			return this.entity instanceof ICommandSender ? ((ICommandSender) this.entity).func_145748_c_() : new ChatComponentText(this.getCommandSenderName());
		}

		@Override
		public void addChatMessage(IChatComponent p_145747_1_) {
			this.sender.addChatMessage(p_145747_1_);
		}

		@Override
		public boolean canCommandSenderUseCommand(int p_70003_1_, String p_70003_2_) {
			return this.sender.canCommandSenderUseCommand(p_70003_1_, p_70003_2_);
		}

		@Override
		public ChunkCoordinates getPlayerCoordinates() {
			return this.pos != null ? this.pos : new ChunkCoordinates(MathHelper.floor_double(this.entity.posX), 
					MathHelper.floor_double(this.entity.posY), MathHelper.floor_double(this.entity.posZ));
		}

		@Override
		public net.minecraft.world.World getEntityWorld() {
			return this.entity.worldObj;
		}
		
		public Entity getEntity() {
			return this.entity;
		}

		@Override
		public void setCommandResult(String commandName, String[] args, String result) {
			if (this.sender instanceof ResultAcceptingCommandSender)
				((ResultAcceptingCommandSender) this.sender).setCommandResult(commandName, args, result);
		}
	}
	
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
	 * Constructs a new {@link CommandSender} with a {@link EntityPlayerMP}
	 * 
	 * @param player the {@link EntityPlayerMP}
	 */
	public CommandSender(EntityPlayerMP player) {
		this.sender = player;
	}
	
	/**
	 * @return the command sender's name
	 */
	public String getSenderName() {
		return this.sender.getCommandSenderName();
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
	public void sendChatComponent(IChatComponent component) {
		if (!(this.sender instanceof EntityPlayerMP)) {if (CommandSender.output) this.sender.addChatMessage(component);}
		else if (CommandSender.output) {
			ServerPlayerSettings settings = MoreCommands.getEntityProperties(ServerPlayerSettings.class, PlayerSettings.MORECOMMANDS_IDENTIFIER, (EntityPlayerMP) sender);
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
	public void sendStringMessage(String message, ChatStyle style) {
		ChatComponentText text = new ChatComponentText(message);
		text.setChatStyle(style);
		this.sendChatComponent(text);
	}
	
	/**
	 * Sends a literal string message to the command sender
	 * 
	 * @param message the message to send
	 * @param formatting the chat formatting to use
	 */
	public void sendStringMessage(String message, EnumChatFormatting formatting) {
		ChatComponentText text = new ChatComponentText(message);
		text.getChatStyle().setColor(formatting);
		this.sendChatComponent(text);
	}
	
	/**
	 * Sends a literal string message to the command sender
	 * 
	 * @param message the message to send
	 */
	public void sendStringMessage(String message) {
		this.sendChatComponent(new ChatComponentText(message));
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
	public void sendLangfileMessage(String langFileEntry, ChatStyle style , Object... formatArgs) {
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
	public void sendLangfileMessage(String langFileEntry, EnumChatFormatting formatting , Object... formatArgs) {
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
	public Coordinate getPosition() {
		return new Coordinate(this.sender.getPlayerCoordinates().posX, this.sender.getPlayerCoordinates().posY, this.sender.getPlayerCoordinates().posZ);
	}
	
	/**
	 * @return the command sender's world
	 */
	public World getWorld() {
		return this.sender.getEntityWorld();
	}
}
