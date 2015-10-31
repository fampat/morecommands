package com.mrnobody.morecommands.command.server;

import java.lang.reflect.Field;
import java.util.Random;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.handler.EventHandler;
import com.mrnobody.morecommands.handler.Listeners.EventListener;
import com.mrnobody.morecommands.patch.EntityPlayerMP;
import com.mrnobody.morecommands.util.ReflectionHelper;
import com.mrnobody.morecommands.util.ServerPlayerSettings;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;
import com.mrnobody.morecommands.wrapper.World;

import net.minecraft.block.Block;
import net.minecraft.block.BlockCactus;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockReed;
import net.minecraft.block.BlockSapling;
import net.minecraft.block.BlockStem;
import net.minecraft.command.ICommandSender;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.event.world.BlockEvent.PlaceEvent;

@Command(
		name = "instantgrow",
		description = "command.instantgrow.description",
		example = "command.instantgrow.example",
		syntax = "command.instantgrow.syntax",
		videoURL = "command.instantgrow.videoURL"
		)
public class CommandInstantgrow extends ServerCommand implements EventListener<PlaceEvent> {
	public CommandInstantgrow() {
		EventHandler.PLACE.getHandler().register(this);
	}

	@Override
	public void onEvent(PlaceEvent event) {
		if (event.player instanceof EntityPlayerMP && ServerPlayerSettings.getPlayerSettings((EntityPlayerMP) event.player).instantgrow) 
			this.growPlant(new World(event.world), event.pos.getX(), event.pos.getY(), event.pos.getZ(), new Random());
	}
	
	@Override
	public String getName() {
		return "instantgrow";
	}

	@Override
	public String getUsage() {
		return "command.instantgrow.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		ServerPlayerSettings settings = ServerPlayerSettings.getPlayerSettings((EntityPlayerMP) sender.getMinecraftISender());
    	
		try {settings.instantgrow = parseTrueFalse(params, 0, settings.instantgrow);}
		catch (IllegalArgumentException ex) {throw new CommandException("command.instantgrow.failure", sender);}
		
		sender.sendLangfileMessage(settings.instantgrow ? "command.instantgrow.on" : "command.instantgrow.off");
	}
	
	private void growPlant(World world, int x, int y, int z, Random rand) {
		Block block = world.getBlock(x, y, z);
		
		if (block instanceof BlockSapling) {
			((BlockSapling) block).grow(world.getMinecraftWorld(), rand,  new BlockPos(x, y, z), ((BlockSapling) block).getStateFromMeta(8));
		}
		else if (block instanceof BlockCrops) {
			world.setBlockMeta(new BlockPos(x, y, z), 7);
		}
		else if (block instanceof BlockCactus || block instanceof BlockReed) {
			int length = 1;
			
			while (true) {
				int blen = length;
				
				if (world.getBlock(x, y + length, z) == block) length++;
				if (world.getBlock(x, y - length, z) == block) length++;
				
				if (blen == length) break;
			}
			
			if (length < 3) {
				for (int i = 0; i <= 3 - length; i++) {
					world.setBlock(new BlockPos(x,  y + i, z), block);
				}
			}
		}
		else if (block instanceof BlockStem) {
			world.setBlockMeta(new BlockPos(x, y, z), 7);
			Field stemBlockField = ReflectionHelper.getField(BlockStem.class, "crop");
			
			if (stemBlockField != null) {
				try {
					Block stemBlock = (Block) stemBlockField.get(block);
					
                    if (world.getBlock(x - 1, y, z) == stemBlock) return;
                    if (world.getBlock(x + 1, y, z) == stemBlock) return;
                    if (world.getBlock(x, y, z - 1) == stemBlock) return;
                    if (world.getBlock(x, y, z + 1) == stemBlock) return;

                    int i = rand.nextInt(4);
                    int j = x;
                    int k = z;

                    if (i == 0) j = x - 1;
                    if (i == 1) ++j;
                    if (i == 2) k = z - 1;
                    if (i == 3) ++k;

                    Block b = world.getBlock(j, y - 1, k);

                    if (world.getMinecraftWorld().isAirBlock(new BlockPos(j, y, k)) && (b.canSustainPlant(world.getMinecraftWorld(), new BlockPos(j, y - 1, k), EnumFacing.UP, (BlockStem) block) || b == Blocks.dirt || b == Blocks.grass))
                    {
                        world.setBlock(new BlockPos(j, y, k), stemBlock);
                    }
				}
				catch (Exception ex) {ex.printStackTrace();}
			}
		}
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
	
	@Override
	public boolean canSenderUse(ICommandSender sender) {
		return sender instanceof EntityPlayerMP;
	}
}
