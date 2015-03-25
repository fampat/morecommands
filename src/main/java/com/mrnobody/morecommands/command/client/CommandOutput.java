package com.mrnobody.morecommands.command.client;

import net.minecraft.util.ChatComponentTranslation;

import com.mrnobody.morecommands.command.ClientCommand;
import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandBase.Requirement;
import com.mrnobody.morecommands.command.CommandBase.ServerType;
import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.packet.client.C03PacketOutput;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;
import com.mrnobody.morecommands.wrapper.Player;

import cpw.mods.fml.relauncher.Side;


//MAKE COMMAND CLIENT SIDE TOO

@Command(
		name = "output",
		description = "command.output.description",
		example = "command.output.example",
		syntax = "command.output.syntax",
		videoURL = "command.output.videoURL"
		)
public class CommandOutput extends ClientCommand {

	@Override
	public String getCommandName() {
		return "output";
	}

	@Override
	public String getUsage() {
		return "command.output.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
    	boolean output = false;
    	boolean success = false;
    	
    	if (params.length >= 1) {
    		if (params[0].toLowerCase().equals("true")) {output = true; success = true;}
    		else if (params[0].toLowerCase().equals("false")) {output = false; success = true;}
    		else if (params[0].toLowerCase().equals("0")) {output = false; success = true;}
    		else if (params[0].toLowerCase().equals("1")) {output = true; success = true;}
    		else if (params[0].toLowerCase().equals("on")) {output = true; success = true;}
    		else if (params[0].toLowerCase().equals("off")) {output = false; success = true;}
    		else {success = false;}
    	}
    	else {output = !CommandSender.output; success = true;}
    	
    	if (success) CommandSender.output = output;
    	sender.getMinecraftISender().addChatMessage(new ChatComponentTranslation(success ? output ? "command.output.on" : "command.output.off"  : "command.output.failure", new Object[0]));
    	
    	if (MoreCommands.getMoreCommands().getPlayerUUID() != null) {
    		C03PacketOutput packet = new C03PacketOutput();
    		packet.playerUUID = MoreCommands.getMoreCommands().getPlayerUUID();
    		packet.output = output;
    		MoreCommands.getMoreCommands().getNetwork().sendToServer(packet);
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
	public boolean registerIfServerModded() {
		return true;
	}
	
	@Override
	public int getPermissionLevel() {
		return 0;
	}
}
