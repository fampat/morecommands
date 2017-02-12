package com.mrnobody.morecommands.asm;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mrnobody.morecommands.asm.transform.TransformBlockRailBase;
import com.mrnobody.morecommands.asm.transform.TransformChatStyle;
import com.mrnobody.morecommands.asm.transform.TransformForgeHooks;
import com.mrnobody.morecommands.asm.transform.TransformItemStack;
import com.mrnobody.morecommands.asm.transform.TransformTextureCompass;
import com.mrnobody.morecommands.util.Reference;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import net.minecraft.launchwrapper.IClassTransformer;

/**
 * The {@link IFMLLoadingPlugin} that provides {@link IClassTransformer}s<br>
 * which transform minecraft classes so that functionality which MoreCommands needs for some<br>
 * commands is added. Additionally determines whether we are in a deobfuscated environment
 * 
 * @author MrNobody98
 */
@IFMLLoadingPlugin.SortingIndex(1001)
@IFMLLoadingPlugin.MCVersion("1.7.10")
@IFMLLoadingPlugin.DependsOn("forge")
@IFMLLoadingPlugin.TransformerExclusions({"com.mrnobody.morecommands.asm."})
public class MoreCommandsLoadingPlugin implements IFMLLoadingPlugin {
	public static final Logger logger = LogManager.getLogger(Reference.MODID);
	private static boolean isDeobf = false, wasLoaded = false;
	
	/**
	 * @return Whether this {@link IFMLLoadingPlugin} was instantiated.<br>
	 * This happens when either the "FMLCorePlugin" property of the manifest file<br>
	 * of the jar archive containing this mod or the system property<br>
	 * "fml.coreMods.load" is set to this class's fully qualified name<br>
	 * This plugin not being loaded indicates that the manifest file was manipulated.
	 */
	public static boolean wasLoaded() {
		return wasLoaded;
	}
	
	/**
	 * @return whether we are running in a deobfuscated environment
	 */
	public static boolean isDeobf() {
		return isDeobf;
	}
	
	public MoreCommandsLoadingPlugin() {
		logger.info("Loading MoreCommands Transformers");
		this.wasLoaded = true;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String[] getASMTransformerClass() {
		return new String[] {
				TransformBlockRailBase.class.getName(),
				TransformForgeHooks.class.getName(),
				TransformItemStack.class.getName(),
				TransformTextureCompass.class.getName(),
				TransformChatStyle.class.getName()
		};
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getModContainerClass() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getSetupClass() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void injectData(Map<String, Object> data) {
		isDeobf = !(Boolean) data.get("runtimeDeobfuscationEnabled");
		logger.info("Running in deobfuscated Environment: " + isDeobf);
		
		ASMNames.setEnvironmentNames(isDeobf);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getAccessTransformerClass() {
		return null;
	}
}
