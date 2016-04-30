package com.mrnobody.morecommands.asm;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

/**
 * The abstract {@link Transformer} for transformations using the tree based
 * api of the objectweb ASM library
 * 
 * @author MrNobody98
 */
public abstract class NodeTransformer implements Transformer {
	/**
	 * {@inheritDoc}
	 */
	@Override
	public final byte[] transform(String name, String transformedName, byte[] basicClass) {
		if (!this.getTransformClassNames().contains(transformedName)) return basicClass;
		
		MoreCommandsLoadingPlugin.logger.info("Transforming class " + transformedName);
		
		this.beforeTransform();
		
		ClassReader cr = new ClassReader(basicClass);
		ClassNode node = new ClassNode(Opcodes.ASM5);
		cr.accept(node, this.getReadFlags());
		
		this.transform(transformedName, node);
		
		ClassWriter cw = new ClassWriter(this.getWriteFlags());
		node.accept(cw);
		
		this.afterTransform();
		
		return cw.toByteArray();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getReadFlags() {
		return 0;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getWriteFlags() {
		return 0;
	}
	
	/**
	 * Transforms a class using a {@link ClassNode}
	 * 
	 * @param className The name of the class being transformed
	 * @param node the class node representing the class
	 */
	public abstract void transform(String className, ClassNode node);
}
