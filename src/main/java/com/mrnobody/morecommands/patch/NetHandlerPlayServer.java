package com.mrnobody.morecommands.patch;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.tuple.ImmutablePair;

import com.mrnobody.morecommands.command.CommandSender;
import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.settings.GlobalSettings;
import com.mrnobody.morecommands.settings.MoreCommandsConfig;
import com.mrnobody.morecommands.settings.PlayerSettings;
import com.mrnobody.morecommands.settings.ServerPlayerSettings;
import com.mrnobody.morecommands.util.EntityUtils;
import com.mrnobody.morecommands.util.LanguageManager;
import com.mrnobody.morecommands.util.ObfuscatedNames.ObfuscatedField;
import com.mrnobody.morecommands.util.ReflectionHelper;
import com.mrnobody.morecommands.util.Variables;
import com.mrnobody.morecommands.util.WorldUtils;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketThreadUtil;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.server.S18PacketEntityTeleport;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.WorldServer;

/**
 * The patched class of {@link net.minecraft.network.NetHandlerPlayServer} <br>
 * It controls incoming packets from the client. The patch is needed to allow <br>
 * noclipping and to replace variables in the chat.
 * 
 * @author MrNobody98
 *
 */
public class NetHandlerPlayServer extends net.minecraft.network.NetHandlerPlayServer {
	private static final Field FIELD_LASTPOSZ = ReflectionHelper.getField(ObfuscatedField.NetHandlerPlayServer_lastPosX);
	private static final Field FIELD_LASTPOSY = ReflectionHelper.getField(ObfuscatedField.NetHandlerPlayServer_lastPosY);
	private static final Field FIELD_LASTPOSX = ReflectionHelper.getField(ObfuscatedField.NetHandlerPlayServer_lastPosZ);
	private static final Field FIELD_HASMOVED = ReflectionHelper.getField(ObfuscatedField.NetHandlerPlayServer_hasMoved);
	private static final Field FIELD_147366_G = ReflectionHelper.getField(ObfuscatedField.NetHandlerPlayServer_field_147366_g);
	private static final Field FIELD_175090_F = ReflectionHelper.getField(ObfuscatedField.NetHandlerPlayServer_field_175090_f);
	private static final Field FIELD_NETWORKTICKCOUNT = ReflectionHelper.getField(ObfuscatedField.NetHandlerPlayServer_networkTickCount);
	private static final Field FIELD_FLOATINGTICKCOUNT = ReflectionHelper.getField(ObfuscatedField.NetHandlerPlayServer_floatingTickCount);
	
    private MinecraftServer mcServer;
    public final boolean enabled;
	private boolean overrideNoclip = false;
	
    public NetHandlerPlayServer(MinecraftServer par1, NetworkManager par2, net.minecraft.entity.player.EntityPlayerMP par3) {
        super(par1, par2, par3);
        this.mcServer = par1;
        this.enabled = FIELD_147366_G != null && FIELD_175090_F != null && FIELD_HASMOVED != null &&
    			FIELD_LASTPOSX != null && FIELD_LASTPOSY != null && FIELD_LASTPOSZ != null &&
    			FIELD_NETWORKTICKCOUNT != null && FIELD_FLOATINGTICKCOUNT != null;
    }
    
	public void setOverrideNoclip(boolean override) {
		this.overrideNoclip   = override;
	}
	
	public boolean getOverrideNoclip() {
		return this.overrideNoclip;
	}
	
    @Override
    public void processChatMessage(C01PacketChatMessage p_147354_1_) {
    	PacketThreadUtil.checkThreadAndEnqueue(p_147354_1_, this, this.playerEntity.getServerForPlayer());
    	String message = p_147354_1_.getMessage();
    	
    	ServerPlayerSettings settings = MoreCommands.getEntityProperties(ServerPlayerSettings.class, PlayerSettings.MORECOMMANDS_IDENTIFIER, this.playerEntity);
		Map<String, String> playerVars = settings == null ? new HashMap<String, String>() : settings.variables;
		boolean replaceIgnored;
		
		if (message.length() > 1 && message.charAt(0) == '%') {
			int end = message.indexOf('%', 1);
			String val = end > 0 ? playerVars.get(message.substring(1, end)) : null;
			
			replaceIgnored = val == null || !val.startsWith("/") ||
							(message.length() - 1 != end && message.charAt(end + 1) != ' ') ||
							!MinecraftServer.getServer().getCommandManager().getCommands().containsKey(val.substring(1));
		}
		else replaceIgnored = !message.startsWith("/") || !MinecraftServer.getServer().getCommandManager().getCommands().containsKey(message.substring(1).split(" ")[0]);
		
    	try {
    		String world = this.playerEntity.getEntityWorld().getSaveHandler().getWorldDirectoryName(), dim = this.playerEntity.getEntityWorld().provider.getDimensionName();
    		
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
    	
    	super.processChatMessage(new C01PacketChatMessage(message));
    }
    
    @Override
    public void processPlayer(C03PacketPlayer packet) {
    	PacketThreadUtil.checkThreadAndEnqueue(packet, this, this.playerEntity.getServerForPlayer());
        
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
				
				if (playerPos.getY() > 0) {
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
    
    public void handleNoclip(C03PacketPlayer packetIn)
    {
    	PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.playerEntity.getServerForPlayer());
        checkSafe(this, this.playerEntity);
    	
        WorldServer worldserver = this.mcServer.worldServerForDimension(this.playerEntity.dimension);
        setBoolean(FIELD_147366_G, true);

        if (!this.playerEntity.playerConqueredTheEnd)
        {
            double d0 = this.playerEntity.posX;
            double d1 = this.playerEntity.posY;
            double d2 = this.playerEntity.posZ;
            double d3 = 0.0D;
            double d4 = packetIn.getPositionX() - this.getDouble(this.FIELD_LASTPOSX);
            double d5 = packetIn.getPositionY() - this.getDouble(this.FIELD_LASTPOSY);
            double d6 = packetIn.getPositionZ() - this.getDouble(this.FIELD_LASTPOSZ);

            if (packetIn.isMoving())
            {
                d3 = d4 * d4 + d5 * d5 + d6 * d6;

                if (!this.getBoolean(this.FIELD_HASMOVED) && d3 < 0.25D)
                {
                    this.setBoolean(this.FIELD_HASMOVED, true);
                }
            }

            if (this.getBoolean(this.FIELD_HASMOVED))
            {
                setInt(FIELD_175090_F, getInt(FIELD_NETWORKTICKCOUNT));
                
                double d8;
                double d9;
                double d10;

                if (this.playerEntity.ridingEntity != null)
                {
                    float f4 = this.playerEntity.rotationYaw;
                    float f = this.playerEntity.rotationPitch;
                    this.playerEntity.ridingEntity.updateRiderPosition();
                    d8 = this.playerEntity.posX;
                    d9 = this.playerEntity.posY;
                    d10 = this.playerEntity.posZ;

                    if (packetIn.getRotating())
                    {
                        f4 = packetIn.getYaw();
                        f = packetIn.getPitch();
                    }

                    this.playerEntity.onGround = packetIn.isOnGround();
                    this.playerEntity.onUpdateEntity();
                    this.playerEntity.setPositionAndRotation(d8, d9, d10, f4, f);

                    if (this.playerEntity.ridingEntity != null)
                    {
                        this.playerEntity.ridingEntity.updateRiderPosition();
                    }

                    if (!this.getBoolean(this.FIELD_HASMOVED)) return;

                    this.mcServer.getConfigurationManager().serverUpdateMountedMovingPlayer(this.playerEntity);

                    if (this.playerEntity.ridingEntity != null)
                    {
                        if (d3 > 4.0D)
                        {
                            Entity entity = this.playerEntity.ridingEntity;
                            this.playerEntity.playerNetServerHandler.sendPacket(new S18PacketEntityTeleport(entity));
                            this.setPlayerLocation(this.playerEntity.posX, this.playerEntity.posY, this.playerEntity.posZ, this.playerEntity.rotationYaw, this.playerEntity.rotationPitch);
                        }

                        this.playerEntity.ridingEntity.isAirBorne = true;
                    }

                    if (this.getBoolean(this.FIELD_HASMOVED))
                    {
                        this.setDouble(this.FIELD_LASTPOSX, this.playerEntity.posX);
                        this.setDouble(this.FIELD_LASTPOSY, this.playerEntity.posY);
                        this.setDouble(this.FIELD_LASTPOSZ, this.playerEntity.posZ);
                    }

                    worldserver.updateEntity(this.playerEntity);
                    return;
                }

                if (this.playerEntity.isPlayerSleeping())
                {
                    this.playerEntity.onUpdateEntity();
                    this.playerEntity.setPositionAndRotation(this.getDouble(this.FIELD_LASTPOSX), this.getDouble(this.FIELD_LASTPOSY), this.getDouble(this.FIELD_LASTPOSZ), this.playerEntity.rotationYaw, this.playerEntity.rotationPitch);
                    worldserver.updateEntity(this.playerEntity);
                    return;
                }

                double d7 = this.playerEntity.posY;
                this.setDouble(this.FIELD_LASTPOSX, this.playerEntity.posX);
                this.setDouble(this.FIELD_LASTPOSY, this.playerEntity.posY);
                this.setDouble(this.FIELD_LASTPOSZ, this.playerEntity.posZ);
                d8 = this.playerEntity.posX;
                d9 = this.playerEntity.posY;
                d10 = this.playerEntity.posZ;
                float f1 = this.playerEntity.rotationYaw;
                float f2 = this.playerEntity.rotationPitch;

                if (packetIn.isMoving() && packetIn.getPositionY() == -999.0D)
                {
                    packetIn.setMoving(false);
                }

                if (packetIn.isMoving())
                {
                    d8 = packetIn.getPositionX();
                    d9 = packetIn.getPositionY();
                    d10 = packetIn.getPositionZ();

                    if (Math.abs(packetIn.getPositionX()) > 3.0E7D || Math.abs(packetIn.getPositionZ()) > 3.0E7D)
                    {
                        this.kickPlayerFromServer("Illegal position");
                        return;
                    }
                }

                if (packetIn.getRotating())
                {
                    f1 = packetIn.getYaw();
                    f2 = packetIn.getPitch();
                }

                this.playerEntity.onUpdateEntity();
                this.playerEntity.setPositionAndRotation(this.getDouble(this.FIELD_LASTPOSX), this.getDouble(this.FIELD_LASTPOSY), this.getDouble(this.FIELD_LASTPOSZ), f1, f2);

                if (!this.getBoolean(this.FIELD_HASMOVED))
                {
                    return;
                }

                double d11 = d8 - this.playerEntity.posX;
                double d12 = d9 - this.playerEntity.posY;
                double d13 = d10 - this.playerEntity.posZ;
                double d14 = Math.max(Math.abs(d11), Math.abs(this.playerEntity.motionX));
                double d15 = Math.max(Math.abs(d12), Math.abs(this.playerEntity.motionY));
                double d16 = Math.max(Math.abs(d13), Math.abs(this.playerEntity.motionZ));
                double d17 = d14 * d14 + d15 * d15 + d16 * d16;

                if (d17 > 100.0D && (!this.mcServer.isSinglePlayer() || !this.mcServer.getServerOwner().equals(this.playerEntity.getName())))
                {
                    this.mcServer.logWarning(this.playerEntity.getName() + " moved too quickly! " + d11 + "," + d12 + "," + d13 + " (" + d14 + ", " + d15 + ", " + d16 + ")");
                    this.setPlayerLocation(this.getDouble(this.FIELD_LASTPOSX), this.getDouble(this.FIELD_LASTPOSY), this.getDouble(this.FIELD_LASTPOSY), this.playerEntity.rotationYaw, this.playerEntity.rotationPitch);
                    return;
                }

                float f3 = 0.0625F;
                boolean flag = worldserver.getCollidingBoundingBoxes(this.playerEntity, this.playerEntity.getEntityBoundingBox().contract((double)f3, (double)f3, (double)f3)).isEmpty();

                if (this.playerEntity.onGround && !packetIn.isOnGround() && d12 > 0.0D)
                {
                    this.playerEntity.jump();
                }

                if (!this.getBoolean(FIELD_HASMOVED)) return;

                this.playerEntity.moveEntity(d11, d12, d13);
                this.playerEntity.onGround = packetIn.isOnGround();
                double d18 = d12;
                d11 = d8 - this.playerEntity.posX;
                d12 = d9 - this.playerEntity.posY;

                if (d12 > -0.5D || d12 < 0.5D)
                {
                    d12 = 0.0D;
                }

                d13 = d10 - this.playerEntity.posZ;
                d17 = d11 * d11 + d12 * d12 + d13 * d13;
                boolean flag1 = false;

              //BYPASSES MOVED WORNGLY WARNING
                /*if (d17 > 0.0625D && !this.playerEntity.isPlayerSleeping() && !this.playerEntity.theItemInWorldManager.isCreative())
                {
                    flag1 = true;
                    this.mcServer.logWarning(this.playerEntity.getName() + " moved wrongly!");
                }*/

                if (!this.getBoolean(this.FIELD_HASMOVED)) return;

                this.playerEntity.setPositionAndRotation(d8, d9, d10, f1, f2);
                this.playerEntity.addMovementStat(this.playerEntity.posX - d0, this.playerEntity.posY - d1, this.playerEntity.posZ - d2);

                //BYPASSES NOCLIP CHECK
                /*if (!this.playerEntity.noClip)
                {
                    boolean flag2 = worldserver.getCollidingBoundingBoxes(this.playerEntity, this.playerEntity.getEntityBoundingBox().contract((double)f3, (double)f3, (double)f3)).isEmpty();

                    if (flag && (flag1 || !flag2) && !this.playerEntity.isPlayerSleeping() && !this.playerEntity.noClip)
                    {
                        this.setPlayerLocation(this.getDouble(this.FIELD_LASTPOSX), this.getDouble(this.FIELD_LASTPOSY), this.getDouble(this.FIELD_LASTPOSZ), f1, f2);
                        return;
                    }
                }*/

                AxisAlignedBB axisalignedbb = this.playerEntity.getEntityBoundingBox().expand((double)f3, (double)f3, (double)f3).addCoord(0.0D, -0.55D, 0.0D);

                if (!this.mcServer.isFlightAllowed() && !this.playerEntity.capabilities.allowFlying && !worldserver.checkBlockCollision(axisalignedbb))
                {
                    if (d18 >= -0.03125D)
                    {
                    	setInt(FIELD_FLOATINGTICKCOUNT, getInt(FIELD_FLOATINGTICKCOUNT) + 1);

                        if (getInt(FIELD_FLOATINGTICKCOUNT) > 80)
                        {
                            this.mcServer.logWarning(this.playerEntity.getName() + " was kicked for floating too long!");
                            this.kickPlayerFromServer("Flying is not enabled on this server");
                            return;
                        }
                    }
                }
                else
                {
                	setInt(FIELD_FLOATINGTICKCOUNT, 0);
                }

                if (!this.getBoolean(FIELD_HASMOVED)) return;

                this.playerEntity.onGround = packetIn.isOnGround();
                this.mcServer.getConfigurationManager().serverUpdateMountedMovingPlayer(this.playerEntity);
                this.playerEntity.handleFalling(this.playerEntity.posY - d7, packetIn.isOnGround());
            }
        	else if (getInt(FIELD_NETWORKTICKCOUNT) - getInt(FIELD_175090_F) > 20)
            {
                this.setPlayerLocation(this.getDouble(this.FIELD_LASTPOSZ), this.getDouble(this.FIELD_LASTPOSY), this.getDouble(this.FIELD_LASTPOSZ), this.playerEntity.rotationYaw, this.playerEntity.rotationPitch);
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
