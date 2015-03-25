package com.mrnobody.morecommands.util;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import com.mrnobody.morecommands.network.PacketHandlerClient;


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
								IBlockState state = mc.theWorld.getBlockState(new BlockPos(x, y, z));
								if (state == null || state.getBlock() == null) continue;
								
								for(XrayHelper.XrayBlockInfo xrayBlock : PacketHandlerClient.INSTANCE.xrayHelper.blockMapping.values()){
									if ((xrayBlock.draw) && xrayBlock.block == state.getBlock()){
										temp.add(new XrayHelper.BlockInfo(x, y, z, xrayBlock.color));
										break;
									}
								}
							}
						}
					}
					XrayRenderTick.ores.clear();
					XrayRenderTick.ores.addAll(temp);
					nextTimeMs = System.currentTimeMillis() + delayMs;
				} else {
					this.thread.interrupt();
				}
			}
			this.thread = null;
		} catch (Exception ex) {ex.printStackTrace();}
	}
}