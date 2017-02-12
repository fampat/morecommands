package com.mrnobody.morecommands.patch;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.common.collect.ForwardingList;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mrnobody.morecommands.command.AbstractCommand;
import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.settings.MoreCommandsConfig;
import com.mrnobody.morecommands.settings.PlayerSettings;
import com.mrnobody.morecommands.settings.ServerPlayerSettings;
import com.mrnobody.morecommands.util.ChatChannel;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.network.play.server.S05PacketSpawnPosition;
import net.minecraft.network.play.server.S07PacketRespawn;
import net.minecraft.network.play.server.S1DPacketEntityEffect;
import net.minecraft.network.play.server.S1FPacketSetExperience;
import net.minecraft.network.play.server.S2BPacketChangeGameState;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.management.ItemInWorldManager;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.Teleporter;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.demo.DemoWorldManager;

/**
 * The patched class of {@link net.minecraft.server.dedicated.DedicatedPlayerList} <br>
 * Patching this class is needed to use a patched {@link EntityPlayerMP} class and to
 * be able to use {@link ChatChannel}s
 * The patch is also needed to make the keepinventory command working.
 * 
 * @author MrNobody98
 *
 */
public class ServerConfigurationManagerDedicated extends net.minecraft.server.dedicated.DedicatedPlayerList {
	private WorldSettings.GameType gameType;
	private MinecraftServer mcServer;
	
	public ServerConfigurationManagerDedicated(DedicatedServer server) {
		super(server);
		this.mcServer = server;
	}
	
	@Override
	public void sendChatMsgImpl(IChatComponent message, boolean isChat) {
		MoreCommands.getProxy().ensureChatChannelsLoaded();
		ChatChannel.getMasterChannel().sendChatMessage(message, isChat);
	}
	
	@Override
    public EntityPlayerMP createPlayerForUser(GameProfile p_148545_1_)
    {
        UUID uuid = EntityPlayer.func_146094_a(p_148545_1_);
        ArrayList arraylist = Lists.newArrayList();
        EntityPlayerMP entityplayermp;

        for (int i = 0; i < this.playerEntityList.size(); ++i)
        {
            entityplayermp = (EntityPlayerMP)this.playerEntityList.get(i);

            if (entityplayermp.getUniqueID().equals(uuid))
            {
                arraylist.add(entityplayermp);
            }
        }

        Iterator iterator = arraylist.iterator();

        while (iterator.hasNext())
        {
            entityplayermp = (EntityPlayerMP)iterator.next();
            entityplayermp.playerNetServerHandler.kickPlayerFromServer("You logged in from another location");
        }

        Object object;

        if (this.mcServer.isDemo())
        {
            object = new DemoWorldManager(this.mcServer.worldServerForDimension(0));
        }
        else
        {
            object = new ItemInWorldManager(this.mcServer.worldServerForDimension(0));
        }

        return new com.mrnobody.morecommands.patch.EntityPlayerMP(this.mcServer, this.mcServer.worldServerForDimension(0), p_148545_1_, (ItemInWorldManager)object);
    }
	
	@Override
    public EntityPlayerMP respawnPlayer(EntityPlayerMP p_72368_1_, int p_72368_2_, boolean p_72368_3_)
    {
        World world = mcServer.worldServerForDimension(p_72368_2_);
        if (world == null)
        {
            p_72368_2_ = 0;
        }
        else if (!world.provider.canRespawnHere())
        {
            p_72368_2_ = world.provider.getRespawnDimension(p_72368_1_);
        }

        p_72368_1_.getServerForPlayer().getEntityTracker().removePlayerFromTrackers(p_72368_1_);
        p_72368_1_.getServerForPlayer().getEntityTracker().removeEntityFromAllTrackingPlayers(p_72368_1_);
        p_72368_1_.getServerForPlayer().getPlayerManager().removePlayer(p_72368_1_);
        this.playerEntityList.remove(p_72368_1_);
        this.mcServer.worldServerForDimension(p_72368_1_.dimension).removePlayerEntityDangerously(p_72368_1_);
        ChunkCoordinates chunkcoordinates = p_72368_1_.getBedLocation(p_72368_2_);
        boolean flag1 = p_72368_1_.isSpawnForced(p_72368_2_);
        p_72368_1_.dimension = p_72368_2_;
        Object object;

        if (this.mcServer.isDemo())
        {
            object = new DemoWorldManager(this.mcServer.worldServerForDimension(p_72368_1_.dimension));
        }
        else
        {
            object = new ItemInWorldManager(this.mcServer.worldServerForDimension(p_72368_1_.dimension));
        }

        EntityPlayerMP entityplayermp1 = new com.mrnobody.morecommands.patch.EntityPlayerMP(this.mcServer, this.mcServer.worldServerForDimension(p_72368_1_.dimension), p_72368_1_.getGameProfile(), (ItemInWorldManager)object);
        entityplayermp1.playerNetServerHandler = p_72368_1_.playerNetServerHandler;
        entityplayermp1.clonePlayer(p_72368_1_, p_72368_3_);
        entityplayermp1.dimension = p_72368_2_;
        entityplayermp1.setEntityId(p_72368_1_.getEntityId());
        WorldServer worldserver = this.mcServer.worldServerForDimension(p_72368_1_.dimension);
        this.func_72381_a(entityplayermp1, p_72368_1_, worldserver);
        ChunkCoordinates chunkcoordinates1;

        if (chunkcoordinates != null)
        {
            chunkcoordinates1 = EntityPlayer.verifyRespawnCoordinates(this.mcServer.worldServerForDimension(p_72368_1_.dimension), chunkcoordinates, flag1);

            if (chunkcoordinates1 != null)
            {
                entityplayermp1.setLocationAndAngles((double)((float)chunkcoordinates1.posX + 0.5F), (double)((float)chunkcoordinates1.posY + 0.1F), (double)((float)chunkcoordinates1.posZ + 0.5F), 0.0F, 0.0F);
                entityplayermp1.setSpawnChunk(chunkcoordinates, flag1);
            }
            else
            {
                entityplayermp1.playerNetServerHandler.sendPacket(new S2BPacketChangeGameState(0, 0.0F));
            }
        }

        worldserver.theChunkProviderServer.loadChunk((int)entityplayermp1.posX >> 4, (int)entityplayermp1.posZ >> 4);

        while (!worldserver.getCollidingBoundingBoxes(entityplayermp1, entityplayermp1.boundingBox).isEmpty())
        {
            entityplayermp1.setPosition(entityplayermp1.posX, entityplayermp1.posY + 1.0D, entityplayermp1.posZ);
        }

        entityplayermp1.playerNetServerHandler.sendPacket(new S07PacketRespawn(entityplayermp1.dimension, entityplayermp1.worldObj.difficultySetting, entityplayermp1.worldObj.getWorldInfo().getTerrainType(), entityplayermp1.theItemInWorldManager.getGameType()));
        chunkcoordinates1 = worldserver.getSpawnPoint();
        entityplayermp1.playerNetServerHandler.setPlayerLocation(entityplayermp1.posX, entityplayermp1.posY, entityplayermp1.posZ, entityplayermp1.rotationYaw, entityplayermp1.rotationPitch);
        entityplayermp1.playerNetServerHandler.sendPacket(new S05PacketSpawnPosition(chunkcoordinates1.posX, chunkcoordinates1.posY, chunkcoordinates1.posZ));
        entityplayermp1.playerNetServerHandler.sendPacket(new S1FPacketSetExperience(entityplayermp1.experience, entityplayermp1.experienceTotal, entityplayermp1.experienceLevel));
        this.updateTimeAndWeatherForPlayer(entityplayermp1, worldserver);
        worldserver.getPlayerManager().addPlayer(entityplayermp1);
        worldserver.spawnEntityInWorld(entityplayermp1);
        this.playerEntityList.add(entityplayermp1);
        entityplayermp1.addSelfToInternalCraftingInventory();
        entityplayermp1.setHealth(entityplayermp1.getHealth());
        
        ServerPlayerSettings settings = MoreCommands.getEntityProperties(ServerPlayerSettings.class, PlayerSettings.MORECOMMANDS_IDENTIFIER, p_72368_1_);
        if (settings != null && settings.keepinventory) {
        	entityplayermp1.inventory.copyInventory(p_72368_1_.inventory);
        	((com.mrnobody.morecommands.patch.EntityPlayerMP) entityplayermp1).setKeepInventory(true);
        }
        
        FMLCommonHandler.instance().firePlayerRespawnEvent(entityplayermp1);
        return entityplayermp1;
    }
	
    public void transferPlayerToDimension(EntityPlayerMP p_72356_1_, int p_72356_2_, Teleporter teleporter)
    {
        int j = p_72356_1_.dimension;
        WorldServer worldserver = this.mcServer.worldServerForDimension(p_72356_1_.dimension);
        p_72356_1_.dimension = p_72356_2_;
        WorldServer worldserver1 = this.mcServer.worldServerForDimension(p_72356_1_.dimension);
        p_72356_1_.playerNetServerHandler.sendPacket(new S07PacketRespawn(p_72356_1_.dimension, worldserver1.difficultySetting, worldserver1.getWorldInfo().getTerrainType(), p_72356_1_.theItemInWorldManager.getGameType())); // Forge: Use new dimensions information
        
        //MoreCommands Bug fix: client world has wrong spawn position, because WorldClient is recreated after receiving S07PacketRespawn
        //with default spawn coordinates 8, 64, 8. This causes e.g. the compass to point to a wrong direction. Solution is sending a S05PacketSpawnPosition.
        //Fixes https://bugs.mojang.com/browse/MC-679
        p_72356_1_.playerNetServerHandler.sendPacket(new S05PacketSpawnPosition(worldserver1.getWorldInfo().getSpawnX(), worldserver1.getWorldInfo().getSpawnY(), worldserver1.getWorldInfo().getSpawnZ()));
        
        worldserver.removePlayerEntityDangerously(p_72356_1_);
        p_72356_1_.isDead = false;
        this.transferEntityToWorld(p_72356_1_, j, worldserver, worldserver1, teleporter);
        this.func_72375_a(p_72356_1_, worldserver);
        p_72356_1_.playerNetServerHandler.setPlayerLocation(p_72356_1_.posX, p_72356_1_.posY, p_72356_1_.posZ, p_72356_1_.rotationYaw, p_72356_1_.rotationPitch);
        p_72356_1_.theItemInWorldManager.setWorld(worldserver1);
        this.updateTimeAndWeatherForPlayer(p_72356_1_, worldserver1);
        this.syncPlayerInventory(p_72356_1_);
        Iterator iterator = p_72356_1_.getActivePotionEffects().iterator();

        while (iterator.hasNext())
        {
            PotionEffect potioneffect = (PotionEffect)iterator.next();
            p_72356_1_.playerNetServerHandler.sendPacket(new S1DPacketEntityEffect(p_72356_1_.getEntityId(), potioneffect));
        }
        FMLCommonHandler.instance().firePlayerChangedDimensionEvent(p_72356_1_, j, p_72356_2_);
    }
	
    private void func_72381_a(EntityPlayerMP p_72381_1_, EntityPlayerMP p_72381_2_, World p_72381_3_)
    {
        if (p_72381_2_ != null)
        {
            p_72381_1_.theItemInWorldManager.setGameType(p_72381_2_.theItemInWorldManager.getGameType());
        }
        else if (this.gameType != null)
        {
            p_72381_1_.theItemInWorldManager.setGameType(this.gameType);
        }

        p_72381_1_.theItemInWorldManager.initializeGameType(p_72381_3_.getWorldInfo().getGameType());
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public void func_152604_a(WorldSettings.GameType p_152604_1_)
    {
        this.gameType = p_152604_1_;
    }
}
