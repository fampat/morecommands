package com.mrnobody.morecommands.command.server;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.ServerCommandProperties;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.util.ObfuscatedNames.ObfuscatedField;
import com.mrnobody.morecommands.util.ReflectionHelper;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;
import com.mrnobody.morecommands.wrapper.Player;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.stats.Achievement;
import net.minecraft.stats.AchievementList;
import net.minecraft.stats.StatBase;
import net.minecraft.util.EnumChatFormatting;

@Command(
		name = "achievement",
		description = "command.achievement.description",
		example = "command.achievement.example",
		syntax = "command.achievement.syntax",
		videoURL = "command.achievement.videoURL"
		)
public class CommandAchievement extends StandardCommand implements ServerCommandProperties {
	private static final int PAGE_MAX = 15;
	private static final ImmutableMap<String, Achievement> achievements;
	private static final ImmutableList<String> achievementNameList;
	
	static {
		ImmutableMap.Builder<String, Achievement> builder = ImmutableMap.builder();
		
		Map<String, StatBase> stats = (Map<String, StatBase>) ReflectionHelper.get(ObfuscatedField.StatList_oneShotStats, null);
		if (stats != null) {
			for (Map.Entry<String, StatBase> entry : stats.entrySet()) {
				if (entry.getValue().isAchievement()) builder.put(entry.getKey().substring("achievement.".length()), (Achievement) entry.getValue());
			}
		}
    	
    	achievements = builder.build();
    	achievementNameList = ImmutableList.<String>builder().addAll(achievements.keySet()).build();
	}
	
    public String getCommandName() {
        return "achievement";
    }
    
    public String getUsage() {
        return "command.achievement.syntax";
    }
    
    public void execute(CommandSender sender, String[] params) throws CommandException {
    	final Player player = new Player(getSenderAsEntity(sender.getMinecraftISender(), EntityPlayerMP.class));
    	
    	if (params.length > 0) {
    		if(params[0].equals("list")) {
    			int page = 0;
    			
    			if (params.length > 1) {
    				try {
    					page = Integer.parseInt(params[1]) - 1; 
    					if (page < 0) page = 0;
    					else if (page * PAGE_MAX > achievementNameList.size()) page = achievementNameList.size() / PAGE_MAX;
    				}
    				catch (NumberFormatException e) {throw new CommandException("command.generic.invalidUsage", sender, this.getCommandName());}
    			}
    			
    			final int stop = (page + 1) * PAGE_MAX;;
    			for (int i = page * PAGE_MAX; i < stop && i < achievementNameList.size(); i++)
    				sender.sendStringMessage(" - '" + achievementNameList.get(i) + "'");
    			
    			sender.sendLangfileMessage("command.achievement.more", EnumChatFormatting.RED);
    		}
    		else if (params.length > 1 && params[0].equalsIgnoreCase("unlock")) {
    			if (params[1].equals("*")) {
                    Iterator<Achievement> iterator = ((List<Achievement>) AchievementList.achievementList).iterator();
                    while (iterator.hasNext()) player.addAchievement(iterator.next());
        			sender.sendLangfileMessage("command.achievement.unlockAllSuccess");
    			}
    			else {
    				Achievement achievement = achievements.get(params[1]);
    				if (achievement == null) throw new CommandException("command.achievement.doesNotExist", sender, params[1]);
    				
    				if (player.hasAchievement(achievement))
    					throw new CommandException("command.achievement.alreadyUnlocked", sender, params[1]);
    				
    				List<Achievement> unlock = Lists.newArrayList();
    				for (; achievement.parentAchievement != null && !player.hasAchievement(achievement); achievement = achievement.parentAchievement)
    					unlock.add(achievement.parentAchievement);
    				
    				Iterator<Achievement> iterator = Lists.reverse(unlock).iterator();
    				while (iterator.hasNext()) player.addAchievement(iterator.next());
    				
    				sender.sendLangfileMessage("command.achievement.unlockSuccess", params[1]);
    			}
    		}
    		else if (params.length > 1 && params[0].equalsIgnoreCase("lock")) {
    			if (params[1].equals("*")) {
                    Iterator<Achievement> iterator = ((List<Achievement>) Lists.reverse(AchievementList.achievementList)).iterator();
                    while (iterator.hasNext()) player.removeAchievement(iterator.next());
        			sender.sendLangfileMessage("command.achievement.lockAllSuccess");
    			}
    			else {
    				Achievement achievement = achievements.get(params[1]); final Achievement achievement_f = achievement;
    				if (achievement == null) throw new CommandException("command.achievement.doesNotExist", sender, params[1]);
    				
    				if (!player.hasAchievement(achievement))
    					throw new CommandException("command.achievement.dontHave", sender, params[1]);
    				
    				List<Achievement> lock = Lists.newArrayList(Iterators.filter(((List<Achievement>) AchievementList.achievementList).iterator(), new Predicate<Achievement>() {
						@Override public boolean apply(Achievement input) {
							return player.hasAchievement(input) && input != achievement_f;
						}	
    				}));
    				
    				for (; achievement.parentAchievement != null && player.hasAchievement(achievement.parentAchievement); achievement = achievement.parentAchievement)
    					lock.remove(achievement.parentAchievement);
    				
                    Iterator<Achievement> iterator = lock.iterator();
                    while (iterator.hasNext()) player.removeAchievement(iterator.next());
        			sender.sendLangfileMessage("command.achievement.lockSuccess", params[1]);
    			}
    		}
    		else throw new CommandException("command.generic.invalidUsage", sender, this.getCommandName());
    	}
    	else throw new CommandException("command.generic.invalidUsage", sender, this.getCommandName());
    }
    
	@Override
	public CommandRequirement[] getRequirements() {
		return new CommandRequirement[0];
	}
	
	@Override
	public ServerType getAllowedServerType() {
		return ServerType.ALL;
	}
	
	@Override
	public int getDefaultPermissionLevel() {
		return 2;
	}
	
	@Override
	public boolean canSenderUse(String commandName, ICommandSender sender, String[] params) {
		return isSenderOfEntityType(sender, EntityPlayerMP.class);
	}
}
