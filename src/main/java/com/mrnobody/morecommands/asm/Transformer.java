package com.mrnobody.morecommands.asm;

import java.util.Set;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import net.minecraft.launchwrapper.IClassTransformer;

public interface Transformer extends IClassTransformer {
	/**
	 * @return The set of class names this class transformers transforms
	 */
	public Set<String> getTransformClassNames();
	
	/**
	 * This should be invoked before each class transformation
	 */
	public void beforeTransform();
	
	/**
	 * This should be invoked after each class transformation
	 */
	public void afterTransform();
	
	/**
	 * @return the flags that should be handed over to {@link ClassReader#accept(lassVisitor, int)}
	 */
	public int getReadFlags();
	
	/**
	 * @return the flags that should be handed over to {@link ClassWriter#ClassWriter(int)}
	 */
	public int getWriteFlags();
}
