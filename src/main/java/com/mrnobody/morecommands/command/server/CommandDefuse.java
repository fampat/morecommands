package com.mrnobody.morecommands.command.server;

import java.util.Iterator;
import java.util.List;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.command.CommandBase.Requirement;
import com.mrnobody.morecommands.command.CommandBase.ServerType;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

import cpw.mods.fml.relauncher.Side;

@Command(
		name = "defuse",
		description = "command.defuse.description",
		example = "command.defuse.example",
		syntax = "command.defuse.syntax",
		videoURL = "command.defuse.videoURL"
		)
public class CommandDefuse extends ServerCommand {

	@Override
	public String getCommandName() {
		return "defuse";
	}

	@Override
	public String getUsage() {
		return "command.defuse.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		double radius = 16;
		
		if (params.length > 0) {
			try {radius = Double.parseDouble(params[0]);}
			catch (NumberFormatException nfe) {sender.sendLangfileMessageToPlayer("command.defuse.invalidArg", new Object[0]); return;}
		}
		
		World world = sender.toPlayer().getWorld().getMinecraftWorld();
		EntityPlayer player = sender.toPlayer().getMinecraftPlayer();
		
		Iterator<Entity> tntPrimedIterator = world.getEntitiesWithinAABB(EntityTNTPrimed.class, AxisAlignedBB.getBoundingBox(player.posX - radius, player.posY - radius, player.posZ - radius, player.posX + radius, player.posY + radius, player.posZ + radius)).iterator();
	
		while (tntPrimedIterator.hasNext()) {
			Entity tntPrimed = tntPrimedIterator.next();
			tntPrimed.setDead();
			
			EntityItem tnt = new EntityItem(world, tntPrimed.posX, tntPrimed.posY, tntPrimed.posZ, new ItemStack((Item) Item.itemRegistry.getObject("tnt"), 1));
			world.spawnEntityInWorld(tnt);
		}
		
		sender.sendLangfileMessageToPlayer("command.defuse.defused", new Object[0]);
	}
	
	@Override
	public Requirement[] getRequirements() {
		return new Requirement[0];
	}
	
	@Override
	public void unregisterFromHandler() {}

	@Override
	public ServerType getAllowedServerType() {
		return ServerType.ALL;
	}
	
	@Override
	public int getPermissionLevel() {
		return 2;
	}
}
