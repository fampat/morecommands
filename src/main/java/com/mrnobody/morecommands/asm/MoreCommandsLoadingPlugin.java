package com.mrnobody.morecommands.asm;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mrnobody.morecommands.asm.transform.TransformBlockRailBase;
import com.mrnobody.morecommands.util.Reference;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

@IFMLLoadingPlugin.SortingIndex(1001)
@IFMLLoadingPlugin.MCVersion("1.7.10")
@IFMLLoadingPlugin.DependsOn("forge")
@IFMLLoadingPlugin.TransformerExclusions({"com.mrnobody.morecommands.asm."})
public class MoreCommandsLoadingPlugin implements IFMLLoadingPlugin {
	public static final Logger logger = LogManager.getLogger(Reference.MODID);
	private static boolean isDeobf = false;
	
	public static boolean isDeobf() {
		return isDeobf;
	}
	
	public MoreCommandsLoadingPlugin() {
		logger.info("Loading MoreCommands Transformers");
	}
	
	@Override
	public String[] getASMTransformerClass() {
		return new String[] {
				TransformBlockRailBase.class.getName(),
		};
	}

	@Override
	public String getModContainerClass() {
		return null;
	}

	@Override
	public String getSetupClass() {
		return null;
	}

	@Override
	public void injectData(Map<String, Object> data) {
		isDeobf = !(Boolean) data.get("runtimeDeobfuscationEnabled");
		logger.info("Running in deobfuscated Environment: " + isDeobf);
	}

	@Override
	public String getAccessTransformerClass() {
		return null;
	}

}
