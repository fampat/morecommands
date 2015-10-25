package com.mrnobody.morecommands.asm.transform;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import com.mrnobody.morecommands.asm.CommonTransformer;

public class TransformBlockRailBase extends CommonTransformer {
	@Override
	public String getTransformClassName() {
		return "net.minecraft.block.BlockRailBase";
	}

	@Override
	public void beforeTransform() {}
	
	@Override
	public void afterTransform() {}
	
	@Override
	public void visitEnd() {
		this.cv.visitField(Opcodes.ACC_PRIVATE + Opcodes.ACC_STATIC, "maxRailSpeed", "F", null, 0.4F).visitEnd();
		
		MethodVisitor mv = this.cv.visitMethod(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, "setMaxRailSpeed", "(F)V", null, null);
		
		mv.visitCode();
		mv.visitVarInsn(Opcodes.FLOAD, 0);
		mv.visitFieldInsn(Opcodes.PUTSTATIC, "net/minecraft/block/BlockRailBase", "maxRailSpeed", "F");
		mv.visitInsn(Opcodes.RETURN);
		mv.visitMaxs(1, 1);
		mv.visitEnd();
	}
	
	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		if (name.equals("getRailMaxSpeed") && desc.equals("(Lnet/minecraft/world/World;Lnet/minecraft/entity/item/EntityMinecart;Lnet/minecraft/util/BlockPos;)F")) {
			MethodVisitor mv = this.cv.visitMethod(access, name, desc, signature, exceptions);
			
			mv.visitCode();
			mv.visitFieldInsn(Opcodes.GETSTATIC, "net/minecraft/block/BlockRailBase", "maxRailSpeed", "F");
			mv.visitInsn(Opcodes.FRETURN);
			mv.visitMaxs(1, 6);
			mv.visitEnd();
			
			return null;
		}
		else return this.cv.visitMethod(access, name, desc, signature, exceptions);
	}
}
