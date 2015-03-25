package com.mrnobody.morecommands.util;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.registry.GameData;

import com.mrnobody.morecommands.handler.EventHandler;
import com.mrnobody.morecommands.handler.Listener;

/**
 * The class which handles everything what has to do with xray
 * 
 * @author MrNobody98
 *
 */
public class XrayHelper {
	private class TickListener implements Listener<TickEvent> {
		@Override
		public void onEvent(TickEvent event) {
			if (event instanceof TickEvent.ClientTickEvent) {
				clTick.tick((TickEvent.ClientTickEvent) event);
			}
		}
	}
	
	private class RenderListener implements Listener<RenderWorldLastEvent> {
		@Override
		public void onEvent(RenderWorldLastEvent event) {
			rTick.onRenderEvent(event);
		}
	}
	
	public static int localPlayerX, localPlayerY, localPlayerZ;
	public static boolean xrayEnabled = false;
	public static int blockRadius = 32;
	public static List<Block> blockList = new ArrayList<Block>();
	public static Map<Block, XrayBlockInfo> blockMapping = new HashMap<Block, XrayBlockInfo>();
	
	private XrayClientTick clTick;
	private XrayRenderTick rTick;
	private XrayConfGui confGUI = new XrayConfGui(Minecraft.getMinecraft());
	
	public static class XrayBlockInfo {
		public Block block;
		public Color color;
		public boolean draw;
		
		public XrayBlockInfo(Block block, Color color, boolean draw){
			this.block = block;
			this.color = color;
			this.draw = draw;
		}
		
		public void disable() {this.draw = false;}
		
		public void enable() {this.draw = true;}
	}
	
	public static class BlockInfo {
		public int x, y, z;
		public Color color;
		
		public BlockInfo(int bx, int by, int bz, Color c){
			this.x = bx;
			this.y = by;
			this.z = bz;
			this.color = c;
		}
	}
	
	public XrayHelper() {
		EventHandler.TICK.getHandler().register(new TickListener());
		EventHandler.RENDERWORLD.getHandler().register(new RenderListener());
		this.clTick = new XrayClientTick();
		this.rTick = new XrayRenderTick();
		
		Block block;
		Iterator<Block> blocks = GameData.getBlockRegistry().iterator();
		while (blocks.hasNext()) {
			block = blocks.next();
			blockList.add(block);
			this.blockMapping.put(block, new XrayBlockInfo(block, Color.WHITE, false));
		}
	}
	
	public void showConfig() {
		this.confGUI.displayGUI();
	}
	
	public void changeSettings(int blockRadius, boolean enableXray) {
		this.blockRadius = blockRadius;
		this.xrayEnabled = enableXray;
	}
}
