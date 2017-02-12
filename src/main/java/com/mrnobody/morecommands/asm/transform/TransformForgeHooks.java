package com.mrnobody.morecommands.asm.transform;

import java.util.ListIterator;
import java.util.Set;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import com.google.common.collect.ImmutableSet;
import com.mrnobody.morecommands.asm.ASMNames;
import com.mrnobody.morecommands.asm.NodeTransformer;

/**
 * Transforms {@link net.minecraftforge.common.ForgeHooks} so that its 
 * {@link net.minecraftforge.common.ForgeHooks#onPlaceItemIntoWorld(net.minecraft.item.ItemStack, net.minecraft.entity.player.EntityPlayer, net.minecraft.world.World, int, int, int, int, float, float, float)}
 *  method fires an {@link com.mrnobody.morecommands.event.ItemStackChangeSizeEvent}
 *  after the ItemStack's stack size is modified so that the stack size can be manipulated
 * 
 * @author MrNobody98
 */
public class TransformForgeHooks extends NodeTransformer {
	private static final ASMNames.Method PLACE_ITEM = ASMNames.Method.ForgeHooks_onPlaceItemIntoWorld;
	private static final ASMNames.Method CHANGE_SIZE_EVENT_INIT = ASMNames.Method.ItemStackChangeSizeEvent_init;
	private static final ASMNames.Method POST = ASMNames.Method.EventHandler_post;
	
	private static final ASMNames.Method func_190920_e = ASMNames.Method.ItemStack_func_190920_e;
	private static final ASMNames.Field EVENTHANDLER_CHANGE_SIZE = ASMNames.Field.EventHandler_ITEMSTACK_CHANGE_SIZE;
	private static final ASMNames.Field NEW_SIZE = ASMNames.Field.ItemStackChangeSizeEvent_newSize;
	
	private static final ASMNames.Type CHANGE_SIZE_EVENT = ASMNames.Type.ItemStackChangeSizeEvent;
	
	private final ImmutableSet<String> transformClasses = ImmutableSet.of(ASMNames.Type.ForgeHooks.getName());
	
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
			if (method.name.equals(PLACE_ITEM.getEnvName()) && method.desc.equals(PLACE_ITEM.getDesc())) {
				boolean inserted = false;
				
				for (ListIterator<AbstractInsnNode> iterator = method.instructions.iterator(); iterator.hasNext();) {
					AbstractInsnNode insn = iterator.next();
					
					if (insn.getNext() instanceof VarInsnNode && insn.getNext().getNext() instanceof VarInsnNode && insn.getNext().getNext().getNext() instanceof MethodInsnNode) {
						VarInsnNode next1 = (VarInsnNode) insn.getNext();
						VarInsnNode next2 = (VarInsnNode) next1.getNext();
						MethodInsnNode next3 = (MethodInsnNode) next2.getNext();
						
						if (next1.getOpcode() == Opcodes.ALOAD && next1.var == 0 && 
							next2.getOpcode() == Opcodes.ILOAD && next2.var == 14 &&
							next3.getOpcode() == Opcodes.INVOKEVIRTUAL && next3.owner.equals(func_190920_e.getOwnerInternalName()) && 
							next3.name.equals(func_190920_e.getEnvName()) && next3.desc.equals(func_190920_e.getDesc())) {
							
							int varIndex = method.maxLocals; method.maxLocals++;
							InsnList postEvent = new InsnList();
							LabelNode label = new LabelNode(new Label());
							
							postEvent.add(new TypeInsnNode(Opcodes.NEW, CHANGE_SIZE_EVENT.getInternalName()));
							postEvent.add(new InsnNode(Opcodes.DUP));
							postEvent.add(new VarInsnNode(Opcodes.ALOAD, 1));
							postEvent.add(new VarInsnNode(Opcodes.ALOAD, 0));
							postEvent.add(new VarInsnNode(Opcodes.ILOAD, 10));
							postEvent.add(new VarInsnNode(Opcodes.ILOAD, 14));
							postEvent.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, CHANGE_SIZE_EVENT_INIT.getOwnerInternalName(), CHANGE_SIZE_EVENT_INIT.getEnvName(), CHANGE_SIZE_EVENT_INIT.getDesc(), false));
							postEvent.add(new VarInsnNode(Opcodes.ASTORE, varIndex));
							postEvent.add(new FieldInsnNode(Opcodes.GETSTATIC, EVENTHANDLER_CHANGE_SIZE.getOwnerInternalName(), EVENTHANDLER_CHANGE_SIZE.getEnvName(), EVENTHANDLER_CHANGE_SIZE.getDesc()));
							postEvent.add(new VarInsnNode(Opcodes.ALOAD, varIndex));
							postEvent.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, POST.getOwnerInternalName(), POST.getEnvName(), POST.getDesc(), false));
							postEvent.add(new JumpInsnNode(Opcodes.IFEQ, label));
							postEvent.add(new VarInsnNode(Opcodes.ALOAD, varIndex));
							postEvent.add(new FieldInsnNode(Opcodes.GETFIELD, NEW_SIZE.getOwnerInternalName(), NEW_SIZE.getEnvName(), NEW_SIZE.getDesc()));
							postEvent.add(new VarInsnNode(Opcodes.ISTORE, 14));
							postEvent.add(label);
							
							method.instructions.insert(insn, postEvent);
							
							inserted = true; break;
						}
					}
				}
				
				if (inserted) break;
			}
		}
	}
}