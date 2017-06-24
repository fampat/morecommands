package com.mrnobody.morecommands.command.server;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandException;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.CommandSender;
import com.mrnobody.morecommands.command.ServerCommandProperties;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.event.EventHandler;
import com.mrnobody.morecommands.event.Listeners.EventListener;
import com.mrnobody.morecommands.settings.ServerPlayerSettings;
import com.mrnobody.morecommands.util.ChatChannel;
import com.mrnobody.morecommands.util.ChatChannel.ChannelPolicy;
import com.mrnobody.morecommands.util.DefaultChannelPolicies;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.event.ServerChatEvent;

@Command(
		name = "chatchannel",
		description = "command.chatchannel.description",
		example = "command.chatchannel.example",
		syntax = "command.chatchannel.syntax",
		videoURL = "command.chatchannel.videoURL"
		)
public class CommandChatchannel extends StandardCommand implements ServerCommandProperties, EventListener<ServerChatEvent> {
	public CommandChatchannel() {
		EventHandler.SERVER_CHAT.register(this);
	}
	
	@Override
	public String getCommandName() {
		return "chatchannel";
	}
	
	@Override
	public String getCommandUsage() {
		return "command.chatchannel.syntax";
	}
	
	public void onEvent(ServerChatEvent event) {
		ServerPlayerSettings settings = getPlayerSettings(event.getPlayer());
		
		for (ChatChannel channel : settings.chatChannels) 
			channel.sendChatMessage(event.getComponent(), ChatType.CHAT);
		
		event.setCanceled(true);
	}
	
	@Override
	public String execute(CommandSender sender, String[] params) throws CommandException {
		EntityPlayerMP player = getSenderAsEntity(sender.getMinecraftISender(), EntityPlayerMP.class);
		
		if (params.length <= 0) 
			throw new CommandException("command.generic.invalidUsage", sender, this.getCommandName());
		
		if (params[0].equalsIgnoreCase("list")) {
			if (params.length > 1 && params[1].equalsIgnoreCase("all")) {
				List<ITextComponent> list = Lists.newArrayList();
				buildChannelTree(ChatChannel.getMasterChannel(), list, 0);
				for (ITextComponent msg : list) sender.sendChatComponent(msg);
			}
			else {
				for (ChatChannel channel : getPlayerSettings(player).chatChannels)
					sender.sendChatComponent(new TextComponentString("- " + channel.getName()));
			}
		}
		else if (params.length > 1) {
			String name = params[1];
			
			if (params[0].equalsIgnoreCase("show")) {
				ChatChannel channel = ChatChannel.getChannel(name);
				if (channel == null) throw new  CommandException("command.chatchannel.notFound", sender, name);
				
				sender.sendLangfileMessage("command.chatchannel.name", channel.getName());
				sender.sendLangfileMessage("command.chatchannel.policy", channel.getChannelPolicy().getDescription(player));
				sender.sendLangfileMessage("command.chatchannel.parent", channel.getParent() != null ? channel.getParent().getName() : "none");
				sender.sendLangfileMessage("command.chatchannel.children", Lists.transform(Lists.newArrayList(channel.getChildren()), new Function<ChatChannel, String>() {
					@Override
					public String apply(ChatChannel input) {
						return input.getName();
					}
				}).toString());
			}
			else if (params[0].equalsIgnoreCase("create")) {
				ChatChannel parent = params.length > 2 ? ChatChannel.getChannel(params[2]) : ChatChannel.getMasterChannel();
				if (parent == null) throw new CommandException("command.chatchannel.notFound", sender, params[2]);
				
				boolean receiveParentMessages;
				ChannelPolicy policy;
				
				try {receiveParentMessages = parseTrueFalse(params, 3, true);}
				catch (IllegalArgumentException ex) {throw new CommandException("command.generic.invalidUsage", sender, this.getCommandName());}
				
				Callable<ChannelPolicy> policyCallable = ChatChannel.getChannelPolicy(params.length > 4 ? params[4] : DefaultChannelPolicies.OpenChannelPolicy.NAME);
				if (policyCallable == null) throw new CommandException("command.chatchannel.invalidPolicy", sender, params.length > 4 ? params[4] : "open");
				
				try {policy = policyCallable.call();}
				catch (Exception ex) {throw new CommandException("command.chatchannel.policyException", sender, ex.getMessage());}
				
				if (params.length > 5) {
					String settingsString = rejoinParams(Arrays.copyOfRange(params, 5, params.length));
					JsonParser parser = new JsonParser(); JsonObject policySettings;
					
					try {
						JsonElement parsed = parser.parse(settingsString);
						if (!parsed.isJsonObject()) throw new JsonSyntaxException("not an object");
						policySettings = parsed.getAsJsonObject();
					}
					catch (JsonSyntaxException ex) {throw new CommandException("command.chatchannel.invalidSyntax", sender, settingsString);}
					catch (JsonParseException ex) {throw new CommandException("command.chatchannel.invalidSyntax", sender, settingsString);}
					
					policy.readFromJson(policySettings);
				}
				
				try {ChatChannel.newChatChannel(name, policy, receiveParentMessages, parent, true);}
				catch (ChatChannel.ChannelCreationException ex) {throw new CommandException(ex.getLangFileKey(), sender, ex.getFormatArgs());}
			
				sender.sendLangfileMessage("command.chatchannel.createSuccess", name);
			}
			else if (params[0].equalsIgnoreCase("destroy")) {
				ChatChannel channel = ChatChannel.getChannel(name), proposedNewChannel = null;
				if (channel == null) throw new CommandException("command.chatchannel.notFound", sender, name);

				if (channel == ChatChannel.getMasterChannel())
					throw new CommandException("command.chatchannel.master.destroy", sender);
				
				boolean destroyChildren;

				try {destroyChildren = parseTrueFalse(params, 2, false);}
				catch (IllegalArgumentException ex) {throw new CommandException("command.generic.invalidUsage", sender, this.getCommandName());}
				
				if (params.length > 3) {
					proposedNewChannel = ChatChannel.getChannel(params[3]);
					if (proposedNewChannel == null) throw new CommandException("command.chatchannel.notFound", sender, params[2]);
				}
				
				try {channel.destroyChannel(destroyChildren, proposedNewChannel);}
				catch (IllegalStateException ex) {throw new CommandException("command.chatchannel.destroyException", sender, name, ex.getMessage());}
				
				sender.sendLangfileMessage("command.chatchannel.destroySuccess", name);
			}
			else if (params[0].equalsIgnoreCase("update")) {
				ChatChannel channel = ChatChannel.getChannel(name);
				if (channel == null) throw new CommandException("command.chatchannel.notFound", sender, name);

				if (channel == ChatChannel.getMasterChannel())
					throw new CommandException("command.chatchannel.master.policy", sender);
				
				if (params.length <= 2)
					throw new CommandException("command.generic.invalidUsage", sender, this.getCommandName());
				
				if (params[2].startsWith("{")) {
					String settingsString = rejoinParams(Arrays.copyOfRange(params, 2, params.length));
					JsonParser parser = new JsonParser(); JsonObject policySettings;
					
					try {
						JsonElement parsed = parser.parse(settingsString);
						if (!parsed.isJsonObject()) throw new JsonSyntaxException("not an object");
						policySettings = parsed.getAsJsonObject();
					}
					catch (JsonSyntaxException ex) {throw new CommandException("command.chatchannel.invalidSyntax", sender, settingsString);}
					catch (JsonParseException ex) {throw new CommandException("command.chatchannel.invalidSyntax", sender, settingsString);}
					
					channel.getChannelPolicy().readFromJson(policySettings);
					sender.sendLangfileMessage("command.chatchannel.updated", channel.getName());
				}
				else {
					Callable<ChannelPolicy> policyCallable = ChatChannel.getChannelPolicy(params[2]);
					if (policyCallable == null) throw new CommandException("command.chatchannel.invalidPolicy", sender, params.length > 4 ? params[4] : "open");
					
					ChannelPolicy policy;
					ChatChannel proposedNewChannel = null;
					
					try {policy = policyCallable.call();}
					catch (Exception ex) {throw new CommandException("command.chatchannel.policyException", sender, ex.getMessage());}
					
					if (params.length > 3) {
						String settingsString = rejoinParams(Arrays.copyOfRange(params, 3, params.length));
						JsonParser parser = new JsonParser(); JsonObject policySettings;
						
						try {
							JsonElement parsed = parser.parse(settingsString);
							if (!parsed.isJsonObject()) throw new JsonSyntaxException("not an object");
							policySettings = parsed.getAsJsonObject();
						}
						catch (JsonSyntaxException ex) {throw new CommandException("command.chatchannel.invalidSyntax", sender, settingsString);}
						catch (JsonParseException ex) {throw new CommandException("command.chatchannel.invalidSyntax", sender, settingsString);}
						
						proposedNewChannel = policySettings.get("proposedNewChannel") instanceof JsonPrimitive ? 
									ChatChannel.getChannel(policySettings.get("proposedNewChannel").getAsString()) : null;
						
						policy.readFromJson(policySettings);
					}
					
					channel.setChannelPolicy(policy, proposedNewChannel);
					sender.sendLangfileMessage("command.chatchannel.updated", name);
				}
			}
			else if (params[0].equalsIgnoreCase("join")) {
				ChatChannel channel = ChatChannel.getChannel(name);
				if (channel == null) throw new CommandException("command.chatchannel.notFound", sender, name);
				
				if (!channel.join(player)) throw new CommandException("command.chatchannel.cantJoin", sender, name);
				else sender.sendLangfileMessage("command.chatchannel.joinSuccess", name);
			}
			else if (params[0].equalsIgnoreCase("leave")) {
				ChatChannel channel = ChatChannel.getChannel(name);
				if (channel == null) throw new CommandException("command.chatchannel.notFound", sender, name);
				
				if (!channel.leave(player)) throw new CommandException("command.chatchannel.cantLeave", sender, name);
				else sender.sendLangfileMessage("command.chatchannel.leaveSuccess", name);
			}
		}
		else throw new CommandException("command.generic.invalidUsage", sender, this.getCommandName());
		
		return null;
	}
	
	private static void buildChannelTree(ChatChannel channel, List<ITextComponent> list, int indent) {
		list.add(new TextComponentString(times("  ", indent) + "- " + channel.getName()));
		for (ChatChannel ch : channel.getChildren()) buildChannelTree(ch, list, indent + 1);
	}
	
	private static String times(String str, int n) {
		StringBuilder sb = new StringBuilder();
		while (n-- > 0) sb.append(str);
		return str.toString();
	}
	
	@Override
	public CommandRequirement[] getRequirements() {
		return new CommandRequirement[] {CommandRequirement.PATCH_SERVERCONFIGMANAGER};
	}
	
	@Override
	public ServerType getAllowedServerType() {
		return ServerType.ALL;
	}
	
	@Override
	public int getDefaultPermissionLevel(String[] args) {
		String action = args != null && args.length > 0 ? args[0] : null;
		
		if (action != null) return action.equalsIgnoreCase("join") || action.equalsIgnoreCase("leave") || 
									action.equalsIgnoreCase("list") || action.equalsIgnoreCase("show") ? 0 : 2;
		else return 2;
	}
	
	@Override
	public boolean canSenderUse(String commandName, ICommandSender sender, String[] params) {
		return isSenderOfEntityType(sender, EntityPlayerMP.class);
	}
}
