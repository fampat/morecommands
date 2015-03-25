package com.mrnobody.morecommands.command.server;

import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.server.management.ItemInWorldManager;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.handler.EventHandler;
import com.mrnobody.morecommands.handler.Listener;
import com.mrnobody.morecommands.util.GlobalSettings;
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
				this.tryHarvestBlock(event, ((EntityPlayerMP) event.getPlayer()).theItemInWorldManager, event.x, event.y, event.z);
				event.setCanceled(true);
			}
		}
	}

	@Override
	public String getCommandName() {
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
	}
	
	/**
	 * Just a copy-paste of {@link ItemInWorldManager#tryHarvestBlock(int, int, int)} <br>
	 * with a little difference: the line dropping the block is commented out
	 */
	private boolean tryHarvestBlock(BreakEvent event, ItemInWorldManager manager, int p_73084_1_, int p_73084_2_, int p_73084_3_) {
        ItemStack stack = manager.thisPlayerMP.getCurrentEquippedItem();
        if (stack != null && stack.getItem().onBlockStartBreak(stack, p_73084_1_, p_73084_2_, p_73084_3_, manager.thisPlayerMP))
        {
            return false;
        }
        Block block = manager.theWorld.getBlock(p_73084_1_, p_73084_2_, p_73084_3_);
        int l = manager.theWorld.getBlockMetadata(p_73084_1_, p_73084_2_, p_73084_3_);
        manager.theWorld.playAuxSFXAtEntity(manager.thisPlayerMP, 2001, p_73084_1_, p_73084_2_, p_73084_3_, Block.getIdFromBlock(block) + (manager.theWorld.getBlockMetadata(p_73084_1_, p_73084_2_, p_73084_3_) << 12));
        boolean flag = false;

        if (manager.isCreative())
        {
            flag = this.removeBlock(manager, p_73084_1_, p_73084_2_, p_73084_3_, false);
            manager.thisPlayerMP.playerNetServerHandler.sendPacket(new S23PacketBlockChange(p_73084_1_, p_73084_2_, p_73084_3_, manager.theWorld));
        }
        else
        {
            ItemStack itemstack = manager.thisPlayerMP.getCurrentEquippedItem();
            boolean flag1 = block.canHarvestBlock(manager.thisPlayerMP, l);

            if (itemstack != null)
            {
                itemstack.func_150999_a(manager.theWorld, block, p_73084_1_, p_73084_2_, p_73084_3_, manager.thisPlayerMP);

                if (itemstack.stackSize == 0)
                {
                	manager.thisPlayerMP.destroyCurrentEquippedItem();
                }
            }

            flag = this.removeBlock(manager, p_73084_1_, p_73084_2_, p_73084_3_, flag1);
            if (flag && flag1)
            {
            	//We don't want to drop the block ;)
                //block.harvestBlock(manager.theWorld, manager.thisPlayerMP, p_73084_1_, p_73084_2_, p_73084_3_, l);
            }
        }
        
        //we're generous and give the player his xp
        if (!manager.isCreative() && flag && event != null)
        {
            block.dropXpOnBlockBreak(manager.theWorld, p_73084_1_, p_73084_2_, p_73084_3_, event.getExpToDrop());
        }
        return flag;
	}
	
	/**
	 * Just a copy-paste of {@link ItemInWorldManager#removeBlock(int, int, int, boolean)} <br>
	 * because this method is needed by {@link CommandDoDrops#tryHarvestBlock(BreakEvent, ItemInWorldManager, int, int, int)}
	 */
    private boolean removeBlock(ItemInWorldManager manager, int p_73079_1_, int p_73079_2_, int p_73079_3_, boolean canHarvest)
    {
        Block block = manager.theWorld.getBlock(p_73079_1_, p_73079_2_, p_73079_3_);
        int l = manager.theWorld.getBlockMetadata(p_73079_1_, p_73079_2_, p_73079_3_);
        block.onBlockHarvested(manager.theWorld, p_73079_1_, p_73079_2_, p_73079_3_, l, manager.thisPlayerMP);
        boolean flag = block.removedByPlayer(manager.theWorld, manager.thisPlayerMP, p_73079_1_, p_73079_2_, p_73079_3_, canHarvest);

        if (flag)
        {
            block.onBlockDestroyedByPlayer(manager.theWorld, p_73079_1_, p_73079_2_, p_73079_3_, l);
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
