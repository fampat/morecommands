package com.mrnobody.morecommands.command.server;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.ServerCommandProperties;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;
import com.mrnobody.morecommands.wrapper.Player;

import net.minecraft.block.state.IBlockState;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

@Command(
		name = "pick",
		description = "command.pick.description",
		example = "command.pick.example",
		syntax = "command.pick.syntax",
		videoURL = "command.pick.videoURL"
		)
public class CommandPick extends StandardCommand implements ServerCommandProperties {
	@Override
	public String getCommandName() {
		return "pick";
	}

	@Override
	public String getCommandUsage() {
		return "command.pick.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		Player player = new Player(getSenderAsEntity(sender.getMinecraftISender(), EntityPlayerMP.class));
		RayTraceResult pick = player.rayTrace(128.0D, 0.0D, 1.0F);
		int amount = 1;
		
		if (params.length > 0) {
			try {amount = Integer.parseInt(params[0]);}
			catch (NumberFormatException nfe) {throw new CommandException("command.pick.NAN", sender);}
		}
		
		if (pick != null) {
			if (!this.onPickBlock(pick, player.getMinecraftPlayer(), player.getMinecraftPlayer().worldObj, amount))
				throw new CommandException("command.pick.cantgive", sender);
		}
		else throw new CommandException("command.pick.notInSight", sender);
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
	
    private boolean onPickBlock(RayTraceResult target, EntityPlayer player, World world, int amount)
    {
        ItemStack result = null;

        if (target.typeOfHit == RayTraceResult.Type.BLOCK)
        {
            IBlockState block = world.getBlockState(target.getBlockPos());

            if (block.getBlock().isAir(block, world, target.getBlockPos()))
            {
                return false;
            }
            
            result = block.getBlock().getPickBlock(block, target, world, target.getBlockPos(), player);
        }
        else
        {
            if (target.typeOfHit != RayTraceResult.Type.ENTITY || target.entityHit == null)
            {
                return false;
            }

            result = target.entityHit.getPickedResult(target);
        }

        if (result == null)
        {
            return false;
        }
        
        result.stackSize = amount;

        for (int x = 0; x < 9; x++)
        {
            ItemStack stack = player.inventory.getStackInSlot(x);
            if (stack != null && stack.isItemEqual(result) && ItemStack.areItemStackTagsEqual(stack, result))
            {
            	if (amount > stack.getMaxStackSize()) amount = stack.getMaxStackSize();
                player.inventory.currentItem = x;
                
                if (stack.stackSize + amount > stack.getMaxStackSize()) {
                	int oldStackSize = stack.stackSize;
                	stack.stackSize = stack.getMaxStackSize();
                	
                	if (player.inventory.getFirstEmptyStack() > 0) {
                		ItemStack copy = ItemStack.copyItemStack(stack); copy.stackSize = (oldStackSize + amount) - stack.getMaxStackSize();
                		player.inventory.mainInventory[player.inventory.getFirstEmptyStack()] = copy;
                	}
                }
                else stack.stackSize += amount;
                
                return true;
            }
        }

        int slot = player.inventory.getFirstEmptyStack();
        if (slot < 0 || slot >= 9)
        {
            slot = player.inventory.currentItem;
        }

        player.inventory.setInventorySlotContents(slot, result);
        player.inventory.currentItem = slot;
        return true;
    }
    
	@Override
	public boolean canSenderUse(String commandName, ICommandSender sender, String[] params) {
		return isSenderOfEntityType(sender, EntityPlayerMP.class);
	}
}
