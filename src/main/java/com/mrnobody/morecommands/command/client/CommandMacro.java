package com.mrnobody.morecommands.command.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.ClientCommandHandler;

import com.mrnobody.morecommands.command.ClientCommand;
import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.util.Reference;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

@Command(
	name = "macro",
	description = "command.macro.description",
	syntax = "command.macro.syntax",
	example = "command.macro.example",
	videoURL = "command.macro.videoURL"
		)
public class CommandMacro extends ClientCommand {

	@Override
	public boolean registerIfServerModded() {
		return true;
	}

	@Override
	public String getName() {
		return "macro";
	}

	@Override
	public String getUsage() {
		return "command.macro.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		if (params.length > 0) {
			if ((params[0].equalsIgnoreCase("delete") || params[0].equalsIgnoreCase("del") || params[0].equalsIgnoreCase("remove") || params[0].equalsIgnoreCase("rem")) && params.length > 1) {
				String name = params[1] + ".cfg";
				File macroDir = Reference.getMacroDir();
				for (File f : macroDir.listFiles()) {
					if (f.getName().equalsIgnoreCase(name) && f.isFile()) {
						f.delete();
						sender.sendLangfileMessage("command.macro.deleteSuccess", f.getName());
						break;
					}
				}
			}
			else if ((params[0].equalsIgnoreCase("exec") || params[0].equalsIgnoreCase("execute")) && params.length > 1) {
				String name = params[1] + ".cfg";
				File macro = null;
				
				for (File f : Reference.getMacroDir().listFiles()) {
					if (f.getName().equalsIgnoreCase(name) && f.isFile()) {
						macro = f;
						break;
					}
				}
				
				if (macro != null) {
					try {
						BufferedReader br = new BufferedReader(new FileReader(macro));
						String line;
						
						while ((line = br.readLine()) != null) {
							if (ClientCommandHandler.instance.executeCommand(Minecraft.getMinecraft().thePlayer, line) == 0)
								Minecraft.getMinecraft().thePlayer.sendChatMessage(line.startsWith("/") ? line : "/" + line);
						}
						
						br.close();
					}
					catch (IOException ex) {ex.printStackTrace(); throw new CommandException("command.macro.executeError", sender);}
				}
				else throw new CommandException("command.macro.notFound", sender, params[1]);
			}
			else if ((params[0].equalsIgnoreCase("add") || params[0].equalsIgnoreCase("new") || params[0].equalsIgnoreCase("create") || params[0].equalsIgnoreCase("edit")) && params.length > 1) {
				File macro = new File(Reference.getMacroDir(), params[1] + ".cfg");
				
				if (macro.exists() && macro.isFile()) {
					if (params[0].equalsIgnoreCase("add") || params[0].equalsIgnoreCase("new") || params[0].equalsIgnoreCase("create") || params[0].equalsIgnoreCase("add"))
						throw new CommandException("command.macro.exists", sender, params[1]);
					macro.delete();
				}
				
				try {macro.createNewFile();}
				catch (IOException ex) {ex.printStackTrace(); throw new CommandException("command.macro.writeError", sender);}
				
				if (params.length > 2) {
					String commandlist = "";
					for (int i = 2; i < params.length; i++) commandlist += " " + params[i];
					String[] commands = commandlist.split(";");
					
					try {
						BufferedWriter bw = new BufferedWriter(new FileWriter(macro));
						
						for (String command : commands) {
							bw.write(command.trim());
							bw.newLine();
						}
						
						bw.close();
					}
					catch (IOException ex) {ex.printStackTrace(); throw new CommandException("command.macro.writeError", sender);}
				}
				
				sender.sendLangfileMessage("command.macro.createSuccess", macro.getName());
			}
			else throw new CommandException("command.macro.invalidUsage", sender);
		}
		else throw new CommandException("command.macro.invalidUsage", sender);
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
		return 0;
	}
}
