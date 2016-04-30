package com.mrnobody.morecommands.util;

import java.awt.Color;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import cpw.mods.fml.client.config.GuiCheckBox;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * The xray configuration gui class
 * 
 * @author MrNobody98
 *
 */
public class XrayConfGui extends GuiScreen {
	/**
	 * The edit gui for a single block
	 * 
	 * @author MrNobody98
	 */
	public class GuiEdit extends GuiScreen {
		/**
		 * A color slider
		 * 
		 * @author MrNobody98
		 */
		public class GuiSlider extends GuiButton {
			private float sliderValue = 1.0F;
			private float sliderMaxValue = 1.0F;
			private boolean dragging = false;
			private String label;

			public GuiSlider(int id, int x, int y, String label, float startingValue, float maxValue) {
				super(id, x, y, 150, 20, label);
				this.label = label;
	        	this.sliderValue = startingValue;
	        	this.sliderMaxValue = maxValue;
			}

			public int getHoverState(boolean par1) {
				return 0;
			}

			protected void mouseDragged(Minecraft par1Minecraft, int par2, int par3) {
	            	if (this.dragging) {
	            		this.sliderValue = (float) (par2 - (this.xPosition + 4)) / (float) (this.width - 8);

	            		if (this.sliderValue < 0.0F) {
	            			this.sliderValue = 0.0F;
	            		}

	            		if (this.sliderValue > 1.0F) {
	            			this.sliderValue = 1.0F;
	            		}

	            	}

	            	this.displayString = label + ": " + (int) (sliderValue * sliderMaxValue);
	            	GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
	            	this.drawTexturedModalRect(this.xPosition + (int) (this.sliderValue * (float) (this.width - 8)), this.yPosition, 0, 66, 4, 20);
	            	this.drawTexturedModalRect(this.xPosition + (int) (this.sliderValue * (float) (this.width - 8)) + 4, this.yPosition, 196, 66, 4, 20);
			}

			public boolean mousePressed(Minecraft par1Minecraft, int par2, int par3) {
				if (super.mousePressed(par1Minecraft, par2, par3)) {
					this.sliderValue = (float) (par2 - (this.xPosition + 4)) / (float) (this.width - 8);

					if (this.sliderValue < 0.0F) {
						this.sliderValue = 0.0F;
					}

					if (this.sliderValue > 1.0F) {
						this.sliderValue = 1.0F;
					}

					this.dragging = true;
					return true;
				} else {
					return false;
				}
			}

			public void mouseReleased(int par1, int par2) {
				this.dragging = false;
			}
		}
		
		private GuiSlider redSlider;
		private GuiSlider greenSlider;
		private GuiSlider blueSlider;
		private GuiButton addButton;
		private GuiCheckBox enableBox;
		private boolean isEnabled;
		
		public GuiEdit(boolean isEnabled) {this.isEnabled = isEnabled;}
	
		@Override
		public void initGui() {
			this.redSlider = new GuiSlider(1, this.width / 2 - 100, this.height / 2 - 40, "Red", 0, 255 );
			this.greenSlider = new GuiSlider(2, this.width / 2 - 100, this.height / 2 - 20, "Green", 0, 255 );
			this.blueSlider = new GuiSlider(3, this.width / 2 - 100, this.height / 2 -  0, "Blue", 0, 255 );
			this.enableBox = new GuiCheckBox(4, this.width / 2 - 100, this.height / 2 - 60, "Enable Xray for this block", this.isEnabled);
			this.addButton = new GuiButton(98, this.width - 100, this.height - 20, 100, 20, "Apply");
			
			this.redSlider.enabled = false;
			this.greenSlider.enabled = false;
			this.blueSlider.enabled = false;
			
			this.buttonList.add(new GuiButton(99, 2, this.height - 20, 100, 20, "Cancel"));
			this.buttonList.add(this.addButton);
			this.buttonList.add(this.redSlider);
			this.buttonList.add(this.greenSlider);
			this.buttonList.add(this.blueSlider);
			this.buttonList.add(this.enableBox);
			
			this.redSlider.sliderValue = 0.0F;
			this.greenSlider.sliderValue = 1.0F;
			this.blueSlider.sliderValue = 0.0F;
		}
	
		@Override
		public void actionPerformed(GuiButton button) {
			if (button.id == 4) {boolean flag = ((GuiCheckBox) button).isChecked(); this.redSlider.enabled = flag; this.greenSlider.enabled = flag; this.blueSlider.enabled = flag;}
			if (button.id == 98) {XrayConfGui.this.setBlockInfo(redSlider.sliderValue, greenSlider.sliderValue, blueSlider.sliderValue, this.enableBox.isChecked()); mc.thePlayer.closeScreen();}
			else if (button.id == 99) mc.thePlayer.closeScreen();
		}
	
		@Override
		public void drawScreen(int x, int y, float f){
			drawDefaultBackground();
			super.drawScreen(x, y, f);
		
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glDepthMask(false);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		
			GL11.glBegin(GL11.GL_QUADS);
			GL11.glColor3f(redSlider.sliderValue, greenSlider.sliderValue, blueSlider.sliderValue);
			GL11.glVertex2d(width / 2 + 50, height / 2 - 40);
			GL11.glVertex2d(width / 2 + 50, height / 2 + 20);
			GL11.glVertex2d(width / 2 + 112, height / 2 + 20);
			GL11.glVertex2d(width / 2 + 112, height / 2 - 40);
			GL11.glEnd();
		
			GL11.glDepthMask(true);
			GL11.glDisable(GL11.GL_BLEND);
		}
	}
	
	/**
	 * The block list gui
	 * 
	 * @author MrNobody98
	 *
	 */
	public class GuiList extends GuiSlot {
		private RenderItem itemRender = new RenderItem();
		private FontRenderer fontRenderer;
		
		public GuiList() {
			super(XrayConfGui.this.mc, XrayConfGui.this.width, XrayConfGui.this.height, 30, XrayConfGui.this.height - 30, XrayConfGui.this.mc.fontRenderer.FONT_HEIGHT + 10);
			this.fontRenderer = XrayConfGui.this.mc.fontRenderer;
		}

		@Override
		protected int getSize() {
			return XrayConfGui.this.blockList.length;
		}

		@Override
		protected void elementClicked(int index, boolean doubleClick, int p_148144_3_, int p_148144_4_) {
			XrayConfGui.this.elementSelected = index;
			boolean buttonsEnabled = XrayConfGui.this.elementSelected >= 0 && XrayConfGui.this.elementSelected < this.getSize();
			XrayConfGui.this.guiButtonConfigure.enabled = buttonsEnabled;
			
			if (doubleClick && buttonsEnabled)
					XrayConfGui.this.loadConfigGUI(XrayConfGui.this.xray.drawBlock(XrayConfGui.this.blockList[XrayConfGui.this.elementSelected]));
		}

		@Override
		protected boolean isSelected(int index) {
			return index == XrayConfGui.this.elementSelected;
		}
		
		@Override
		protected int getContentHeight() {
			return XrayConfGui.this.blockList.length * (XrayConfGui.this.mc.fontRenderer.FONT_HEIGHT + 10);
		}

		@Override
		protected void drawBackground() {
			XrayConfGui.this.drawDefaultBackground();
		}
		
		@Override
		protected void drawContainerBackground(Tessellator tessellator) {}

		@Override
		protected void drawSlot(int index, int left, int top, int p_148126_4_, Tessellator p_148126_5_, int p_148126_6_, int p_148126_7_) {
			String blockName = XrayConfGui.this.blockList[index].getLocalizedName();
			Item blockItem = Item.getItemFromBlock(XrayConfGui.this.blockList[index]);
			
			if (blockItem != null) {
		        itemRender.zLevel = 100.0F;
		        GL11.glEnable(GL11.GL_LIGHTING);
		        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
				this.itemRender.renderItemAndEffectIntoGUI(this.fontRenderer, XrayConfGui.this.mc.getTextureManager(), new ItemStack(blockItem), left, top);
				this.itemRender.renderItemOverlayIntoGUI(this.fontRenderer, XrayConfGui.this.mc.getTextureManager(), new ItemStack(blockItem), left, top);
		        GL11.glDisable(GL11.GL_LIGHTING);
		        itemRender.zLevel = 0.0F;
			}
			XrayConfGui.this.drawString(this.fontRenderer, blockName + " - " + (XrayConfGui.this.xray.drawBlock(XrayConfGui.this.blockList[index]) ? "ENABLED" : "DISABLED"), left + 20, top + 3, 16777215);
		}
	}
	
	private Minecraft mc;
	private Xray xray;
	private String heading = "MoreCommands: Xray Configuration";
	private int elementSelected;
	private GuiList guiList;
	private GuiButton guiButtonConfigure;
	private Block[] blockList;
	
	public XrayConfGui(Minecraft mc, Xray xray) {
		this.mc = mc;
		this.xray = xray;
		this.blockList = xray.getAllBlocks();
	}
	
	@Override
	public void initGui() {
		this.guiList = new GuiList();
		this.guiList.registerScrollButtons(4, 5);
		this.guiButtonConfigure = new GuiButton(1, this.width - 100, this.height - 20, 100, 20, "Configure");
		this.guiButtonConfigure.enabled = false;
		this.buttonList.add(this.guiButtonConfigure);
		this.buttonList.add(new GuiButton(0, 2, this.height - 20, 100, 20, "Cancel"));
	}
	
	@Override
	protected void actionPerformed(GuiButton button) {
		if (button.enabled) {
			if (button.id == 1) {
				if (this.blockList[this.elementSelected] != null)
					this.loadConfigGUI(this.xray.drawBlock(this.blockList[this.elementSelected]));
			}
			else if (button.id == 0) {
				this.mc.thePlayer.closeScreen();
			}
		}
	}
	
	@Override
	public void drawScreen(int p_73863_1_, int p_73863_2_, float p_73863_3_) {
		this.guiList.drawScreen(p_73863_1_, p_73863_2_, p_73863_3_);
        this.drawCenteredString(this.fontRendererObj, this.heading, this.width / 2, 10, 16777215);
        super.drawScreen(p_73863_1_, p_73863_2_, p_73863_3_);
	}
	
	public void loadConfigGUI(boolean isEnabled) {
		this.mc.displayGuiScreen(new GuiEdit(isEnabled));
	}
	
	public void setBlockInfo(float red, float green, float blue, boolean draw) {
		this.xray.changeBlockSettings(this.blockList[this.elementSelected], draw, new Color(red, green, blue));
	}
	
	public void displayGUI() {this.initGui(); this.mc.displayGuiScreen(this);}
}
