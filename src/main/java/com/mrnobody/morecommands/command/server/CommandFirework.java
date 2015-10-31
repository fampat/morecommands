package com.mrnobody.morecommands.command.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.item.EntityFireworkRocket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemFirework;
import net.minecraft.item.ItemFireworkCharge;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.RecipeFireworks;
import net.minecraft.util.BlockPos;

import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;
import com.mrnobody.morecommands.wrapper.Entity;

@Command(
		name = "firework",
		description = "command.firework.description",
		example = "command.firework.example",
		syntax = "command.firework.syntax",
		videoURL = "command.firework.videoURL"
		)
public class CommandFirework extends ServerCommand {
	private List<Item> shapeModifiers;
	private List<Item> effectModifiers;
	private final int MAX_GUNPOWDER = 3;
	private final int MAX_DYE_TYPES = ItemDye.dyeColors.length;
	
	public CommandFirework() {
		this.shapeModifiers = new ArrayList<Item>();
		
		this.shapeModifiers.add(Items.fire_charge);
		this.shapeModifiers.add(Items.gold_nugget);
		this.shapeModifiers.add(Items.skull);
		this.shapeModifiers.add(Items.feather);
		
		this.effectModifiers = new ArrayList<Item>();
		
		this.effectModifiers.add(Items.glowstone_dust);
		this.effectModifiers.add(Items.diamond);
	}

	@Override
	public String getName() {
		return "firework";
	}

	@Override
	public String getUsage() {
		return "command.firework.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		Entity entity = new Entity((net.minecraft.entity.Entity) sender.getMinecraftISender());
		BlockPos spawn = entity.traceBlock(128.0D);
		if (spawn == null) throw new CommandException("command.firework.notFound", sender);
		
		Random rand = new Random();
		List recipes = CraftingManager.getInstance().getRecipeList();
		RecipeFireworks recipe = null;
		
		for (Object o : recipes) {
			if (o instanceof RecipeFireworks) {recipe = (RecipeFireworks) o; break;}
		}
		
		if (recipe != null) {
			InventoryCrafting inv = new InventoryCrafting(new Container()
		    {
		        public boolean canInteractWith(EntityPlayer playerIn) {return false;}
		    }, 3, 3);
			
			ItemStack dye;
			ItemStack output;
			
			do {
				inv.clear();
				
				dye = new ItemStack(Items.dye, 1, rand.nextInt(this.MAX_DYE_TYPES));
				
				inv.setInventorySlotContents(0, dye);
				inv.setInventorySlotContents(1, new ItemStack(Items.gunpowder));
				
				if (rand.nextBoolean())
					inv.setInventorySlotContents(2, new ItemStack(this.shapeModifiers.get(rand.nextInt(this.shapeModifiers.size()))));
				
				if (rand.nextBoolean())
					inv.setInventorySlotContents(3, new ItemStack(this.effectModifiers.get(rand.nextInt(this.effectModifiers.size()))));
			}
			while (!recipe.matches(inv, sender.getMinecraftISender().getEntityWorld()));
			
			output = recipe.getRecipeOutput();
				
			if (output.getItem() instanceof ItemFireworkCharge) {
				do {
					inv.clear();
					
					inv.setInventorySlotContents(0, output);
					inv.setInventorySlotContents(1, new ItemStack(Items.paper));
					
					int gunpowder = rand.nextInt(this.MAX_GUNPOWDER);
					for (int i = 0; i < gunpowder; i++) inv.setInventorySlotContents(2 + i, new ItemStack(Items.gunpowder));
				}
				while (!recipe.matches(inv, sender.getMinecraftISender().getEntityWorld()));
				
				output = recipe.getRecipeOutput();
				
				if (output.getItem() instanceof ItemFirework) {
					ItemFirework firework = (ItemFirework) output.getItem();
					
					EntityFireworkRocket rocket = new EntityFireworkRocket(entity.getWorld().getMinecraftWorld(), spawn.getX(), spawn.getY(), spawn.getZ(), output);;
					entity.getWorld().getMinecraftWorld().spawnEntityInWorld(rocket);
				}
			}
		}
	}
	
	@Override
	public Requirement[] getRequirements() {
		return new Requirement[0];
	}
	
	@Override
	public ServerType getAllowedServerType() {
		return ServerType.ALL;
	}
	
	@Override
	public int getPermissionLevel() {
		return 2;
	}
	
	@Override
	public boolean canSenderUse(ICommandSender sender) {
		return sender instanceof net.minecraft.entity.Entity;
	}
}
