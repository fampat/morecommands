package com.mrnobody.morecommands.command.client;

import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;

import com.mrnobody.morecommands.command.ClientCommand;
import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.core.Patcher;
import com.mrnobody.morecommands.packet.client.C03PacketOutput;
import com.mrnobody.morecommands.util.LanguageManager;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;


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
	public String getName() {
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
    		if (params[0].equalsIgnoreCase("true")) {output = true; success = true;}
    		else if (params[0].equalsIgnoreCase("false")) {output = false; success = true;}
    		else if (params[0].equalsIgnoreCase("0")) {output = false; success = true;}
    		else if (params[0].equalsIgnoreCase("1")) {output = true; success = true;}
    		else if (params[0].equalsIgnoreCase("on")) {output = true; success = true;}
    		else if (params[0].equalsIgnoreCase("off")) {output = false; success = true;}
    		else if (params[0].equalsIgnoreCase("enable")) {output = true; success = true;}
    		else if (params[0].equalsIgnoreCase("disable")) {output = false; success = true;}
    		else {success = false;}
    	}
    	else {output = !CommandSender.output; success = true;}
    	
    	String text = LanguageManager.getTranslation(MoreCommands.getMoreCommands().getCurrentLang(sender.getMinecraftISender()), success ? output ? "command.output.on" : "command.output.off"  : "command.output.failure", new Object[0]);
    	if (success) CommandSender.output = output;
    	sender.getMinecraftISender().addChatMessage(new ChatComponentText(text));
    	
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
