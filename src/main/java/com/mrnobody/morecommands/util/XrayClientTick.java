package com.mrnobody.morecommands.util;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraftforge.fml.common.gameevent.TickEvent;


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
	private XrayHelper xray;
	
	public XrayClientTick(XrayHelper xray) {
		this.xray = xray;
	}

	public void tick(TickEvent.ClientTickEvent event) {
		if (event.phase == TickEvent.Phase.END && this.mc.thePlayer != null) {
			this.xray.localPlayerX = MathHelper.floor_double(this.mc.thePlayer.posX);
			this.xray.localPlayerY = MathHelper.floor_double(this.mc.thePlayer.posY);
			this.xray.localPlayerZ = MathHelper.floor_double(this.mc.thePlayer.posZ);

			if(this.xray.xrayEnabled && (this.thread == null || !this.thread.isAlive()) &&
			(this.mc.theWorld != null && this.mc.thePlayer != null)) {
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
				if (this.xray.xrayEnabled && !this.xray.blockList.isEmpty() && this.mc != null && this.mc.theWorld != null && this.mc.thePlayer != null) {
					if (this.nextTimeMs > System.currentTimeMillis()) {continue;}
					List temp = new ArrayList();
					int radius = this.xray.blockRadius;
					int px = this.xray.localPlayerX;
					int py = this.xray.localPlayerY;
					int pz = this.xray.localPlayerZ;
					for (int y = Math.max(0, py - 96); y < py + 32; y++){
						for (int x = px - radius; x < px + radius; x++) {
							for (int z = pz - radius; z < pz + radius; z++) {
								IBlockState state = this.mc.theWorld.getBlockState(new BlockPos(x, y, z));
								if (state == null || state.getBlock() == null) continue;
								
								for(XrayHelper.BlockSettings xrayBlock : this.xray.blockMapping.values()){
									if ((xrayBlock.draw) && xrayBlock.block == state.getBlock()){
										temp.add(new XrayHelper.BlockPosition(x, y, z, xrayBlock.color));
										break;
									}
								}
							}
						}
					}
					XrayRenderTick.blocks.clear();
					XrayRenderTick.blocks.addAll(temp);
					this.nextTimeMs = System.currentTimeMillis() + this.delayMs;
				} else {
					this.thread.interrupt();
				}
			}
			this.thread = null;
		} catch (Exception ex) {ex.printStackTrace();}
	}
}