package com.mrnobody.morecommands.command.client;

import com.mrnobody.morecommands.command.ClientCommandProperties;
import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.CommandException;
import com.mrnobody.morecommands.command.CommandRequirement;
import com.mrnobody.morecommands.command.CommandSender;
import com.mrnobody.morecommands.command.StandardCommand;
import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.event.EventHandler;
import com.mrnobody.morecommands.event.Listeners.EventListener;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.MusicTicker;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.client.event.sound.PlaySoundEvent;

@Command(
		name = "music",
		description = "command.music.description",
		example = "command.music.example",
		syntax = "command.music.syntax",
		videoURL = "command.music.videoURL"
		)
public class CommandMusic extends StandardCommand implements ClientCommandProperties, EventListener<PlaySoundEvent> {
	private boolean stopSound = false;

	@Override
	public void onEvent(PlaySoundEvent event) {
		if (event.getSound().getCategory() == SoundCategory.MUSIC) {
			if (this.stopSound) 
				event.setResultSound(null);
		}
	}
	
	public CommandMusic() {
		EventHandler.SOUND.register(this);
	}
	
	@Override
	public String getCommandName() {
		return "music";
	}

	@Override
	public String getCommandUsage() {
		return "command.music.syntax";
	}

	@Override
	public String execute(CommandSender sender, String[] params) throws CommandException {
		if (params.length > 0) {
			if (params[0].equalsIgnoreCase("play")) {
				if (!this.stopSound)
					throw new CommandException("command.music.isplaying", sender);
				
				this.stopSound = false;
				Minecraft.getMinecraft().getMusicTicker().playMusic(Minecraft.getMinecraft().getAmbientMusicType());				
				sender.sendLangfileMessage("command.music.played");
			}
			else if (params[0].equalsIgnoreCase("next") || params[0].equalsIgnoreCase("skip")) {
				MusicTicker musicTicker = Minecraft.getMinecraft().getMusicTicker();
				musicTicker.stopMusic();
				musicTicker.playMusic(Minecraft.getMinecraft().getAmbientMusicType());
			
				this.stopSound = false;
				sender.sendLangfileMessage("command.music.skipped");
			}
			else if (params[0].equalsIgnoreCase("stop")) {
				Minecraft.getMinecraft().getMusicTicker().stopMusic();
				this.stopSound = true;
				sender.sendLangfileMessage("command.music.stopped");
			}
			else if (params[0].equalsIgnoreCase("volume") && params.length > 1) {
				try {
					int volume = Integer.parseInt(params[1]);
					
					if (volume < 0) volume = 0;
					if (volume > 100) volume = 100;
					
					Minecraft.getMinecraft().gameSettings.setSoundLevel(SoundCategory.MUSIC, volume / 100.0F);
					Minecraft.getMinecraft().getSoundHandler().setSoundLevel(SoundCategory.MUSIC, volume / 100.0F);
					
					sender.sendLangfileMessage("command.music.volumeset");
				}
				catch (NumberFormatException nfe) {throw new CommandException("command.music.invalidArg", sender);}
			}
			else throw new CommandException("command.music.invalidUsage", sender);
		}
		
		return null;
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
	public boolean registerIfServerModded() {
		return true;
	}
	
	@Override
	public int getDefaultPermissionLevel(String[] args) {
		return 0;
	}
}
