package com.mrnobody.morecommands.command.client;

import java.lang.reflect.Field;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.MusicTicker;
import net.minecraft.client.audio.MusicTicker.MusicType;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundCategory;

import com.mrnobody.morecommands.command.ClientCommand;
import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.util.ReflectionHelper;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

@Command(
		name = "music",
		description = "command.music.description",
		example = "command.music.example",
		syntax = "command.music.syntax",
		videoURL = "command.music.videoURL"
		)
public class CommandMusic extends ClientCommand {
	private Field musicTickerField = ReflectionHelper.getField(Minecraft.class, "mcMusicTicker");
	private Field playingField = ReflectionHelper.getField(MusicTicker.class, "currentMusic");
	private Field playingTimerField = ReflectionHelper.getField(MusicTicker.class, "timeUntilNextMusic");

	@Override
	public String getName() {
		return "music";
	}

	@Override
	public String getUsage() {
		return "command.music.syntax";
	}

	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		sender.sendStringMessageToPlayer("Currently not working properly. Please wait for a fix");
		/*if (params.length > 0) {
			if (params[0].equalsIgnoreCase("play")) {
				this.playRandomMusic();
				sender.sendLangfileMessageToPlayer("command.music.played", new Object[0]);
			}
			else if (params[0].equalsIgnoreCase("stop")) {
				if (this.getPlayingMusic() != null) this.stopSound(this.getPlayingMusic());
				sender.sendLangfileMessageToPlayer("command.music.stopped", new Object[0]);
			}
			else if (params[0].equalsIgnoreCase("next") || params[0].equalsIgnoreCase("skip")) {
				if (this.getPlayingMusic() != null) this.stopSound(this.getPlayingMusic());
				this.playRandomMusic();
				sender.sendLangfileMessageToPlayer("command.music.skipped", new Object[0]);
			}
			else if (params[0].equalsIgnoreCase("volume") && params.length > 1) {
				try {
					int volume = Integer.parseInt(params[1]);
					
					if (volume < 0) volume = 0;
					if (volume > 100) volume = 100;
					
					Minecraft.getMinecraft().gameSettings.setSoundLevel(SoundCategory.MUSIC, volume / 100.0F);
					Minecraft.getMinecraft().getSoundHandler().setSoundLevel(SoundCategory.MUSIC, volume / 100.0F);
					
					sender.sendLangfileMessageToPlayer("command.music.volumeset", new Object[0]);
				}
				catch (NumberFormatException nfe) {sender.sendLangfileMessageToPlayer("command.music.invalidArg", new Object[0]);}
			}
			else sender.sendLangfileMessageToPlayer("command.music.invalidUsage", new Object[0]);
		}
		else {
			this.playRandomMusic();
			sender.sendLangfileMessageToPlayer("command.music.playedrandom", new Object[0]);
		}*/
	}
	
	private ISound getPlayingMusic() {
		if (this.musicTickerField != null && this.playingField != null) {
			try {
				MusicTicker musicTicker = (MusicTicker) musicTickerField.get(Minecraft.getMinecraft());
				ISound playing = (ISound) playingField.get(musicTicker);
				
				return playing;
			}
			catch (Exception ex) {ex.printStackTrace();}
		}
		return null;
	}
	
	private void stopSound(ISound sound) {
		if (sound != null && this.playingTimerField != null) {
			try {
				MusicTicker musicTicker = (MusicTicker) this.musicTickerField.get(Minecraft.getMinecraft());
					
				if (Minecraft.getMinecraft().getSoundHandler().isSoundPlaying(sound))
					Minecraft.getMinecraft().getSoundHandler().stopSound(sound);
				
				System.out.println("playing: " + Minecraft.getMinecraft().getSoundHandler().isSoundPlaying(sound));
				
				this.playingField.set(musicTicker, null);
				this.playingTimerField.setInt(musicTicker, Integer.MAX_VALUE);
			}
			catch (Exception ex) {ex.printStackTrace();}
		}
	}
	
	private void playSound(ISound sound) {
		if (sound != null && this.playingTimerField != null) {
			try {
				MusicTicker musicTicker = (MusicTicker) this.musicTickerField.get(Minecraft.getMinecraft());
				int playTimer = this.playingTimerField.getInt(musicTicker);
					
				if (Minecraft.getMinecraft().getSoundHandler().isSoundPlaying(sound))
					Minecraft.getMinecraft().getSoundHandler().stopSound(sound);
					
				Minecraft.getMinecraft().getSoundHandler().playSound(sound);
				
				this.playingField.set(musicTicker, sound);
				this.playingTimerField.setInt(musicTicker, Integer.MAX_VALUE);
			}
			catch (Exception ex) {ex.printStackTrace();}
		}
	}
	
	private void playRandomMusic() {
		ISound playing = this.getPlayingMusic();
		
		if (this.playingTimerField != null) {
			try {
				MusicTicker musicTicker = (MusicTicker) this.musicTickerField.get(Minecraft.getMinecraft());
				int playTimer = this.playingTimerField.getInt(musicTicker);
					
				if (playing != null && Minecraft.getMinecraft().getSoundHandler().isSoundPlaying(playing))
					Minecraft.getMinecraft().getSoundHandler().stopSound(playing);
				
				MusicType music = Minecraft.getMinecraft().getAmbientMusicType();
				
				playing = PositionedSoundRecord.create(music.getMusicLocation());
				Minecraft.getMinecraft().getSoundHandler().playSound(playing);
				
				this.playingField.set(musicTicker, playing);
				this.playingTimerField.setInt(musicTicker, Integer.MAX_VALUE);
			}
			catch (Exception ex) {ex.printStackTrace();}
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
