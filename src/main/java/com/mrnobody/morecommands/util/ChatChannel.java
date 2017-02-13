package com.mrnobody.morecommands.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.settings.MoreCommandsConfig;
import com.mrnobody.morecommands.settings.PlayerSettings;
import com.mrnobody.morecommands.settings.ServerPlayerSettings;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.FMLCommonHandler;

/**
 * A ChatChannel is a channel to broadcast chat messages.
 * The ChatChannel system is a tree hierarchy meaning that there's
 * a "master" channel of which all other channels are derived from.
 * Therefore a ChatChannel has a parent channel (except for the master channel)
 * and optionally child channels. Unless otherwise specified by a child
 * channel, a parent channel sends all chat messages to its child channels.
 * Every ChatChannel has a ChannelPolicy which defines who is allowed to
 * join this channel. A ChannelPolicy is inherited by a channels parent
 * which means that child channels check all the ChannelPolicies of its 
 * parents to determine whether a player can join.
 * 
 * @author MrNobody98
 *
 */
public class ChatChannel {
	/**
	 * An Exception which says a ChatChannel could not be created. This can
	 * have 2 reasons:<br>
	 *   <li>The policy of the newly created channel is not registered</li>
	 *   <li>A channel with the same name already exists</li>
	 * 
	 * @author MrNobody98
	 */
	public static final class ChannelCreationException extends Exception {
		private String langFileKey;
		private Object[] formatArgs;
		
		/**
		 * Constructs a new ChannelCreationException
		 * 
		 * @param langFileKey the key in the language file to translate the error messsage
		 * @param formatArgs the format args for the error message
		 */
		public ChannelCreationException(String langFileKey, Object... formatArgs) {
			super(LanguageManager.translate("en_US", langFileKey, formatArgs));
			this.langFileKey = langFileKey;
			this.formatArgs = formatArgs;
		}
		
		/**
		 * @return the key in the language file to translate the error messsage
		 */
		public String getLangFileKey() {
			return this.langFileKey;
		}
		
		/**
		 * @return the format args for the error message
		 */
		public Object[] getFormatArgs() {
			return this.formatArgs;
		}
	}
	
	/**
	 * The ChannelPolicy interface defines which players can join a ChatChannel
	 * 
	 * @author MrNobody98
	 */
	public static interface ChannelPolicy {
		/**
		 * @param player the player to check
		 * @return whether the player can join a channel with this policy
		 */
		boolean canPlayerJoin(EntityPlayerMP player);
		
		/**
		 * Notifies this policy that a new ChatChannel has been associated with this policy
		 * @param channel the associated channel
		 */
		void addChannel(ChatChannel channel);
		
		/**
		 * Notifies this policy that a ChatChannel is no longer associated with this policy
		 * @param channel the associated channel
		 */
		void removeChannel(ChatChannel channel);
		
		/**
		 * Reads the policy's settings from a JsonObject
		 * @param object the settings JsonObject
		 */
		void readFromJson(JsonObject object);
		
		/**
		 * Writes the policy's settings to a JsonObject
		 * @param object the settings JsonObject
		 */
		void writeToJson(JsonObject object);
		
		/**
		 * If a policy change occurs for a channel or a channel is destroyed, this method
		 * is invoked to notify the player about a force removal from the channel
		 * 
		 * @param player the removed player
		 * @param ChatChannel kickedFrom the channel from which the player was kicked
		 * @param newChannel the new channel for the player. This may be null if the player can't
		 *                   join any of the possible new channels (usually all parent channels
		 *                   are checked as new channels so if this is null, the player is probably
		 *                   already in a child channel of these channels)
		 * @param isPolicyChange Whether this is a policy change of a channel or whether the channel has been destroyed
		 */
		void kickMessage(EntityPlayerMP player, ChatChannel kickedFrom, ChatChannel newChannel, boolean isPolicyChange);
		
		/**
		 * @return the name of this policy
		 */
		String getName();
		
		/**
		 * Gets a description for this policy
		 * 
		 * @param player the player who requests a description (useful e.g. to determine the player's language)
		 * @return the description
		 */
		String getDescription(EntityPlayerMP player);
	}
	
	private static final String MASTER_CHANNEL_NAME = "MASTER";
	private static final Map<String, Callable<ChannelPolicy>> policies = Maps.newHashMap();
	private static final Map<String, ChatChannel> allChannels = Maps.newHashMap();
	private static MasterChannel masterChannel;
	
	/**
	 * Registers a Callable for a ChannelPolicy
	 * 
	 * @param name the name of the policy. IMPORTANT: This must match {@link ChannelPolicy#getName()}
	 * @param policy the Callable for the ChannelPolicy. This is used to fetch an instance of the ChannelPolicy
	 * @return whether the ChannelPolicy was successfully registered (only false if a policy with the same name has already been registered)
	 */
	public static boolean registerPolicyType(String name, Callable<ChannelPolicy> policy) {
		if (!policies.containsKey(name)) {policies.put(name, policy); return true;}
		return false;
	}
	
	/**
	 * Returns the Callable for a ChannelPolicy
	 * 
	 * @param name the name of the policy
	 * @return the ChannelPolicy Callable if there's one registered to this name, otherwise null
	 */
	public static Callable<ChannelPolicy> getChannelPolicy(String name) {
		return policies.get(name);
	}
	
	/**
	 * Creates and returns a new ChatChannel with the master channel as its parent
	 * 
	 * @param name the name of the channel
	 * @param policyName the name of the channel policy
	 * @param receiveParentMessages whether this channel should receive messages from its parent
	 * @param init whether to immediately initialize this channel
	 * @return the new chat channel
	 * @throws ChannelCreationException if the policy was not registered or a channel with the same name already exists
	 * @throws Exception if the policy was registered but threw an exception during the invocation of {@link Callable#call()}
	 */
	public static ChatChannel newChatChannel(String name, String policyName, boolean receiveParentMessages, boolean init) throws ChannelCreationException, Exception {
		return newChatChannel(policyName, policyName, receiveParentMessages, masterChannel, init);
	}
	
	/**
	 * Creates and returns a new ChatChannel
	 * 
	 * @param name the name of the channel
	 * @param policyName the name of the channel policy
	 * @param receiveParentMessages whether this channel should receive messages from its parent
	 * @param init whether to immediately initialize this channel
	 * @param parent the parent channel of the new ChatChannel
	 * @return the new chat channel
	 * @throws ChannelCreationException if the policy was not registered or a channel with the same name already exists
	 * @throws Exception if the policy was registered but threw an exception during the invocation of {@link Callable#call()}
	 */
	public static ChatChannel newChatChannel(String name, String policyName, boolean receiveParentMessages, ChatChannel parent, boolean init) throws ChannelCreationException, Exception {
		Callable<ChannelPolicy> policy = getChannelPolicy(policyName);
		if (policy == null) throw new ChannelCreationException("command.chatchannel.unregisteredPolicy", policyName);
		return newChatChannel(policyName, policy.call(), receiveParentMessages, parent, init);
	}
	
	/**
	 * Creates and returns a new ChatChannel with the master channel as its parent
	 * 
	 * @param name the name of the channel
	 * @param policy the channel policy
	 * @param receiveParentMessages whether this channel should receive messages from its parent
	 * @param init whether to immediately initialize this channel
	 * @return the new chat channel
	 * @throws ChannelCreationException if the policy was not registered or a channel with the same name already exists
	 */
	public static ChatChannel newChatChannel(String name, ChannelPolicy policy, boolean receiveParentMessages, boolean init) throws ChannelCreationException {
		return newChatChannel(name, policy, receiveParentMessages, masterChannel, init);
	}
	
	/**
	 * Creates and returns a new ChatChannel
	 * 
	 * @param name the name of the channel
	 * @param policy the channel policy
	 * @param receiveParentMessages whether this channel should receive messages from its parent
	 * @param parent the parent channel of the new ChatChannel
	 * @param init whether to immediately initialize this channel
	 * @return the new chat channel
	 * @throws ChannelCreationException if the policy was not registered or a channel with the same name already exists
	 */
	public static ChatChannel newChatChannel(String name, ChannelPolicy policy, boolean receiveParentMessages, ChatChannel parent, boolean init) throws ChannelCreationException {
		ChatChannel channel = new ChatChannel(name, policy, receiveParentMessages);
		if (!channel.setParent(parent)) throw new ChannelCreationException("command.chatchannel.invalidParent", parent.getName());
		
		if (init) channel.initChannel();
		return channel;
	}
	
	/**
	 * @return Returns the master channel. WARNING: This is null if the ChatChannels haven't yet been loaded
	 *         via {@link #loadChannelsFromSettings(File)} or have been saved via {@link #saveChannelsToSettings(File)}.
	 *         You can check this via {@link #channelsLoaded()}
	 */
	public static ChatChannel getMasterChannel() {
		return masterChannel;
	}
	
	/**
	 * Returns a ChatChannel which is still active
	 * 
	 * @param name the name of the channel
	 * @return the channel or null if it doesn't exist or has been destroyed
	 */
	public static ChatChannel getChannel(String name) {
		return allChannels.get(name);
	}
	
	/**
	 * @return All active ChatChannels
	 */
	public static Map<String, ChatChannel> getAllChannels() {
		return ImmutableMap.copyOf(allChannels);
	}
	
	protected ChatChannel parent;
	protected final Set<ChatChannel> children = Sets.newHashSet();
	protected final String name;
	protected final Set<EntityPlayerMP> members = Sets.newHashSet();
	protected ChannelPolicy policy;
	protected boolean receiveParentMessages;
	private boolean initialized = false, destroyed = false;
	
	/**
	 * Constructs a new ChatChannel
	 * 
	 * @param name the name of this channel
	 * @param policy the channel policy
	 * @param receiveParentMessages whether to receive messages from the parent channel
	 * @throws ChannelCreationException if the policy was not registered or a channel with the same name already exists
	 */
	protected ChatChannel(String name, ChannelPolicy policy, boolean receiveParentMessages) throws ChannelCreationException {
		this.name = name;
		this.policy = policy;
		this.receiveParentMessages = receiveParentMessages;
		
		if (allChannels.containsKey(name))
			throw new ChannelCreationException("command.chatchannel.existsAlready", name);
		
		if (!policies.containsKey(policy.getName()))
			throw new ChannelCreationException("command.chatchannel.unregisteredPolicy", policy.getName());
	}
	
	/**
	 * Initializes this ChatChannel (Registers it to its parent and children, notifies the ChannelPolicy, etc.)<br>
	 * If a ChatChannel has not been initialized, it can't be used.<br>
	 * WARNING: If this ChatChannel has already been destroyed, it can't be re-initialized
	 * 
	 * @throws IllegalStateException if this method is invoked although this ChatChannel has been destroyed
	 */
	public void initChannel() throws IllegalStateException {
		if (this.destroyed)
			throw new IllegalStateException("Tried to initialize a destroyed ChatChannel");
		
		if (this.initialized) 
			return;
		
		this.policy.addChannel(this);
		this.initialized = true;
		allChannels.put(this.name, this);
		
		if (this.parent != null) this.parent.children.add(this);
		for (ChatChannel child : this.children) child.parent = this;
	}
	
	/**
	 * Destroys this channel (Unregisters it from its parent and children, notifies the ChannelPolicy, etc.)<br>
	 * If a ChatChannel has been destroyed it can't be used anymore.<br>
	 * This kicks all players from this channel and either moves them to an appropriate parent channel
	 * or to a proposed new channel but only if they can join such a channel via {@link #canJoin(EntityPlayerMP)}
	 * 
	 * @param destroyChildren whether to destroy the children of this channel as well. If you set this
	 *                        to false, the parent of all children will be set to the parent of this channel
	 * @param proposedNewChannel the proposed new channel for the players. May be null to use the first appropriate parent channel
	 * @throws IllegalStateException if this channel hasn't yet been initialized
	 */
	public void destroyChannel(boolean destroyChildren, ChatChannel proposedNewChannel) throws IllegalStateException {
		if (!this.initialized)
			throw new IllegalStateException("Tried to destroy a non-initialized ChatChannel");
		
		if (this.destroyed)
			return;
		
		this.policy.removeChannel(this);
		this.destroyed = true;
		allChannels.remove(this.name);
		
		if (this.parent != null) this.parent.children.remove(this);
		for (ChatChannel child : Sets.newHashSet(this.children)) {   //copy to prevent exception during removal of elements in the destroyChannel() call on child
			child.parent = this.parent; if (this.parent != null) this.parent.children.add(child);
			if (destroyChildren) child.destroyChannel(true, proposedNewChannel);
		}
		
		this.removePlayersAndFindNewChannel(proposedNewChannel, false);
	}
	
	/**
	 * @return whether this channel was destroyed via {@link #destroyChannel(boolean, ChatChannel)}
	 */
	public boolean isDestroyed() {
		return this.destroyed;
	}
	
	/**
	 * @return whether this channel was initialized via {@link #initChannel()}
	 */
	public boolean isInitialized() {
		return this.initialized;
	}
	
	/**
	 * Checks for a valid state (initialized but not destroyed)
	 */
	private void checkState() {
		if (!this.initialized) throw new IllegalStateException("ChatChannel not initialized");
		if (this.destroyed) throw new IllegalStateException("ChatChannel destroyed");
	}
	
	/**
	 * Adds a child channel to this channel<br>
	 * This is not possible if the child is an indirect (or direct) parent of this channel
	 * 
	 * @param child the child
	 * @return whether the channel could be added as a child
	 */
	public boolean addChild(ChatChannel child) {
		if (isIndirectParent(child) || this.equals(child))
			return false;
		else {
			this.children.add(child);
			child.parent = this;
			return true;
		}
	}

	/**
	 * Removes a child channel
	 * 
	 * @param child the child channels
	 * @param destroyHierarchy whether to destroy all child channels of the child (otherwise they will be made to children of this channel)
	 * @param proposedNewChannel the proposed new channel for players of the child channel (may be null to use the first appropriate parent)
	 * @return whether the given channel actually is a child of this channel and the removal was successful
	 */
	public boolean removeChild(ChatChannel child, boolean destroyHierarchy, ChatChannel proposedNewChannel) {
		if (!this.children.contains(child))
			return false;
		else {
			child.destroyChannel(destroyHierarchy, proposedNewChannel);
			return true;
		}
	}
	
	/**
	 * Sets the parent of this channel<br>
	 * This is not possible if the parent is an indirect (or direct) child of this channel
	 * 
	 * @param parent the parent
	 * @return whether the channel could be set as a parent
	 */
	public boolean setParent(ChatChannel parent) {
		if (isIndirectChild(parent) || this.equals(parent))
			return false;
		else {
			if (this.parent != null) this.parent.children.remove(this);
			this.parent = parent;
			this.parent.children.add(this);
			return true;
		}
	}
	
	/**
	 * Tests whether a channel is the direct parent of this channel
	 * 
	 * @param channel the channel to test
	 * @return whether the channel is the direct parent of this channel
	 */
	public boolean isDirectParent(ChatChannel channel) {
		return this.parent != null ? this.parent.equals(channel) : false;
	}
	
	/**
	 * Tests whether a channel is an indirect parent of this channel
	 * 
	 * @param channel the channel to test
	 * @return whether the channel is an indirect parent of this channel
	 */
	public boolean isIndirectParent(ChatChannel channel) {
		ChatChannel parent = this.parent;
		
		while (parent != null) {
			if (parent.equals(channel)) return true;
			parent = parent.parent;
		}
		
		return false;
	}
	
	/**
	 * Tests whether a channel is a direct child of this channel
	 * 
	 * @param channel the channel to test
	 * @return whether the channel is a direct child of this channel
	 */
	public boolean isDirectChild(ChatChannel channel) {
		return this.children.contains(channel);
	}
	
	/**
	 * Tests whether a channel is an indirect child of this channel
	 * 
	 * @param channel the channel to test
	 * @return whether the channel is an indirect child of this channel
	 */
	public boolean isIndirectChild(ChatChannel channel) {
		return this.isIndirectChildImpl(this.children, channel);
	}
	
	//the recursive implementation of isIndirectChild()
	private boolean isIndirectChildImpl(Set<ChatChannel> children, ChatChannel channel) {
		for (ChatChannel child : children)
			if (child.equals(channel) || isIndirectChildImpl(child.children, channel)) return true;
		
		return false;
	}
	
	/**
	 * When a player respawns (e.g. due to death), the EntityPlayerMP instance is recreated.<br>
	 * Therefore all references to the old instance have to be replaced and this is what this
	 * method is intended for.
	 * 
	 * @param original the original player instance
	 * @param player the new player instance
	 */
	public void replaceRespawnedPlayer(EntityPlayerMP original, EntityPlayerMP player) {
		if (original.equals(player) && this.members.remove(original))
			this.members.add(player);
	}
	
	/**
	 * @return the channel policy
	 */
	public ChannelPolicy getChannelPolicy() {
		return this.policy;
	}
	
	/**
	 * Sets a new channel policy for this channel and revalidates whether
	 * players are allowed to be in this channel
	 * 
	 * @param policy the new policy
	 * @param proposedNewChannel if players have to be kicked because of the new policy, this is a proposed new channel (if null the first appropriate parent channel will be used)
	 */
	public void setChannelPolicy(ChannelPolicy policy, ChatChannel proposedNewChannel) {
		this.policy.removeChannel(this);
		this.policy = policy;
		this.policy.addChannel(this);
		
		revalidatePolicy(proposedNewChannel);
	}
	
	/**
	 * Revalidates whether players are allowed to be in this channel,
	 * e.g. after a policy change
	 * 
	 * @param proposedNewChannel if players have to be kicked because of a policy change, this is a proposed new channel (if null the first appropriate parent channel will be used)
	 */
	public void revalidatePolicy(ChatChannel proposedNewChannel) {
		revalidateChildren(Sets.newHashSet(this), proposedNewChannel);
	}
	
	/**
	 * A revalidatePolicy call will also have an effect on all children since
	 * policies are inherited -> revalidate all children
	 * 
	 * @param children the set of children to revalidates
	 * @param proposedNewChannel a proposed new channel for players (if null the first appropriate parent channel will be used)
	 */
	private void revalidateChildren(Set<ChatChannel> children, ChatChannel proposedNewChannel) {
		for (ChatChannel child : children) {
			for (EntityPlayerMP player : Sets.newHashSet(child.members))   //copy to prevent exception during removal in removePlayerAndFindNewChannel
				if (!this.policy.canPlayerJoin(player))
					removePlayerAndFindNewChannel(child, player, this.parent, proposedNewChannel, true);
			
			revalidateChildren(child.children, proposedNewChannel);
		}
	}
	
	/**
	 * Removes players from a channel
	 * 
	 * @param proposedNewChannel a proposed new channel for players (if null the first appropriate parent channel will be used)
	 * @param isPolicyChange whether the removal is due to a policy change
	 */
	private void removePlayersAndFindNewChannel(ChatChannel proposedNewChannel, boolean isPolicyChange) {
		this.removePlayersAndFindNewChannel(this, ImmutableSet.copyOf(this.members), this.parent, proposedNewChannel, isPolicyChange);
	}
	
	/**
	 * Removes players from a channel
	 * 
	 * @param playerChannel the channel to remove the players from
	 * @param players the players to remove
	 * @param initialParent the parent to start looking from whether a player can join
	 * @param proposedNewChannel a proposed new channel for a player (if null the first appropriate parent channel will be used)
	 * @param isPolicyChange whether the removal is due to a policy change
	 */
	private void removePlayersAndFindNewChannel(ChatChannel playerChannel, Set<EntityPlayerMP> players, ChatChannel initialParent, ChatChannel proposedNewChannel, boolean isPolicyChange) {
		for (EntityPlayerMP player : players)
			removePlayerAndFindNewChannel(playerChannel, player, initialParent, proposedNewChannel, isPolicyChange);
	}
	
	/**
	 * Removes a player from a channel
	 * 
	 * @param playerChannel the channel to remove the players from
	 * @param player the player to remove
	 * @param initialParent the parent to start looking from whether a player can join
	 * @param proposedNewChannel a proposed new channel for a player (if null the first appropriate parent channel will be used)
	 * @param isPolicyChange whether the removal is due to a policy change
	 * @return the new ChatChannel for the player (may be null if no appropriate channel was found)
	 */
	private ChatChannel removePlayerAndFindNewChannel(ChatChannel playerChannel, EntityPlayerMP player, ChatChannel initialParent, ChatChannel proposedNewChannel, boolean isPolicyChange) {
		removePlayer(playerChannel, player);
		ChatChannel parent = initialParent;
		
		if (proposedNewChannel != null && proposedNewChannel.join(player)) {
			this.policy.kickMessage(player, playerChannel, proposedNewChannel, isPolicyChange);
			return proposedNewChannel;
		}
		
		while (parent != null) {
			if (parent.join(player)) {
				this.policy.kickMessage(player, playerChannel, parent, isPolicyChange);
				return parent;
			}
			
			parent = parent.parent;
		}
		
		this.policy.kickMessage(player, playerChannel, null, isPolicyChange);
		return null;
	}
	
	/**
	 * Sends a chat message to this channel and, if a child wants to
	 * receive messages from its parent, also to all child channels.
	 * 
	 * @param message the message
	 * @param msgType the message type
	 */
	public void sendChatMessage(ITextComponent message, byte msgType) {
		checkState();
		ITextComponent msgToSend = message;
		
		if (MoreCommandsConfig.prefixChannelName)
			msgToSend = new TextComponentString("[" + this.getName() + "] ").appendSibling(message);
		
		SPacketChat packet = new SPacketChat(msgToSend, msgType);
		
		for (EntityPlayerMP player : this.members)
			player.connection.sendPacket(packet);
		
		for (ChatChannel child : this.children)
			if (child.receiveParentMessages)
				child.sendChatMessage(message, msgType);
	}
	
	/**
	 * Leaves this channel
	 * 
	 * @param player the player to leave
	 * @return whether the player was in this channel
	 */
	public boolean leave(EntityPlayerMP player) {
		checkState();
		return removePlayer(this, player);
	}
	
	/**
	 * Checks whether a player can join this channel.<br>
	 * This is the case if he is not in any of the parent channels
	 * and not in any of the child channels
	 * 
	 * @param player the player to join
	 * @return whether the player can join
	 */
	public boolean canJoin(EntityPlayerMP player) {
		checkState();
		return !isInChannelHierarchy(player);
	}
	
	/**
	 * Checks whether a player is member of a parent or a child channel
	 * 
	 * @param player the player to check
	 * @return whether the player is member of a parent or a child channel
	 */
	private boolean isInChannelHierarchy(EntityPlayerMP player) {
		ChatChannel parent = this;
		
		while (parent != null) {
			if (parent.isChannelMember(player) || !parent.policy.canPlayerJoin(player)) return true;
			parent = parent.parent;
		}
		
		return checkChildren(this.children, player);
	}
	
	/**
	 * Checks whether a player is member of a set of child channels
	 * 
	 * @param children the set of child channels
	 * @param player the player
	 * @return whether the player is member of a set of child channels
	 */
	private boolean checkChildren(Set<ChatChannel> children, EntityPlayerMP player) {
		for (ChatChannel child : children)
			if (child.isChannelMember(player) || checkChildren(child.children, player)) return true;
		
		return false;
	}
	
	/**
	 * Joins this channel.<br>
	 * This method makes a call to {@link #canJoin(EntityPlayerMP)} so you can
	 * omit a call to this method and directly try to join
	 * 
	 * @param player the player to join
	 * @return whether the player can join (See {@link #canJoin(EntityPlayerMP)})
	 */
	public boolean join(EntityPlayerMP player) {
		checkState();
		
		if (this.canJoin(player))
			return addPlayer(this, player);
		else
			return false;
	}
	
	/**
	 * Adds a player to a channel
	 * 
	 * @param channel the channel to add the player to
	 * @param player the player to add
	 * @return whether the player was successfully added
	 */
	private boolean addPlayer(ChatChannel channel, EntityPlayerMP player) {
		if (channel.members.add(player)) {
			ServerPlayerSettings settings = player.getCapability(PlayerSettings.SETTINGS_CAP_SERVER, null);
			if (settings != null) settings.chatChannels.add(channel);
			return true;
		}
		else return false;
	}
	
	/**
	 * Removes a player from a channel
	 * 
	 * @param channel the channel to remove the player from
	 * @param player the player to remove
	 * @return whether the player was successfully removed
	 */
	private boolean removePlayer(ChatChannel channel, EntityPlayerMP player) {
		if (channel.members.remove(player)) {
			ServerPlayerSettings settings = player.getCapability(PlayerSettings.SETTINGS_CAP_SERVER, null);
			if (settings != null) settings.chatChannels.remove(channel);
			return true;
		}
		else return false;
	}
	
	/**
	 * Checks whether a player is a channel member
	 * 
	 * @param player the player to check
	 * @return whether the player is a channel member
	 */
	public boolean isChannelMember(EntityPlayerMP player) {
		return this.members.contains(player);
	}
	
	/**
	 * @return whether to receive messages from the parent channel
	 */
	public boolean receiveParentMessages() {
		return this.receiveParentMessages;
	}
	
	/**
	 * Sets whether to receive messages from the parent channel
	 * 
	 * @param receiveParentMessages whether to receive messages from the parent channel
	 */
	public void setReceiveParentMessages(boolean receiveParentMessages) {
		this.receiveParentMessages = receiveParentMessages;
	}
	
	/**
	 * @return the name of this channel. All names are unique
	 */
	public String getName() {
		return this.name;
	}
	
	/**
	 * @return An immutable set of all children of this channel
	 */
	public Set<ChatChannel> getChildren() {
		return ImmutableSet.copyOf(this.children);
	}
	
	/**
	 * @return the parent channel of this channel
	 */
	public ChatChannel getParent() {
		return this.parent;
	}
	
	/**
	 * @return An immutable set of all members of this channel
	 */
	public Set<EntityPlayerMP> getMembers() {
		return ImmutableSet.copyOf(this.members);
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		else if (o instanceof ChatChannel) return ((ChatChannel) o).name.equals(this.name); //channel names are unique
		else return false;
	}
	
	@Override
	public int hashCode() {
		return this.name.hashCode();
	}
	
	@Override
	public String toString() {
		return this.name;
	}
	
	private static final class MasterChannel extends ChatChannel {
		private MasterChannel(String name) throws ChannelCreationException {
			super(name, DefaultChannelPolicies.OpenChannelPolicy.INSTANCE, false);
		}
		
		@Override 
		public boolean setParent(ChatChannel channel) {
			throw new IllegalStateException("Can't set a parent for the Master Channel");
		}
		
		@Override 
		public void destroyChannel(boolean destroyChildren, ChatChannel proposedNewChannel) {
			throw new IllegalStateException("Can't destroy the Master Channel");
		}
		
		@Override
		public void setChannelPolicy(ChannelPolicy policy, ChatChannel proposedNewChannel) {
			throw new IllegalStateException("Can't set a policy for the Master Channel");
		}
		
		@Override
		public void sendChatMessage(ITextComponent message, byte msgType) {
			FMLCommonHandler.instance().getMinecraftServerInstance().addChatMessage(message);
			super.sendChatMessage(message, msgType);
		}
		
		private void destroyMasterForSaveOrLoad() {
			super.destroyChannel(true, null);
		}
	}
	
	private static boolean channelsLoaded = false;
	
	/**
	 * @return Whether channels have been loaded from a settings file
	 */
	public static boolean channelsLoaded() {
		return channelsLoaded;
	}
	
	/**
	 * Loads all Chat Channels from a settings file
	 * 
	 * @param settingsFile the settings file in JSON format
	 */
	public static void loadChannelsFromSettings(File settingsFile) {
		if (channelsLoaded) return;
		if (masterChannel != null) masterChannel.destroyMasterForSaveOrLoad();
		
		if (!allChannels.isEmpty())
			allChannels.clear();
		
		try {masterChannel = new MasterChannel(MASTER_CHANNEL_NAME); masterChannel.initChannel();}
		catch (ChannelCreationException ex) {ex.printStackTrace(); return;} //Should never happen
		
		JsonReader reader = null;
		
		if (settingsFile.exists() && settingsFile.isFile()) {
			try {
				JsonParser p = new JsonParser();
				JsonElement root = p.parse(reader = new JsonReader(new InputStreamReader(new FileInputStream(settingsFile))));
				
				if (!root.isJsonArray())
					throw new JsonSyntaxException("Chat Channels must be a Json Array");
				
				readChildren(root.getAsJsonArray(), masterChannel);
			}
			catch (IOException ex) {MoreCommands.INSTANCE.getLogger().info("Couldn't read chat channels due to the following exception: ", ex);}
			catch (JsonIOException ex) {MoreCommands.INSTANCE.getLogger().info("Couldn't read chat channels due to the following exception: ", ex);}
			catch (JsonSyntaxException ex)  {MoreCommands.INSTANCE.getLogger().info("Invalid syntax in chat channels file: " + ex.getMessage());}
			finally {try {if (reader != null) reader.close();} catch (IOException ex) {}}
		}
		
		channelsLoaded = true;
	}

	/**
	 * Reads child channels
	 * 
	 * @param children the {@link JsonArray} of children
	 * @param parent the parent channel
	 */
	private static void readChildren(JsonArray children, ChatChannel parent) {
		for (JsonElement elem : children) {
			if (!elem.isJsonObject()) continue;
			JsonObject channel = elem.getAsJsonObject();
			
			if (!channel.has("name") || !channel.get("name").isJsonPrimitive()) continue;
			String name = channel.get("name").getAsString();
			
			boolean receiveParentMessages = channel.has("receiveParentMessages") ? 
											channel.get("receiveParentMessages").isJsonPrimitive() ? 
											channel.get("receiveParentMessages").getAsBoolean() : true : true;
			
			ChannelPolicy policy;
			ChatChannel child;
			
			try {policy = channel.has("policy") && channel.get("policy").isJsonObject() ? readPolicy(channel.get("policy").getAsJsonObject()) : DefaultChannelPolicies.OpenChannelPolicy.INSTANCE;}
			catch (Exception ex) {MoreCommands.INSTANCE.getLogger().warn("Illegal chat channel policy in chat channel file", ex); continue;}
			
			try {child = newChatChannel(name, policy, receiveParentMessages, parent, true);}
			catch (Exception ex) {MoreCommands.INSTANCE.getLogger().warn("Illegal chat channel in chat channel file", name); continue;}
			
			if (channel.has("children") && channel.get("children").isJsonArray())
				readChildren(channel.get("children").getAsJsonArray(), child);
		}
	}
	
	/**
	 * Reads a policy from a {@link JsonObject}
	 * 
	 * @param object the {@link JsonObject}
	 * @return the policy
	 */
	private static ChannelPolicy readPolicy(JsonObject object) {
		if (!object.has("type") || !object.get("type").isJsonPrimitive()) throw new IllegalArgumentException("policy has no valid 'type' attribute");
		String type = object.get("type").getAsString();
		
		if (!policies.containsKey(type)) throw new IllegalArgumentException("policy type " + type + " is not registered");
		ChannelPolicy policy;
		
		try {policy = policies.get(type).call();}
		catch (Exception ex) {throw new IllegalStateException("This should not happen", ex);}
		
		object.remove("type");
		policy.readFromJson(object);
		
		return policy;
	}
	
	/**
	 * Saves all Chat Channels to a settings file
	 * 
	 * @param settingsFile the settings file in JSON format
	 */
	public static void saveChannelsToSettings(File settingsFile) {
		if (!channelsLoaded || masterChannel == null) return;
		JsonArray children = saveChildren(masterChannel.children);
		
		String out = new GsonBuilder().setPrettyPrinting().create().toJson(children);
		OutputStreamWriter w = null;
		
		try {
			if (!settingsFile.exists() || !settingsFile.isFile()) settingsFile.createNewFile();
			w = new OutputStreamWriter(new FileOutputStream(settingsFile), "UTF-8");
			
			w.write(out); w.flush(); w.close();
		}
		catch (IOException ex) {MoreCommands.INSTANCE.getLogger().info("Couldn't save chat channels due to the following exception: ", ex);}
		finally {try {if (w != null) w.close();} catch (IOException ex) {}}
		
		masterChannel.destroyMasterForSaveOrLoad();
		masterChannel = null;
		
		if (!allChannels.isEmpty())
			allChannels.clear();
		
		channelsLoaded = false;
	}
	
	/**
	 * Saves child channels in a {@link JsonArray}
	 * 
	 * @param children the set of child channels
	 * @return the {@link JsonArray} of child channels
	 */
	private static JsonArray saveChildren(Set<ChatChannel> children) {
		JsonArray array = new JsonArray();
		
		for (ChatChannel child : children) {
			JsonObject channel = new JsonObject();
			
			channel.addProperty("name", child.name);
			if (!child.receiveParentMessages) channel.addProperty("receiveParentMessages", child.receiveParentMessages);
			
			JsonObject policy = new JsonObject();
			child.policy.writeToJson(policy);
			
			policy.addProperty("type", child.policy.getName());
			channel.add("policy", policy);	
			
			if (!child.children.isEmpty())
				channel.add("children", saveChildren(child.children));
			
			array.add(channel);
		}
		
		return array;
	}
}
