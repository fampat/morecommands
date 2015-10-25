package com.mrnobody.morecommands.command.server;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFlowerPot;
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

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;
import com.mrnobody.morecommands.wrapper.Player;

@Command(
		name = "pick",
		description = "command.pick.description",
		example = "command.pick.example",
		syntax = "command.pick.syntax",
		videoURL = "command.pick.videoURL"
		)
public class CommandPick extends ServerCommand {
	@Override
	public String getName() {
		return "pick";
	}

	@Override
	public String getUsage() {
		return "command.pick.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		Player player = new Player((EntityPlayerMP) sender.getMinecraftISender());
		MovingObjectPosition pick = player.rayTrace(128.0D, 0.0D, 1.0F);
		int amount = 64;
		
		if (params.length > 0) {
			try {amount = Integer.parseInt(params[0]);}
			catch (NumberFormatException nfe) {throw new CommandException("command.pick.NAN", sender);}
		}
		
		if (pick != null) {
			if (!this.onPickBlock(pick, player.getMinecraftPlayer(), player.getWorld().getMinecraftWorld(), amount))
				throw new CommandException("command.pick.cantgive", sender);
		}
		else throw new CommandException("command.pick.notInSight", sender);
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
	
    private boolean onPickBlock(MovingObjectPosition target, EntityPlayer player, World world, int amount)
    {
        ItemStack result = null;

        if (target.typeOfHit == MovingObjectType.BLOCK)
        {
        	BlockPos pos = target.getBlockPos();
            Block block = world.getBlockState(pos).getBlock();

            if (block.isAir(world, pos))
            {
                return false;
            }

            //result = block.getPickBlock(target, world, pos); //does not work on servers because methods used by this method are client side only
            result = this.getPickBlock(block, target, world, pos);
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
        
        result.stackSize = amount;

        for (int x = 0; x < 9; x++)
        {
            ItemStack stack = player.inventory.getStackInSlot(x);
            if (stack != null && stack.isItemEqual(result) && ItemStack.areItemStackTagsEqual(stack, result))
            {
                player.inventory.currentItem = x;
                stack.stackSize += amount;
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
    
    private ItemStack getPickBlock(Block block, MovingObjectPosition target, World world, BlockPos pos)
    {
        Item item = Item.getItemFromBlock(block);

        if (item == null) {
        	String unlocalized = block.getUnlocalizedName();
        	if (unlocalized.startsWith("tile.")) unlocalized = block.getUnlocalizedName().substring(5);
        	item = Item.getByNameOrId("minecraft:" + unlocalized);
        }
        
        if (item == null) return null;

        Block result = item instanceof ItemBlock && !(block instanceof BlockFlowerPot) ? Block.getBlockFromItem(item) : block;
        return new ItemStack(item, 1, result.getDamageValue(world, pos));
    }
    
	@Override
	public boolean canSenderUse(ICommandSender sender) {
		return sender instanceof EntityPlayerMP;
	}
}
