package com.mrnobody.morecommands.asm.transform;

import java.util.Set;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import com.google.common.collect.ImmutableSet;
import com.mrnobody.morecommands.asm.ASMNames;
import com.mrnobody.morecommands.asm.WriterTransformer;

/**
 * Transforms {@link net.minecraft.block.BlockRailBase} so that its 
 * {@link net.minecraft.block.BlockRailBase#getRailMaxSpeed(net.minecraft.world.World, net.minecraft.entity.item.EntityMinecart, int, int, int)}
 * method does not return a constant value but fetches it from a field named "maxRailSpeed" which is private and gets
 * generated with this transformer too. Additionally generates a public setter for this field.
 * 
 * @author MrNobody98
 */
public class TransformBlockRailBase extends WriterTransformer {
	private static final ASMNames.Type BLOCK_RAIL_BASE = ASMNames.Type.BlockRailBase;
	private static final ASMNames.Method GET_MAX_SPEED = ASMNames.Method.BlockRailBase_getRailMaxSpeed;
	
	private static final String F_MAXRAILSPEED_NAME = "maxRailSpeed";
	private static final String F_MAXRAILSPEED_DESC = "F";
	private static final float MAXRAILSPEED_INITIAL_VALUE = 0.4F;
	
	private static final String M_SETMAXRAILSPEED_NAME = "setMaxRailSpeed";
	private static final String M_SETMAXRAILSPEED_DESC = "(F)V";
	
	private final ImmutableSet<String> transformClasses = ImmutableSet.of(ASMNames.Type.BlockRailBase.getName());
	
	@Override
	public void setCurrentClassName(String name) {}
	
	@Override
	public Set<String> getTransformClassNames() {
		return transformClasses;
	}

	@Override
	public void beforeTransform() {}
	
	@Override
	public void afterTransform() {}
	
	@Override
	public void visitEnd() {
		this.cv.visitField(Opcodes.ACC_PRIVATE + Opcodes.ACC_STATIC, F_MAXRAILSPEED_NAME, F_MAXRAILSPEED_DESC, null, MAXRAILSPEED_INITIAL_VALUE).visitEnd();
		
		MethodVisitor mv = this.cv.visitMethod(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, M_SETMAXRAILSPEED_NAME, M_SETMAXRAILSPEED_DESC, null, null);
		
		mv.visitCode();
		mv.visitVarInsn(Opcodes.FLOAD, 0);
		mv.visitFieldInsn(Opcodes.PUTSTATIC, BLOCK_RAIL_BASE.getInternalName(), F_MAXRAILSPEED_NAME, F_MAXRAILSPEED_DESC);
		mv.visitInsn(Opcodes.RETURN);
		mv.visitMaxs(1, 1);
		mv.visitEnd();
	}
	
	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		if (name.equals(GET_MAX_SPEED.getEnvName()) && desc.equals(GET_MAX_SPEED.getDesc())) {
			MethodVisitor mv = this.cv.visitMethod(access, name, desc, signature, exceptions);
			
			mv.visitCode();
			mv.visitFieldInsn(Opcodes.GETSTATIC, BLOCK_RAIL_BASE.getInternalName(), F_MAXRAILSPEED_NAME, F_MAXRAILSPEED_DESC);
			mv.visitInsn(Opcodes.FRETURN);
			mv.visitMaxs(1, 6);
			mv.visitEnd();
			
			return null;
		}
		else return this.cv.visitMethod(access, name, desc, signature, exceptions);
	}
	
	
}
