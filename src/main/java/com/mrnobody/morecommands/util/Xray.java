package com.mrnobody.morecommands.util;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.ImmutableMap;
import com.mrnobody.morecommands.event.EventHandler;
import com.mrnobody.morecommands.event.Listeners.EventListener;
import com.mrnobody.morecommands.event.Listeners.TwoEventListener;
import com.mrnobody.morecommands.settings.MoreCommandsConfig;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.registry.GameData;

/**
 * The class controlling xray.
 * 
 * @author MrNobody98
 */
public final class Xray implements Runnable, TwoEventListener<TickEvent, RenderWorldLastEvent>, EventListener<WorldEvent.Unload> {
	/**
	 * A class that contains the xray setting for a block
	 * 
	 * @author MrNobody98
	 */
	private static class BlockSettings {
		/** the block for which this settings apply */
		public Block block;
		/** the xray color for this block */
		public Color color;
		/** whether to highlight this block via xray */
		public boolean draw;
		
		public BlockSettings(Block block, Color color, boolean draw){
			this.block = block;
			this.color = color;
			this.draw = draw;
		}
	}
	
	/**
	 * This class contains the position of a block which should be
	 * highlighted via xray and the highlighting color
	 * 
	 * @author MrNobody98
	 */
	private static class BlockPosition {
		/** the position of the block to highlight */
		public int x, y, z;
		/** The color with which the block should be highlighted */
		public Color color;
		
		public BlockPosition(int x, int y, int z, Color c){
			this.x = x;
			this.y = y;
			this.z = z;
			this.color = c;
		}
	}
	
	/** The singleton xray instance */
	private static Xray INSTANCE;
	
	/**
	 * @return the {@link Xray} instance
	 */
	public static Xray getXray() {
		if (INSTANCE == null) INSTANCE = new Xray();
		return INSTANCE;
	}
	
	/** The delay between xray updates */
	private static final int DELAY = MoreCommandsConfig.xrayUPS <= 0 ? 1000 : MoreCommandsConfig.xrayUPS > 10 ? 100 : 1000 / MoreCommandsConfig.xrayUPS;
	/** The default radius in which blocks should be highlighted */
	private static final int DEFAULT_RADIUS = 32;
	
	/** The coordinates of the client player's position */
	private int localPlayerX, localPlayerY, localPlayerZ;
	/** Whether xray is turned on */
	private boolean xrayEnabled = false;
	/** The current radius in which blocks should be highlighted */
	private int blockRadius = DEFAULT_RADIUS;
	
	/** A reference to the Minecraft object */
	private final Minecraft mc = Minecraft.getMinecraft();
	/** A map that contains the settings for each block */
	private final ImmutableMap<Block, BlockSettings> blockSettings;
	/** The block for which should be highlighted/which are to b rendered */
	private final List<BlockPosition> renderBlocks = new ArrayList<BlockPosition>();
	
	/** The configuration gui */
	private XrayConfGui confGUI;
	/** The thread that performs xray updates */
	private Thread thread;
	
	private Xray() {
		EventHandler.TICK.registerFirst(this);
		EventHandler.RENDERWORLD.registerSecond(this);
		EventHandler.UNLOAD_WORLD.register(this);
		
		ImmutableMap.Builder<Block, BlockSettings> builder = ImmutableMap.builder();
		Block block;
		Iterator<Block> blocks = GameData.getBlockRegistry().iterator();
		
		while (blocks.hasNext()) {
			block = blocks.next();
			builder.put(block, new BlockSettings(block, Color.WHITE, false));
		}
		
		this.blockSettings = builder.build();
		this.confGUI = new XrayConfGui(this.mc, this); 
	}
	
	/**
	 * Updates the local player's position and starts the xray thread if xray was enabled
	 */
	public void onEvent1(TickEvent event) {
		if (!(event instanceof TickEvent.ClientTickEvent) || event.phase != TickEvent.Phase.END || this.mc.thePlayer == null) return;
		
		this.localPlayerX = MathHelper.floor_double(this.mc.thePlayer.posX);
		this.localPlayerY = MathHelper.floor_double(this.mc.thePlayer.posY);
		this.localPlayerZ = MathHelper.floor_double(this.mc.thePlayer.posZ);

		if (this.xrayEnabled && (this.thread == null || !this.thread.isAlive()) && this.mc.theWorld != null && this.mc.thePlayer != null) {
			this.thread = new Thread(this);
			this.thread.setDaemon(false);
			this.thread.setName("MoreCommands Xray Thread");
			this.thread.start();
		}
	}
	
	/**
	 * Draws the highlighting for the block for which xray is enabled
	 */
	public void onEvent2(RenderWorldLastEvent event) {
		if (this.mc.theWorld != null && this.xrayEnabled) {
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
	
	/**
	 * Invoked when the client world is unloaded. Resets all xray settings and stops the xray thread
	 */
	public void onEvent(WorldEvent.Unload event) {
		if (event.world == Minecraft.getMinecraft().theWorld) {
			this.changeXraySettings(DEFAULT_RADIUS, false);
			
			if (this.thread != null && this.thread.isAlive()) {
				this.thread.interrupt();
				try {this.thread.join();}
				catch (InterruptedException ex) {}
			}
			
			this.renderBlocks.clear();
		}
	}
	
	/**
	 * Periodically updates the blocks for which the highlighting has to be rendered
	 */
	public void run() {
		try{
			while(!this.thread.isInterrupted()) {
				try {Thread.sleep(DELAY);}
				catch (InterruptedException ex) {break;}
				
				if (this.xrayEnabled && !this.blockSettings.isEmpty() && this.mc != null && this.mc.theWorld != null && this.mc.thePlayer != null) {
					List<BlockPosition> temp = new ArrayList<BlockPosition>();
					int radius = this.blockRadius;
					int px = this.localPlayerX;
					int py = this.localPlayerY;
					int pz = this.localPlayerZ;
					
					for (int y = Math.max(0, py - 96); y < py + 32; y++) {
						for (int x = px - radius; x < px + radius; x++) {
							for (int z = pz - radius; z < pz + radius; z++) {
								IBlockState state = this.mc.theWorld.getBlockState(new BlockPos(x, y, z));
								if (state == null || state.getBlock() == null) continue;
								
								for(BlockSettings settings : this.blockSettings.values()){
									if (settings.draw && settings.block == state.getBlock()){
										temp.add(new BlockPosition(x, y, z, settings.color));
										break;
									}
								}
							}
						}
					}
					
					this.renderBlocks.clear();
					this.renderBlocks.addAll(temp);
				}
				else break;
			}
		}
		catch (Exception ex) {ex.printStackTrace();}
		
		this.thread = null;
	}
	
	/**
	 * Highlights all blocks which have to be rendered
	 * 
	 * @param px the x position of the middle point
	 * @param py the y position of the middle point
	 * @param pz the z position of the middle point
	 */
	private void drawOres(float px, float py, float pz){
		int bx, by, bz;
		
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_CULL_FACE);
		GL11.glDepthMask(false);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glLineWidth(1f);
		WorldRenderer w = Tessellator.getInstance().getWorldRenderer();
		
		List<BlockPosition> temp = new ArrayList();
		temp.addAll(this.renderBlocks);
		
		for (BlockPosition b : temp){
			bx = b.x;
			by = b.y;
			bz = b.z;
			float f = 0.0f;
			float f1 = 1.0f;
			int red = b.color.getRed(), green = b.color.getGreen(), blue = b.color.getBlue();
			
			w.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
			
			w.pos(bx-px + f, by-py + f1, bz-pz + f).color(red, green, blue, 255).endVertex(); 
			w.pos(bx-px + f1, by-py + f1, bz-pz + f).color(red, green, blue, 255).endVertex();
			w.pos(bx-px + f1, by-py + f1, bz-pz + f).color(red, green, blue, 255).endVertex(); 
			w.pos(bx-px + f1, by-py + f1, bz-pz + f1).color(red, green, blue, 255).endVertex(); 
			w.pos(bx-px + f1, by-py + f1, bz-pz + f1).color(red, green, blue, 255).endVertex(); 
			w.pos(bx-px + f, by-py + f1, bz-pz + f1).color(red, green, blue, 255).endVertex();
			w.pos(bx-px + f, by-py + f1, bz-pz + f1).color(red, green, blue, 255).endVertex(); 
			w.pos(bx-px + f, by-py + f1, bz-pz + f).color(red, green, blue, 255).endVertex();
	
			w.pos(bx-px + f1, by-py + f, bz-pz + f).color(red, green, blue, 255).endVertex(); 
			w.pos(bx-px + f1, by-py + f, bz-pz + f1).color(red, green, blue, 255).endVertex();
			w.pos(bx-px + f1, by-py + f, bz-pz + f1).color(red, green, blue, 255).endVertex(); 
			w.pos(bx-px + f, by-py + f, bz-pz + f1).color(red, green, blue, 255).endVertex();
			w.pos(bx-px + f, by-py + f, bz-pz + f1).color(red, green, blue, 255).endVertex(); 
			w.pos(bx-px + f, by-py + f, bz-pz + f).color(red, green, blue, 255).endVertex();
			w.pos(bx-px + f, by-py + f, bz-pz + f).color(red, green, blue, 255).endVertex(); 
			w.pos(bx-px + f1, by-py + f, bz-pz + f).color(red, green, blue, 255).endVertex();
			
			w.pos(bx-px + f1, by-py + f, bz-pz + f1).color(red, green, blue, 255).endVertex(); 
			w.pos(bx-px + f1, by-py + f1, bz-pz + f1).color(red, green, blue, 255).endVertex();
			w.pos(bx-px + f1, by-py + f, bz-pz + f).color(red, green, blue, 255).endVertex(); 
			w.pos(bx-px + f1, by-py + f1, bz-pz + f).color(red, green, blue, 255).endVertex();
			w.pos(bx-px + f, by-py + f, bz-pz + f1).color(red, green, blue, 255).endVertex();
			w.pos(bx-px + f, by-py + f1, bz-pz + f1).color(red, green, blue, 255).endVertex();
			w.pos(bx-px + f, by-py + f, bz-pz + f).color(red, green, blue, 255).endVertex(); 
			w.pos(bx-px + f, by-py + f1, bz-pz + f).color(red, green, blue, 255).endVertex();
			
			Tessellator.getInstance().draw();
		}
		
		GL11.glDepthMask(true);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_CULL_FACE);
	}
	
	/**
	 * Display the xray configuration menu
	 */
	public void showConfig() {
		this.confGUI.displayGUI();
	}
	
	/**
	 * Changes the settings for a block
	 * 
	 * @param block the block for which the settings are to be changed
	 * @param draw whether to draw xray for this block
	 * @param color the xray color for this block
	 */
	public void changeBlockSettings(Block block, boolean draw, Color color) {
		BlockSettings settings = blockSettings.get(block);
		if (settings != null) {settings.draw = draw; settings.color = color;}
	}
	
	/**
	 * Changes the settings for a block
	 * 
	 * @param block the block for which the settings are to be changed
	 * @param color the xray color for this block
	 */
	public void changeBlockSettings(Block block, Color color) {
		BlockSettings settings = blockSettings.get(block);
		if (settings != null) settings.color = color;
	}
	
	/**
	 * Changes the settings for a block
	 * 
	 * @param block the block for which the settings are to be changed
	 * @param draw whether to draw xray for this block
	 */
	public void changeBlockSettings(Block block, boolean draw) {
		BlockSettings settings = blockSettings.get(block);
		if (settings != null) settings.draw = draw;
	}
	
	/**
	 * Checks whether xray is enabled for a block
	 * 
	 * @param block the block to check
	 * @return whether xray is enabled for this block
	 */
	public boolean drawBlock(Block block) {
		return this.blockSettings.containsKey(block) ? this.blockSettings.get(block).draw : false;
	}
	
	/**
	 * Gets the xray color for a block
	 * 
	 * @param block the block to get the color for
	 * @return the color
	 */
	public Color getColor(Block block) {
		return this.blockSettings.containsKey(block) ? this.blockSettings.get(block).color : Color.WHITE;
	}
	
	/**
	 * @return the current xray block radius
	 */
	public int getRadius() {
		return this.blockRadius;
	}
	
	/**
	 * @return whether xray is turned on
	 */
	public boolean isEnabled() {
		return this.xrayEnabled;
	}
	
	/**
	 * Changes xray settings
	 * 
	 * @param blockRadius the new xray block radius
	 * @param enableXray whether xray is turned on
	 */
	public void changeXraySettings(int blockRadius, boolean enableXray) {
		this.blockRadius = blockRadius;
		this.xrayEnabled = enableXray;
	}
	
	/**
	 * Changes xray settings
	 * 
	 * @param blockRadius the new xray block radius
	 */
	public void changeXraySettings(int blockRadius) {
		this.blockRadius = blockRadius;
	}
	
	/**
	 * Changes xray settings
	 * 
	 * @param enableXray whether xray is turned on
	 */
	public void changeXraySettings(boolean enableXray) {
		this.xrayEnabled = enableXray;
	}
	
	/**
	 * @return all blocks for which xray can be used
	 */
	public Block[] getAllBlocks() {
		return this.blockSettings.keySet().toArray(new Block[this.blockSettings.size()]);
	}
}
