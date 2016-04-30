package com.mrnobody.morecommands.command.server;

import java.util.List;
import java.util.Random;

import com.google.common.collect.ImmutableList;
import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.ServerCommandProperties;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;
import com.mrnobody.morecommands.wrapper.Coordinate;
import com.mrnobody.morecommands.wrapper.EntityLivingBase;

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

@Command(
		name = "firework",
		description = "command.firework.description",
		example = "command.firework.example",
		syntax = "command.firework.syntax",
		videoURL = "command.firework.videoURL"
		)
public class CommandFirework extends StandardCommand implements ServerCommandProperties {
	private static final ImmutableList<Item> shapeModifiers = ImmutableList.of(Items.fire_charge, Items.gold_nugget, Items.skull, Items.feather);
	private static final ImmutableList<Item> effectModifiers = ImmutableList.of(Items.glowstone_dust, Items.diamond);
	private static final int MAX_GUNPOWDER = 3;
	private static final int MAX_DYE_TYPES = ItemDye.field_150922_c.length;
	
	@Override
	public String getCommandName() {
		return "firework";
	}

	@Override
	public String getUsage() {
		return "command.firework.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		EntityLivingBase entity = params.length > 2 ? null : 
			isSenderOfEntityType(sender.getMinecraftISender(), net.minecraft.entity.EntityLivingBase.class) ? 
			new EntityLivingBase(getSenderAsEntity(sender.getMinecraftISender(), net.minecraft.entity.EntityLivingBase.class)) : null;
		Coordinate spawn = null;
		
		if (params.length > 2)
			spawn = getCoordFromParams(sender.getMinecraftISender(), params, 0);
		else
			spawn = entity == null ? sender.getPosition() : entity.traceBlock(128.0D);
		
		if (spawn == null) 
			throw new CommandException("command.firework.notFound", sender);
		
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
		        for (int i = 0; i < inv.getSizeInventory(); ++i) inv.setInventorySlotContents(i, null);
				
				dye = new ItemStack(Items.dye, 1, rand.nextInt(MAX_DYE_TYPES));
				
				inv.setInventorySlotContents(0, dye);
				inv.setInventorySlotContents(1, new ItemStack(Items.gunpowder));
				
				if (rand.nextBoolean())
					inv.setInventorySlotContents(2, new ItemStack(shapeModifiers.get(rand.nextInt(shapeModifiers.size()))));
				
				if (rand.nextBoolean())
					inv.setInventorySlotContents(3, new ItemStack(effectModifiers.get(rand.nextInt(effectModifiers.size()))));
			}
			while (!recipe.matches(inv, sender.getMinecraftISender().getEntityWorld()));
			
			output = recipe.getRecipeOutput();
				
			if (output.getItem() instanceof ItemFireworkCharge) {
				do {
					for (int i = 0; i < inv.getSizeInventory(); ++i) inv.setInventorySlotContents(i, null);
					
					inv.setInventorySlotContents(0, output);
					inv.setInventorySlotContents(1, new ItemStack(Items.paper));
					
					int gunpowder = rand.nextInt(MAX_GUNPOWDER);
					for (int i = 0; i < gunpowder; i++) inv.setInventorySlotContents(2 + i, new ItemStack(Items.gunpowder));
				}
				while (!recipe.matches(inv, sender.getMinecraftISender().getEntityWorld()));
				
				output = recipe.getRecipeOutput();
				
				if (output.getItem() instanceof ItemFirework) {
					ItemFirework firework = (ItemFirework) output.getItem();
					
					EntityFireworkRocket rocket = new EntityFireworkRocket(sender.getWorld().getMinecraftWorld(), spawn.getX(), spawn.getY(), spawn.getZ(), output);;
					sender.getWorld().getMinecraftWorld().spawnEntityInWorld(rocket);
				}
			}
		}
	}
	
	@Override
	public CommandRequirement[] getRequirements() {
		return new CommandRequirement[0];
	}

	@Override
	public ServerType getAllowedServerType() {
		return ServerType.ALL;
	}
	
	@Override
	public int getDefaultPermissionLevel() {
		return 2;
	}
	
	@Override
	public boolean canSenderUse(String commandName, ICommandSender sender, String[] params) {
		return true;
	}
}
