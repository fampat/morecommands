package com.mrnobody.morecommands.util;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.util.MathHelper;

import com.mrnobody.morecommands.network.PacketHandlerClient;

import cpw.mods.fml.common.gameevent.TickEvent;

/**
 * Class for ticking xray updates
 * 
 * @author MrNobody98
 *
 */
public class XrayClientTick implements Runnable {
	private final Minecraft mc = Minecraft.getMinecraft();
	private long nextTimeMs = System.currentTimeMillis();
	private final int delayMs = 200;
	private Thread thread = null;

	public void tick(TickEvent.ClientTickEvent event) {
		if (event.phase == TickEvent.Phase.END && mc.thePlayer != null) {
			PacketHandlerClient.INSTANCE.xrayHelper.localPlayerX = MathHelper.floor_double(mc.thePlayer.posX);
			PacketHandlerClient.INSTANCE.xrayHelper.localPlayerY = MathHelper.floor_double(mc.thePlayer.posY);
			PacketHandlerClient.INSTANCE.xrayHelper.localPlayerZ = MathHelper.floor_double(mc.thePlayer.posZ);

			if(PacketHandlerClient.INSTANCE.xrayHelper.xrayEnabled && (this.thread == null || !this.thread.isAlive()) &&
			(mc.theWorld != null && mc.thePlayer != null)) {
				this.thread = new Thread(this);
				this.thread.setDaemon(false);
				this.thread.setPriority(Thread.MAX_PRIORITY);
				this.thread.start();
			}
		}
	}


	@Override
	public void run() {
		try{
			while(!this.thread.isInterrupted()){
				if (PacketHandlerClient.INSTANCE.xrayHelper.xrayEnabled && !PacketHandlerClient.INSTANCE.xrayHelper.blockList.isEmpty() && (mc != null) && (mc.theWorld != null) && (mc.thePlayer != null)) {
					if (nextTimeMs > System.currentTimeMillis()) {continue;}
					List temp = new ArrayList();
					int radius = PacketHandlerClient.INSTANCE.xrayHelper.blockRadius;
					int px = PacketHandlerClient.INSTANCE.xrayHelper.localPlayerX;
					int py = PacketHandlerClient.INSTANCE.xrayHelper.localPlayerY;
					int pz = PacketHandlerClient.INSTANCE.xrayHelper.localPlayerZ;
					for (int y = Math.max(0, py - 96); y < py + 32; y++){
						for (int x = px - radius; x < px + radius; x++) {
							for (int z = pz - radius; z < pz + radius; z++) {
								Block block = mc.theWorld.getBlock(x, y, z);
								
								for(XrayHelper.xrayBlockInfo xrayBlock : PacketHandlerClient.INSTANCE.xrayHelper.blockMapping.values()){
									if ((xrayBlock.draw) && xrayBlock.block == block){
										temp.add(new XrayHelper.BlockInfo(x, y, z, xrayBlock.color));
										break;
									}
								}
							}
						}
					}
					XrayRenderTick.blocks.clear();
					XrayRenderTick.blocks.addAll(temp);
					nextTimeMs = System.currentTimeMillis() + delayMs;
				} else {
					this.thread.interrupt();
				}
			}
			this.thread = null;
		} catch (Exception ex) {ex.printStackTrace();}
	}
}