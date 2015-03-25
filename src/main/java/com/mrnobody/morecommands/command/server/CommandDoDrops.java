package com.mrnobody.morecommands.command.server;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.server.management.ItemInWorldManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.handler.EventHandler;
import com.mrnobody.morecommands.handler.Listener;
import com.mrnobody.morecommands.util.ServerPlayerSettings;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

@Command(
		name = "dodrops",
		description = "command.dodrops.description",
		example = "command.dodrops.example",
		syntax = "command.dodrops.syntax",
		videoURL = "command.dodrops.videoURL"
		)
public class CommandDoDrops extends ServerCommand implements Listener<BreakEvent> {
	public CommandDoDrops() {
		EventHandler.DROPS.getHandler().register(this);
	}
	
	@Override
	public void onEvent(BreakEvent event) {
		if (event.getPlayer() instanceof EntityPlayerMP && ServerPlayerSettings.playerSettingsMapping.containsKey(event.getPlayer())) {
			if (!ServerPlayerSettings.playerSettingsMapping.get(event.getPlayer()).dodrops) {
				//This redirection to a modified method works only as long as the boolean return of tryHarvestBlock
				//does not affect the game behavior, but this is the only way to enable/disable drops player sensitive,
				//otherwise dodrops would be globally and affect every player (e.g. by canceling the spawn of item entities)
				this.tryHarvestBlock(event, ((EntityPlayerMP) event.getPlayer()).theItemInWorldManager, event.pos);
				event.setCanceled(true);
			}
		}
	}
	@Override
	public String getName() {
		return "dodrops";
	}

	@Override
	public String getUsage() {
		return "command.dodrops.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		ServerPlayerSettings settings = ServerPlayerSettings.playerSettingsMapping.get(sender.getMinecraftISender());
		if (settings != null) settings.dodrops = !settings.dodrops;
		sender.sendLangfileMessageToPlayer(settings.dodrops ? "command.dodrops.enabled" : "command.dodrops.disabled", new Object[0]);
	}
	
	/**
	 * Just a copy-paste of {@link ItemInWorldManager#tryHarvestBlock(BlockPos)} <br>
	 * with a little difference: the line dropping the block is commented out
	 */
    private boolean tryHarvestBlock(BreakEvent event, ItemInWorldManager manager, BlockPos pos)
    {
        IBlockState iblockstate = manager.theWorld.getBlockState(pos);
        TileEntity tileentity = manager.theWorld.getTileEntity(pos);

        ItemStack stack = manager.thisPlayerMP.getCurrentEquippedItem();
        if (stack != null && stack.getItem().onBlockStartBreak(stack, pos, manager.thisPlayerMP)) return false;

        manager.theWorld.playAuxSFXAtEntity(manager.thisPlayerMP, 2001, pos, Block.getStateId(iblockstate));
        boolean flag1 = false;

        if (manager.isCreative())
        {
            flag1 = this.removeBlock(manager, pos, false);
            manager.thisPlayerMP.playerNetServerHandler.sendPacket(new S23PacketBlockChange(manager.theWorld, pos));
        }
        else
        {
            ItemStack itemstack1 = manager.thisPlayerMP.getCurrentEquippedItem();
            boolean flag = iblockstate.getBlock().canHarvestBlock(manager.theWorld, pos, manager.thisPlayerMP);

            if (itemstack1 != null)
            {
                itemstack1.onBlockDestroyed(manager.theWorld, iblockstate.getBlock(), pos, manager.thisPlayerMP);

                if (itemstack1.stackSize == 0)
                {
                	manager.thisPlayerMP.destroyCurrentEquippedItem();
                }
            }

            flag1 = this.removeBlock(manager, pos, flag);
            if (flag1 && flag)
            {
            	//We don't want to drop the block ;)
                //iblockstate.getBlock().harvestBlock(manager.theWorld, manager.thisPlayerMP, pos, iblockstate, tileentity);
            }
        }
        
        if (!manager.isCreative() && flag1 && event.getExpToDrop() > 0)
        {
            iblockstate.getBlock().dropXpOnBlockBreak(manager.theWorld, pos, event.getExpToDrop());
        }
        return flag1;
    }
    
	/**
	 * Just a copy-paste of {@link ItemInWorldManager#removeBlock(BlockPos, boolean)} <br>
	 * because this method is needed by {@link CommandDoDrops#tryHarvestBlock(BreakEvent, ItemInWorldManager, BlockPos)}
	 */
    private boolean removeBlock(ItemInWorldManager manager, BlockPos pos, boolean canHarvest)
    {
        IBlockState iblockstate = manager.theWorld.getBlockState(pos);
        iblockstate.getBlock().onBlockHarvested(manager.theWorld, pos, iblockstate, manager.thisPlayerMP);
        boolean flag = iblockstate.getBlock().removedByPlayer(manager.theWorld, pos, manager.thisPlayerMP, canHarvest);

        if (flag)
        {
            iblockstate.getBlock().onBlockDestroyedByPlayer(manager.theWorld, pos, iblockstate);
        }

        return flag;
    }
	
	@Override
	public Requirement[] getRequirements() {
		return new Requirement[0];
	}
	
	@Override
	public void unregisterFromHandler() {
		EventHandler.DROPS.getHandler().unregister(this);
	}

	@Override
	public ServerType getAllowedServerType() {
		return ServerType.ALL;
	}
	
	@Override
	public int getPermissionLevel() {
		return 2;
	}
}
