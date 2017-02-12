package com.mrnobody.morecommands.command.server;

import java.util.Map;

import com.google.common.collect.MapMaker;
import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandException;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.CommandSender;
import com.mrnobody.morecommands.command.ServerCommandProperties;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.event.EventHandler;
import com.mrnobody.morecommands.event.Listeners.EventListener;
import com.mrnobody.morecommands.util.EntityUtils;
import com.mrnobody.morecommands.util.WorldUtils;

import net.minecraft.block.Block;
import net.minecraft.block.BlockAnvil;
import net.minecraft.block.BlockWorkbench;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityMinecartContainer;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerEnchantment;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.IInteractionObject;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerOpenContainerEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;

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
	public String getCommandUsage() {
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
	public String execute(CommandSender sender, String[] params) throws CommandException {
		EntityPlayerMP player = getSenderAsEntity(sender.getMinecraftISender(), EntityPlayerMP.class);
		Entity entity = EntityUtils.traceEntity(player, 128D);
		
		if (params.length == 0 && entity instanceof EntityHorse && ((EntityHorse) entity).isChested()) {
			((EntityHorse) entity).openGUI(player);
		}
		else if (params.length == 0 && entity instanceof EntityVillager) {
			player.displayVillagerTradeGui((EntityVillager) entity);
		}
		else if (params.length == 0 && entity instanceof EntityMinecartContainer) {
			player.displayGui((EntityMinecartContainer) entity);
		}
		else {
			if (params.length == 0 || params.length > 2) {
				BlockPos trace; 
				
				try {trace = params.length > 2 ? getCoordFromParams(sender.getMinecraftISender(), params, 0) : EntityUtils.traceBlock(player, 128D);}
				catch (NumberFormatException nfe) {throw new CommandException("command.open.NAN", sender);}
				
				if (trace == null)
					throw new CommandException("command.open.noBlock", sender);
				
				TileEntity te = sender.getWorld().getTileEntity(trace);
				Block block = WorldUtils.getBlock(sender.getWorld(), trace);
				
				if (te instanceof IInteractionObject)
					player.displayGui((IInteractionObject) te);
				else if (te instanceof IInventory)
					player.displayGUIChest((IInventory) te);
				else if (block == Blocks.anvil)
					player.displayGui(new BlockAnvil.Anvil(sender.getWorld(), trace));
				else if (block == Blocks.crafting_table)
					player.displayGui(new BlockWorkbench.InterfaceCraftingTable(sender.getWorld(), trace));
				else 
					throw new CommandException("command.open.invalidBlock", sender);
			}
			else if (params.length > 0) {
				if (params[0].equalsIgnoreCase("enderchest"))
					player.displayGUIChest(player.getInventoryEnderChest());
				else if (params[0].equalsIgnoreCase("enchantment_table") || (params.length > 1 && params[0].equalsIgnoreCase("enchantment") && params[1].equalsIgnoreCase("table"))) {
					final World w = sender.getWorld();
					
					player.displayGui(new IInteractionObject() {
						@Override public boolean hasCustomName() {return false;}
						@Override public String getName() {return "container.enchant";}
						@Override public IChatComponent getDisplayName() {return new ChatComponentTranslation(this.getName());}
						@Override public String getGuiID() {return "minecraft:enchanting_table";}
						@Override public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn) {
							return new ContainerEnchantment(playerInventory, w, BlockPos.ORIGIN);
						}
					});
					this.allowedInteractions.put(player, player.openContainer);
				}
				else if (params[0].equalsIgnoreCase("anvil")) {
					player.displayGui(new BlockAnvil.Anvil(sender.getWorld(), BlockPos.ORIGIN)); 
					this.allowedInteractions.put(player, player.openContainer);
				}
				else if (params[0].equalsIgnoreCase("workbench") || params[0].equalsIgnoreCase("crafting_table") || (params.length > 1 && params[0].equalsIgnoreCase("crafting") && params[1].equalsIgnoreCase("table")) ) {
					player.displayGui(new BlockWorkbench.InterfaceCraftingTable(sender.getWorld(), BlockPos.ORIGIN));
					this.allowedInteractions.put(player, player.openContainer);
				}
				else if (params[0].equalsIgnoreCase("furnace") || params[0].equalsIgnoreCase("brewing_stand") || (params.length > 1 && params[0].equalsIgnoreCase("brewing") && params[1].equalsIgnoreCase("stand")))
					throw new CommandException("command.open.cantOpenTEs", sender);
				else
					throw new CommandException("command.open.invalidContainer", sender, params[0]);
			}
			else throw new CommandException("command.generic.invalidUsage", sender, this.getCommandName());
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
		return isSenderOfEntityType(sender, EntityPlayerMP.class);
	}
}
