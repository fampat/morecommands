package com.mrnobody.morecommands.patch;

import java.lang.reflect.Field;

import com.mrnobody.morecommands.command.server.CommandNoclip;
import com.mrnobody.morecommands.util.ReflectionHelper;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;

public class NetHandlerPlayServer extends net.minecraft.network.NetHandlerPlayServer
{
    private static String FIELD_LASTPOSZ = "lastPosZ"; //Obfuscated Name: "field_147381_q"; //ReflectionHelper.getField fetches obfuscated name from ObfuscationResolver automatically
    private static String FIELD_LASTPOSX = "lastPosX"; //Obfuscated Name: "field_147373_o"; //ReflectionHelper.getField fetches obfuscated name from ObfuscationResolver automatically
    private static String FIELD_LASTPOSY = "lastPosY"; //Obfuscated Name: "field_147382_p"; //ReflectionHelper.getField fetches obfuscated name from ObfuscationResolver automatically
    private static String FIELD_HASMOVED = "hasMoved"; //Obfuscated Name: "field_147380_r"; //ReflectionHelper.getField fetches obfuscated name from ObfuscationResolver automatically
    private MinecraftServer mcServer;

    public NetHandlerPlayServer(MinecraftServer par1, NetworkManager par2, EntityPlayerMP par3) {
        super(par1, par2, par3);
        this.mcServer = par1;
    }

    @Override
    public void processPlayer(C03PacketPlayer packet) {
        if (this.playerEntity.noClip) {
            handleNoclip(packet);
        } else {
            super.processPlayer(packet);
        }
    }

    public void handleNoclip(C03PacketPlayer packet) {
    	CommandNoclip.checkSafe(this.playerEntity);
    	
        WorldServer var2 = this.mcServer.worldServerForDimension(this.playerEntity.dimension);
        if (!this.playerEntity.playerConqueredTheEnd) {
            if (!getBoolField(FIELD_HASMOVED)) {
                double var3 = packet.func_149467_d() - getDoubleField(FIELD_LASTPOSY);
                if ((packet.func_149464_c() == getDoubleField(FIELD_LASTPOSX)) && (var3 * var3 < 0.01D) && (packet.func_149472_e() == getDoubleField(FIELD_LASTPOSZ))) {
                    setBoolField(FIELD_HASMOVED, true);
                }
            }
            if (getBoolField(FIELD_HASMOVED)) {
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
                    setDoubleField(FIELD_LASTPOSX, this.playerEntity.posX);
                    setDoubleField(FIELD_LASTPOSY, this.playerEntity.posY);
                    setDoubleField(FIELD_LASTPOSZ, this.playerEntity.posZ);
                    var2.updateEntity(this.playerEntity);
                        return;
                    }
                if (this.playerEntity.isPlayerSleeping()) {
                    this.playerEntity.onUpdateEntity();
                    this.playerEntity.setPositionAndRotation(getDoubleField(FIELD_LASTPOSX), getDoubleField(FIELD_LASTPOSY), getDoubleField(FIELD_LASTPOSZ), this.playerEntity.rotationYaw, this.playerEntity.rotationPitch);
                    var2.updateEntity(this.playerEntity);
                    return;
                }
                double var3 = this.playerEntity.posY;
                setDoubleField(FIELD_LASTPOSX, this.playerEntity.posX);
                setDoubleField(FIELD_LASTPOSY, this.playerEntity.posY);
                setDoubleField(FIELD_LASTPOSZ, this.playerEntity.posZ);
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
                this.playerEntity.setPositionAndRotation(getDoubleField(FIELD_LASTPOSX), getDoubleField(FIELD_LASTPOSY), getDoubleField(FIELD_LASTPOSZ), var11, var12);
                if (!getBoolField(FIELD_HASMOVED)) {
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

    private boolean getBoolField(String fieldName) {
    	Field field = ReflectionHelper.getField(net.minecraft.network.NetHandlerPlayServer.class, fieldName);
    	
    	if (field != null) {try {return field.getBoolean(this);} catch (Exception ex) {ex.printStackTrace();}}
    	return false;
    }

    private void setBoolField(String fieldName, boolean value) {
    	Field field = ReflectionHelper.getField(net.minecraft.network.NetHandlerPlayServer.class, fieldName);
    	try {field.setBoolean(this, value);} catch (Exception ex) {ex.printStackTrace();}
    }

    private double getDoubleField(String fieldName) {
    	Field field = ReflectionHelper.getField(net.minecraft.network.NetHandlerPlayServer.class, fieldName);
    	
    	if (field != null) {try {return field.getDouble(this);} catch (Exception ex) {ex.printStackTrace();}}
    	return 0.0D;
    }

    private void setDoubleField(String fieldName, double value) {
    	Field field = ReflectionHelper.getField(net.minecraft.network.NetHandlerPlayServer.class, fieldName);
    	try {field.setDouble(this, value);} catch (Exception ex) {ex.printStackTrace();}
    }

    private float getFloatField(String fieldName) {
    	Field field = ReflectionHelper.getField(net.minecraft.network.NetHandlerPlayServer.class, fieldName);
    	
    	if (field != null) {try {return field.getFloat(this);} catch (Exception ex) {ex.printStackTrace();}}
    	return 0.0F;
    }

    private void setFloatField(String fieldName, float value) {
    	Field field = ReflectionHelper.getField(net.minecraft.network.NetHandlerPlayServer.class, fieldName);
    	try {field.setFloat(this, value);} catch (Exception ex) {ex.printStackTrace();}
    }
}
