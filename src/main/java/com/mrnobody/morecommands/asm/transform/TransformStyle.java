package com.mrnobody.morecommands.asm.transform;

import java.util.Set;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import com.google.common.collect.ImmutableSet;
import com.mrnobody.morecommands.asm.ASMNames;
import com.mrnobody.morecommands.asm.WriterTransformer;

/**
 * Transforms {@link net.minecraft.util.text.Style} so that its 
 * constructor sets the "parentStyle" field to the value of the static "defaultStyle" field which will also be generated
 * by this transformer. This is required for the chatstyle_global command.
 * 
 * @author MrNobody98
 */
public class TransformStyle extends WriterTransformer {
	private static final ASMNames.Type STYLE = ASMNames.Type.Style;
	private static final ASMNames.Method STYLE_INIT = ASMNames.Method.Style_init;
	private static final ASMNames.Field STYLE_PARENTSTYLE = ASMNames.Field.Style_parentStyle;
	
	private static final String DEFAULT_STYLE = "defaultStyle";
	private static final ASMNames.Method OBJECT_INIT = ASMNames.Method.Object_init;
	
	private final ImmutableSet<String> transformClasses = ImmutableSet.of(STYLE.getName());
	
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
		this.cv.visitField(Opcodes.ACC_PRIVATE + Opcodes.ACC_STATIC, DEFAULT_STYLE, STYLE.getDesc(), null, null);
	}
	
	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		if (name.equals(STYLE_INIT.getEnvName()) && desc.equals(STYLE_INIT.getDesc())) {
			MethodVisitor mv = this.cv.visitMethod(access, name, desc, signature, exceptions);
			
			mv.visitCode();
			
			mv.visitVarInsn(Opcodes.ALOAD, 0);
			mv.visitMethodInsn(Opcodes.INVOKESPECIAL, OBJECT_INIT.getOwnerInternalName(), OBJECT_INIT.getEnvName(), OBJECT_INIT.getDesc(), false);
			
			mv.visitVarInsn(Opcodes.ALOAD, 0);
			mv.visitFieldInsn(Opcodes.GETSTATIC, STYLE.getInternalName(), DEFAULT_STYLE, STYLE.getDesc());
			mv.visitFieldInsn(Opcodes.PUTFIELD, STYLE_PARENTSTYLE.getOwnerInternalName(), STYLE_PARENTSTYLE.getEnvName(), STYLE_PARENTSTYLE.getDesc());
			
			mv.visitInsn(Opcodes.RETURN);
			mv.visitMaxs(2, 1);
			
			mv.visitEnd();
			
			return null;
		}
		else return this.cv.visitMethod(access, name, desc, signature, exceptions);
	}
}
