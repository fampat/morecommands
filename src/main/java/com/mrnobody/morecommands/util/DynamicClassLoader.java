package com.mrnobody.morecommands.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.mrnobody.morecommands.command.ClientCommand;
import com.mrnobody.morecommands.command.CommandBase;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.core.MoreCommands;

import cpw.mods.fml.relauncher.Side;

/**
 * This class loads loads command classes, packet classes <br>
 * and resources (only text files, e.g. language files)
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
	private ArrayList<Class<?>> clientCommandClasses = new ArrayList<Class<?>>();
	private ArrayList<Class<?>> serverCommandClasses = new ArrayList<Class<?>>();
	
	/**
	 * Lists of packet classes
	 */
	private ArrayList<Class<?>> clientPacketClasses = new ArrayList<Class<?>>();
	private ArrayList<Class<?>> serverPacketClasses = new ArrayList<Class<?>>();
	
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
	 * @param baseClass the command base class for the command classes. Must be {@link ClientCommand} or {@link ServerCommand}
	 * @return A list of the loaded command classes
	 */
	public List<Class<?>> getCommandClasses(String pkg, Class<? extends CommandBase> baseClass) {
		String commandType;
		
		if (ClientCommand.class.getSuperclass() != CommandBase.class) return null;
		if (ServerCommand.class.getSuperclass() != CommandBase.class) return null;
		
		if (baseClass == ClientCommand.class.asSubclass(CommandBase.class)) commandType = "client";
		else if (baseClass == ServerCommand.class.asSubclass(CommandBase.class)) commandType = "server";
		else return null;
		
		if (commandType.equals("client") && this.clientCommandClasses.size() > 0) return this.clientCommandClasses;
		else if (commandType.equals("server") && this.serverCommandClasses.size() > 0) return this.serverCommandClasses;
		
		ArrayList<Class<?>> commandClasses = this.getClasses(pkg);
		
		if (commandClasses != null) {
			if (commandType.equals("client")) {
				this.clientCommandClasses.addAll(commandClasses);
				return this.clientCommandClasses;
			}
			else if (commandType.equals("server")) {
				this.serverCommandClasses.addAll(commandClasses);
				return this.serverCommandClasses;
			}
			else return null;
		}
		else return null;
	}
	
	/**
	 * Loads packet classes
	 * 
	 * @param pkg the package where the packet classes are in
	 * @param side the side which the packet belongs to. Must be either {@link Side#SERVER} or {@link Side#CLIENT}
	 * @return A list of the loaded packet classes
	 */
	public List<Class<?>> getPacketClasses(String pkg, Side side) {
		if (side == Side.CLIENT) {
			if (this.clientPacketClasses.size() != 0) return this.clientPacketClasses;
			else {
				ArrayList<Class<?>> packets = this.getClasses(pkg);
				if (packets != null) {this.clientPacketClasses.addAll(packets); return this.clientPacketClasses;}
				else return null;
			}
		}
		else if (side == Side.SERVER) {
			if (this.serverPacketClasses.size() != 0) return this.serverPacketClasses;
			else {
				ArrayList<Class<?>> packets = this.getClasses(pkg);
				if (packets != null) {this.serverPacketClasses.addAll(packets); return this.serverPacketClasses;}
				else return null;
			}
		}
		else return null;
	}
	
	/**
	 * Loads classes from a packet
	 * 
	 * @param pkg The package
	 * @return A list of the loaded classes
	 */
	public ArrayList<Class<?>> getClasses(String pkg) {
		pkg = pkg.replace(".", "/");
		
		String modClass = MoreCommands.class.getName().replace(".", "/");
		URL location = this.CLASSLOADER.getResource(modClass + ".class");
		
		if (location == null) return null;
		
		ArrayList<Class<?>> classes = null;
		
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
	 * Loads resources from a packet. Must be text files
	 * 
	 * @param pkg The package
	 * @param extension The file extension
	 * @return A map of file names to file objects
	 */
	public Map<String, File> getResources(String pkg, String extension) {
		pkg = pkg.replace(".", "/");
		
		String modClass = MoreCommands.class.getName().replace(".", "/");
		URL location = this.CLASSLOADER.getResource(modClass + ".class");
		
		if (location == null) return null;
		
		Map<String, File> classes = null;
		
		try {
			if(location.toString().toLowerCase().startsWith("jar")) {
				String jarLocation = location.toString().replaceAll("jar:", "").split("!")[0];
				File root = new File((new URL(jarLocation)).toURI());
				classes = loadResourceFromJAR(root, pkg, extension);
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
	private ArrayList<Class<?>> loadClassesFromJAR(File jar, String pkg) {
		pkg = pkg.replace(".", "/");
		
		ArrayList<Class<?>> classes = new ArrayList<Class<?>>();
		
		try {
			JarFile jf = new JarFile(jar);
			Enumeration<JarEntry> em = jf.entries();

			while (em.hasMoreElements()) {
				JarEntry je = em.nextElement();
				try {
					String entry = je.getName();
					if(!entry.startsWith(pkg)) {
						continue;
					}
					if (!entry.contains("$")) classes.add(loadClass(entry, null));
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
	 * Loads resources from a packet. Must be text files
	 * 
	 * @param jar The jar file
	 * @param pkg The package
	 * @param extension The file extension
	 * @return A map of file names to file objects
	 */
	private Map<String, File> loadResourceFromJAR(File jar, String pkg, String extension) {
		pkg = pkg.replace(".", "/");
		
		HashMap<String, File> classes = new HashMap<String, File>();
		
		try {
			JarFile jf = new JarFile(jar);
			Enumeration<JarEntry> em = jf.entries();

			while (em.hasMoreElements()) {
				JarEntry je = em.nextElement();
				try {
					String entry = je.getName();
					if (!entry.startsWith(pkg) || !entry.endsWith(extension)) {
						continue;
					}
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
	 * Loads resources from a packet. Must be text files
	 * 
	 * @param directory The directory
	 * @param pkg The package
	 * @return A map of file names to file objects
	 */
	private Map<String, File> loadResourceFromDirectory(File directory, String pkg) {
		HashMap<String, File> classes = new HashMap<String, File>();
		
		try {
			File files[] = directory.listFiles();
			for (File file : files) {
				try {
					if (file.isFile()) {
						classes.put(file.getName(), loadResource(file.getName(), pkg));
					} else {
						classes.putAll(loadResourceFromDirectory(file.getParentFile(), pkg));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
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
	private ArrayList<Class<?>> loadClassFromDirectory(File directory, String pkg) {
		ArrayList<Class<?>> classes = new ArrayList<Class<?>>();
		
		try {
			File files[] = directory.listFiles();
			for (File file : files) {
				try {
					if (file.isFile()) {
						if (!file.getName().contains("$")) classes.add(loadClass(file.getName(), pkg));
					} else {
						classes.addAll(loadClassFromDirectory(file.getParentFile(), pkg));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return classes;
	}
	
	/**
	 * Loads a resource. Must be a text file
	 * 
	 * @param resource The resource directory
	 * @param pack The package
	 * @return the file object
	 */
	public File loadResource(String resource, String pack) throws Exception {
		if (pack != null) {
			pack = pack.endsWith("/") ? pack.substring(0, pack.length() - 1) : pack;
		}
		resource = pack == null ? resource : pack + "/" + resource;
		if(resource.startsWith(".")) {
			resource = resource.substring(1);
		}
		
		File temp;
		try {
			BufferedReader input = new BufferedReader(new InputStreamReader(this.CLASSLOADER.getResourceAsStream(resource)));
			temp = File.createTempFile("rsrc" + this.rsrcWriteIndex, ".tmp");
			BufferedWriter output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(temp)));
			String line;
			
			while ((line = input.readLine()) != null) {
				output.write(line);
				output.newLine();
			}
						
			input.close();
			output.close();
            temp.deleteOnExit();
			this.rsrcWriteIndex++;
		} catch (Throwable t) {
			t.printStackTrace();
			return null;
		}    
		return temp;
	}
	
	/**
	 * Loads a class
	 * 
	 * @param calzz The class name
	 * @param pack The package
	 * @return the loaded class
	 */
	public Class<?> loadClass(String clazz, String pack) throws Exception {
		if (!clazz.endsWith(".class")) {
			throw new Exception("'" + clazz + "' is not a class.");
		}
		clazz = clazz.split("\\.")[0];
		if (pack != null) {
			pack = pack.endsWith("/") ? pack.substring(0, pack.length() - 1) : pack;
		}
		clazz = pack == null ? clazz : pack + "." + clazz;
		clazz = clazz.replaceAll("/", ".");
		if(clazz.startsWith(".")) {
			clazz = clazz.substring(1);
		}
		
		Class<?> c;
		try {
			c = this.CLASSLOADER.loadClass(clazz);
		} catch (Throwable t) {
			t.printStackTrace();
			return null;
		}    
		return c;
	}
}
