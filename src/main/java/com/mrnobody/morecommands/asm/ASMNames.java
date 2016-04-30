package com.mrnobody.morecommands.asm;

import com.google.common.base.Joiner;

/**
 * A class containing names and descriptors of types, methods and fields required for bytecode transformations<br>
 * 
 * @author MrNobody98
 */
public final class ASMNames {
	private static final Joiner concat = Joiner.on("");
	private ASMNames() {}
	
	/**
	 * Sets the environment names of all {@link Method}s and {@link Field}s<br>
	 * See {@link Method#getEnvName()} or {@link Field#getEnvName()} for more details 
	 * 
	 * @param deobf whether the running environment is a deobfuscated one
	 */
	public static void setEnvironmentNames(boolean deobf) {
		for (Method m : Method.values()) m.envName = deobf ? m.deobfName : m.obfName;
		for (Field f : Field.values()) f.envName = deobf ? f.deobfName : f.obfName;
	}
	
	/**
	 * An enum representing the name of a type/class
	 * 
	 * @author MrNobody98
	 */
	public static enum Type {
		Object("java.lang.Object"),
		String("java.lang.String"),
		Item("net.minecraft.item.Item"),
		ItemStack("net.minecraft.item.ItemStack"),
		ItemStackChangeSizeEvent("com.mrnobody.morecommands.event.ItemStackChangeSizeEvent"),
		EventHandler("com.mrnobody.morecommands.event.EventHandler"),
		EntityPlayer("net.minecraft.entity.player.EntityPlayer"),
		World("net.minecraft.world.World"),
		Event("cpw.mods.fml.common.eventhandler.Event"),
		ForgeHooks("net.minecraftforge.common.ForgeHooks"),
		BlockRailBase("net.minecraft.block.BlockRailBase"),
		EntityMinecart("net.minecraft.entity.item.EntityMinecart"),
		NBTTagCompound("net.minecraft.nbt.NBTTagCompound"),
		RegistryNamespaced("net.minecraft.util.RegistryNamespaced"),
		TextureCompass("net.minecraft.client.renderer.texture.TextureCompass"),
		ChunkCoordinates("net.minecraft.util.ChunkCoordinates"),
		TransformTextureCompass("com.mrnobody.morecommands.asm.transform.TransformTextureCompass"),
		EntityLivingBase("net.minecraft.entity.EntityLivingBase"),
		DamageItemEvent("com.mrnobody.morecommands.event.DamageItemEvent")
		;
		
		private final String name, internalName, descriptor;
		
		/**
		 * @param name the regular fully qualified name with dots as separator (e.g. java.lang.System)
		 */
		private Type(String name) {
			this.name = name;
			this.internalName = this.name.replace(".", "/");
			this.descriptor = "L" + this.internalName + ";";
		}
		
		/**
		 * return name the regular fully qualified name with dots as separator (e.g. java.lang.System)
		 */
		public String getName() {
			return this.name;
		}
		
		/**
		 * return name the internal type name with slashes as separator (e.g. java/lang/System)
		 */
		public String getInternalName() {
			return this.internalName;
		}
		
		/**
		 * return name the type descriptor of this type which is: "L" + internal name + ";"
		 */
		public String getDesc() {
			return this.descriptor;
		}
	}
	
	/**
	 * An enum representing the name of a method
	 * 
	 * @author MrNobody98
	 */
	public static enum Method {
		ItemStack_tryPlaceItemIntoWorld(Type.ItemStack, "tryPlaceItemIntoWorld", "func_77943_a", "Z", Type.EntityPlayer.getDesc(), Type.World.getDesc(), "IIIIFFF"),
		ItemStack_getItem(Type.ItemStack, "getItem", "func_77973_b", Type.Item.getDesc()),
		Item_onItemUse(Type.Item, "onItemUse", "func_77648_a", "Z", Type.ItemStack.getDesc(), Type.EntityPlayer.getDesc(), Type.World.getDesc(), "IIIIFFF"),
		ItemStackChangeSizeEvent_init(Type.ItemStackChangeSizeEvent, "<init>", "<init>", "V", Type.EntityPlayer.getDesc(), Type.ItemStack.getDesc(), "II"),
		EventHandler_post(Type.EventHandler, "post", "post", "Z", Type.Event.getDesc()),
		ForgeHooks_onPlaceItemIntoWorld(Type.ForgeHooks, "onPlaceItemIntoWorld", "onPlaceItemIntoWorld", "Z", Type.ItemStack.getDesc(), Type.EntityPlayer.getDesc(), Type.World.getDesc(), "IIIIFFF"),
		BlockRailBase_getRailMaxSpeed(Type.BlockRailBase, "getRailMaxSpeed", "getRailMaxSpeed", "F", Type.World.getDesc(), Type.EntityMinecart.getDesc(), "III"),
		ItemStack_readFromNBT(Type.ItemStack, "readFromNBT", "func_77963_c", "V", Type.NBTTagCompound.getDesc()),
		ItemStack_writeToNBT(Type.ItemStack, "writeToNBT", "func_77955_b", Type.NBTTagCompound.getDesc(), Type.NBTTagCompound.getDesc()),
		NBTTagCompound_getShort(Type.NBTTagCompound, "getShort", "func_74765_d", "S", Type.String.getDesc()),
		NBTTagCompound_getString(Type.NBTTagCompound, "getString", "func_74779_i", Type.String.getDesc(), Type.String.getDesc()),
		NBTTagCompound_hasKey_WITH_ID(Type.NBTTagCompound, "hasKey", "func_150297_b", "Z", Type.String.getDesc(), "I"),
		Item_getItemById(Type.Item, "getItemById", "func_150899_d", Type.Item.getDesc(), "I"),
		RegistryNamespaced_getObject(Type.RegistryNamespaced, "getObject", "func_82594_a", Type.Object.getDesc(), Type.String.getDesc()),
		ItemStack_func_150996_a(Type.ItemStack, "func_150996_a", "func_150996_a", "V", Type.Item.getDesc()),
		TextureCompass_updateCompass(Type.TextureCompass, "updateCompass", "func_94241_a", "V", Type.World.getDesc(), "DDDZZ"),
		World_getSpawnPoint(Type.World, "getSpawnPoint", "func_72861_E", Type.ChunkCoordinates.getDesc()),
		TransformTextureCompass_getCompassTarget(Type.TransformTextureCompass, "getCompassTarget", "getCompassTarget", Type.ChunkCoordinates.getDesc(), Type.World.getDesc()),
		ItemStack_damageItem(Type.ItemStack, "damageItem", "func_77972_a", "V", "I", Type.EntityLivingBase.getDesc()),
		DamageItemEvent_init(Type.DamageItemEvent, "<init>", "<init>", "V", Type.EntityLivingBase.getDesc(), "I", Type.ItemStack.getDesc())
		;
		
		private final String deobfName, obfName, descriptor;
		private final Type owner;
		private String envName = null;
		
		/**
		 * @param owner the owner type of this method (the type that declares this method)
		 * @param deobfName the deobfuscated name of this method
		 * @param obfName the obfuscated name of this method
		 * @param retType the type descriptor of the return type of this method (equal to <b>{@link Type#getDesc()}</b> for <b>object type</b>, 
		 *                 <b>"I"</b> for <b>integer</b>, <b>"J"</b> for <b>long</b>, <b>"S"</b> for <b>short</b>, <b>"V"</b> for <b>void</b>, 
		 *                 <b>"B"</b> for <b>byte</b>, <b>"Z"</b> for <b>boolean</b>, <b>"F"</b> for <b>float</b> and <b>"D"</b> for <b>double</b>)
		 * @param paramTypes the type descriptor for the parameter type. (See <i>retType</i> for a description of a type descriptor)
		 */
		private Method(Type owner, String deobfName, String obfName, String retType, String... paramTypes) {
			this.owner = owner;
			this.deobfName = deobfName;
			this.obfName = obfName;
			this.descriptor = "(" + concat.join(paramTypes) + ")" + retType;
		}
		
		/**
		 * @return the owner type of this method
		 */
		public Type getOwner() {
			return this.owner;
		}
		
		/**
		 * @return The internal name of the owner type of this method
		 */
		public String getOwnerInternalName() {
			return this.owner.getInternalName();
		}
		
		/**
		 * @return the deobfuscated name of this method
		 */
		public String getDeobfName() {
			return this.deobfName;
		}
		
		/**
		 * @return the obfuscated name of this method
		 */
		public String getObfName() {
			return this.obfName;
		}
		
		/**
		 * @return Either the obfuscated or the deobfusctated name of this method depending on whether the environment is deobfuscated or not.
		 *         Returns null if the environment names haven't yet been set via {@link ASMNames#setEnvironmentNames(boolean)}
		 */
		public String getEnvName() {
			return this.envName;
		}
		
		/**
		 * @return The type descriptor of this method which looks like this: <b>(parameter_type_descriptors)return_type_descriptor</b>,<br>
		 *         e.g. "(IFLjava/io/File;Z)V
		 */
		public String getDesc() {
			return this.descriptor;
		}
	}
	
	/**
	 * An enum representing the name of a field
	 * 
	 * @author MrNobody98
	 */
	public static enum Field {
		ItemStack_stackSize(Type.ItemStack, "stackSize", "field_77994_a", "I"),
		EventHandler_ITEMSTACK_CHANGE_SIZE(Type.EventHandler, "ITEMSTACK_CHANGE_SIZE", "ITEMSTACK_CHANGE_SIZE", Type.EventHandler.getDesc()),
		EventHandler_DAMAGE_ITEM(Type.EventHandler, "DAMAGE_ITEM", "DAMAGE_ITEM", Type.EventHandler.getDesc()),
		ItemStackChangeSizeEvent_newSize(Type.ItemStackChangeSizeEvent, "newSize", "newSize", "I"),
		Item_itemRegistry(Type.Item, "itemRegistry", "field_150901_e", Type.RegistryNamespaced.getDesc()),
		DamageItemEvent_damage(Type.DamageItemEvent, "damage", "damage", "I")
		;
		
		private final String deobfName, obfName, descriptor;
		private final Type owner;
		private String envName = null;
		
		/**
		 * @param owner the owner type of this field (the type that declares this field)
		 * @param deobfName the deobfuscated name of this field
		 * @param obfName the obfuscated name of this field
		 * @param descriptor the type descriptor of this field (equal to <b>{@link Type#getDesc()}</b> for <b>object type</b>, 
		 *                 <b>"I"</b> for <b>integer</b>, <b>"J"</b> for <b>long</b>, <b>"S"</b> for <b>short</b>, <b>"V"</b> for <b>void</b>, 
		 *                 <b>"B"</b> for <b>byte</b>, <b>"Z"</b> for <b>boolean</b>, <b>"F"</b> for <b>float</b> and <b>"D"</b> for <b>double</b>)
		 */
		private Field(Type owner, String deobfName, String obfName, String descriptor) {
			this.owner = owner;
			this.deobfName = deobfName;
			this.obfName = obfName;
			this.descriptor = descriptor;
		}
		
		/**
		 * @return the owner type of this field
		 */
		public Type getOwner() {
			return this.owner;
		}
		
		/**
		 * @return The internal name of the owner type of this field
		 */
		public String getOwnerInternalName() {
			return this.owner.getInternalName();
		}
		
		/**
		 * @return the deobfuscated name of this field
		 */
		public String getDeobfName() {
			return this.deobfName;
		}
		
		/**
		 * @return the obfuscated name of this field
		 */
		public String getObfName() {
			return this.obfName;
		}
		
		/**
		 * @return Either the obfuscated or the deobfusctated name of this field depending on whether the environment is deobfuscated or not.
		 *         Returns null if the environment names haven't yet been set via {@link ASMNames#setEnvironmentNames(boolean)}
		 */
		public String getEnvName() {
			return this.envName;
		}
		
		/**
		 * @return The type descriptor of this field. See parameter <i>retType</i> of {@link Field#Field(Type, String, String, String)}
		 *         for a descripition
		 */
		public String getDesc() {
			return this.descriptor;
		}
	}
}
