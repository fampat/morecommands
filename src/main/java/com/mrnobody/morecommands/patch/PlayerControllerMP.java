package com.mrnobody.morecommands.patch;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.stats.StatFileWriter;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;

/**
 * The patched class of {@link net.minecraft.client.multiplayer.PlayerControllerMP} <br>
 * This class sets the client player ({@link Minecraft#thePlayer}), which is the actual target
 * I want to modify. <br> By modifying the {@link PlayerControllerMP#func_147493_a(World, StatFileWriter)}
 * method, my own patched class of {@link EntityClientPlayerMP} is used. Another point is that
 * this class determines the block reach distance, which can be modified by the reach command.
 * 
 * @author MrNobody98
 *
 */
public class PlayerControllerMP extends net.minecraft.client.multiplayer.PlayerControllerMP {
	private NetHandlerPlayClient netClientHandler;
	private Minecraft mc;
	private int currentPlayerItem;
	
	private float reachDistance = 5.0F;
	
	public PlayerControllerMP(Minecraft mc, NetHandlerPlayClient netClientHandler) {
		super(mc, netClientHandler);
		this.netClientHandler = netClientHandler;
		this.mc = mc;
	}
	
	@Override
    public float getBlockReachDistance()
    {
        return this.reachDistance;
    }
	
    public void setBlockReachDistance(float distance)
    {
    	this.reachDistance = distance;
    }
    
    @Override
    public EntityClientPlayerMP func_147493_a(World p_147493_1_, StatFileWriter p_147493_2_)
    {
        return new com.mrnobody.morecommands.patch.EntityClientPlayerMP(this.mc, p_147493_1_, this.mc.getSession(), this.netClientHandler, p_147493_2_);
    }
}
