package com.mrnobody.morecommands.patch;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.S05PacketSpawnPosition;
import net.minecraft.network.play.server.S07PacketRespawn;
import net.minecraft.network.play.server.S1FPacketSetExperience;
import net.minecraft.network.play.server.S2BPacketChangeGameState;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.management.ItemInWorldManager;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.demo.DemoWorldManager;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.util.ServerPlayerSettings;

/**
 * The patched class of {@link net.minecraft.server.dedicated.DedicatedPlayerList} <br>
 * Patching this class is needed to use my own patched {@link EntityPlayerMP} class.
 * The patch is also needed to make the keepinventory command working.
 * 
 * @author MrNobody98
 *
 */
public class ServerConfigurationManagerDedicated extends net.minecraft.server.dedicated.DedicatedPlayerList {
	private WorldSettings.GameType gameType;
	private MinecraftServer mcServer;
	
	private boolean keepinventory = false;
	
	public void setKeepInventory(boolean keepinventory) {
		this.keepinventory = keepinventory;
	}
	
	public boolean getKeepInventory() {
		return this.keepinventory;
	}
	
	public ServerConfigurationManagerDedicated(DedicatedServer server) {
		super(server);
		this.mcServer = server;
	}
	
    public EntityPlayerMP createPlayerForUser(GameProfile profile)
    {
        UUID uuid = EntityPlayer.getUUID(profile);
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

        return new com.mrnobody.morecommands.patch.EntityPlayerMP(this.mcServer, this.mcServer.worldServerForDimension(0), profile, (ItemInWorldManager)object);
    }
    
    @Override
    public EntityPlayerMP recreatePlayerEntity(EntityPlayerMP playerIn, int dimension, boolean conqueredEnd)
    {
        World world = mcServer.worldServerForDimension(dimension);
        if (world == null)
        {
            dimension = 0;
        }
        else if (!world.provider.canRespawnHere())
        {
            dimension = world.provider.getRespawnDimension(playerIn);
        }

        playerIn.getServerForPlayer().getEntityTracker().removePlayerFromTrackers(playerIn);
        playerIn.getServerForPlayer().getEntityTracker().untrackEntity(playerIn);
        playerIn.getServerForPlayer().getPlayerManager().removePlayer(playerIn);
        this.playerEntityList.remove(playerIn);
        this.mcServer.worldServerForDimension(playerIn.dimension).removePlayerEntityDangerously(playerIn);
        BlockPos blockpos = playerIn.getBedLocation(dimension);
        boolean flag1 = playerIn.isSpawnForced(dimension);
        playerIn.dimension = dimension;
        Object object;

        if (this.mcServer.isDemo())
        {
            object = new DemoWorldManager(this.mcServer.worldServerForDimension(playerIn.dimension));
        }
        else
        {
            object = new ItemInWorldManager(this.mcServer.worldServerForDimension(playerIn.dimension));
        }

        EntityPlayerMP entityplayermp1 = new com.mrnobody.morecommands.patch.EntityPlayerMP(this.mcServer, this.mcServer.worldServerForDimension(playerIn.dimension), playerIn.getGameProfile(), (ItemInWorldManager)object);
        entityplayermp1.playerNetServerHandler = playerIn.playerNetServerHandler;
        entityplayermp1.clonePlayer(playerIn, conqueredEnd);
        entityplayermp1.dimension = dimension;
        entityplayermp1.setEntityId(playerIn.getEntityId());
        entityplayermp1.func_174817_o(playerIn);
        WorldServer worldserver = this.mcServer.worldServerForDimension(playerIn.dimension);
        this.func_72381_a(entityplayermp1, playerIn, worldserver);
        BlockPos blockpos1;

        if (blockpos != null)
        {
            blockpos1 = EntityPlayer.getBedSpawnLocation(this.mcServer.worldServerForDimension(playerIn.dimension), blockpos, flag1);

            if (blockpos1 != null)
            {
                entityplayermp1.setLocationAndAngles((double)((float)blockpos1.getX() + 0.5F), (double)((float)blockpos1.getY() + 0.1F), (double)((float)blockpos1.getZ() + 0.5F), 0.0F, 0.0F);
                entityplayermp1.setSpawnPoint(blockpos, flag1);
            }
            else
            {
                entityplayermp1.playerNetServerHandler.sendPacket(new S2BPacketChangeGameState(0, 0.0F));
            }
        }

        worldserver.theChunkProviderServer.loadChunk((int)entityplayermp1.posX >> 4, (int)entityplayermp1.posZ >> 4);

        while (!worldserver.getCollidingBoundingBoxes(entityplayermp1, entityplayermp1.getEntityBoundingBox()).isEmpty() && entityplayermp1.posY < 256.0D)
        {
            entityplayermp1.setPosition(entityplayermp1.posX, entityplayermp1.posY + 1.0D, entityplayermp1.posZ);
        }

        entityplayermp1.playerNetServerHandler.sendPacket(new S07PacketRespawn(entityplayermp1.dimension, entityplayermp1.worldObj.getDifficulty(), entityplayermp1.worldObj.getWorldInfo().getTerrainType(), entityplayermp1.theItemInWorldManager.getGameType()));
        blockpos1 = worldserver.getSpawnPoint();
        entityplayermp1.playerNetServerHandler.setPlayerLocation(entityplayermp1.posX, entityplayermp1.posY, entityplayermp1.posZ, entityplayermp1.rotationYaw, entityplayermp1.rotationPitch);
        entityplayermp1.playerNetServerHandler.sendPacket(new S05PacketSpawnPosition(blockpos1));
        entityplayermp1.playerNetServerHandler.sendPacket(new S1FPacketSetExperience(entityplayermp1.experience, entityplayermp1.experienceTotal, entityplayermp1.experienceLevel));
        this.updateTimeAndWeatherForPlayer(entityplayermp1, worldserver);
        worldserver.getPlayerManager().addPlayer(entityplayermp1);
        worldserver.spawnEntityInWorld(entityplayermp1);
        this.playerEntityList.add(entityplayermp1);
        this.uuidToPlayerMap.put(entityplayermp1.getUniqueID(), entityplayermp1);
        entityplayermp1.addSelfToInternalCraftingInventory();
        entityplayermp1.setHealth(entityplayermp1.getHealth());
        
        if (ServerPlayerSettings.playerSettingsMapping.containsKey(playerIn) && ServerPlayerSettings.playerSettingsMapping.get(playerIn).keepinventory) {
        	entityplayermp1.inventory.copyInventory(playerIn.inventory);
        	((com.mrnobody.morecommands.patch.EntityPlayerMP) entityplayermp1).setKeepInventory(true);
        }
        
        net.minecraftforge.fml.common.FMLCommonHandler.instance().firePlayerRespawnEvent(entityplayermp1);
        return entityplayermp1;
    }
    
    private void func_72381_a(EntityPlayerMP p_72381_1_, EntityPlayerMP p_72381_2_, World worldIn)
    {
        if (p_72381_2_ != null)
        {
            p_72381_1_.theItemInWorldManager.setGameType(p_72381_2_.theItemInWorldManager.getGameType());
        }
        else if (this.gameType != null)
        {
            p_72381_1_.theItemInWorldManager.setGameType(this.gameType);
        }

        p_72381_1_.theItemInWorldManager.initializeGameType(worldIn.getWorldInfo().getGameType());
    }
}
