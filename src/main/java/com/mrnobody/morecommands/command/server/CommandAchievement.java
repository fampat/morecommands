package com.mrnobody.morecommands.command.server;

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
    	Player player = sender.toPlayer();
    	
    	if (params.length > 0) {
    		if(params[0].equals("list")) {
    			Object[] nameList = Achievements.getAchievementNameList();
    			int page = 1;
    			int PAGE_MAX = 15;
    			boolean validParam = true;
    			
    			if (params.length > 1) {
    				try {page = Integer.parseInt(params[1]);} 
    				catch (NumberFormatException e) {validParam = false;}
    			}
    			
    			if (validParam) {
    				int to = PAGE_MAX * page <= nameList.length ? PAGE_MAX * page : nameList.length;
    				int from = to - PAGE_MAX;
    				
    				for (int index = from; index < to; index++) {sender.sendStringMessageToPlayer(" - '" + nameList[index] + "'");}
    				sender.sendLangfileMessageToPlayer("command.achievement.more", new Object[0]);
    			}
    			else {sender.sendLangfileMessageToPlayer("command.achievement.invalidUsage", new Object[0]);}
    		}
    		
    		else if (params[0].equals("unlockAll")) {
    			for (Object ach : Achievements.getAchievementNameList()) {
    				if (ach instanceof String) {
    					player.addAchievement(Achievements.getAchievementRequirement((String) ach));
    					player.addAchievement((String) ach);
    				}
    			}
    			sender.sendLangfileMessageToPlayer("command.achievement.unlockAllSuccess", new Object[0]);
    		}
    		
    		else if (params[0].equals("unlock")) {
    			if (params.length > 1) {
    				boolean broken = false;
    				
    				for (Object ach : Achievements.getAchievementNameList()) {
    					if (ach instanceof String && params[1].equalsIgnoreCase((String) ach)) {
    						if (player.addAchievement((String) (ach))) {sender.sendLangfileMessageToPlayer("command.achievement.unlockSuccess", new Object[0]);}
    						else {sender.sendLangfileMessageToPlayer("command.achievement.parent", Achievements.getAchievementRequirement(params[1]));}
    						broken = true; break;
    					}
    				}
    				if (!broken) sender.sendLangfileMessageToPlayer("command.achievement.unlockFailure", new Object[0]);
    			}
    			else {sender.sendLangfileMessageToPlayer("command.achievement.invalidUsage", new Object[0]);}
    		}
    		else {sender.sendLangfileMessageToPlayer("command.achievement.invalidUsage", new Object[0]);}
    	}
    	else {sender.sendLangfileMessageToPlayer("command.achievement.invalidUsage", new Object[0]);}
    }

	@Override
	public Requirement[] getRequirements() {
		return new Requirement[0];
	}
	
	@Override
	public ServerType getAllowedServerType() {
		return ServerType.ALL;
	}
	
	public void unregisterFromHandler() {}
	
	@Override
	public int getPermissionLevel() {
		return 2;
	}
}
