package com.mrnobody.morecommands.command.server;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandException;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.CommandSender;
import com.mrnobody.morecommands.command.MultipleCommands;
import com.mrnobody.morecommands.command.ServerCommandProperties;
import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.settings.MoreCommandsConfig;
import com.mrnobody.morecommands.util.LanguageManager;
import com.mrnobody.morecommands.util.Reference;
import com.mrnobody.morecommands.util.TargetSelector;

import net.minecraft.command.CommandResultStats;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTBase;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.UserListOpsEntry;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Command.MultipleCommand(
		description = {"command.execute.description", "command.execute.confirm.description", "command.execute.deny.description", "command.execute.requests.description"},
		example = {"command.execute.example", "command.execute.confirm.example", "command.execute.deny.example", "command.execute.requests.example"},
		name = {"execute", "exec_confirm", "exec_deny", "exec_requests"},
		syntax = {"command.execute.syntax", "command.execute.confirm.syntax", "command.execute.deny.syntax", "command.execute.requests.syntax"},
		videoURL = {"command.execute.videoURL", "command.execute.videoURL", "command.execute.videoURL", "command.execute.videoURL"}
		)
public class CommandExecute extends MultipleCommands implements ServerCommandProperties {
	private static final ResourceLocation REQUESTS_KEY = new ResourceLocation(Reference.MODID, "exec_requests");
	
	@CapabilityInject(CommandExecutionRequests.class)
	private static final Capability<CommandExecutionRequests> EXEC_REQUESTS_CAP = null;
	
	static {
		CapabilityManager.INSTANCE.register(CommandExecutionRequests.class, new Capability.IStorage<CommandExecutionRequests>() {
			@Override public NBTBase writeNBT(Capability<CommandExecutionRequests> capability, CommandExecutionRequests instance, EnumFacing side) {return null;}
			@Override public void readNBT(Capability<CommandExecutionRequests> capability, CommandExecutionRequests instance, EnumFacing side, NBTBase nbt) {}
		}, new Callable<CommandExecutionRequests>() {
			@Override
			public CommandExecutionRequests call() throws Exception {
				throw new UnsupportedOperationException("Default instance not supported");
			}
		});
		
		MinecraftForge.EVENT_BUS.register(AttachCapsHandler.INSTANCE);
	}
	
	public CommandExecute() {
		super();
	}
	
	public CommandExecute(int typeIndex) {
		super(typeIndex);
	}
	
	@Override
	public String[] getCommandNames() {
		return new String[] {"execute", "exec_confirm", "exec_deny", "exec_requests"};
	}
	
	@Override
	public String[] getCommandUsages() {
		return new String[] {"command.execute.syntax", "command.execute.confirm.syntax", "command.execute.deny.syntax", "command.execute.requests.syntax"};
	}
	
	@Override
	public String execute(String commandName, final CommandSender sender, String[] params) throws CommandException {
		if (!commandName.equals("execute")) {
			String action = commandName.split("_")[1];
			EntityPlayerMP player = getSenderAsEntity(sender.getMinecraftISender(), EntityPlayerMP.class);
			
			if (action.equals("confirm") || action.equals("deny")) {
				Pair<ICommandSender, ExecutionRequest> request = null;
				
				if (params.length > 0) {
					try {request = getAndRemoveRequest(player, Integer.parseInt(params[0]));}
					catch (NumberFormatException nfe) {throw new CommandException("command.generic.NaN", sender, this.getCommandName());}
				}
				else request = getAndRemoveLatestRequest(player);
				
				if (request == null) throw new CommandException("command.execute.invalidRequest", sender, params.length > 0 ? Integer.parseInt(params[0]) : 0);
				else {
					if (action.equals("confirm")) executeCommand(player, request.getLeft(), request.getRight().relCoord, request.getRight().command, true);
					else new CommandSender(request.getLeft()).sendLangfileMessage("command.execute.denial", TextFormatting.RED, player.getName(), request.getRight().command);
				}
			}
			else {
				sender.sendLangfileMessage("command.execute.reqHeader");
				
				for (Map.Entry<String, ExecutionRequest> req : getRequests(player).entries())
					sender.sendLangfileMessage("command.execute.request", req.getValue().requestID, req.getValue().command, req.getKey());
				
				sender.sendLangfileMessage("command.execute.reqFooter");
			}
			
			return null;
		}
		
		params = reparseParamsWithNBTData(params); BlockPos relCoord = null;
		
		if (params.length <= 1)
			throw new CommandException("command.generic.invalidUsage", sender, this.getCommandName());
		
		if (params[1].equalsIgnoreCase("rel") || params[1].equalsIgnoreCase("relative")) {
			if (params.length <= 5) throw new CommandException("command.generic.invalidUsage", sender, this.getCommandName());
			
			try {relCoord = getCoordFromParams(sender.getMinecraftISender(), params, 2);}
			catch (NumberFormatException nfe) {}
		}
		
		String target = params[0]; params = Arrays.copyOfRange(params, relCoord == null ? 1 : 5, params.length);
		String command = rejoinParams(params);
		
		if (isTargetSelector(target)) {
			if (target.startsWith("@b")) throw new CommandException("command.execute.invalidTarget", sender);
			List<? extends Entity> entities = TargetSelector.EntitySelector.matchEntites(sender.getMinecraftISender(), target, Entity.class);
			int tooManyRequests = 0;
			
			for (Entity entity : entities)
				tooManyRequests += executeCommand(entity, sender.getMinecraftISender(), relCoord, command, false) ? 0 : 1;
		
			if (tooManyRequests > 0)
				throw new CommandException("command.execute.tooManyRequests", sender, tooManyRequests);
		}
		else {
			EntityPlayerMP player = getPlayer(sender.getServer(), target);
			if (player == null) throw new CommandException("command.execute.playerNotFound", sender, target);
			
			if (!executeCommand(player, sender.getMinecraftISender(), relCoord, command, false))
				throw new CommandException("command.execute.tooManyRequests", sender, 1);
		}
		
		return null;
	}
	
	private boolean executeCommand(Entity entity, ICommandSender sender, BlockPos relCoord, String command, boolean force) {
		if (!force && MoreCommandsConfig.useExecRequests && entity instanceof EntityPlayerMP && playerHasRequestLevel((EntityPlayerMP) entity)) {
			int request = -1; if ((request = addRequest(entity, sender, relCoord, command)) < 0) return false;
			
			TextComponentString confirmText = new TextComponentString(LanguageManager.translate(
					MoreCommands.INSTANCE.getCurrentLang((EntityPlayerMP) entity), "command.execute.confirmText", sender.getName(), command, request));
			
			confirmText.getChatStyle().setColor(TextFormatting.GREEN);
			((EntityPlayerMP) entity).addChatMessage(confirmText);
			
			new CommandSender(sender).sendLangfileMessage("command.execute.reqAdded");
		}
		else sender.getServer().getCommandManager().executeCommand(wrapSender(entity, sender, relCoord), command);
		
		return true;
	}
	
	private static boolean playerHasRequestLevel(EntityPlayerMP player) {
		if (MoreCommandsConfig.minExecRequestLevel == 0) return true;
		else {
			if (player.getServer().getPlayerList().canSendCommands(player.getGameProfile())) {
				UserListOpsEntry entry = (UserListOpsEntry) player.getServer().getPlayerList().getOppedPlayers().getEntry(player.getGameProfile());
				return entry != null ? entry.getPermissionLevel() >= MoreCommandsConfig.minExecRequestLevel : player.getServer().getOpPermissionLevel() >= MoreCommandsConfig.minExecRequestLevel;
			}
			else return false;
		}
	}
	
	private static ICommandSender wrapSender(final Entity entity, final ICommandSender sender, final BlockPos relCoord) {
		return new ICommandSender() {
			public String getName() {return entity.getName();}
			public ITextComponent getDisplayName() {return entity.getDisplayName();}
			public void addChatMessage(ITextComponent message) {sender.addChatMessage(message);}
			public boolean canCommandSenderUseCommand(int permLevel, String commandName) {return sender.canCommandSenderUseCommand(permLevel, commandName);}
			public BlockPos getPosition(){return relCoord == null ? this.getCommandSenderEntity().getPosition() : relCoord;}
			public Vec3d getPositionVector() {return new Vec3d(this.getPosition().getX(), this.getPosition().getY(), this.getPosition().getZ());}
			public World getEntityWorld() {return this.getCommandSenderEntity().worldObj;}
			public Entity getCommandSenderEntity() {return entity;}
			public boolean sendCommandFeedback() {return sender.getServer() == null || sender.getServer().worldServers[0].getGameRules().getBoolean("commandBlockOutput");}
			public void setCommandStat(CommandResultStats.Type type, int amount) {entity.setCommandStat(type, amount);}
			public MinecraftServer getServer() {return sender.getServer();}
		};
	}

	@Override
	public CommandRequirement[] getRequirements() {
		return new CommandRequirement[0];
	}

	@Override
	public ServerType getAllowedServerType() {
		return ServerType.ALL;
	}

	@Override
	public int getDefaultPermissionLevel(String[] args) {
		return 2;
	}

	@Override
	public boolean canSenderUse(String commandName, ICommandSender sender, String[] params) {
		return true;
	}
	
	private static int getRequestsCount(Entity entity) {
		CommandExecutionRequests cetc = entity.getCapability(EXEC_REQUESTS_CAP, null);
		return cetc == null ? 0 : cetc.getRequestsCount();
	}
	
	private static ListMultimap<String, ExecutionRequest> getRequests(Entity entity) {
		CommandExecutionRequests cetc = entity.getCapability(EXEC_REQUESTS_CAP, null);
		return cetc == null ? ArrayListMultimap.<String, ExecutionRequest>create() : cetc.getRequests();
	}
	
	private static Pair<ICommandSender, ExecutionRequest> getAndRemoveRequest(Entity entity, int requestID) {
		CommandExecutionRequests cetc = entity.getCapability(EXEC_REQUESTS_CAP, null);
		return cetc == null ? null : cetc.getAndRemoveRequest(requestID);
	}
	
	private static Pair<ICommandSender, ExecutionRequest> getAndRemoveOldestRequest(Entity entity) {
		CommandExecutionRequests cetc = entity.getCapability(EXEC_REQUESTS_CAP, null);
		return cetc == null ? null : cetc.getAndRemoveOldestRequest();
	}
	
	private static Pair<ICommandSender, ExecutionRequest> getAndRemoveLatestRequest(Entity entity) {
		CommandExecutionRequests cetc = entity.getCapability(EXEC_REQUESTS_CAP, null);
		return cetc == null ? null : cetc.getAndRemoveLatestRequest();
	}
	
	private static int addRequest(Entity entity, ICommandSender sender, BlockPos relCoord, String command) {
		CommandExecutionRequests cetc = entity.getCapability(EXEC_REQUESTS_CAP, null);
		if (cetc == null) return -1; ///Should not happen
		return cetc.addRequest(sender, relCoord, command);
	}
	
	private static class EntityWeakRef<T> extends WeakReference<T> {
		public final boolean referentIsEntity;
		public final int referredEntityID;
		
		public EntityWeakRef(T referent, ReferenceQueue<T> queue) {
			super(referent, queue);
			this.referentIsEntity = referent instanceof Entity;
			this.referredEntityID = this.referentIsEntity ? ((Entity) referent).getEntityId() : -1;
		}
		
		public EntityWeakRef(T referent) {
			super(referent);
			this.referentIsEntity = referent instanceof Entity;
			this.referredEntityID = this.referentIsEntity ? ((Entity) referent).getEntityId() : -1;
		}
	}
	
	private static final class ExecutionRequest {
		public final int requestID;
		public final BlockPos relCoord;
		public final String command;
		public final long creationTime = System.currentTimeMillis();
		
		public ExecutionRequest(BlockPos relCoord, String command, int requestID) {
			this.relCoord = relCoord;
			this.command = command;
			this.requestID = requestID;
		}
	}
	
	private static class CommandExecutionRequests implements ICapabilityProvider {
		public CommandExecutionRequests(Entity entity) {
			this.entity = entity;
		}
		
		private BitSet freeRequestIDs = new BitSet(MoreCommandsConfig.maxExecRequests);
		private Entity entity = null;
		private final ReferenceQueue<ICommandSender> enqueuedSenders = new ReferenceQueue<ICommandSender>();
		private final ListMultimap<WeakReference<ICommandSender>, ExecutionRequest> requests = ArrayListMultimap.create();
		
		private void cleanup() {
			WeakReference<ICommandSender> sender;
			CommandSender notify = new CommandSender(this.entity);
			
			while ((sender = (WeakReference<ICommandSender>) this.enqueuedSenders.poll()) != null) {
				List<ExecutionRequest> er = this.requests.removeAll(sender); boolean replacedSender = false;
				
				if (sender instanceof EntityWeakRef<?> && ((EntityWeakRef<?>) sender).referentIsEntity) {
					int entityID = ((EntityWeakRef<?>) sender).referredEntityID;
					
					for (World w : notify.getServer().worldServers) {
						if (w.getEntityByID(entityID) instanceof ICommandSender) {
							this.requests.putAll(new EntityWeakRef<ICommandSender>((ICommandSender) w.getEntityByID(entityID), this.enqueuedSenders), er);
							replacedSender = true; break;
						}
					}
				}
				
				if (!replacedSender) {
					for (ExecutionRequest e : er)
						this.freeRequestIDs.clear(e.requestID);
					
					if (notify != null) {
						StringBuilder reqs = new StringBuilder("["); 
						for (int i = 0; i < er.size() - 1; i++) reqs.append(",").append(er.get(i).requestID);
						reqs.append(er.get(er.size() - 1).requestID).append("]");
						
						notify.sendLangfileMessage("command.execute.senderNotAvailable", TextFormatting.RED, reqs.toString());
					}
				}
			}
			
			
			Iterator<Map.Entry<WeakReference<ICommandSender>, ExecutionRequest>> itr = this.requests.entries().iterator();
			
			while (itr.hasNext()) {
				Map.Entry<WeakReference<ICommandSender>, ExecutionRequest> er = itr.next();
				
				if (System.currentTimeMillis() - er.getValue().creationTime > MoreCommandsConfig.execRequestTimeout * 1000) {
					itr.remove(); ICommandSender s = er.getKey().get();
					this.freeRequestIDs.clear(er.getValue().requestID);
					
					if (notify != null)
						notify.sendLangfileMessage("command.execute.entityRequestTimeout", TextFormatting.RED, er.getValue().requestID);
					
					if (s != null && this.entity != null) 
						new CommandSender(s).sendLangfileMessage("command.execute.senderRequestTimeout", TextFormatting.RED, this.entity.getName(), er.getValue().command);
				}
			}
		}
		
		public int getRequestsCount() {
			cleanup();
			return this.requests.size();
		}
		
		public ListMultimap<String, ExecutionRequest> getRequests() {
			cleanup();
			ListMultimap<String, ExecutionRequest> list = ArrayListMultimap.create();
			
			for (Map.Entry<WeakReference<ICommandSender>, Collection<ExecutionRequest>> entry : this.requests.asMap().entrySet()) {
				ICommandSender sender = entry.getKey().get();
				if (sender != null) list.putAll(sender.getName(), entry.getValue());
			}
			
			return list;
		}
		
		public Pair<ICommandSender, ExecutionRequest> getAndRemoveRequest(int requestID) {
			cleanup();
			Map.Entry<WeakReference<ICommandSender>, ExecutionRequest> request = null;
			
			for (Map.Entry<WeakReference<ICommandSender>, ExecutionRequest> entry : this.requests.entries())
				if (entry.getValue().requestID == requestID) {request = entry; break;}
			
			if (request != null) {
				ICommandSender sender = request.getKey().get();
				
				this.requests.remove(request.getKey(), request.getValue());
				this.freeRequestIDs.clear(request.getValue().requestID);
				
				return sender != null ? ImmutablePair.of(sender, request.getValue()) : null;
			}
			else return null;
		}
		
		public Pair<ICommandSender, ExecutionRequest> getAndRemoveOldestRequest() {
			cleanup();
			
			long time = System.currentTimeMillis();
			Map.Entry<WeakReference<ICommandSender>, ExecutionRequest> request = null;
			
			for (Map.Entry<WeakReference<ICommandSender>, ExecutionRequest> entry : this.requests.entries())
				if (entry.getValue().creationTime < time) {time = entry.getValue().creationTime; request = entry;}
			
			if (request != null) {
				ICommandSender sender = request.getKey().get();
				
				this.requests.remove(request.getKey(), request.getValue());
				this.freeRequestIDs.clear(request.getValue().requestID);
				
				return sender != null ? ImmutablePair.of(sender, request.getValue()) : null;
			}
			else return null;
		}
		
		public Pair<ICommandSender, ExecutionRequest> getAndRemoveLatestRequest() {
			cleanup();
			
			long time = 0;
			Map.Entry<WeakReference<ICommandSender>, ExecutionRequest> request = null;
			
			for (Map.Entry<WeakReference<ICommandSender>, ExecutionRequest> entry : this.requests.entries())
				if (entry.getValue().creationTime > time) {time = entry.getValue().creationTime; request = entry;}
			
			if (request != null) {
				ICommandSender sender = request.getKey().get();
				
				this.requests.remove(request.getKey(), request.getValue());
				this.freeRequestIDs.clear(request.getValue().requestID);
				
				return sender != null ? ImmutablePair.of(sender, request.getValue()) : null;
			}
			else return null;
		}
		
		public int addRequest(ICommandSender sender, BlockPos relCoord, String command) {
			cleanup();
			WeakReference<ICommandSender> ref = null;
			
			int requestID = this.freeRequestIDs.nextClearBit(0);
			if (requestID < 0 || requestID >= MoreCommandsConfig.maxExecRequests) return -1;
			this.freeRequestIDs.set(requestID);
			
			for (WeakReference<ICommandSender> key : this.requests.keySet())
				if (sender.equals(key.get())) {ref = key; break;}
			
			if (ref == null) ref = new EntityWeakRef<ICommandSender>(sender, this.enqueuedSenders);
			this.requests.put(ref, new ExecutionRequest(relCoord, command, requestID));
			
			return requestID;
		}

		@Override
		public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
			return capability == EXEC_REQUESTS_CAP;
		}

		@Override
		public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
			return capability == EXEC_REQUESTS_CAP ? EXEC_REQUESTS_CAP.<T>cast(this) : null;
		}
	}
	
	private static enum AttachCapsHandler {
		INSTANCE;
		
		@SubscribeEvent
		public void attachCaps(AttachCapabilitiesEvent.Entity event) {
			event.addCapability(REQUESTS_KEY, new CommandExecutionRequests(event.getEntity()));
		}
	}
}
