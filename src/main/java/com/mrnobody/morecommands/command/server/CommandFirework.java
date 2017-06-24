package com.mrnobody.morecommands.command.server;

import java.util.List;
import java.util.Random;

import com.google.common.collect.ImmutableList;
import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandException;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.CommandSender;
import com.mrnobody.morecommands.command.ServerCommandProperties;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.util.EntityUtils;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
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
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

@Command(
		name = "firework",
		description = "command.firework.description",
		example = "command.firework.example",
		syntax = "command.firework.syntax",
		videoURL = "command.firework.videoURL"
		)
public class CommandFirework extends StandardCommand implements ServerCommandProperties {
	private static final ImmutableList<Item> shapeModifiers = ImmutableList.of(Items.FIRE_CHARGE, Items.GOLD_NUGGET, Items.SKULL, Items.FEATHER);
	private static final ImmutableList<Item> effectModifiers = ImmutableList.of(Items.GLOWSTONE_DUST, Items.DIAMOND);
	private static final int MAX_GUNPOWDER = 3;
	private static final int MAX_DYE_TYPES = ItemDye.DYE_COLORS.length;
	
	@Override
	public String getCommandName() {
		return "firework";
	}

	@Override
	public String getCommandUsage() {
		return "command.firework.syntax";
	}

	@Override
	public String execute(CommandSender sender, String[] params) throws CommandException {
		Entity entity = params.length > 2 ? null : 
			isSenderOfEntityType(sender.getMinecraftISender(), Entity.class) ? 
			getSenderAsEntity(sender.getMinecraftISender(), Entity.class) : null;
		BlockPos spawn = null;
		
		if (params.length > 2)
			spawn = getCoordFromParams(sender.getMinecraftISender(), params, 0);
		else
			spawn = entity == null ? sender.getPosition() : EntityUtils.traceBlock(entity, 128.0D);
		
		if (spawn == null) 
			throw new CommandException("command.firework.notFound", sender);
		
		Random rand = new Random();
		RecipeFireworks recipe = (RecipeFireworks) CraftingManager.REGISTRY.getObject(new ResourceLocation("fireworks"));
		
		if (recipe != null) {
			InventoryCrafting inv = new InventoryCrafting(new Container()
		    {
		        public boolean canInteractWith(EntityPlayer playerIn) {return false;}
		    }, 3, 3);
			
			ItemStack dye;
			ItemStack output;
			
			do {
		        for (int i = 0; i < inv.getSizeInventory(); ++i) inv.setInventorySlotContents(i, null);
				
				dye = new ItemStack(Items.DYE, 1, rand.nextInt(MAX_DYE_TYPES));
				
				inv.setInventorySlotContents(0, dye);
				inv.setInventorySlotContents(1, new ItemStack(Items.GUNPOWDER));
				
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
					inv.setInventorySlotContents(1, new ItemStack(Items.PAPER));
					
					int gunpowder = rand.nextInt(MAX_GUNPOWDER);
					for (int i = 0; i < gunpowder; i++) inv.setInventorySlotContents(2 + i, new ItemStack(Items.GUNPOWDER));
				}
				while (!recipe.matches(inv, sender.getMinecraftISender().getEntityWorld()));
				
				output = recipe.getRecipeOutput();
				
				if (output.getItem() instanceof ItemFirework) {
					ItemFirework firework = (ItemFirework) output.getItem();
					
					EntityFireworkRocket rocket = new EntityFireworkRocket(sender.getWorld(), spawn.getX(), spawn.getY(), spawn.getZ(), output);;
					sender.getWorld().spawnEntity(rocket);
				}
			}
		}
		
		return null;
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
	public int getDefaultPermissionLevel(String[] args) {
		return 2;
	}
	
	@Override
	public boolean canSenderUse(String commandName, ICommandSender sender, String[] params) {
		return true;
	}
}
