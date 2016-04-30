package com.mrnobody.morecommands.asm.transform;

import java.util.ListIterator;
import java.util.Set;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import com.google.common.collect.ImmutableSet;
import com.mrnobody.morecommands.asm.ASMNames;
import com.mrnobody.morecommands.asm.NodeTransformer;

import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

/**
 * Transforms {@link net.minecraft.client.renderer.texture.TextureCompass} so that its 
 * {@link net.minecraft.client.renderer.texture.TextureCompass#updateCompass(World, double, double, double, boolean, boolean)}
 * does not use the spawn point as compass target but the target that the method 
 * {@link TransformTextureCompass#getCompassTarget(World)} returns.
 * 
 * @author MrNobody98
 */
public class TransformTextureCompass extends NodeTransformer {
	/**
	 * A callback for retrieving the compass target
	 * 
	 * @author MrNobody98
	 */
	public static interface CompassTargetCallBack {
		/**
		 * Gets the compass target coordinates
		 * 
		 * @param world the world the player using the compass is in
		 * @return the {@link ChunkCoordinates} to which the compass should point to
		 */
		BlockPos getTarget(World world);
	}
	
	private static CompassTargetCallBack callback = null;
	
	/**
	 * Gets the compass target from the {@link CompassTargetCallBack} that was set via
	 * {@link TransformTextureCompass#setCompassTargetCallback(CompassTargetCallBack)}.
	 * If it was not set, returns the spawn point of the world
	 * 
	 * @param world the world the player using the compass is in
	 * @return the target coordinates to which the compass should point to
	 * @see CompassTargetCallBack
	 */
	public static BlockPos getCompassTarget(World world) {
		return callback == null ? world.getSpawnPoint() : callback.getTarget(world);
	}
	
	/**
	 * Sets the current {@link CompassTargetCallBack} that is used by {@link TransformTextureCompass#getCompassTarget(World)} 
	 * to retrieve the compass target coordinates
	 * 
	 * @param callback the {@link CompassTargetCallBack} that is used to retrieve the compass target coordinates
	 */
	public static void setCompassTargetCallback(CompassTargetCallBack callback) {
		TransformTextureCompass.callback = callback;
	}
	
	private static final ASMNames.Method updateCompass = ASMNames.Method.TextureCompass_updateCompass;
	private static final ASMNames.Method getSpawn = ASMNames.Method.World_getSpawnPoint;
	private static final ASMNames.Method getTarget = ASMNames.Method.TransformTextureCompass_getCompassTarget;
	
	private final ImmutableSet<String> transformClasses = ImmutableSet.of(ASMNames.Type.TextureCompass.getName());
	
	@Override
	public Set<String> getTransformClassNames() {
		return transformClasses;
	}

	@Override
	public void beforeTransform() {}

	@Override
	public void afterTransform() {}

	@Override
	public void transform(String className, ClassNode node) {
		for (MethodNode method : node.methods) {
			if (method.name.equals(updateCompass.getEnvName()) && method.desc.equals(updateCompass.getDesc())) {
				for (ListIterator<AbstractInsnNode> iterator = method.instructions.iterator(); iterator.hasNext();) {
					AbstractInsnNode insn = iterator.next();
					
					if (insn.getOpcode() == Opcodes.INVOKEVIRTUAL && ((MethodInsnNode) insn).name.equals(getSpawn.getEnvName()) && ((MethodInsnNode) insn).desc.equals(getSpawn.getDesc())) {
						method.instructions.insert(insn, new MethodInsnNode(Opcodes.INVOKESTATIC, getTarget.getOwnerInternalName(), getTarget.getEnvName(), getTarget.getDesc(), false));
						method.instructions.remove(insn);
						break;
					}
				}
				
				break;
			}
		}
	}
}
