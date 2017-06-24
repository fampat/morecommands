package com.mrnobody.morecommands.patch;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Sets;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Floats;
import com.mrnobody.morecommands.command.CommandSender;
import com.mrnobody.morecommands.core.MoreCommands;
import com.mrnobody.morecommands.patch.PatchManager.AppliedPatches;
import com.mrnobody.morecommands.settings.GlobalSettings;
import com.mrnobody.morecommands.settings.MoreCommandsConfig;
import com.mrnobody.morecommands.settings.PlayerSettings;
import com.mrnobody.morecommands.settings.ServerPlayerSettings;
import com.mrnobody.morecommands.util.EntityUtils;
import com.mrnobody.morecommands.util.ObfuscatedNames.ObfuscatedField;
import com.mrnobody.morecommands.util.ReflectionHelper;
import com.mrnobody.morecommands.util.Variables;
import com.mrnobody.morecommands.util.WorldUtils;

import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketThreadUtil;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * This patch replaces {@link net.minecraft.network.NetHandlerPlayServer} instances with a modified version<br>
 * It handles incoming packets from the client. The patch is needed to allow <br>
 * noclipping and to replace variables in the chat.
 * 
 * @author MrNobody98
 *
 */
public class PatchNetHandlerPlayServer implements PatchManager.ForgeEventBasedPatch {
	private String displayName;
	
	PatchNetHandlerPlayServer(String displayName) {
		this.displayName = displayName;
	}
	
	@Override
	public Collection<Class<? extends Event>> forgeEventClasses() {
		return Sets.<Class<? extends Event>>newHashSet(EntityJoinWorldEvent.class);
	}
	
	@Override
	public <T extends Event> boolean applyForgeEventPatch(T e) {
		EntityJoinWorldEvent event = (EntityJoinWorldEvent) e;
		
		EntityPlayerMP player = (EntityPlayerMP) event.getEntity();
		AppliedPatches patches = PatchManager.instance().getAppliedPatchesForPlayer(player);
		
		if (player.connection.player == event.getEntity() && !(player.connection instanceof NetHandlerPlayServer)) {
			net.minecraft.network.NetHandlerPlayServer handler = player.connection;
			player.connection = new NetHandlerPlayServer(player.getServer(), handler.netManager, handler.player);
			
			if (patches != null) 
				patches.setPatchSuccessfullyApplied(this.displayName, true);
		}
		
		return true;
	}

	@Override
	public <T extends Event> boolean needsToBeApplied(T event) {
		return ((EntityJoinWorldEvent) event).getEntity() instanceof EntityPlayerMP;
	}
	
	@Override
	public <T extends Event> boolean printLogFor(T event) {
		return true;
	}

	@Override
	public String getDisplayName() {
		return this.displayName;
	}

	@Override
	public String getFailureConsequences() {
		return "Noclip disabled, variable replacement disabled server side";
	}
	
	public static class NetHandlerPlayServer extends net.minecraft.network.NetHandlerPlayServer {
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

	    NetHandlerPlayServer(MinecraftServer mcServer, NetworkManager netManager, net.minecraft.entity.player.EntityPlayerMP player) {
	        super(mcServer, netManager, player);
	        this.mcServer = mcServer;
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
	    public void processChatMessage(CPacketChatMessage p_147354_1_) {
	    	PacketThreadUtil.checkThreadAndEnqueue(p_147354_1_, this, this.player.getServerWorld());
	    	String message = p_147354_1_.getMessage();
			
	    	ServerPlayerSettings settings = this.player.getCapability(PlayerSettings.SETTINGS_CAP_SERVER, null);
			Map<String, String> playerVars = settings == null ? new HashMap<String, String>() : settings.variables;
			boolean replaceIgnored;
			
			if (message.length() > 1 && message.charAt(0) == '%') {
				int end = message.indexOf('%', 1);
				String val = end > 0 ? playerVars.get(message.substring(1, end)) : null;
				
				replaceIgnored = val == null || !val.startsWith("/") ||
								(message.length() - 1 != end && message.charAt(end + 1) != ' ') ||
								!this.player.getServer().getCommandManager().getCommands().containsKey(val.substring(1));
			}
			else replaceIgnored = !message.startsWith("/") || !this.player.getServer().getCommandManager().getCommands().containsKey(message.substring(1).split(" ")[0]);
			
	    	try {
	    		String world = this.player.getEntityWorld().getSaveHandler().getWorldDirectory().getName(), dim = this.player.getEntityWorld().provider.getDimensionType().getName();
	    		
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
	    	
	    	super.processChatMessage(new CPacketChatMessage(message));
	    }
	    
	    @Override
	    public void processPlayer(CPacketPlayer packet) {
	    	PacketThreadUtil.checkThreadAndEnqueue(packet, this, this.player.getServerWorld());
	    	
	        if (this.enabled && this.overrideNoclip) {
	        	handleNoclip(packet);
	        } else {
	            super.processPlayer(packet);
	        }
	    }
	    
	    private static void checkSafe(NetHandlerPlayServer handler, EntityPlayerMP player) {
			if (handler.getOverrideNoclip() && !player.capabilities.isFlying) {
				handler.setOverrideNoclip(false);
				MoreCommands.INSTANCE.getPacketDispatcher().sendS06Noclip(player, false);
				
				(new CommandSender(player)).sendLangfileMessage("command.noclip.autodisable");
				ascendPlayer(player);
			}
		}

		private static boolean ascendPlayer(EntityPlayerMP player) {
			BlockPos playerPos = player.getPosition();
			if (WorldUtils.isClearBelow(player.world, playerPos) && playerPos.getY() > 0) {
				return false;
			}
			double y = playerPos.getY() - 1;
			while (y < 260) {
				if (WorldUtils.isClear(player.world, new BlockPos(playerPos.getX(), y++, playerPos.getZ()))) {
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
			PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.player.getServer());
			checkSafe(this, this.player);

	        if (isMovePlayerPacketInvalid(packetIn))
	        {
	            this.disconnect(new TextComponentTranslation("multiplayer.disconnect.invalid_player_movement"));
	        }
	        else
	        {
	            WorldServer worldserver = this.mcServer.getWorld(this.player.dimension);

	            if (!this.player.queuedEndExit)
	            {
	                if (getInt(networkTickCount) == 0)
	                {
	                	this.captureCurrentPosition();
	                }

	                if (getObject(targetPos) != null)
	                {
	                    if (getInt(networkTickCount) - getInt(lastPositionUpdate) > 20)
	                    {
	                        setInt(lastPositionUpdate, getInt(networkTickCount));
	                        this.setPlayerLocation(((Vec3d) getObject(targetPos)).x, ((Vec3d) getObject(targetPos)).y, ((Vec3d) getObject(targetPos)).z, this.player.rotationYaw, this.player.rotationPitch);
	                    }
	                }
	                else
	                {
	                	setInt(lastPositionUpdate, getInt(networkTickCount));

	                    if (this.player.isRiding())
	                    {
	                        this.player.setPositionAndRotation(this.player.posX, this.player.posY, this.player.posZ, packetIn.getYaw(this.player.rotationYaw), packetIn.getPitch(this.player.rotationPitch));
	                        this.mcServer.getPlayerList().serverUpdateMovingPlayer(this.player);
	                    }
	                    else
	                    {
	                        double d0 = this.player.posX;
	                        double d1 = this.player.posY;
	                        double d2 = this.player.posZ;
	                        double d3 = this.player.posY;
	                        double d4 = packetIn.getX(this.player.posX);
	                        double d5 = packetIn.getY(this.player.posY);
	                        double d6 = packetIn.getZ(this.player.posZ);
	                        float f = packetIn.getYaw(this.player.rotationYaw);
	                        float f1 = packetIn.getPitch(this.player.rotationPitch);
	                        double d7 = d4 - getDouble(firstGoodX);
	                        double d8 = d5 - getDouble(firstGoodY);
	                        double d9 = d6 - getDouble(firstGoodZ);
	                        double d10 = this.player.motionX * this.player.motionX + this.player.motionY * this.player.motionY + this.player.motionZ * this.player.motionZ;
	                        double d11 = d7 * d7 + d8 * d8 + d9 * d9;
	                        
	                        if (this.player.isPlayerSleeping())
	                        {
	                            if (d11 > 1.0D)
	                            {
	                                this.setPlayerLocation(this.player.posX, this.player.posY, this.player.posZ, packetIn.getYaw(this.player.rotationYaw), packetIn.getPitch(this.player.rotationPitch));
	                            }
	                        }
	                        else {
	                            setInt(movePacketCounter, getInt(movePacketCounter) + 1);
	                            int i = getInt(movePacketCounter) - getInt(lastMovePacketCounter);
	                            
	                            if (i > 5)
	                            {
	                            	logger.debug("{} is sending move packets too frequently ({} packets since last tick)", this.player.getName(), i);
	                                i = 1;
	                            }

	                            if (!this.player.isInvulnerableDimensionChange() && (!this.player.world.getGameRules().getBoolean("disableElytraMovementCheck") || !this.player.isElytraFlying()))
	                            {
	                                float f2 = this.player.isElytraFlying() ? 300.0F : 100.0F;

	                                if (d11 - d10 > (double)(f2 * (float)i) && (!this.mcServer.isSinglePlayer() || !this.mcServer.getServerOwner().equals(this.player.getName())))
	                                {
	                                    logger.warn("{} moved too quickly! {},{},{}", this.player.getName(), d7, d8, d9);
	                                    this.setPlayerLocation(this.player.posX, this.player.posY, this.player.posZ, this.player.rotationYaw, this.player.rotationPitch);
	                                    return;
	                                }
	                            }
	                            
	                            boolean flag2 = worldserver.getCollisionBoxes(this.player, this.player.getEntityBoundingBox().shrink(0.0625D)).isEmpty();
	                            d7 = d4 - getDouble(lastGoodX);
	                            d8 = d5 - getDouble(lastGoodY);
	                            d9 = d6 - getDouble(lastGoodZ);

	                            if (this.player.onGround && !packetIn.isOnGround() && d8 > 0.0D)
	                            {
	                                this.player.jump();
	                            }

	                            this.player.move(MoverType.PLAYER, d7, d8, d9);
	                            this.player.onGround = packetIn.isOnGround();
	                            double d12 = d8;
	                            d7 = d4 - this.player.posX;
	                            d8 = d5 - this.player.posY;

	                            if (d8 > -0.5D || d8 < 0.5D)
	                            {
	                                d8 = 0.0D;
	                            }

	                            d9 = d6 - this.player.posZ;
	                            d11 = d7 * d7 + d8 * d8 + d9 * d9;
	                            boolean flag = false;
	                            
	                            //BYPASSES MOVED WORNGLY WARNING
	                            /*if (!this.player.isInvulnerableDimensionChange() && d11 > 0.0625D && !this.player.isPlayerSleeping() && !this.player.interactionManager.isCreative() && this.player.interactionManager.getGameType() != WorldSettings.GameType.SPECTATOR)
	                            {
	                                flag = true;
	                                logger.warn("{} moved wrongly!", this.player.getName());
	                            }*/

	                            this.player.setPositionAndRotation(d4, d5, d6, f, f1);
	                            this.player.addMovementStat(this.player.posX - d0, this.player.posY - d1, this.player.posZ - d2);
	                            
	                            //BYPASSES NOCLIP CHECK
	                            /*if (!this.player.noClip && !this.player.isPlayerSleeping())
	                            {
	                                boolean flag1 = worldserver.getCubes(this.player, this.player.getEntityBoundingBox().func_186664_h(0.0625D)).isEmpty();

	                                if (flag2 && (flag || !flag1))
	                                {
	                                    this.setPlayerLocation(d0, d1, d2, f, f1);
	                                    return;
	                                }
	                            }*/
	                            
	                            boolean val = d12 >= -0.03125D;
	                            val &= !this.mcServer.isFlightAllowed() && !this.player.capabilities.allowFlying;
	                            val &= !this.player.isPotionActive(MobEffects.LEVITATION) && !this.player.isElytraFlying() && !worldserver.checkBlockCollision(this.player.getEntityBoundingBox().grow(0.0625D).expand(0.0D, -0.55D, 0.0D));
	                            setBoolean(floating, val);
	                            this.player.onGround = packetIn.isOnGround();
	                            this.mcServer.getPlayerList().serverUpdateMovingPlayer(this.player);
	                            this.player.handleFalling(this.player.posY - d3, packetIn.isOnGround());
	                            setDouble(lastGoodX, this.player.posX);
	                            setDouble(lastGoodY, this.player.posY);
	                            setDouble(lastGoodZ, this.player.posZ);
	                        }
	                    }
	                }
	            }
	        }
	    }
		
	    private void captureCurrentPosition() {
			setDouble(firstGoodX, this.player.posX);
			setDouble(firstGoodY, this.player.posY);
			setDouble(firstGoodZ, this.player.posZ);
			setDouble(lastGoodX, this.player.posX);
			setDouble(lastGoodY, this.player.posY);
			setDouble(lastGoodZ, this.player.posX);
	    }
		
	    private static boolean isMovePlayerPacketInvalid(CPacketPlayer packetIn) {
	        return Doubles.isFinite(packetIn.getX(0.0D)) && Doubles.isFinite(packetIn.getY(0.0D)) && Doubles.isFinite(packetIn.getZ(0.0D)) && Floats.isFinite(packetIn.getPitch(0.0F)) && Floats.isFinite(packetIn.getYaw(0.0F)) ? false : Math.abs(packetIn.getX(0.0D)) <= 3.0E7D && Math.abs(packetIn.getX(0.0D)) <= 3.0E7D;
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
}
