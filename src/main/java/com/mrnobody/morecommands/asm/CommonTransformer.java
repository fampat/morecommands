package com.mrnobody.morecommands.asm;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import net.minecraft.launchwrapper.IClassTransformer;

public abstract class CommonTransformer extends ClassVisitor implements IClassTransformer {
	public CommonTransformer() {
		super(Opcodes.ASM5, new ClassWriter(0));
	}

	@Override
	public final byte[] transform(String name, String transformedName, byte[] basicClass) {
		if (!this.getTransformClassName().equals(transformedName)) return basicClass;
		
		MoreCommandsLoadingPlugin.logger.info("Transforming class " + transformedName);
		
		this.beforeTransform();
		
		ClassReader cr = new ClassReader(basicClass);
		cr.accept(this, 0);
		
		this.afterTransform();
		
		return ((ClassWriter) this.cv).toByteArray();
	}
	
	public abstract String getTransformClassName();
	public abstract void beforeTransform();
	public abstract void afterTransform();
}
