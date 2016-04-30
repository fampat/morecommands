package com.mrnobody.morecommands.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.mrnobody.morecommands.core.MoreCommands;

/**
 * This class loads classes (e.g. all classes in a package) and resources (e.g. lang files)
 * 
 * @author MrNobody98
 */
public class DynamicClassLoader {
	/**
	 * The class loader
	 */
	private final ClassLoader CLASSLOADER;
	
	/**
	 * Lists of command classes
	 */
	private List<Class<?>> clientCommandClasses = new ArrayList<Class<?>>();
	private List<Class<?>> serverCommandClasses = new ArrayList<Class<?>>();
	
	private int rsrcWriteIndex = 0;
	
	public DynamicClassLoader(ClassLoader loader) {
		this.CLASSLOADER = loader;
	}
	
	/**
	 * @return The class loader
	 */
	public ClassLoader getClassLoader() {
		return this.CLASSLOADER;
	}
	
	/**
	 * Loads command classes
	 * 
	 * @param pkg the package where the command classes are in
	 * @param clientClasses whether the classes in this package are client commands
	 * @return A list of the loaded command classes
	 */
	public List<Class<?>> getCommandClasses(String pkg, boolean clientClasses) {
		if (clientClasses && this.clientCommandClasses.size() > 0) return this.clientCommandClasses;
		else if (!clientClasses && this.serverCommandClasses.size() > 0) return this.serverCommandClasses;
		
		List<Class<?>> commandClasses = this.getClasses(pkg);
		
		if (commandClasses != null) {
			List<Class<?>> remove = new ArrayList<Class<?>>(commandClasses.size());
			
			for (Class<?> commandClass : commandClasses)
				if (commandClass.getName().contains("$")) remove.add(commandClass);
			
			commandClasses.removeAll(remove);
			remove = null;
			
			if (clientClasses) {
				this.clientCommandClasses.addAll(commandClasses);
				return this.clientCommandClasses;
			}
			else {
				this.serverCommandClasses.addAll(commandClasses);
				return this.serverCommandClasses;
			}
		}
		else return null;
	}
	
	/**
	 * Loads classes from a package
	 * 
	 * @param pkg The package
	 * @return A list of the loaded classes
	 */
	public List<Class<?>> getClasses(String pkg) {
		pkg = pkg.replace(".", "/");
		
		String modClass = MoreCommands.class.getName().replace(".", "/");
		URL location = this.CLASSLOADER.getResource(modClass + ".class");
		
		if (location == null) return null;
		
		List<Class<?>> classes = null;
		
		try {
			if(location.toString().toLowerCase().startsWith("jar")) {
				String jarLocation = location.toString().replaceAll("jar:", "").split("!")[0];
				File root = new File((new URL(jarLocation)).toURI());
				classes = loadClassesFromJAR(root, pkg);
			}
			else {
				File directory = new File(location.getFile().substring(0, location.getFile().length() - ".class".length() - modClass.length()) + pkg);
				classes = loadClassFromDirectory(directory, pkg);
			}
		}
		catch (Exception ex) {ex.printStackTrace(); return null;}
		
		return classes;
	}
	
	/**
	 * Loads resources from a package
	 * 
	 * @param pkg The package
	 * @param extension The file extension
	 * @return A map of file names to file objects
	 */
	public Map<String, File> getResources(String pkg, FilenameFilter filter) {
		pkg = pkg.replace(".", "/");
		
		String modClass = MoreCommands.class.getName().replace(".", "/");
		URL location = this.CLASSLOADER.getResource(modClass + ".class");
		
		if (location == null) return null;
		
		Map<String, File> classes = null;
		
		try {
			if(location.toString().toLowerCase().startsWith("jar")) {
				String jarLocation = location.toString().replaceAll("jar:", "").split("!")[0];
				File root = new File((new URL(jarLocation)).toURI());
				classes = loadResourceFromJAR(root, pkg, filter);
			}
			else {
				File directory = new File(location.getFile().substring(0, location.getFile().length() - ".class".length() - modClass.length()) + pkg);
				classes = loadResourceFromDirectory(directory, pkg);
			}
		}
		catch (Exception ex) {ex.printStackTrace(); return null;}
		
		return classes;
	}
	
	/**
	 * Loads classes from a jar file
	 * 
	 * @param jar The jar file
	 * @param pkg The package
	 * @return A list of the loaded classes
	 */
	private List<Class<?>> loadClassesFromJAR(File jar, String pkg) {
		pkg = pkg.replace(".", "/");
		List<Class<?>> classes = new ArrayList<Class<?>>();
		
		try {
			JarFile jf = new JarFile(jar);
			Enumeration<JarEntry> em = jf.entries();

			while (em.hasMoreElements()) {
				JarEntry je = em.nextElement();
				try {
					String entry = je.getName();
					if(!entry.startsWith(pkg)) continue;
					classes.add(loadClass(entry, null));
				} catch (Throwable t) {}
			}
			
			jf.close();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return classes;
	}
	
	/**
	 * Loads classes from a directory
	 * 
	 * @param directory The directory
	 * @param pkg The package
	 * @return A list of the loaded classes
	 */
	private List<Class<?>> loadClassFromDirectory(File directory, String pkg) {
		List<Class<?>> classes = new ArrayList<Class<?>>();
		
		try {
			File files[] = directory.listFiles();
			for (File file : files) {
				try {
					if (file.isFile()) classes.add(loadClass(file.getName(), pkg));
					else classes.addAll(loadClassFromDirectory(file.getParentFile(), pkg));
				} catch (Exception e) {e.printStackTrace();}
			}
		} catch (Exception e) {e.printStackTrace();}
		return classes;
	}
	
	/**
	 * Loads resources from a package
	 * 
	 * @param jar The jar file
	 * @param pkg The package
	 * @param extension The file extension
	 * @return A map of file names to file objects
	 */
	private Map<String, File> loadResourceFromJAR(File jar, String pkg, FilenameFilter filter) {
		pkg = pkg.replace(".", "/");
		Map<String, File> classes = new HashMap<String, File>();
		
		try {
			JarFile jf = new JarFile(jar);
			Enumeration<JarEntry> em = jf.entries();

			while (em.hasMoreElements()) {
				JarEntry je = em.nextElement();
				try {
					String entry = je.getName();
					if (!entry.startsWith(pkg) || !filter.accept(jar, entry)) continue;
					classes.put(entry.substring(pkg.length() + 1), loadResource(entry, null));
				} catch (Throwable t) {}
			}
			
			jf.close();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return classes;
	}
	
	/**
	 * Loads resources from a package
	 * 
	 * @param directory The directory
	 * @param pkg The package
	 * @return A map of file names to file objects
	 */
	private Map<String, File> loadResourceFromDirectory(File directory, String pkg) {
		Map<String, File> classes = new HashMap<String, File>();
		
		try {
			File files[] = directory.listFiles();
			for (File file : files) {
				try {
					if (file.isFile()) classes.put(file.getName(), loadResource(file.getName(), pkg));
					else classes.putAll(loadResourceFromDirectory(file.getParentFile(), pkg));
				} catch (Exception e) {e.printStackTrace();}
			}
		} catch (Exception e) {e.printStackTrace();}
		return classes;
	}
	
	/**
	 * Loads a resource
	 * 
	 * @param resource The resource directory
	 * @param pack The package
	 * @return the file object
	 */
	public File loadResource(String resource, String pack) throws Exception {
		if (pack != null) pack = pack.endsWith("/") ? pack.substring(0, pack.length() - 1) : pack;
		resource = pack == null ? resource : pack + "/" + resource;
		if (resource.startsWith(".")) resource = resource.substring(1);
		
		File temp = null;
		InputStream is = null;
		FileOutputStream fos = null;
		
		try {
			byte[] buffer = new byte[1024]; int bytes;
			is = this.CLASSLOADER.getResourceAsStream(resource);
			temp = File.createTempFile("rsrc" + this.rsrcWriteIndex++, ".tmp");
			temp.deleteOnExit();
			fos = new FileOutputStream(temp);
			
			while ((bytes = is.read(buffer)) > 0)
				fos.write(buffer, 0, bytes);
            
		} catch (Throwable t) {
			t.printStackTrace();
			return null;
		}
		finally {
			if (is != null) is.close();
			if (fos != null) fos.close();
		}
		return temp;
	}
	
	/**
	 * Loads a class
	 * 
	 * @param clazz The class name
	 * @param pack The package
	 * @return the loaded class
	 */
	public Class<?> loadClass(String clazz, String pack) throws Exception {
		if (!clazz.endsWith(".class")) throw new Exception("'" + clazz + "' is not a class.");
		clazz = clazz.split("\\.")[0];
		if (pack != null) pack = pack.endsWith("/") ? pack.substring(0, pack.length() - 1) : pack;
		
		clazz = pack == null ? clazz : pack + "." + clazz;
		clazz = clazz.replaceAll("/", ".");
		if(clazz.startsWith(".")) clazz = clazz.substring(1);
		
		Class<?> c;
		
		try {c = this.CLASSLOADER.loadClass(clazz);}
		catch (Throwable t) {
			t.printStackTrace();
			return null;
		}    
		return c;
	}
}
