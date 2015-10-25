package com.mrnobody.morecommands.command.server;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;

import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.wrapper.Achievements;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;
import com.mrnobody.morecommands.wrapper.Player;

@Command(
		name = "achievement",
		description = "command.achievement.description",
		example = "command.achievement.example",
		syntax = "command.achievement.syntax",
		videoURL = "command.achievement.videoURL"
		)
public class CommandAchievement extends ServerCommand {
    public String getName()
    {
        return "achievement";
    }
    
    public String getUsage()
    {
        return "command.achievement.syntax";
    }
    
    public void execute(CommandSender sender, String[] params) throws CommandException {
    	Player player = new Player((EntityPlayerMP) sender.getMinecraftISender());
    	
    	if (params.length > 0) {
    		if(params[0].equals("list")) {
    			Object[] nameList = Achievements.getAchievementNameList();
    			int page = 1;
    			int PAGE_MAX = 15;
    			
    			if (params.length > 1) {
    				try {page = Integer.parseInt(params[1]);} 
    				catch (NumberFormatException e) {throw new CommandException("command.achievement.invalidUsage", sender);}
    			}
    			
    			int to = PAGE_MAX * page <= nameList.length ? PAGE_MAX * page : nameList.length;
    			int from = to - PAGE_MAX;
    				
    			for (int index = from; index < to; index++) {sender.sendStringMessage(" - '" + nameList[index] + "'");}
    			sender.sendLangfileMessage("command.achievement.more");
    		}
    		
    		else if (params[0].equals("unlockAll")) {
    			for (Object ach : Achievements.getAchievementNameList()) {
    				if (ach instanceof String) {
    					player.addAchievement(Achievements.getAchievementRequirement((String) ach));
    					player.addAchievement((String) ach);
    				}
    			}
    			sender.sendLangfileMessage("command.achievement.unlockAllSuccess");
    		}
    		
    		else if (params[0].equals("unlock")) {
    			if (params.length > 1) {
    				boolean found = false;
    				
    				for (Object ach : Achievements.getAchievementNameList()) {
    					if (ach instanceof String && params[1].equalsIgnoreCase((String) ach)) {
    						if (player.addAchievement((String) (ach))) {sender.sendLangfileMessage("command.achievement.unlockSuccess");}
    						else {sender.sendLangfileMessage("command.achievement.parent", Achievements.getAchievementRequirement(params[1]));}
    						found = true; break;
    					}
    				}
    				if (!found) throw new CommandException("command.achievement.unlockFailure", sender);
    			}
    			else throw new CommandException("command.achievement.invalidUsage", sender);
    		}
    		else throw new CommandException("command.achievement.invalidUsage", sender);
    	}
    	else throw new CommandException("command.achievement.invalidUsage", sender);
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
