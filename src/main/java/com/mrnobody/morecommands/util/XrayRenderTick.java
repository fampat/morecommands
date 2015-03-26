package com.mrnobody.morecommands.util;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraftforge.client.event.RenderWorldLastEvent;

import org.lwjgl.opengl.GL11;

import com.mrnobody.morecommands.command.server.CommandXray;
import com.mrnobody.morecommands.network.PacketHandlerClient;

/**
 * Class to render block highlighting used for xray
 * 
 * @author MrNobody98
 *
 */
public class XrayRenderTick {
	private XrayHelper xray;
	private final Minecraft mc = Minecraft.getMinecraft();
	public static List<XrayHelper.BlockPosition> blocks = new ArrayList();
	
	public XrayRenderTick(XrayHelper xray) {
		this.xray = xray;
	}

	public void onRenderEvent(RenderWorldLastEvent event){
		if (this.mc.theWorld != null && this.xray.xrayEnabled) {
			float f = event.partialTicks;
			float px = (float) this.mc.thePlayer.posX;
			float py = (float) this.mc.thePlayer.posY;
			float pz = (float) this.mc.thePlayer.posZ;
			float mx = (float) this.mc.thePlayer.prevPosX;
			float my = (float) this.mc.thePlayer.prevPosY;
			float mz = (float) this.mc.thePlayer.prevPosZ;
			float dx = mx + (px - mx) * f;
			float dy = my + (py - my) * f;
			float dz = mz + (pz - mz) * f;
			drawOres(dx, dy, dz);
		}
	}
	
	private void drawOres(float px, float py, float pz){
		int bx, by, bz;
		
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_CULL_FACE);
		GL11.glDepthMask(false);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glLineWidth(1f);
		Tessellator tes = Tessellator.instance;
		
		List<XrayHelper.BlockPosition> temp = new ArrayList();
		temp.addAll(this.blocks);
		
		for (XrayHelper.BlockPosition b : temp){
			bx = b.x;
			by = b.y;
			bz = b.z;
			float f = 0.0f;
			float f1 = 1.0f;
			
			tes.startDrawing(GL11.GL_LINES);
			tes.setColorRGBA(b.color.getRed(), b.color.getGreen(), b.color.getBlue(), 255);
			tes.setBrightness(200);
			
			tes.addVertex(bx-px + f, by-py + f1, bz-pz + f); tes.addVertex(bx-px + f1, by-py + f1, bz-pz + f);
			tes.addVertex(bx-px + f1, by-py + f1, bz-pz + f); tes.addVertex(bx-px + f1, by-py + f1, bz-pz + f1); 
			tes.addVertex(bx-px + f1, by-py + f1, bz-pz + f1); tes.addVertex(bx-px + f, by-py + f1, bz-pz + f1);
			tes.addVertex(bx-px + f, by-py + f1, bz-pz + f1); tes.addVertex(bx-px + f, by-py + f1, bz-pz + f);
	
			tes.addVertex(bx-px + f1, by-py + f, bz-pz + f); tes.addVertex(bx-px + f1, by-py + f, bz-pz + f1);
			tes.addVertex(bx-px + f1, by-py + f, bz-pz + f1); tes.addVertex(bx-px + f, by-py + f, bz-pz + f1);
			tes.addVertex(bx-px + f, by-py + f, bz-pz + f1); tes.addVertex(bx-px + f, by-py + f, bz-pz + f);
			tes.addVertex(bx-px + f, by-py + f, bz-pz + f); tes.addVertex(bx-px + f1, by-py + f, bz-pz + f);
			
			tes.addVertex(bx-px + f1, by-py + f, bz-pz + f1); tes.addVertex(bx-px + f1, by-py + f1, bz-pz + f1);
			tes.addVertex(bx-px + f1, by-py + f, bz-pz + f); tes.addVertex(bx-px + f1, by-py + f1, bz-pz + f);
			tes.addVertex(bx-px + f, by-py + f, bz-pz + f1); tes.addVertex(bx-px + f, by-py + f1, bz-pz + f1);
			tes.addVertex(bx-px + f, by-py + f, bz-pz + f); tes.addVertex(bx-px + f, by-py + f1, bz-pz + f);
			
			tes.draw();
		}
		
		GL11.glDepthMask(true);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_CULL_FACE);
	}
}
