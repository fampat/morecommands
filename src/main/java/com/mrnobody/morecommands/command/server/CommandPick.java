package com.mrnobody.morecommands.command.server;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.ServerCommandProperties;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;
import com.mrnobody.morecommands.wrapper.Player;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
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
	
    private boolean onPickBlock(RayTraceResult target, EntityPlayerMP player, World world, int amount)
    {
        ItemStack result = null;

        if (target.typeOfHit == RayTraceResult.Type.BLOCK)
        {
            IBlockState state = world.getBlockState(target.getBlockPos());

            if (state.getBlock().isAir(state, world, target.getBlockPos()))
            {
                return false;
            }

            result = state.getBlock().getPickBlock(state, target, world, target.getBlockPos(), player);
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

        if (result.getItem() == null)
        {
            String s1 = "";

            if (target.typeOfHit == RayTraceResult.Type.BLOCK)
            {
                s1 = Block.REGISTRY.getNameForObject(world.getBlockState(target.getBlockPos()).getBlock()).toString();
            }
            else if (target.typeOfHit == RayTraceResult.Type.ENTITY)
            {
                s1 = EntityList.getEntityString(target.entityHit);
            }
            
            return true;
        }

        func_184434_a(player.inventory, result);
        return true;
    }
    
    private void func_184434_a(InventoryPlayer inventory, ItemStack stack) {
        //We want to allow duplicate item stacks
    	/*int i = getSlotFor(inventory, stack);

        if (inventory.isHotbar(i))
        {
            inventory.currentItem = i;
        }
        else
        {
            if (i == -1)
            {*/
                inventory.currentItem = inventory.getBestHotbarSlot();

                if (inventory.mainInventory[inventory.currentItem] != null)
                {
                    int j = inventory.getFirstEmptyStack();

                    if (j != -1)
                    {
                        inventory.mainInventory[j] = inventory.mainInventory[inventory.currentItem];
                    }
                }

                inventory.mainInventory[inventory.currentItem] = stack;
            /*}
            else
            {
                inventory.pickItem(i);
            }
        }*/
    }
    
    /*
    private int getSlotFor(InventoryPlayer inventory, ItemStack stack) {
        for (int i = 0; i < inventory.mainInventory.length; ++i)
        {
            if (inventory.mainInventory[i] != null && stackEqualExact(stack, inventory.mainInventory[i]))
            {
                return i;
            }
        }

        return -1;
    }
    
    private boolean stackEqualExact(ItemStack stack1, ItemStack stack2) {
    	return stack1.getItem() == stack2.getItem() && (!stack1.getHasSubtypes() || stack1.getMetadata() == stack2.getMetadata()) && ItemStack.areItemStackTagsEqual(stack1, stack2);
    }
    */
    
	@Override
	public boolean canSenderUse(String commandName, ICommandSender sender, String[] params) {
		return isSenderOfEntityType(sender, EntityPlayerMP.class);
	}
}
