package com.mrnobody.morecommands.patch;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import com.mrnobody.morecommands.command.AbstractCommand.ResultAcceptingCommandSender;
import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.settings.PlayerSettings;
import com.mrnobody.morecommands.settings.ServerPlayerSettings;
import com.mrnobody.morecommands.util.ChatChannel;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.server.S05PacketSpawnPosition;
import net.minecraft.network.play.server.S07PacketRespawn;
import net.minecraft.network.play.server.S1DPacketEntityEffect;
import net.minecraft.network.play.server.S1FPacketSetExperience;
import net.minecraft.network.play.server.S2BPacketChangeGameState;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.server.management.ItemInWorldManager;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.Teleporter;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldSettings.GameType;
import net.minecraft.world.demo.DemoWorldManager;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.event.FMLStateEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * This patch substitues the {@link PlayerList} instance of the server
 * with a modified one which will produce modified {@link net.minecraf.entity.player.EntityPlayerMP} instances.
 * This allowes to modify the behaviour of EntityPlayerMP <br>
 * This patch is needed to make several commands available and working, <br>
 * e.g. instant kill, always critical hits, keeping inventory on death, etc.
 * 
 * @author MrNobody98
 *
 */
public class PatchEntityPlayerMP implements PatchManager.StateEventBasedPatch {
	private String displayName;
	
	PatchEntityPlayerMP(String displayName) {
		this.displayName = displayName;
	}
	
	@Override
	public Collection<Class<? extends FMLStateEvent>> stateEventClasses() {
		return Sets.<Class<? extends FMLStateEvent>>newHashSet(FMLServerAboutToStartEvent.class);
	}
	
	@Override
	public <T extends FMLStateEvent> boolean applyStateEventPatch(T e) {
		FMLServerAboutToStartEvent event = (FMLServerAboutToStartEvent) e;
		
		try {
			Constructor<ServerConfigurationManager> ctor;
			
			if (event.getServer().isDedicatedServer())
				ctor = (Constructor<ServerConfigurationManager>) (Constructor<?>) ServerConfigurationManagerDedicated.class.getDeclaredConstructor(DedicatedServer.class);
			else
				ctor = (Constructor<ServerConfigurationManager>) (Constructor<?>) ServerConfigurationManagerIntegrated.class.getDeclaredConstructor(IntegratedServer.class);
			
			ctor.setAccessible(true);
			
			if (event.getServer().isDedicatedServer())
				event.getServer().setConfigManager(ctor.newInstance((DedicatedServer) event.getServer()));
			else
				event.getServer().setConfigManager(ctor.newInstance((IntegratedServer) event.getServer()));
		
			PatchManager.instance().getGlobalAppliedPatches().setPatchSuccessfullyApplied(this.displayName, true);
			return true;
		}
		catch (Exception ex) {
			PatchManager.instance().getGlobalAppliedPatches().setPatchSuccessfullyApplied(this.displayName, false);
			return false;
		}
	}
	
	@Override
	public <T extends FMLStateEvent> boolean needsToBeApplied(T event) {
		return true;
	}
	
	@Override
	public <T extends FMLStateEvent> boolean printLogFor(T event) {
		return true;
	}

	@Override
	public String getDisplayName() {
		return this.displayName;
	}

	@Override
	public String getFailureConsequences() {
		return "Disables several commands which rely on modifications on EntityPlayerMP and PlayerList such as keepinventory, jumpheight, ...";
	}
	
	private static void setPlayerGameTypeBasedOnOther(GameType myGameType, net.minecraft.entity.player.EntityPlayerMP target, 
														net.minecraft.entity.player.EntityPlayerMP source, World worldIn) {
		if (source != null)
			target.theItemInWorldManager.setGameType(source.theItemInWorldManager.getGameType());
		
        else if (myGameType != null)
        	target.theItemInWorldManager.setGameType(myGameType);
		
        target.theItemInWorldManager.initializeGameType(worldIn.getWorldInfo().getGameType());
    }
	
	private static net.minecraft.entity.player.EntityPlayerMP createPlayerForUser(ServerConfigurationManager scm, GameProfile profile) {
		MinecraftServer mcServer = scm.getServerInstance();
		
		UUID uuid = EntityPlayer.getUUID(profile);
		List<net.minecraft.entity.player.EntityPlayerMP> list = Lists.newArrayList();
		
		for (net.minecraft.entity.player.EntityPlayerMP player : scm.getPlayerList())
			if (player.getUniqueID().equals(uuid))
				list.add(player);
		
		net.minecraft.entity.player.EntityPlayerMP player = (net.minecraft.entity.player.EntityPlayerMP) 
																	scm.getPlayerByUUID(profile.getId());

        if (player != null && !list.contains(player))
        	list.add(player);
        
        for (net.minecraft.entity.player.EntityPlayerMP player2 : list)
        	player2.playerNetServerHandler.kickPlayerFromServer("You logged in from another location");
        
        ItemInWorldManager iwm;

        if (mcServer.isDemo())
        	iwm = new DemoWorldManager(mcServer.worldServerForDimension(0));
        else
        	iwm = new ItemInWorldManager(mcServer.worldServerForDimension(0));
        
        return new EntityPlayerMP(mcServer, mcServer.worldServerForDimension(0), profile, iwm);
	}
	
	private static net.minecraft.entity.player.EntityPlayerMP recreatePlayerEntity(ServerConfigurationManager scm, GameType myGameType, 
										net.minecraft.entity.player.EntityPlayerMP player, int dimension, boolean conqueredEnd) {
		MinecraftServer mcServer = scm.getServerInstance();
		World world = mcServer.worldServerForDimension(dimension);
		
		if (world == null)
			dimension = 0;
		else if (!world.provider.canRespawnHere())
        	dimension = world.provider.getRespawnDimension(player);

		player.getServerForPlayer().getEntityTracker().removePlayerFromTrackers(player);
		player.getServerForPlayer().getEntityTracker().untrackEntity(player);
		player.getServerForPlayer().getPlayerManager().removePlayer(player);
        
		scm.getPlayerList().remove(player);
		mcServer.worldServerForDimension(player.dimension).removePlayerEntityDangerously(player);
        
		BlockPos blockpos = player.getBedLocation(dimension);
		boolean flag = player.isSpawnForced(dimension);
        
		player.dimension = dimension;
		ItemInWorldManager iwm;
		
        if (mcServer.isDemo())
        	iwm = new DemoWorldManager(mcServer.worldServerForDimension(player.dimension));
        else
        	iwm = new ItemInWorldManager(mcServer.worldServerForDimension(player.dimension));
        
        net.minecraft.entity.player.EntityPlayerMP newPlayer = new EntityPlayerMP(mcServer, 
        		mcServer.worldServerForDimension(player.dimension), player.getGameProfile(), iwm);
        
        newPlayer.playerNetServerHandler = player.playerNetServerHandler;
        newPlayer.clonePlayer(player, conqueredEnd);
        newPlayer.dimension = dimension;
        newPlayer.setEntityId(player.getEntityId());
        newPlayer.func_174817_o(player);
        
        WorldServer worldserver = mcServer.worldServerForDimension(player.dimension);
        setPlayerGameTypeBasedOnOther(myGameType, newPlayer, player, worldserver);

        if (blockpos != null) {
        	BlockPos blockpos1 = EntityPlayer.getBedSpawnLocation(mcServer.worldServerForDimension(player.dimension), blockpos, flag);

            if (blockpos1 != null) {
            	newPlayer.setLocationAndAngles((double)((float)blockpos1.getX() + 0.5F), (double)((float)blockpos1.getY() + 0.1F), (double)((float)blockpos1.getZ() + 0.5F), 0.0F, 0.0F);
            	newPlayer.setSpawnPoint(blockpos, flag);
            }
            else {
            	newPlayer.playerNetServerHandler.sendPacket(new S2BPacketChangeGameState(0, 0.0F));
            }
        }
        
        worldserver.getChunkProvider().provideChunk((int)newPlayer.posX >> 4, (int)newPlayer.posZ >> 4);
        
        while (!worldserver.getCollidingBoundingBoxes(newPlayer, newPlayer.getEntityBoundingBox()).isEmpty() && newPlayer.posY < 256.0D)
        	newPlayer.setPosition(newPlayer.posX, newPlayer.posY + 1.0D, newPlayer.posZ);
        
        newPlayer.playerNetServerHandler.sendPacket(new S07PacketRespawn(newPlayer.dimension, newPlayer.worldObj.getDifficulty(), newPlayer.worldObj.getWorldInfo().getTerrainType(), newPlayer.theItemInWorldManager.getGameType()));
        BlockPos blockpos2 = worldserver.getSpawnPoint();
        
        newPlayer.playerNetServerHandler.setPlayerLocation(newPlayer.posX, newPlayer.posY, newPlayer.posZ, newPlayer.rotationYaw, newPlayer.rotationPitch);
        newPlayer.playerNetServerHandler.sendPacket(new S05PacketSpawnPosition(blockpos2));
        newPlayer.playerNetServerHandler.sendPacket(new S1FPacketSetExperience(newPlayer.experience, newPlayer.experienceTotal, newPlayer.experienceLevel));
        
        scm.updateTimeAndWeatherForPlayer(newPlayer, worldserver);
        worldserver.getPlayerManager().addPlayer(newPlayer);
        worldserver.spawnEntityInWorld(newPlayer);
        
        scm.getPlayerList().add(newPlayer);
        scm.uuidToPlayerMap.put(newPlayer.getUniqueID(), newPlayer);
        
        newPlayer.addSelfToInternalCraftingInventory();
        newPlayer.setHealth(newPlayer.getHealth());
        
        ServerPlayerSettings settings = MoreCommands.getEntityProperties(ServerPlayerSettings.class, PlayerSettings.MORECOMMANDS_IDENTIFIER, player);
        
        if (settings != null && settings.keepinventory) {
        	newPlayer.inventory.copyInventory(player.inventory);
        	((EntityPlayerMP) newPlayer).setKeepInventory(true);
        }
        
        net.minecraftforge.fml.common.FMLCommonHandler.instance().firePlayerRespawnEvent(newPlayer);
        return newPlayer;
    }
	
	private static void transferPlayerToDimension(ServerConfigurationManager scm, net.minecraft.entity.player.EntityPlayerMP player, int dimension, Teleporter teleporter) {
        int i = player.dimension;
        WorldServer worldserver = scm.getServerInstance().worldServerForDimension(player.dimension);
        player.dimension = dimension;
        WorldServer worldserver1 = scm.getServerInstance().worldServerForDimension(player.dimension);
        player.playerNetServerHandler.sendPacket(new S07PacketRespawn(player.dimension, worldserver1.getDifficulty(), worldserver1.getWorldInfo().getTerrainType(), player.theItemInWorldManager.getGameType()));
        
        //MoreCommands Bug fix: client world has wrong spawn position, because WorldClient is recreated after receiving S07PacketRespawn
        //with default spawn coordinates 8, 64, 8. This causes e.g. the compass to point to a wrong direction. A possible solution is sending a S05PacketSpawnPosition.
        //Fixes https://bugs.mojang.com/browse/MC-679
        player.playerNetServerHandler.sendPacket(new S05PacketSpawnPosition(new BlockPos(worldserver1.getWorldInfo().getSpawnX(), worldserver1.getWorldInfo().getSpawnY(), worldserver1.getWorldInfo().getSpawnZ())));
        
        worldserver.removePlayerEntityDangerously(player);
        player.isDead = false;
        scm.transferEntityToWorld(player, i, worldserver, worldserver1, teleporter);
        scm.preparePlayer(player, worldserver);
        player.playerNetServerHandler.setPlayerLocation(player.posX, player.posY, player.posZ, player.rotationYaw, player.rotationPitch);
        player.theItemInWorldManager.setWorld(worldserver1);
        scm.updateTimeAndWeatherForPlayer(player, worldserver1);
        scm.syncPlayerInventory(player);
        
        for (PotionEffect potioneffect : player.getActivePotionEffects())
        {
            player.playerNetServerHandler.sendPacket(new S1DPacketEntityEffect(player.getEntityId(), potioneffect));
        }
        net.minecraftforge.fml.common.FMLCommonHandler.instance().firePlayerChangedDimensionEvent(player, i, dimension);
    }
	
	private static void sendMessage(IChatComponent message, boolean isSystemMessage) {
		MoreCommands.getProxy().ensureChatChannelsLoaded();
		ChatChannel.getMasterChannel().sendChatMessage(message, isSystemMessage ? (byte) 1 : (byte) 0);
	}
	
	@SideOnly(Side.SERVER)
	public static class ServerConfigurationManagerDedicated extends net.minecraft.server.dedicated.DedicatedPlayerList {
		private GameType gameType;
		
		ServerConfigurationManagerDedicated(DedicatedServer server) {
			super(server);
		}
		
		@Override
		public void sendChatMsgImpl(IChatComponent message, boolean isSystemMessage) {
			PatchEntityPlayerMP.sendMessage(message, isSystemMessage);
		}
		
		@Override
		public net.minecraft.entity.player.EntityPlayerMP createPlayerForUser(GameProfile profile) {
			return PatchEntityPlayerMP.createPlayerForUser(this, profile);
	    }
		
		@Override
		public net.minecraft.entity.player.EntityPlayerMP recreatePlayerEntity(net.minecraft.entity.player.EntityPlayerMP playerIn, int dimension, boolean conqueredEnd) {
	        return PatchEntityPlayerMP.recreatePlayerEntity(this, this.gameType, playerIn, dimension, conqueredEnd);
	    }
		
		@Override
		public void transferPlayerToDimension(net.minecraft.entity.player.EntityPlayerMP player, int dimension, Teleporter teleporter) {
			PatchEntityPlayerMP.transferPlayerToDimension(this, player, dimension, teleporter);
		}
	    
	    //Simply copied from PlayerList
		@SideOnly(Side.CLIENT)
		public void setGameType(GameType gameModeIn) {
			this.gameType = gameModeIn;
		}
	}
	
	@SideOnly(Side.CLIENT)
	public static class ServerConfigurationManagerIntegrated extends net.minecraft.server.integrated.IntegratedPlayerList {
		private GameType gameType;
		
		ServerConfigurationManagerIntegrated(IntegratedServer server) {
			super(server);
		}
		
		@Override
		public void sendChatMsgImpl(IChatComponent message, boolean isSystemMessage) {
			PatchEntityPlayerMP.sendMessage(message, isSystemMessage);
		}
		
		@Override
		public net.minecraft.entity.player.EntityPlayerMP createPlayerForUser(GameProfile profile) {
			return PatchEntityPlayerMP.createPlayerForUser(this, profile);
	    }
		
		@Override
		public net.minecraft.entity.player.EntityPlayerMP recreatePlayerEntity(net.minecraft.entity.player.EntityPlayerMP playerIn, int dimension, boolean conqueredEnd) {
	        return PatchEntityPlayerMP.recreatePlayerEntity(this, this.gameType, playerIn, dimension, conqueredEnd);
	    }
		
		@Override
		public void transferPlayerToDimension(net.minecraft.entity.player.EntityPlayerMP player, int dimension, Teleporter teleporter) {
			PatchEntityPlayerMP.transferPlayerToDimension(this, player, dimension, teleporter);
		}
	    
		//Simply copied from PlayerList
		@SideOnly(Side.CLIENT)
		public void setGameType(GameType gameModeIn) {
			this.gameType = gameModeIn;
		}
	}
	
	public static class EntityPlayerMP extends net.minecraft.entity.player.EntityPlayerMP implements ResultAcceptingCommandSender {
		private boolean instantkill = false;
		private boolean criticalhit = false;
		private boolean instantmine = false;
		private boolean keepinventory = false;
		private boolean infinitesprinting = false;
		private boolean fluidmovement = true;
		private boolean overrideOnLadder = false;
		
		private double gravity = 1F;

		private String capturedCommandResult = null;
		private StringBuilder capturedCommandMessages = new StringBuilder();
		private boolean captureNextCommandResult = false;
		
		protected EntityPlayerMP(MinecraftServer p_i45285_1_, WorldServer p_i45285_2_, GameProfile p_i45285_3_, ItemInWorldManager p_i45285_4_) {
			super(p_i45285_1_, p_i45285_2_, p_i45285_3_, p_i45285_4_);
		}

	    /**
	     * This method should be invoked before this entity is passed to {@link net.minecraft.command.ICommandManager#executeCommand(net.minecraft.command.ICommandSender, String)}. 
	     * Invoking this method will make this entity capture the result of the command exection. Result either means the return value
	     * of the {@link com.mrnobody.morecommands.command.AbstractCommand#processCommand(com.mrnobody.morecommands.wrapper.CommandSender, String[])} method
	     * if the command is a subclass of this class or, if it is not, or if the return value is null, the chat messages sent via the
	     * {@link #addChatMessage(IChatComponent)} method. After command execution, the captured results must be reset via the
	     * {@link #getCapturedCommandResult()} method. This method also returns the result. 
	     */
	    public void setCaptureNextCommandResult() {
	    	this.captureNextCommandResult = true;
	    }
	    
	    @Override
	    public void addChatMessage(IChatComponent message) {
	    	if (this.captureNextCommandResult) this.capturedCommandMessages.append(" " + message.getUnformattedText());
	    	super.addChatMessage(message);
	    }
	    
	    @Override
	    public void setCommandResult(String commandName, String[] args, String result) {
	    	if (this.captureNextCommandResult && result != null) 
	    		this.capturedCommandResult = result;
	    }
	    
	    /**
	     * Disables capturing of command results and resets and returns them.
	     * 
	     * @return the captured result of the command execution (requires enabling capturing before command execution via
	     * 			{@link #setCaptureNextCommandResult()}. Will never be null
	     * @see #setCaptureNextCommandResult()
	     */
	    public String getCapturedCommandResult() {
	    	String result = null;
	    	
	    	if (this.capturedCommandResult != null) result = this.capturedCommandResult;
	    	else result = this.capturedCommandMessages.toString().trim();
	    	
	    	this.capturedCommandResult = null;
	    	this.capturedCommandMessages = new StringBuilder();
	    	this.captureNextCommandResult = false;
	    	
	    	return result;
	    }
		
		public boolean getCriticalHit() {
			return this.criticalhit;
		}
		
		public boolean getInstantkill() {
			return this.instantkill;
		}
		
		public boolean getInstantmine() {
			return this.instantmine;
		}
		
		public boolean getKeepInventory() {
			return this.keepinventory;
		}
		
		public void setCriticalhit(boolean criticalhit) {
			this.criticalhit = criticalhit;
		}
		
		public void setInstantkill(boolean instantkill) {
			this.instantkill = instantkill;
		}
		
		public void setInstantmine(boolean instantmine) {
			this.instantmine = instantmine;
		}
		
		public void setKeepInventory(boolean keepinventory) {
			this.keepinventory = keepinventory;
		}
		

		public void setInfiniteSprinting(boolean sprinting) {
			this.infinitesprinting = sprinting;
		}
		
		public boolean getInfiniteSprinting() {
			return this.infinitesprinting;
		}
		
		public void setFluidMovement(boolean fluidmovement) {
			this.fluidmovement = fluidmovement;
		}
		
		public boolean getFluidMovement() {
			return this.fluidmovement;
		}
		
		public double getGravity() {
			return this.gravity;
		}
		
		public void setGravity(double gravity) {
			this.gravity = gravity;
		}
		
		public void setOverrideOnLadder(boolean override) {
			this.overrideOnLadder = override;
		}
		
		public boolean overrideOnLadder() {
			return this.overrideOnLadder;
		}

		@Override
		public boolean isOnLadder() {
			if (this.overrideOnLadder && this.isCollidedHorizontally) return true;
			else return super.isOnLadder();
		}
		
		@Override
		public boolean isInWater() {
			if (!this.fluidmovement) return false;
			return super.isInWater();
		}
		
		@Override
		public boolean isInLava() {
			if (!this.fluidmovement) return false;
			return super.isInLava();
		}
		
		@Override
		protected float getJumpUpwardsMotion() {
			return super.getJumpUpwardsMotion() * (float) this.gravity;
		}
		
		@Override
		public void onLivingUpdate() {
			if (this.infinitesprinting) this.setSprinting(true);
			super.onLivingUpdate();
		}
		
		@Override
		public void attackTargetEntityWithCurrentItem(Entity entity) {
			if (this.instantkill && this.theItemInWorldManager.getGameType() != GameType.SPECTATOR) {
				IAttributeInstance attackDamage = this.getEntityAttribute(SharedMonsterAttributes.attackDamage);
				double oldValue = attackDamage.getBaseValue();
				
				attackDamage.setBaseValue(Double.MAX_VALUE / 2);
				super.attackTargetEntityWithCurrentItem(entity);
				
				attackDamage.setBaseValue(oldValue);
				return;
			} 
			else if (this.criticalhit && this.theItemInWorldManager.getGameType() != GameType.SPECTATOR) {
				double my = this.motionY;
				boolean og = this.onGround;
				boolean iw = this.inWater;
				float fd = this.fallDistance;
				
				super.motionY = -0.1D;
				super.inWater = false;
				super.onGround = false;
				super.fallDistance = 0.1F;
				super.attackTargetEntityWithCurrentItem(entity);
				
				this.motionY = my;
				this.onGround = og;
				this.inWater = iw;
				this.fallDistance = fd;
				return;
			}
			
			super.attackTargetEntityWithCurrentItem(entity);
		}
		
		@Override
		public float getBreakSpeed(IBlockState state, BlockPos pos) {
			if (this.instantmine) return Float.MAX_VALUE;
			else return super.getBreakSpeed(state, pos);
		}
		
		@Override
		public void onDeath(DamageSource cause) {
			boolean keepInventory = this.worldObj.getGameRules().getBoolean("keepInventory");
			this.worldObj.getGameRules().setOrCreateGameRule("keepInventory", Boolean.toString(this.keepinventory));
			
			super.onDeath(cause);
			this.worldObj.getGameRules().setOrCreateGameRule("keepInventory", Boolean.toString(keepInventory));
	    }
	}
}
