package com.mrnobody.morecommands.util;

import net.minecraft.entity.player.EntityPlayerMP;
import cpw.mods.fml.common.gameevent.InputEvent.KeyInputEvent;

/**
 * A key event class used to post key presses
 * received from clients
 * 
 * @author MrNobody98
 *
 */
public class KeyEvent extends KeyInputEvent {
	public final EntityPlayerMP player;
	public final int key;
	
	public KeyEvent(EntityPlayerMP player, int key) {
		this.player = player;
		this.key = key;
	}
}
