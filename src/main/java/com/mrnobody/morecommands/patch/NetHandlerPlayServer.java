package com.mrnobody.morecommands.patch;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.primitives.Doubles;
import com.google.common.primitives.Floats;
import com.mrnobody.morecommands.command.CommandSender;
import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.settings.GlobalSettings;
import com.mrnobody.morecommands.settings.MoreCommandsConfig;
import com.mrnobody.morecommands.settings.PlayerSettings;
import com.mrnobody.morecommands.settings.ServerPlayerSettings;
import com.mrnobody.morecommands.util.EntityUtils;
import com.mrnobody.morecommands.util.ObfuscatedNames.ObfuscatedField;
import com.mrnobody.morecommands.util.ReflectionHelper;
import com.mrnobody.morecommands.util.Variables;
import com.mrnobody.morecommands.util.WorldUtils;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketThreadUtil;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;

/**
 * The patched class of {@link net.minecraft.network.NetHandlerPlayServer} <br>
 * It controls incoming packets from the client. The patch is needed to allow <br>
 * noclipping and to replace variables in the chat.
 * 
 * @author MrNobody98
 *
 */
public class NetHandlerPlayServer extends net.minecraft.network.NetHandlerPlayServer
{
	private static final Logger logger = LogManager.getLogger(net.minecraft.network.NetHandlerPlayServer.class);
	private static final Field FIELD_184349_L = ReflectionHelper.getField(ObfuscatedField.NetHandlerPlayServer_field_184349_l);
	private static final Field FIELD_184350_M = ReflectionHelper.getField(ObfuscatedField.NetHandlerPlayServer_field_184350_m);
	private static final Field FIELD_184351_N = ReflectionHelper.getField(ObfuscatedField.NetHandlerPlayServer_field_184351_n);
	private static final Field FIELD_184352_O = ReflectionHelper.getField(ObfuscatedField.NetHandlerPlayServer_field_184352_o);
	private static final Field FIELD_184353_P = ReflectionHelper.getField(ObfuscatedField.NetHandlerPlayServer_field_184353_p);
	private static final Field FIELD_184354_Q = ReflectionHelper.getField(ObfuscatedField.NetHandlerPlayServer_field_184354_q);
	private static final Field FIELD_184362_Y = ReflectionHelper.getField(ObfuscatedField.NetHandlerPlayServer_field_184362_y);
	private static final Field FIELD_184343_A = ReflectionHelper.getField(ObfuscatedField.NetHandlerPlayServer_field_184343_A);
	private static final Field FIELD_184348_G = ReflectionHelper.getField(ObfuscatedField.NetHandlerPlayServer_field_184348_G);
	private static final Field FIELD_184347_F = ReflectionHelper.getField(ObfuscatedField.NetHandlerPlayServer_field_184347_F);
	private static final Field FIELD_NETWORKTICKCOUNT = ReflectionHelper.getField(ObfuscatedField.NetHandlerPlayServer_networkTickCount);
	private static final Field FIELD_FLOATING = ReflectionHelper.getField(ObfuscatedField.NetHandlerPlayServer_floating);
	
    private MinecraftServer mcServer;
    public final boolean enabled;
	private boolean overrideNoclip = false;

    public NetHandlerPlayServer(MinecraftServer par1, NetworkManager par2, net.minecraft.entity.player.EntityPlayerMP par3) {
        super(par1, par2, par3);
        this.mcServer = par1;
        this.enabled = FIELD_184349_L != null && FIELD_184350_M != null && FIELD_184351_N != null &&
        		FIELD_184352_O != null && FIELD_184353_P != null && FIELD_184354_Q != null && FIELD_184362_Y != null &&
        		FIELD_184343_A != null && FIELD_184348_G != null && FIELD_184347_F != null &&
    			FIELD_NETWORKTICKCOUNT != null && FIELD_FLOATING != null;
    }
    
	public void setOverrideNoclip(boolean override) {
		this.overrideNoclip = override;
	}
	
	public boolean getOverrideNoclip() {
		return this.overrideNoclip;
	}
	
    @Override
    public void processChatMessage(CPacketChatMessage p_147354_1_) {
    	String message = p_147354_1_.getMessage();
    	
    	//required because because PacketThreadUtil.checkThreadAndQueue() terminates this method if we are not on the main thread
    	if (this.playerEntity.getServerForPlayer().isCallingFromMinecraftThread()) {
    		ServerPlayerSettings settings = this.playerEntity.getCapability(PlayerSettings.SETTINGS_CAP_SERVER, null);
			Map<String, String> playerVars = settings == null ? new HashMap<String, String>() : settings.variables;
    		boolean replaceIgnored;
    		
    		if (message.length() > 1 && message.charAt(0) == '%') {
    			int end = message.indexOf('%', 1);
    			String val = end > 0 ? playerVars.get(message.substring(1, end)) : null;
    			
    			replaceIgnored = val == null || !val.startsWith("/") ||
    							(message.length() - 1 != end && message.charAt(end + 1) != ' ') ||
    							!this.playerEntity.getServer().getCommandManager().getCommands().containsKey(val.substring(1));
    		}
    		else replaceIgnored = !message.startsWith("/") || !this.playerEntity.getServer().getCommandManager().getCommands().containsKey(message.substring(1).split(" ")[0]);
    		
        	try {
        		String world = this.playerEntity.getEntityWorld().getSaveHandler().getWorldDirectory().getName(), dim = this.playerEntity.getEntityWorld().provider.getDimensionType().getName();
        		
    			if (MoreCommandsConfig.enableGlobalVars && MoreCommandsConfig.enablePlayerVars)
    				message = Variables.replaceVars(message, replaceIgnored, playerVars, GlobalSettings.getInstance().variables.get(ImmutablePair.of(world, dim)));
    			else if (MoreCommandsConfig.enablePlayerVars)
    				message = Variables.replaceVars(message, replaceIgnored, playerVars);
    			else if (MoreCommandsConfig.enableGlobalVars)
    				message = Variables.replaceVars(message, replaceIgnored, GlobalSettings.getInstance().variables.get(ImmutablePair.of(world, dim)));
        	}
            catch (Variables.VariablesCouldNotBeResolvedException vcnbre) {
                message = vcnbre.getNewString();
            }
    	}
    	
    	super.processChatMessage(new CPacketChatMessage(message));
    }
    
    @Override
    public void processPlayer(CPacketPlayer packet) {
        if (this.enabled && this.overrideNoclip) {
        	handleNoclip(packet);
        } else {
            super.processPlayer(packet);
        }
    }
    
    private static void checkSafe(NetHandlerPlayServer handler, EntityPlayerMP player) {
		if(handler.getOverrideNoclip() && !player.capabilities.isFlying) {
			handler.setOverrideNoclip(false);
			MoreCommands.INSTANCE.getPacketDispatcher().sendS06Noclip(player, false);
			
			(new CommandSender(player)).sendLangfileMessage("command.noclip.autodisable");
			ascendPlayer(player);
		}
	}

	private static boolean ascendPlayer(EntityPlayerMP player) {
		BlockPos playerPos = player.getPosition();
		if (WorldUtils.isClearBelow(player.worldObj, playerPos) && playerPos.getY() > 0) {
			return false;
		}
		double y = playerPos.getY() - 1;
		while (y < 260) {
			if (WorldUtils.isClear(player.worldObj, new BlockPos(playerPos.getX(), y++, playerPos.getZ()))) {
				final double newY;
				if(playerPos.getY() > 0) {
					newY = y - 1;
				} else {
					newY = y;
				}
				BlockPos newPos = new BlockPos(playerPos.getX() + 0.5F, newY, playerPos.getZ() + 0.5F);
				EntityUtils.setPosition(player, newPos);
				break;
			}
		}
		return true;
	}
	
	public void handleNoclip(CPacketPlayer packetIn) {
		checkSafe(this, this.playerEntity);
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.playerEntity.getServerForPlayer());

        if (isMovePlayerPacketInvalid(packetIn))
        {
            this.kickPlayerFromServer("Invalid move player packet received");
        }
        else
        {
            WorldServer worldserver = this.mcServer.worldServerForDimension(this.playerEntity.dimension);

            if (!this.playerEntity.playerConqueredTheEnd)
            {
                if (getInt(FIELD_NETWORKTICKCOUNT) == 0)
                {
                	setDouble(FIELD_184349_L, this.playerEntity.posX);
                	setDouble(FIELD_184350_M, this.playerEntity.posY);
                	setDouble(FIELD_184351_N, this.playerEntity.posZ);
                	setDouble(FIELD_184352_O, this.playerEntity.posX);
                	setDouble(FIELD_184353_P, this.playerEntity.posY);
                	setDouble(FIELD_184354_Q, this.playerEntity.posX);
                }

                if (getObject(FIELD_184362_Y) != null)
                {
                    if (getInt(FIELD_NETWORKTICKCOUNT) - getInt(FIELD_184343_A) > 20)
                    {
                        setInt(FIELD_184343_A, getInt(FIELD_NETWORKTICKCOUNT));
                        this.setPlayerLocation(((Vec3d) getObject(FIELD_184362_Y)).xCoord, ((Vec3d) getObject(FIELD_184362_Y)).yCoord, ((Vec3d) getObject(FIELD_184362_Y)).zCoord, this.playerEntity.rotationYaw, this.playerEntity.rotationPitch);
                    }
                }
                else
                {
                	setInt(FIELD_184343_A, getInt(FIELD_NETWORKTICKCOUNT));

                    if (this.playerEntity.isRiding())
                    {
                        this.playerEntity.setPositionAndRotation(this.playerEntity.posX, this.playerEntity.posY, this.playerEntity.posZ, packetIn.func_186999_a(this.playerEntity.rotationYaw), packetIn.func_186998_b(this.playerEntity.rotationPitch));
                        this.mcServer.getPlayerList().serverUpdateMountedMovingPlayer(this.playerEntity);
                    }
                    else
                    {
                        double d0 = this.playerEntity.posX;
                        double d1 = this.playerEntity.posY;
                        double d2 = this.playerEntity.posZ;
                        double d3 = this.playerEntity.posY;
                        double d4 = packetIn.func_186997_a(this.playerEntity.posX);
                        double d5 = packetIn.func_186996_b(this.playerEntity.posY);
                        double d6 = packetIn.func_187000_c(this.playerEntity.posZ);
                        float f = packetIn.func_186999_a(this.playerEntity.rotationYaw);
                        float f1 = packetIn.func_186998_b(this.playerEntity.rotationPitch);
                        double d7 = d4 - getDouble(FIELD_184349_L);
                        double d8 = d5 - getDouble(FIELD_184350_M);
                        double d9 = d6 - getDouble(FIELD_184351_N);
                        double d10 = this.playerEntity.motionX * this.playerEntity.motionX + this.playerEntity.motionY * this.playerEntity.motionY + this.playerEntity.motionZ * this.playerEntity.motionZ;
                        double d11 = d7 * d7 + d8 * d8 + d9 * d9;
                        setInt(FIELD_184347_F, getInt(FIELD_184347_F) + 1);
                        int i = getInt(FIELD_184347_F) - getInt(FIELD_184348_G);

                        if (i > 5)
                        {
                            logger.debug(this.playerEntity.getName() + " is sending move packets too frequently (" + i + " packets since last tick)");
                            i = 1;
                        }

                        if (!this.playerEntity.isInvulnerableDimensionChange() && (!this.playerEntity.getServerForPlayer().getGameRules().getBoolean("disableElytraMovementCheck") || !this.playerEntity.isElytraFlying()))
                        {
                            float f2 = this.playerEntity.isElytraFlying() ? 300.0F : 100.0F;

                            if (d11 - d10 > (double)(f2 * (float)i) && (!this.mcServer.isSinglePlayer() || !this.mcServer.getServerOwner().equals(this.playerEntity.getName())))
                            {
                                logger.warn(this.playerEntity.getName() + " moved too quickly! " + d7 + "," + d8 + "," + d9);
                                this.setPlayerLocation(this.playerEntity.posX, this.playerEntity.posY, this.playerEntity.posZ, this.playerEntity.rotationYaw, this.playerEntity.rotationPitch);
                                return;
                            }
                        }

                        boolean flag2 = worldserver.getCubes(this.playerEntity, this.playerEntity.getEntityBoundingBox().func_186664_h(0.0625D)).isEmpty();
                        d7 = d4 - getDouble(FIELD_184352_O);
                        d8 = d5 - getDouble(FIELD_184353_P);
                        d9 = d6 - getDouble(FIELD_184354_Q);

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
                            logger.warn(this.playerEntity.getName() + " moved wrongly!");
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
                        val &= !this.playerEntity.isPotionActive(MobEffects.levitation) && !this.playerEntity.isElytraFlying() && !worldserver.checkBlockCollision(this.playerEntity.getEntityBoundingBox().expandXyz(0.0625D).addCoord(0.0D, -0.55D, 0.0D));
                        setBoolean(FIELD_FLOATING, val);
                        this.playerEntity.onGround = packetIn.isOnGround();
                        this.mcServer.getPlayerList().serverUpdateMountedMovingPlayer(this.playerEntity);
                        this.playerEntity.handleFalling(this.playerEntity.posY - d3, packetIn.isOnGround());
                        setDouble(FIELD_184352_O, this.playerEntity.posX);
                        setDouble(FIELD_184353_P, this.playerEntity.posY);
                        setDouble(FIELD_184354_Q, this.playerEntity.posZ);
                    }
                }
            }
        }
    }
	
    private static boolean isMovePlayerPacketInvalid(CPacketPlayer packetIn) {
        return Doubles.isFinite(packetIn.func_186997_a(0.0D)) && Doubles.isFinite(packetIn.func_186996_b(0.0D)) && Doubles.isFinite(packetIn.func_187000_c(0.0D)) && Floats.isFinite(packetIn.func_186998_b(0.0F)) && Floats.isFinite(packetIn.func_186999_a(0.0F)) ? false : Math.abs(packetIn.func_186997_a(0.0D)) <= 3.0E7D && Math.abs(packetIn.func_186997_a(0.0D)) <= 3.0E7D;
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
