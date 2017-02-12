package com.mrnobody.morecommands.command.server;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandException;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.CommandSender;
import com.mrnobody.morecommands.command.MultipleCommands;
import com.mrnobody.morecommands.command.ServerCommandProperties;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.util.TargetSelector;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Command.MultipleCommand(
		name = {"nbt_apply_inventory", "nbt_apply_block", "nbt_apply_entity", "nbt_test_inventory", "nbt_test_block", "nbt_test_entity"},
		description = {"command.nbt.apply.inventory.description", "command.nbt.apply.block.description", "command.nbt.apply.entity.description", 
						"command.nbt.test.inventory.description", "command.nbt.test.block.description", "command.nbt.test.entity.description"},
		example = {"command.nbt.apply.inventory.example", "command.nbt.apply.block.example", "command.nbt.apply.entity.example", 
						"command.nbt.test.inventory.example", "command.nbt.test.block.example", "command.nbt.test.entity.example"},
		syntax = {"command.nbt.apply.inventory.syntax", "command.nbt.apply.block.syntax", "command.nbt.apply.entity.syntax", 
						"command.nbt.test.inventory.syntax", "command.nbt.test.block.syntax", "command.nbt.test.entity.syntax"},
		videoURL = {"command.nbt.apply.inventory.videoURL", "command.nbt.apply.block.videoURL", "command.nbt.apply.entity.videoURL", 
						"command.nbt.test.inventory.videoURL", "command.nbt.test.block.videoURL", "command.nbt.test.entity.videoURL"}
		)
public class CommandNbt extends MultipleCommands implements ServerCommandProperties {
	public CommandNbt() {}
	
	public CommandNbt(int typeIndex) {
		super(typeIndex);
	}
	
	@Override
	public String[] getCommandNames() {
		return new String[] {"nbt_apply_inventory", "nbt_apply_block", "nbt_apply_entity", "nbt_test_inventory", "nbt_test_block", "nbt_test_entity"};
	}

	@Override
	public String[] getCommandUsages() {
		String[] names = getCommandNames();
		for (int i = 0; i < names.length; i++) 
			names[i] = "command." + names[i].replace('_', '.') + ".syntax";
		return names;
	}
	
	@Override
	public String execute(String name, CommandSender sender, String[] params) throws CommandException {
		params = reparseParamsWithNBTData(params);
		final String action = name.split("_")[1], type = name.split("_")[2];
		
		if (params.length <= 0)
			throw new CommandException("command.generic.invalidUsage", sender, this.getCommandName());
		
		if (action.equalsIgnoreCase("apply")) {
			if (type.equalsIgnoreCase("inventory")) {
				Target target = new Target(this.getCommandName(), sender.getMinecraftISender(), params, false);
				
				if (target.isTarget) params = Arrays.copyOfRange(params, 1, params.length);
				if (params.length <= 0) throw new CommandException("command.generic.invalidUsage", sender, this.getCommandName());
				
				int slot = TargetSelector.getSlotForShortcut(params[0]);
				if (params[0].startsWith("slot.") && slot == -1) throw new CommandException("command.nbt.invalidSlot", sender, params[0]);
				else params = Arrays.copyOfRange(params, 1, params.length);

				if (params.length <= 0) throw new CommandException("command.generic.invalidUsage", sender, this.getCommandName());
				ItemParam param = new ItemParam(sender.getMinecraftISender(), params);
				
				final ItemStack stack = param.item == null ? null : new ItemStack(param.item, param.amount, param.meta);
				if (stack != null && param.nbt != null) stack.setTagCompound(param.nbt);
				
				if (!target.isTarget || (target.isTarget && !target.isBlockTarget)) {
					for (Entity entity : getEntities(sender.getMinecraftISender(), target)) {
						if (slot == -1 && stack == null) TargetSelector.replaceCurrentTag(entity, param.nbt, param.mergeOrEqualLists);
						else if (slot == -1) TargetSelector.replaceCurrentItem(entity, stack);
						else if (stack == null) TargetSelector.replaceTagInInventory(entity, slot, param.nbt, param.mergeOrEqualLists);
						else TargetSelector.replaceItemInInventory(entity, slot, stack);
					}
				}
				else {
					if (slot == -1) throw new CommandException("command.nbt.noCurrentItem", sender);
					
					TargetSelector.BlockSelector.matchBlocks(sender.getMinecraftISender(), target.target, true, 
									new ApplyInventoryCallback(slot, stack, param.nbt, param.mergeOrEqualLists));
				}
			}
			else if (type.equalsIgnoreCase("block")) {
				Target target = new Target(this.getCommandName(), sender.getMinecraftISender(), params, true);
				
				if (target.isBlockTarget) params = Arrays.copyOfRange(params, 1, params.length);
				else params = Arrays.copyOfRange(params, 3, params.length);
				
				if (params.length <= 0) throw new CommandException("command.generic.invalidUsage", sender, this.getCommandName());
				BlockParam param = new BlockParam(sender.getMinecraftISender(), params);
				
				ApplyBlockCallback callback = new ApplyBlockCallback(param.block, param.meta, param.oldBlockHandling, param.nbt, param.mergeOrEqualLists);
				
				if (target.isBlockTarget)
					TargetSelector.BlockSelector.matchBlocks(sender.getMinecraftISender(), target.target, param.block == null, callback);
				else {
					if (param.block == null) {
						TileEntity te = sender.getWorld().getTileEntity(target.coord);
						if (te != null) callback.applyToTileEntity(te);
					}
					else callback.applyToCoordinate(sender.getWorld(), target.coord);
				}
			}
			else if (type.equalsIgnoreCase("entity")) {
				if (!isTargetSelector(params[0])) throw new CommandException("command.nbt.invalidTarget", sender);
				if (params.length <= 1) throw new CommandException("command.generic.invalidUsage", sender, this.getCommandName());
				
				NBTTagCompound tag; boolean mergeLists = params.length > 2 && isMergeLists(params[2]);
				NBTBase nbt = getNBTFromParam(params[1]);
				if (!(nbt instanceof NBTTagCompound)) throw new CommandException("command.nbt.noCompound", sender);
				tag = (NBTTagCompound) nbt;
				
				List<? extends Entity> entities = TargetSelector.EntitySelector.matchEntites(sender.getMinecraftISender(), params[0], Entity.class);
				
				for (Entity entity : entities) {
					NBTTagCompound compound = new NBTTagCompound();
					entity.writeToNBT(compound);
					
					compound.removeTag("UUIDMost");
					compound.removeTag("UUIDLast");
					
					TargetSelector.nbtMerge(compound, tag, mergeLists);
					entity.readFromNBT(compound);
				}
			}
			else throw new CommandException("command.generic.invalidUsage", sender, this.getCommandName());
		}
		else if (action.equalsIgnoreCase("test")) {
			if (type.equalsIgnoreCase("inventory")) {
				Target target = new Target(this.getCommandName(), sender.getMinecraftISender(), params, false);
				if (target.isTarget) params = Arrays.copyOfRange(params, 1, params.length);
				if (params.length <= 0) throw new CommandException("command.generic.invalidUsage", sender, this.getCommandName());
				
				int slot = TargetSelector.getSlotForShortcut(params[0]);
				if (slot == -1) throw new CommandException("command.nbt.invalidSlot", sender, params[0]);
				ItemBoundParam param = new ItemBoundParam(sender.getMinecraftISender(), params);
				
				ItemStack stack = param.tag != null || param.anyItem || param.item != null ? new ItemStack(param.item, param.amount, param.meta) : null;
				if (stack != null) stack.setTagCompound(param.tag); int matchingStacks = 0;
				
				if (!target.isTarget || (target.isTarget && !target.isBlockTarget)) {
					for (Entity entity : getEntities(sender.getMinecraftISender(), target))
						if (TargetSelector.isSlotMatching(entity, slot, stack, param.meta == -1, param.equalLists)) matchingStacks++;
				}
				else {
					if (slot == -1) throw new CommandException("command.nbt.noCurrentItem", sender);
					TestInventoryCallback callback = new TestInventoryCallback(slot, param.meta, stack, param.equalLists);
					TargetSelector.BlockSelector.matchBlocks(sender.getMinecraftISender(), target.target, true, callback);
					matchingStacks = callback.getMatchingStacks();
				}
				
				if (param.boundsIndex == -1)
					throw new CommandException("command.nbt.noBounds", sender);
				else
					checkBounds(sender, params, matchingStacks, param.boundsIndex);
			}
			else if (type.equals("block")) {
				if (!isTargetSelector(params[0]) || !params[0].startsWith("@b")) 
					throw new CommandException("command.nbt.mustBeBlockTarget", sender);
				
				if (params.length <=1)
					throw new CommandException("command.nbt.noBounds", sender);
				
				TestBlockCallback callback = new TestBlockCallback();
				TargetSelector.BlockSelector.matchBlocks(sender.getMinecraftISender(), params[0], false, callback);
				checkBounds(sender, params, callback.getMatchingBlocks(), 1);
			}
			else if (type.equals("entity")) {
				if (!isTargetSelector(params[0]) || params[0].startsWith("@b")) 
					throw new CommandException("command.nbt.mustBeEntityTarget", sender);
				
				if (params.length <=1)
					throw new CommandException("command.nbt.noBounds", sender);
				
				int matchingEntites = TargetSelector.EntitySelector.matchEntites(sender.getMinecraftISender(), params[0], Entity.class).size();
				checkBounds(sender, params, matchingEntites, 1);
			}
			else throw new CommandException("command.generic.invalidUsage", sender, this.getCommandName());
		}
		else throw new CommandException("command.generic.invalidUsage", sender, this.getCommandName());
		
		return null;
	}
	
	private static void checkBounds(CommandSender sender, String[] params, int matched, int boundsIndex) throws CommandException {
		Pair<Integer, Integer> bounds = getBounds(sender, params, boundsIndex);
		
		if (bounds.getLeft() == -1)
			throw new CommandException("command.nbt.notABound", sender);
		
		if (!(matched >= bounds.getLeft() && (bounds.getRight() == -1 || matched <= bounds.getRight())))
			throw new CommandException("command.nbt.noMatch", sender);
		else
			sender.sendLangfileMessage("command.nbt.matched", matched);
	}
	
	private static Pair getBounds(CommandSender sender, String[] params, int boundsIndex) throws CommandException {
		int lowerBound = 1, upperBound = -1;
		
		try {
			for (String bound : Arrays.copyOfRange(params, boundsIndex, params.length)) {
				if (bound == null) continue;
				else if (bound.startsWith(">=") && bound.length() > 2) lowerBound = Integer.parseInt(bound.substring(2));
				else if (bound.startsWith(">") && bound.length() > 1) lowerBound = Integer.parseInt(bound.substring(1)) + 1;
				else if (bound.startsWith("<=") && bound.length() > 2) upperBound = Integer.parseInt(bound.substring(2));
				else if (bound.startsWith("<") && bound.length() > 1) upperBound = Integer.parseInt(bound.substring(1)) - 1;
				else if (bound.startsWith("=") && bound.length() > 1) lowerBound = upperBound = Integer.parseInt(bound.substring(1));
			}
		}
		catch (NumberFormatException nfe) {throw new CommandException("command.nbt.NAN", sender);}
		
		return ImmutablePair.of(lowerBound, upperBound);
	}
	
	private static List<? extends Entity> getEntities(ICommandSender sender, Target target) throws CommandException {
		if (target.isTarget) 
			return TargetSelector.EntitySelector.matchEntites(sender, target.target, Entity.class);
		else if (isSenderOfEntityType(sender, Entity.class))
			return Arrays.asList(getSenderAsEntity(sender, Entity.class));
		else
			throw new CommandException("command.generic.notServer", sender);
	}
	
	private static class ApplyInventoryCallback implements TargetSelector.BlockSelector.BlockCallback {
		private final int slot; private final ItemStack stack;
		private final NBTTagCompound tag; private final boolean mergeLists;
		
		public ApplyInventoryCallback(int slot, ItemStack stack, NBTTagCompound tag, boolean mergeLists) {
			this.slot = slot; this.stack = stack;
			this.tag = tag; this.mergeLists = mergeLists;
		}
		
		@Override public void applyToCoordinate(World world, BlockPos pos) {}
		
		@Override public void applyToTileEntity(TileEntity entity) {
			if (entity instanceof IInventory) {
				if (this.stack == null) TargetSelector.replaceTagInInventory((IInventory) entity, this.slot, (NBTTagCompound) this.tag, this.mergeLists);
				else TargetSelector.replaceItemInInventory((IInventory) entity, this.slot, stack.copy()); 
			}
		}
	}
	
	private static class ApplyBlockCallback implements TargetSelector.BlockSelector.BlockCallback {
		private final int oldBlockHandling;
		private final IBlockState state; private final boolean mergeLists;
		private final NBTTagCompound nbt;
		
		public ApplyBlockCallback(Block block, int meta, int oldBlockHandling, NBTTagCompound nbt, boolean mergeLists) {
			this.state = block == null ? null : meta == -1 ? block.getDefaultState() : block.getStateFromMeta(meta);
			this.oldBlockHandling = oldBlockHandling;
			this.nbt = nbt; this.mergeLists = mergeLists;
		}
		
		@Override public void applyToTileEntity(TileEntity entity) {
			NBTTagCompound tag = new NBTTagCompound();
			entity.writeToNBT(tag);
			TargetSelector.nbtMerge(tag, this.nbt, this.mergeLists);
			entity.readFromNBT(tag);
		}
		
		@Override public void applyToCoordinate(World world, BlockPos pos) {
			if (this.state != null) {
				if (this.oldBlockHandling == 1 && !world.isAirBlock(pos)) return;
				else if (this.oldBlockHandling == 2) world.destroyBlock(pos, true);
				world.setBlockState(pos, this.state);
				
				if (this.state.getBlock().hasTileEntity(this.state)) {
					TileEntity te = world.getTileEntity(pos);
					
					if (te != null && !te.isInvalid()) {
						NBTTagCompound tag = new NBTTagCompound();
						te.writeToNBT(tag);
						TargetSelector.nbtMerge(tag, this.nbt, this.mergeLists);
						te.readFromNBT(tag);
					}
				}
			}
		}
	}
	
	private static class TestBlockCallback implements TargetSelector.BlockSelector.BlockCallback {
		private int matchingBlocks = 0;
		
		@Override public void applyToCoordinate(World world, BlockPos pos) {this.matchingBlocks++;}
		
		@Override public void applyToTileEntity(TileEntity entity) {}
		
		public int getMatchingBlocks() {
			return this.matchingBlocks;
		}
	}
	
	private static class TestInventoryCallback implements TargetSelector.BlockSelector.BlockCallback {
		private final int slot, meta; private int matchingStacks = 0;
		private final ItemStack stack; private final boolean equalLists;
		
		public TestInventoryCallback(int slot, int meta, ItemStack stack, boolean equalLists) {
			this.slot = slot; this.meta = meta;
			this.stack = stack; this.equalLists = equalLists;
		}
		
		@Override public void applyToCoordinate(World world, BlockPos pos) {}
		
		@Override public void applyToTileEntity(TileEntity entity) {
			if (entity instanceof IInventory) {
				if (TargetSelector.isSlotMatching((IInventory) entity, this.slot, this.stack, this.meta == -1, this.equalLists)) this.matchingStacks++;
			}
		}
		
		public int getMatchingStacks() {
			return this.matchingStacks;
		}
	}
	
	private static class Target {
		public boolean isTarget = false, isBlockTarget = false;
		public String target = null; public BlockPos coord = null;
		
		public Target(String commandName, ICommandSender sender, String params[], boolean mustBeABlock) throws CommandException {
			this.isTarget = isTargetSelector(params[0]);
			this.isBlockTarget = this.isTarget && params[0].startsWith("@b");
			this.target = params[0];
			
			if (!this.isBlockTarget && mustBeABlock) {
				if (params.length > 2) {
					try {this.coord = getCoordFromParams(sender, params, 0);}
					catch (NumberFormatException nfe) {throw new CommandException("command.nbt.NAN", sender);}
				}
				else throw new CommandException("command.generic.invalidUsage",sender, commandName);
			}
		}
	}
	
	private static class ItemParam {
		public Item item = null;
		public int meta = -1, amount = -1;
		public boolean mergeOrEqualLists = false;
		public NBTTagCompound nbt = null;
		
		public ItemParam(ICommandSender sender, String[] params) throws CommandException {
			if (isNBTParam(params[0])) {
				NBTBase nbt = getNBTFromParam(params[0]);
				if (!(nbt instanceof NBTTagCompound)) throw new CommandException("command.nbt.noCompound", sender);
				this.nbt = (NBTTagCompound) nbt;
				this.mergeOrEqualLists = params.length > 1 && isMergeLists(params[1]);
			}
			else {
				this.item = getItem(params[0]);
				
				if (this.item == null) 
					throw new CommandException("command.nbt.itemNotFound", sender, params[0]);
				
				if (params.length > 1) {
					try {this.amount = Integer.parseInt(params[1]);}
					catch(NumberFormatException e) {throw new CommandException("command.nbt.NAN", sender);}
				}
				
				if (params.length > 2) {
					try {this.meta = Integer.parseInt(params[2]);}
					catch (NumberFormatException e) {throw new CommandException("command.nbt.NAN", sender);}
					if (!this.item.getHasSubtypes() && this.meta != 0) throw new CommandException("command.nbt.noMeta", sender);
				}
				
				if (params.length > 3) {
					NBTBase nbt = getNBTFromParam(params[3]);
					if (!(nbt instanceof NBTTagCompound)) throw new CommandException("command.nbt.noCompound", sender);
					this.nbt = (NBTTagCompound) nbt;
					this.mergeOrEqualLists = params.length > 4 && isMergeLists(params[4]);
				}
			}
		}
	}
	
	private static class BlockParam {
		public Block block = null;
		public int meta = -1, oldBlockHandling = -1;
		public boolean mergeOrEqualLists = false;
		public NBTTagCompound nbt = null;
		
		public BlockParam(ICommandSender sender, String[] params) throws CommandException {
			if (isNBTParam(params[0])) {
				NBTBase nbt = getNBTFromParam(params[0]);
				if (!(nbt instanceof NBTTagCompound)) throw new CommandException("command.nbt.noCompound", sender);
				this.nbt = (NBTTagCompound) nbt;
				this.mergeOrEqualLists = params.length > 1 && isMergeLists(params[1]);
			}
			else  {
				this.block = getBlock(params[0]);
				
				if (this.block == null) 
					throw new CommandException("command.nbt.blockNotFound", sender, params[0]);
				
				if (params.length > 1) {
					try {this.meta = Integer.parseInt(params[1]);}
					catch(NumberFormatException e) {throw new CommandException("command.nbt.NAN", sender);}
				}
				
				if (params.length > 2)
					this.oldBlockHandling = params[2].equalsIgnoreCase("keep") ? 1 : params[2].equalsIgnoreCase("destroy") ? 2 : 0;
				
				if (params.length > 3) {
					NBTBase nbt = getNBTFromParam(params[3]);
					if (!(nbt instanceof NBTTagCompound)) throw new CommandException("command.nbt.noCompound", sender);
					this.nbt = (NBTTagCompound) nbt;
					this.mergeOrEqualLists = params.length > 4 && isMergeLists(params[4]);
				}
			}
		}
	}
	
	private static class ItemBoundParam {
		private static final Pattern boundPattern = Pattern.compile("(?:[<>]=?|=)\\d+$");
		
		public Item item = null; public int meta = -1, amount = -1, boundsIndex = -1; 
		public NBTTagCompound tag = null; 
		public boolean anyItem = true, equalLists = false;
		
		public ItemBoundParam(ICommandSender sender, String[] params) throws CommandException {
			if (params.length > 1) {
				Matcher matcher = boundPattern.matcher(params[1]);
				
				if (matcher.matches()) this.boundsIndex = 1;
				else if (isNBTParam(params[1])) {
					NBTBase nbt = getNBTFromParam(params[1]);
					if (!(nbt instanceof NBTTagCompound)) throw new CommandException("command.nbt.noCompound", sender);
					this.tag = (NBTTagCompound) nbt;
					
					if (params.length > 2 && !matcher.reset(params[2]).matches()) {
						this.equalLists = isEqualLists(params[2]);
						if (params.length > 3 && matcher.reset(params[3]).matches()) this.boundsIndex = 3;
					}
					else if (params.length > 2) this.boundsIndex = 2;
				}
				else {
					this.anyItem = params[1].equalsIgnoreCase("*");
					if (!this.anyItem) {
						this.item = getItem(params[1]);
						if (this.item == null) throw new CommandException("command.nbt.itemNotFound", sender, params[1]);
					}
					
					if (this.boundsIndex == -1 && params.length > 2 && !matcher.reset(params[2]).matches()) {
						try {this.amount = params[2].equalsIgnoreCase("*") ? -1 : Integer.parseInt(params[2]);}
						catch (NumberFormatException nfe) {throw new CommandException("command.nbt.NAN", sender);}
					}
					else if (params.length > 2) this.boundsIndex = 2;
					
					if (this.boundsIndex == -1 && params.length > 3 && !matcher.reset(params[3]).matches()) {
						try {this.meta = params[3].equalsIgnoreCase("*") ? -1 : Integer.parseInt(params[3]);}
						catch (NumberFormatException nfe) {throw new CommandException("command.nbt.NAN", sender);}
					}
					else if (params.length > 3) this.boundsIndex = 3;
					
					if (this.boundsIndex == -1 && params.length > 4 && !matcher.reset(params[4]).matches()) {
						NBTBase nbt = getNBTFromParam(params[4]);
						if (!(nbt instanceof NBTTagCompound)) throw new CommandException("command.nbt.noCompound", sender);
						this.tag = (NBTTagCompound) nbt;
					}
					else if (params.length > 4) this.boundsIndex = 4;
					
					if (this.boundsIndex == -1 && params.length > 5 && !matcher.reset(params[5]).matches()) {
						this.equalLists = isEqualLists(params[5]);
					}
					else if (params.length > 5) this.boundsIndex = 5;
					
					if (this.boundsIndex == -1 && params.length > 6 && matcher.reset(params[6]).matches()) this.boundsIndex = 6; 
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
	public int getDefaultPermissionLevel(String[] args) {
		return 2;
	}
	
	@Override
	public boolean canSenderUse(String commandName, ICommandSender sender, String[] params) {
		return true;
	}
}
