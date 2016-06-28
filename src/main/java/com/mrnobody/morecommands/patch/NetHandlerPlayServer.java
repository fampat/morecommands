package com.mrnobody.morecommands.patch;

import java.lang.reflect.Field;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.primitives.Doubles;
import com.google.common.primitives.Floats;
import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.util.ObfuscatedNames.ObfuscatedField;
import com.mrnobody.morecommands.util.ReflectionHelper;
import com.mrnobody.morecommands.wrapper.CommandSender;
import com.mrnobody.morecommands.wrapper.Player;

import net.minecraft.init.MobEffects;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketThreadUtil;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
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
	private static final Logger logger = LogManager.getLogger(net.minecraft.network.NetHandlerPlayServer.class);
	private static final Field firstGoodX = ReflectionHelper.getField(ObfuscatedField.NetHandlerPlayServer_firstGoodX);
	private static final Field firstGoodY = ReflectionHelper.getField(ObfuscatedField.NetHandlerPlayServer_firstGoodY);
	private static final Field firstGoodZ = ReflectionHelper.getField(ObfuscatedField.NetHandlerPlayServer_firstGoodZ);
	private static final Field lastGoodX = ReflectionHelper.getField(ObfuscatedField.NetHandlerPlayServer_lastGoodX);
	private static final Field lastGoodY = ReflectionHelper.getField(ObfuscatedField.NetHandlerPlayServer_lastGoodXY);
	private static final Field lastGoodZ = ReflectionHelper.getField(ObfuscatedField.NetHandlerPlayServer_lastGoodZ);
	private static final Field targetPos = ReflectionHelper.getField(ObfuscatedField.NetHandlerPlayServer_targetPos);
	private static final Field lastPositionUpdate = ReflectionHelper.getField(ObfuscatedField.NetHandlerPlayServer_lastPositionUpdate);
	private static final Field lastMovePacketCounter = ReflectionHelper.getField(ObfuscatedField.NetHandlerPlayServer_lastMovePacketCounter);
	private static final Field movePacketCounter = ReflectionHelper.getField(ObfuscatedField.NetHandlerPlayServer_movePacketCounter);
	private static final Field networkTickCount = ReflectionHelper.getField(ObfuscatedField.NetHandlerPlayServer_networkTickCount);
	private static final Field floating = ReflectionHelper.getField(ObfuscatedField.NetHandlerPlayServer_floating);
	
    private MinecraftServer mcServer;
    public final boolean enabled;
	private boolean overrideNoclip = false;

    public NetHandlerPlayServer(MinecraftServer par1, NetworkManager par2, net.minecraft.entity.player.EntityPlayerMP par3) {
        super(par1, par2, par3);
        this.mcServer = par1;
        this.enabled = firstGoodX != null && firstGoodY != null && firstGoodZ != null &&
        		lastGoodX != null && lastGoodY != null && lastGoodZ != null && targetPos != null &&
        		lastPositionUpdate != null && lastMovePacketCounter != null && movePacketCounter != null &&
    			networkTickCount != null && floating != null;
    }
    
	public void setOverrideNoclip(boolean override) {
		this.overrideNoclip = override;
	}
	
	public boolean getOverrideNoclip() {
		return this.overrideNoclip;
	}

    @Override
    public void processPlayer(CPacketPlayer packet) {
        if (this.enabled && this.overrideNoclip) {
        	handleNoclip(packet);
        } else {
            super.processPlayer(packet);
        }
    }
    
    private static void checkSafe(NetHandlerPlayServer handler, net.minecraft.entity.player.EntityPlayerMP player) {
		if(handler.getOverrideNoclip() && !player.capabilities.isFlying) {
			handler.setOverrideNoclip(false);
			MoreCommands.INSTANCE.getPacketDispatcher().sendS06Noclip(player, false);
			
			(new CommandSender(player)).sendLangfileMessage("command.noclip.autodisable");
			ascendPlayer(new Player(player));
		}
	}

	private static boolean ascendPlayer(Player player) {
		BlockPos playerPos = player.getPosition();
		if(player.getWorld().isClearBelow(playerPos) && playerPos.getY() > 0) {
			return false;
		}
		double y = playerPos.getY() - 1;
		while (y < 260) {
			if(player.getWorld().isClear(new BlockPos(playerPos.getX(), y++, playerPos.getZ()))) {
				final double newY;
				if(playerPos.getY() > 0) {
					newY = y - 1;
				} else {
					newY = y;
				}
				BlockPos newPos = new BlockPos(playerPos.getX() + 0.5F, newY, playerPos.getZ() + 0.5F);
				player.setPosition(newPos);
				break;
			}
		}
		return true;
	}
	
	public void handleNoclip(CPacketPlayer packetIn) {
		checkSafe(this, this.playerEntity);
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.playerEntity.getServer());

        if (isMovePlayerPacketInvalid(packetIn))
        {
            this.kickPlayerFromServer("Invalid move player packet received");
        }
        else
        {
            WorldServer worldserver = this.mcServer.worldServerForDimension(this.playerEntity.dimension);

            if (!this.playerEntity.playerConqueredTheEnd)
            {
                if (getInt(networkTickCount) == 0)
                {
            		setDouble(firstGoodX, this.playerEntity.posX);
            		setDouble(firstGoodY, this.playerEntity.posY);
            		setDouble(firstGoodZ, this.playerEntity.posZ);
            		setDouble(lastGoodX, this.playerEntity.posX);
            		setDouble(lastGoodY, this.playerEntity.posY);
            		setDouble(lastGoodZ, this.playerEntity.posX);
                }

                if (getObject(targetPos) != null)
                {
                    if (getInt(networkTickCount) - getInt(lastPositionUpdate) > 20)
                    {
                        setInt(lastPositionUpdate, getInt(networkTickCount));
                        this.setPlayerLocation(((Vec3d) getObject(targetPos)).xCoord, ((Vec3d) getObject(targetPos)).yCoord, ((Vec3d) getObject(targetPos)).zCoord, this.playerEntity.rotationYaw, this.playerEntity.rotationPitch);
                    }
                }
                else
                {
                	setInt(lastPositionUpdate, getInt(networkTickCount));

                    if (this.playerEntity.isRiding())
                    {
                        this.playerEntity.setPositionAndRotation(this.playerEntity.posX, this.playerEntity.posY, this.playerEntity.posZ, packetIn.getYaw(this.playerEntity.rotationYaw), packetIn.getPitch(this.playerEntity.rotationPitch));
                        this.mcServer.getPlayerList().serverUpdateMountedMovingPlayer(this.playerEntity);
                    }
                    else
                    {
                        double d0 = this.playerEntity.posX;
                        double d1 = this.playerEntity.posY;
                        double d2 = this.playerEntity.posZ;
                        double d3 = this.playerEntity.posY;
                        double d4 = packetIn.getX(this.playerEntity.posX);
                        double d5 = packetIn.getY(this.playerEntity.posY);
                        double d6 = packetIn.getZ(this.playerEntity.posZ);
                        float f = packetIn.getYaw(this.playerEntity.rotationYaw);
                        float f1 = packetIn.getPitch(this.playerEntity.rotationPitch);
                        double d7 = d4 - getDouble(firstGoodX);
                        double d8 = d5 - getDouble(firstGoodY);
                        double d9 = d6 - getDouble(firstGoodZ);
                        double d10 = this.playerEntity.motionX * this.playerEntity.motionX + this.playerEntity.motionY * this.playerEntity.motionY + this.playerEntity.motionZ * this.playerEntity.motionZ;
                        double d11 = d7 * d7 + d8 * d8 + d9 * d9;
                        setInt(movePacketCounter, getInt(movePacketCounter) + 1);
                        int i = getInt(movePacketCounter) - getInt(lastMovePacketCounter);

                        if (i > 5)
                        {
                        	logger.debug("{} is sending move packets too frequently ({} packets since last tick)", this.playerEntity.getName(), i);
                            i = 1;
                        }

                        if (!this.playerEntity.isInvulnerableDimensionChange() && (!this.playerEntity.worldObj.getGameRules().getBoolean("disableElytraMovementCheck") || !this.playerEntity.isElytraFlying()))
                        {
                            float f2 = this.playerEntity.isElytraFlying() ? 300.0F : 100.0F;

                            if (d11 - d10 > (double)(f2 * (float)i) && (!this.mcServer.isSinglePlayer() || !this.mcServer.getServerOwner().equals(this.playerEntity.getName())))
                            {
                                logger.warn("{} moved too quickly! {},{},{}", this.playerEntity.getName(), d7, d8, d9);
                                this.setPlayerLocation(this.playerEntity.posX, this.playerEntity.posY, this.playerEntity.posZ, this.playerEntity.rotationYaw, this.playerEntity.rotationPitch);
                                return;
                            }
                        }
                        
                        boolean flag2 = worldserver.getCollisionBoxes(this.playerEntity, this.playerEntity.getEntityBoundingBox().contract(0.0625D)).isEmpty();
                        d7 = d4 - getDouble(lastGoodX);
                        d8 = d5 - getDouble(lastGoodY);
                        d9 = d6 - getDouble(lastGoodZ);

                        if (this.playerEntity.onGround && !packetIn.isOnGround() && d8 > 0.0D)
                        {
                            this.playerEntity.jump();
                        }

                        this.playerEntity.moveEntity(d7, d8, d9);
                        this.playerEntity.onGround = packetIn.isOnGround();
                        double d12 = d8;
                        d7 = d4 - this.playerEntity.posX;
                        d8 = d5 - this.playerEntity.posY;

                        if (d8 > -0.5D || d8 < 0.5D)
                        {
                            d8 = 0.0D;
                        }

                        d9 = d6 - this.playerEntity.posZ;
                        d11 = d7 * d7 + d8 * d8 + d9 * d9;
                        boolean flag = false;
                        
                        //BYPASSES MOVED WORNGLY WARNING
                        /*if (!this.playerEntity.isInvulnerableDimensionChange() && d11 > 0.0625D && !this.playerEntity.isPlayerSleeping() && !this.playerEntity.interactionManager.isCreative() && this.playerEntity.interactionManager.getGameType() != WorldSettings.GameType.SPECTATOR)
                        {
                            flag = true;
                            logger.wwarn("{} moved wrongly!", this.playerEntity.getName());
                        }*/

                        this.playerEntity.setPositionAndRotation(d4, d5, d6, f, f1);
                        this.playerEntity.addMovementStat(this.playerEntity.posX - d0, this.playerEntity.posY - d1, this.playerEntity.posZ - d2);
                        
                        //BYPASSES NOCLIP CHECK
                        /*if (!this.playerEntity.noClip && !this.playerEntity.isPlayerSleeping())
                        {
                            boolean flag1 = worldserver.getCubes(this.playerEntity, this.playerEntity.getEntityBoundingBox().func_186664_h(0.0625D)).isEmpty();

                            if (flag2 && (flag || !flag1))
                            {
                                this.setPlayerLocation(d0, d1, d2, f, f1);
                                return;
                            }
                        }*/
                        
                        boolean val = d12 >= -0.03125D;
                        val &= !this.mcServer.isFlightAllowed() && !this.playerEntity.capabilities.allowFlying;
                        val &= !this.playerEntity.isPotionActive(MobEffects.LEVITATION) && !this.playerEntity.isElytraFlying() && !worldserver.checkBlockCollision(this.playerEntity.getEntityBoundingBox().expandXyz(0.0625D).addCoord(0.0D, -0.55D, 0.0D));
                        setBoolean(floating, val);
                        this.playerEntity.onGround = packetIn.isOnGround();
                        this.mcServer.getPlayerList().serverUpdateMountedMovingPlayer(this.playerEntity);
                        this.playerEntity.handleFalling(this.playerEntity.posY - d3, packetIn.isOnGround());
                        setDouble(lastGoodX, this.playerEntity.posX);
                        setDouble(lastGoodY, this.playerEntity.posY);
                        setDouble(lastGoodZ, this.playerEntity.posZ);
                    }
                }
            }
        }
    }
	
    private static boolean isMovePlayerPacketInvalid(CPacketPlayer packetIn) {
        return Doubles.isFinite(packetIn.getX(0.0D)) && Doubles.isFinite(packetIn.getY(0.0D)) && Doubles.isFinite(packetIn.getZ(0.0D)) && Floats.isFinite(packetIn.getYaw(0.0F)) && Floats.isFinite(packetIn.getPitch(0.0F)) ? false : Math.abs(packetIn.getX(0.0D)) <= 3.0E7D && Math.abs(packetIn.getX(0.0D)) <= 3.0E7D;
    }
	
    private boolean getBoolean(Field field) {
    	try {return field.getBoolean(this);} catch (Exception ex) {ex.printStackTrace();}
    	return false;
    }

    private void setBoolean(Field field, boolean value) {
    	try {field.setBoolean(this, value);} catch (Exception ex) {ex.printStackTrace();}
    }
    
    private Object getObject(Field field) {
    	try {return field.get(this);} catch (Exception ex) {ex.printStackTrace();}
    	return null;
    }

    private void setObject(Field field, Object value) {
    	try {field.set(this, value);} catch (Exception ex) {ex.printStackTrace();}
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
