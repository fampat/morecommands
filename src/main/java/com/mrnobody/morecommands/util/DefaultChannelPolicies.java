package com.mrnobody.morecommands.util;

import java.util.Set;
import java.util.concurrent.Callable;

import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mrnobody.morecommands.command.CommandSender;
import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.util.ChatChannel.ChannelPolicy;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextFormatting;

/**
 * This class contains some default {@link ChannelPolicy}s
 * 
 * @author MrNobody
 */
public final class DefaultChannelPolicies {
	private DefaultChannelPolicies() {}
	
	public static void registerPolicies() {
		ChatChannel.registerPolicyType(OpenChannelPolicy.NAME, OpenChannelPolicy.CREATE);
		ChatChannel.registerPolicyType(WhitelistChannelPolicy.NAME, WhitelistChannelPolicy.CREATE);
		ChatChannel.registerPolicyType(BlacklistChannelPolicy.NAME, BlacklistChannelPolicy.CREATE);
	}
	
	private static abstract class AbstractChannelPolicy implements ChannelPolicy {
		@Override
		public void kickMessage(EntityPlayerMP player, ChatChannel kickedFrom, ChatChannel newChannel, boolean isPolicyChange) {
			CommandSender sender = new CommandSender(player);
			
			if (isPolicyChange) {
				if (newChannel == null) sender.sendLangfileMessage("command.chatchannel.policychange.nullChannel", TextFormatting.RED, kickedFrom.getName());
				else sender.sendLangfileMessage("command.chatchannel.policychange.newChannel", TextFormatting.RED, kickedFrom.getName(), newChannel.getName());
			}
			else {
				if (newChannel == null) sender.sendLangfileMessage("command.chatchannel.destroyed.nullChannel", TextFormatting.RED, kickedFrom.getName());
				else sender.sendLangfileMessage("command.chatchannel.destroyed.newChannel", TextFormatting.RED, kickedFrom.getName(), newChannel.getName());
			}
		}
	}
	
	/**
	 * Open Channel Policy. Allows everyone to join
	 * 
	 * @author MrNobody98
	 */
	public static final class OpenChannelPolicy extends AbstractChannelPolicy {
		public static final OpenChannelPolicy INSTANCE = new OpenChannelPolicy();
		
		public static final String NAME = "open";
		private static final Callable<ChannelPolicy> CREATE = new Callable<ChannelPolicy>() {
			@Override
			public ChannelPolicy call() throws Exception {
				return INSTANCE;
			}
		};
		
		private OpenChannelPolicy() {}
		
		@Override public void writeToJson(JsonObject object) {}
		@Override public void removeChannel(ChatChannel channel) {}
		@Override public void readFromJson(JsonObject object) {}
		@Override public void addChannel(ChatChannel channel) {}
		
		@Override
		public boolean canPlayerJoin(EntityPlayerMP player) {
			return true;
		}
		
		@Override
		public String getName() {
			return NAME;
		}
		
		@Override
		public String getDescription(EntityPlayerMP player) {
			return LanguageManager.translate(MoreCommands.INSTANCE.getCurrentLang(player), "command.chatchannel.policy.open");
		}
	};
	
	private static abstract class PlayerListChannelPolicy extends AbstractChannelPolicy {
		private Set<ChatChannel> myChannels = Sets.newHashSet();
		private String proposedPolicyChangeChannel;
		protected Set<String> players = Sets.newHashSet();
		
		@Override 
		public void addChannel(ChatChannel channel) {
			this.myChannels.add(channel);
		}
		
		@Override
		public void removeChannel(ChatChannel channel) {
			this.myChannels.remove(channel);
		}

		@Override
		public void readFromJson(JsonObject object) {
			this.players.clear();
			this.proposedPolicyChangeChannel = null;
			
			if (object.has("players") && object.get("players").isJsonArray()) {
				JsonArray players = object.getAsJsonArray("players").getAsJsonArray();
				
				for (JsonElement elem : players) {
					if (!elem.isJsonPrimitive()) continue;
					else this.players.add(elem.getAsString());
				}
			}
			
			this.proposedPolicyChangeChannel = object.has("proposedPolicyChangeChannel") && object.get("proposedPolicyChangeChannel").isJsonPrimitive() ? 
								object.get("proposedPolicyChangeChannel").getAsString() : null;
			
			for (ChatChannel channel : this.myChannels)
				channel.revalidatePolicy(ChatChannel.getChannel(this.proposedPolicyChangeChannel));
		}
		
		@Override
		public void writeToJson(JsonObject object) {
			JsonArray players = new JsonArray();
			for (String playerName : this.players) players.add(new JsonPrimitive(playerName));
			
			object.add("players", players);
			if (this.proposedPolicyChangeChannel != null) object.addProperty("proposedPolicyChangeChannel", this.proposedPolicyChangeChannel);
		}
	}

	/**
	 * Whitelist Channel Policy. Allows only players on the whitelist to join
	 * 
	 * @author MrNobody98
	 */
	public static final class WhitelistChannelPolicy extends PlayerListChannelPolicy {
		public static final String NAME = "whitelist";
		public static final Callable<ChannelPolicy> CREATE = new Callable<ChannelPolicy>() {
			@Override
			public ChannelPolicy call() throws Exception {
				return new WhitelistChannelPolicy();
			}
		};
		
		private WhitelistChannelPolicy() {}
		
		@Override
		public boolean canPlayerJoin(EntityPlayerMP player) {
			return this.players.contains(player.getName());
		}

		@Override
		public String getName() {
			return NAME;
		}

		@Override
		public String getDescription(EntityPlayerMP player) {
			return LanguageManager.translate(MoreCommands.INSTANCE.getCurrentLang(player), "command.chatchannel.policy.whitelist");
		}
	}
	
	/**
	 * Blacklist Channel Policy. Allows only players not on the blacklist to join
	 * 
	 * @author MrNobody98
	 */
	public static final class BlacklistChannelPolicy extends PlayerListChannelPolicy {
		public static final String NAME = "blacklist";
		public static final Callable<ChannelPolicy> CREATE = new Callable<ChannelPolicy>() {
			@Override
			public ChannelPolicy call() throws Exception {
				return new BlacklistChannelPolicy();
			}
		};
		
		private BlacklistChannelPolicy() {}
		
		@Override
		public boolean canPlayerJoin(EntityPlayerMP player) {
			return !this.players.contains(player.getName());
		}

		@Override
		public String getName() {
			return NAME;
		}

		@Override
		public String getDescription(EntityPlayerMP player) {
			return LanguageManager.translate(MoreCommands.INSTANCE.getCurrentLang(player), "command.chatchannel.policy.blacklist");
		}
	}
}