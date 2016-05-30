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
import net.minecraft.block.BlockFlowerPot;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
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
	public String getUsage() {
		return "command.pick.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		Player player = new Player(getSenderAsEntity(sender.getMinecraftISender(), EntityPlayerMP.class));
		MovingObjectPosition pick = player.rayTrace(128.0D, 0.0D, 1.0F);
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
	
    private boolean onPickBlock(MovingObjectPosition target, EntityPlayer player, World world, int amount)
    {
    	ItemStack result = null;

        if (target.typeOfHit == MovingObjectType.BLOCK)
        {
            IBlockState state = world.getBlockState(target.getBlockPos());

            if (state.getBlock().isAir(world, target.getBlockPos()))
            {
                return false;
            }

            result = getPickBlock(state.getBlock(), target, world, target.getBlockPos());
        }
        else
        {
            if (target.typeOfHit != MovingObjectType.ENTITY || target.entityHit == null)
            {
                return false;
            }

            result = target.entityHit.getPickedResult(target);
        }

        if (result == null)
        {
            return false;
        }
        
        //We want to allow duplicate stacks
        /*
        for (int x = 0; x < 9; x++)
        {
            ItemStack stack = player.inventory.getStackInSlot(x);
            if (stack != null && stack.isItemEqual(result) && ItemStack.areItemStackTagsEqual(stack, result))
            {
                player.inventory.currentItem = x;
                return true;
            }
        }
        */

        int slot = player.inventory.getFirstEmptyStack();
        if (slot < 0 || slot >= 9)
        {
            slot = player.inventory.currentItem;
        }

        player.inventory.setInventorySlotContents(slot, result);
        player.inventory.currentItem = slot;
        return true;
    }
    
    private ItemStack getPickBlock(Block block, MovingObjectPosition target, World world, BlockPos pos) {
        Item item = Item.getItemFromBlock(block);

        if (item == null)
        {
            return null;
        }

        block = item instanceof ItemBlock && !(block instanceof BlockFlowerPot) ? Block.getBlockFromItem(item) : block;
        return new ItemStack(item, 1, block.getDamageValue(world, pos));
	}
    
	@Override
	public boolean canSenderUse(String commandName, ICommandSender sender, String[] params) {
		return isSenderOfEntityType(sender, EntityPlayerMP.class);
	}
}
