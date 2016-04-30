package com.mrnobody.morecommands.command.server;

import java.util.Map;

import com.google.common.collect.MapMaker;
import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.ServerCommandProperties;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.event.EventHandler;
import com.mrnobody.morecommands.event.Listeners.EventListener;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;
import com.mrnobody.morecommands.wrapper.Coordinate;
import com.mrnobody.morecommands.wrapper.Player;

import cpw.mods.fml.common.eventhandler.Event.Result;
import net.minecraft.block.Block;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityMinecartHopper;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityBeacon;
import net.minecraft.tileentity.TileEntityBrewingStand;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.tileentity.TileEntityEnchantmentTable;
import net.minecraft.tileentity.TileEntityEnderChest;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.tileentity.TileEntityHopper;
import net.minecraftforge.event.entity.player.PlayerOpenContainerEvent;

@Command(
		description = "command.open.description",
		example = "command.open.example",
		name = "command.open.name",
		syntax = "command.open.syntax",
		videoURL = "command.open.videoURL"
		)
public class CommandOpen extends StandardCommand implements ServerCommandProperties, EventListener<PlayerOpenContainerEvent> {
	public CommandOpen() {
		EventHandler.OPEN_CONTAINER.register(this);
	}
	
	@Override
	public String getCommandName() {
		return "open";
	}

	@Override
	public String getUsage() {
		return "command.open.syntax";
	}
	
	private Map<EntityPlayerMP, Container> allowedInteractions = new MapMaker().weakKeys().weakValues().makeMap();
	
	@Override
	public void onEvent(PlayerOpenContainerEvent event) {
		if (this.allowedInteractions.containsKey(event.entityPlayer)) {
			if (this.allowedInteractions.get(event.entityPlayer) == event.entityPlayer.openContainer) event.setResult(Result.ALLOW);
			else this.allowedInteractions.remove(event.entityPlayer);
		}
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		EntityPlayerMP player = getSenderAsEntity(sender.getMinecraftISender(), EntityPlayerMP.class);
		Entity entity = new Player(player).traceEntity(128D);
		
		if (params.length == 0 && entity instanceof EntityHorse && ((EntityHorse) entity).isChested()) {
			((EntityHorse) entity).openGUI(player);
		}
		else if (params.length == 0 && entity instanceof EntityVillager) {
			((EntityVillager) entity).interact(player);
		}
		else if (params.length == 0 && entity instanceof EntityMinecartHopper) {
			player.displayGUIHopperMinecart((EntityMinecartHopper) entity);
		}
		else {
			if (params.length == 0 || params.length > 2) {
				Coordinate trace; 
				
				try {trace = params.length > 2 ? getCoordFromParams(sender.getMinecraftISender(), params, 0) : new Player(player).traceBlock(128D);}
				catch (NumberFormatException nfe) {throw new CommandException("command.open.NAN", sender);}
				
				if (trace == null)
					throw new CommandException("command.open.noBlock", sender);
				
				TileEntity te = sender.getWorld().getTileEntity(trace);
				Block block = sender.getWorld().getBlock(trace);
				
				if (block == Blocks.anvil)
					player.displayGUIAnvil(trace.getBlockX(), trace.getBlockY(), trace.getBlockZ());
				else if (block == Blocks.enchanting_table && te instanceof TileEntityEnchantmentTable)
					player.displayGUIEnchantment(trace.getBlockX(), trace.getBlockY(), trace.getBlockZ(), ((TileEntityEnchantmentTable) te).func_145921_b() ? ((TileEntityEnchantmentTable) te).func_145919_a() : null);
				else if (block == Blocks.crafting_table)
					player.displayGUIWorkbench(trace.getBlockX(), trace.getBlockY(), trace.getBlockZ());
				else if ((block == Blocks.ender_chest && te instanceof TileEntityEnderChest) || (block == Blocks.chest && te instanceof TileEntityChest))
					player.displayGUIChest(te instanceof TileEntityChest ? (TileEntityChest) te : player.getInventoryEnderChest());
				else if (te instanceof TileEntityHopper)
					player.func_146093_a((TileEntityHopper) te);
				else if (te instanceof TileEntityFurnace)
					player.func_146101_a((TileEntityFurnace) te);
				else if (te instanceof TileEntityDispenser)
					player.func_146102_a((TileEntityDispenser) te);
				else if (te instanceof TileEntityBrewingStand)
					player.func_146098_a((TileEntityBrewingStand) te);
				else if (te instanceof TileEntityBeacon)
					player.func_146104_a((TileEntityBeacon) te);
				else 
					throw new CommandException("command.open.invalidBlock", sender);
			}
			else if (params.length > 0) {
				if (params[0].equalsIgnoreCase("enderchest"))
					player.displayGUIChest(player.getInventoryEnderChest());
				else if (params[0].equalsIgnoreCase("enchantment_table") || (params.length > 1 && params[0].equalsIgnoreCase("enchantment") && params[1].equalsIgnoreCase("table")))
					{player.displayGUIEnchantment(0, 0, 0, null); this.allowedInteractions.put(player, player.openContainer);}
				else if (params[0].equalsIgnoreCase("anvil"))
					{player.displayGUIAnvil(0, 0, 0); this.allowedInteractions.put(player, player.openContainer);}
				else if (params[0].equalsIgnoreCase("workbench") || params[0].equalsIgnoreCase("crafting_table") || (params.length > 1 && params[0].equalsIgnoreCase("crafting") && params[1].equalsIgnoreCase("table")))
					{player.displayGUIWorkbench(0, 0, 0); this.allowedInteractions.put(player, player.openContainer);}
				else if (params[0].equalsIgnoreCase("furnace") || params[0].equalsIgnoreCase("brewing_stand") || (params.length > 1 && params[0].equalsIgnoreCase("brewing") && params[1].equalsIgnoreCase("stand")))
					throw new CommandException("command.open.cantOpenTEs", sender);
				else
					throw new CommandException("command.open.invalidContainer", sender, params[0]);
			}
			else throw new CommandException("command.generic.invalidUsage", sender, this.getCommandName());
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
		return isSenderOfEntityType(sender, EntityPlayerMP.class);
	}
}
