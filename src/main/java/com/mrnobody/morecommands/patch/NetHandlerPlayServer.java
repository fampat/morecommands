package com.mrnobody.morecommands.patch;

import java.lang.reflect.Field;

import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.util.ReflectionHelper;
import com.mrnobody.morecommands.wrapper.CommandSender;
import com.mrnobody.morecommands.wrapper.Coordinate;
import com.mrnobody.morecommands.wrapper.Player;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;

/**
 * The patched class of {@link net.minecraft.network.NetHandlerPlayServer} <br>
 * It controls incoming packets from the client. The patch is needed to allow <br>
 * noclipping
 * 
 * @author MrNobody98
 *
 */
public class NetHandlerPlayServer extends net.minecraft.network.NetHandlerPlayServer
{
	private static final Field FIELD_LASTPOSZ = ReflectionHelper.getField(net.minecraft.network.NetHandlerPlayServer.class, "lastPosZ");
	private static final Field FIELD_LASTPOSY = ReflectionHelper.getField(net.minecraft.network.NetHandlerPlayServer.class, "lastPosY");
	private static final Field FIELD_LASTPOSX = ReflectionHelper.getField(net.minecraft.network.NetHandlerPlayServer.class, "lastPosX");
	private static final Field FIELD_HASMOVED = ReflectionHelper.getField(net.minecraft.network.NetHandlerPlayServer.class, "hasMoved");
    
	private MinecraftServer mcServer;
	private boolean enabled;

    public NetHandlerPlayServer(MinecraftServer par1, NetworkManager par2, EntityPlayerMP par3) {
        super(par1, par2, par3);
        this.mcServer = par1;
        this.enabled =  FIELD_HASMOVED != null && FIELD_LASTPOSX != null && FIELD_LASTPOSY != null && FIELD_LASTPOSZ != null;
    }

    @Override
    public void processPlayer(C03PacketPlayer packet) {
        if (this.enabled && this.playerEntity.noClip) {
            handleNoclip(packet);
        } else {
            super.processPlayer(packet);
        }
    }
    
	private static void checkSafe(EntityPlayerMP player) {
		if (player.noClip && !player.capabilities.isFlying) {
			player.noClip = false;
			
			MoreCommands.getMoreCommands().getPacketDispatcher().sendS07Noclip(player, false);
			
			(new CommandSender(player)).sendLangfileMessage("command.noclip.autodisable");
			ascendPlayer(new Player(player));
		}
	}

	private static boolean ascendPlayer(Player player) {
		Coordinate playerPos = player.getPosition();
		if(player.getWorld().isClearBelow(playerPos) && playerPos.getY() > 0) {
			return false;
		}
		double y = playerPos.getY() - 1; // in case player was standing on ground
		while (y < 260) {
			if(player.getWorld().isClear(new Coordinate(playerPos.getX(), y++, playerPos.getZ()))) {
				final double newY;
				if (playerPos.getY() > 0) newY = y - 1;
				else newY = y;
				Coordinate newPos = new Coordinate(playerPos.getX() + 0.5F, newY, playerPos.getZ() + 0.5F);
				player.setPosition(newPos);
				break;
			}
		}
		return true;
	}

    public void handleNoclip(C03PacketPlayer packet) {
    	checkSafe(this.playerEntity);
    	
        WorldServer var2 = this.mcServer.worldServerForDimension(this.playerEntity.dimension);
        if (!this.playerEntity.playerConqueredTheEnd) {
            if (!this.getBoolean(FIELD_HASMOVED)) {
                double var3 = packet.func_149467_d() - this.getDouble(FIELD_LASTPOSY);
                if ((packet.func_149464_c() == this.getDouble(FIELD_LASTPOSX)) && (var3 * var3 < 0.01D) && (packet.func_149472_e() == this.getDouble(FIELD_LASTPOSZ))) {
                    this.setBoolean(FIELD_HASMOVED, true);
                }
            }
            if (this.getBoolean(FIELD_HASMOVED)) {
                if (this.playerEntity.ridingEntity != null) {
                    float var34 = this.playerEntity.rotationYaw;
                    float var4 = this.playerEntity.rotationPitch;
                    this.playerEntity.ridingEntity.updateRiderPosition();
                    double var5 = this.playerEntity.posX;
                    double var7 = this.playerEntity.posY;
                    double var9 = this.playerEntity.posZ;
                    double var35 = 0.0D;
                    double var13 = 0.0D;
                    if (packet.func_149463_k()) {
                        var34 = packet.func_149462_g();
                        var4 = packet.func_149470_h();
                    }
                    if ((packet.func_149466_j()) && (packet.func_149467_d() == -999.0D) && (packet.func_149471_f() == -999.0D)) {
                        if ((Math.abs(packet.func_149464_c()) > 1.0D) || (Math.abs(packet.func_149472_e()) > 1.0D)) {
                            System.err.println(this.playerEntity.getCommandSenderName() + " was caught trying to crash the server with an invalid position.");
                            kickPlayerFromServer("Nope!");
                            return;
                        }
                        var35 = packet.func_149464_c();
                        var13 = packet.func_149472_e();
                    }
                    this.playerEntity.onGround = packet.func_149465_i();
                    this.playerEntity.onUpdateEntity();
                    this.playerEntity.moveEntity(var35, 0.0D, var13);
                    this.playerEntity.setPositionAndRotation(var5, var7, var9, var34, var4);
                    this.playerEntity.motionX = var35;
                    this.playerEntity.motionZ  = var13;
                    if (this.playerEntity.ridingEntity != null) {
                        this.playerEntity.ridingEntity.updateRiderPosition();
                    }
                    this.mcServer.getConfigurationManager().updatePlayerPertinentChunks(this.playerEntity);
                    this.setDouble(FIELD_LASTPOSX, this.playerEntity.posX);
                    this.setDouble(FIELD_LASTPOSY, this.playerEntity.posY);
                    this.setDouble(FIELD_LASTPOSZ, this.playerEntity.posZ);
                    var2.updateEntity(this.playerEntity);
                        return;
                    }
                if (this.playerEntity.isPlayerSleeping()) {
                    this.playerEntity.onUpdateEntity();
                    this.playerEntity.setPositionAndRotation(this.getDouble(FIELD_LASTPOSX), this.getDouble(FIELD_LASTPOSY), this.getDouble(FIELD_LASTPOSZ), this.playerEntity.rotationYaw, this.playerEntity.rotationPitch);
                    var2.updateEntity(this.playerEntity);
                    return;
                }
                double var3 = this.playerEntity.posY;
                this.setDouble(FIELD_LASTPOSX, this.playerEntity.posX);
                this.setDouble(FIELD_LASTPOSY, this.playerEntity.posY);
                this.setDouble(FIELD_LASTPOSZ, this.playerEntity.posZ);
                double var5 = this.playerEntity.posX;
                double var7 = this.playerEntity.posY;
                double var9 = this.playerEntity.posZ;
                float var11 = this.playerEntity.rotationYaw;
                float var12 = this.playerEntity.rotationPitch;
                if ((packet.func_149466_j()) && (packet.func_149467_d() == -999.0D) && (packet.func_149471_f() == -999.0D)) {
                    packet.func_149469_a(false);
                }
                if (packet.func_149466_j()) {
                    var5 = packet.func_149464_c();
                    var7 = packet.func_149467_d();
                    var9 = packet.func_149472_e();
                    double var13 = packet.func_149471_f() - packet.func_149467_d();
                    if ((!this.playerEntity.isPlayerSleeping()) && ((var13 > 1.65D) || (var13 < 0.1D))) {
                        kickPlayerFromServer("Illegal stance");
                        this.mcServer.logWarning(this.playerEntity.getCommandSenderName() + " had an illegal stance: " + var13);
                        return;
                    }
                    if ((Math.abs(packet.func_149464_c()) > 32000000.0D) || (Math.abs(packet.func_149472_e()) > 32000000.0D)) {
                        kickPlayerFromServer("Illegal position");
                        return;
                    }
                }
                if (packet.func_149463_k()) {
                    var11 = packet.func_149462_g();
                    var12 = packet.func_149470_h();
                }
                this.playerEntity.onUpdateEntity();
                this.playerEntity.ySize = 0.0F;
                this.playerEntity.setPositionAndRotation(this.getDouble(FIELD_LASTPOSX), this.getDouble(FIELD_LASTPOSY), this.getDouble(FIELD_LASTPOSZ), var11, var12);
                if (!this.getBoolean(FIELD_HASMOVED)) {
                    return;
                }
                double var13 = var5 - this.playerEntity.posX;
                double var15 = var7 - this.playerEntity.posY;
                double var17 = var9 - this.playerEntity.posZ;
                if ((this.playerEntity.onGround) && (!packet.func_149465_i()) && (var15 > 0.0D)) {
                    this.playerEntity.addExhaustion(0.2F);
                }
                this.playerEntity.moveEntity(var13, var15, var17);
                this.playerEntity.onGround = packet.func_149465_i();
                this.playerEntity.addMovementStat(var13, var15, var17);
                this.playerEntity.setPositionAndRotation(var5, var7, var9, var11, var12);
                this.playerEntity.onGround = packet.func_149465_i();
                this.mcServer.getConfigurationManager().updatePlayerPertinentChunks(this.playerEntity);
                this.playerEntity.handleFalling(this.playerEntity.posY - var3, packet.func_149465_i());
            }
        }
    }

    private boolean getBoolean(Field field) {
    	try {return field.getBoolean(this);} catch (Exception ex) {ex.printStackTrace();}
    	return false;
    }

    private void setBoolean(Field field, boolean value) {
    	try {field.setBoolean(this, value);} catch (Exception ex) {ex.printStackTrace();}
    }

    private double getDouble(Field field) {
    	try {return field.getDouble(this);} catch (Exception ex) {ex.printStackTrace();}
    	return 0.0D;
    }

    private void setDouble(Field field, double value) {
    	try {field.setDouble(this, value);} catch (Exception ex) {ex.printStackTrace();}
    }

    private float getFloat(Field field) {
    	try {return field.getFloat(this);} catch (Exception ex) {ex.printStackTrace();}
    	return 0.0F;
    }

    private void setFloat(Field field, float value) {
    	try {field.setFloat(this, value);} catch (Exception ex) {ex.printStackTrace();}
    }
    
    private int getInt(Field field) {
    	try {return field.getInt(this);} catch (Exception ex) {ex.printStackTrace();}
    	return 0;
    }

    private void setInt(Field field, int value) {
    	try {field.setInt(this, value);} catch (Exception ex) {ex.printStackTrace();}
    }
}
