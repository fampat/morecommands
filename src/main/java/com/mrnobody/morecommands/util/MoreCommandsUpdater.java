package com.mrnobody.morecommands.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.mrnobody.morecommands.core.MoreCommands;

/**
 * This class looks for MoreCommands updates
 * 
 * @author MrNobody98
 */
public final class MoreCommandsUpdater implements Runnable {
	/**
	 * A class implementing this interface is a callback that will be
	 * invoked when a MoreCommands update was found
	 * 
	 * @author MrNobody98
	 */
	public static interface UpdateCallback {
		/**
		 * This method will be invoked when a MoreCommands update was found
		 * 
		 * @param version the version of the update
		 * @param website the website where the download can be found
		 * @param download the direct download link
		 */
		void udpate(String version, String website, String download);
	}
	
	private static final String MANIFEST = "http://bit.ly/morecommands_update";
	private static final int MORECOMMANDSVERSION_MAJOR = Integer.parseInt(Reference.VERSION.split("\\.")[0]);
	private static final int MORECOMMANDSVERSION_MINOR = Integer.parseInt(Reference.VERSION.split("\\.")[1]);
	private final String MCVERSION;
	private final UpdateCallback listener;
	
	/**
	 * Constructs a new {@link MoreCommandsUpdater}
	 * 
	 * @param mcversion the minecraft version
	 * @param listener the callback that will be invoked when an update was found
	 */
	public MoreCommandsUpdater(String mcversion, UpdateCallback listener) {
		this.MCVERSION = mcversion;
		this.listener = listener;
	}
	
	/**
	 * When invoked this {@link MoreCommandsUpdater} will start to look for a new MoreCommands version
	 */
	@Override
	public void run() {
		File file = downloadFile(MANIFEST);
		if (file == null || !file.exists()) return;
		
		Map<String, Map<String, Triple<Long, String, String>>> update = parseManifest(file);
		if (update == null) return;
		
		Map<String, Triple<Long, String, String>> versions = update.get(MCVERSION);
		if (versions == null) {MoreCommands.INSTANCE.getLogger().info("No MoreCommands updates for this minecraft version found"); return;}
		int maxMajor = -1, maxMinor = -1; String upd = null;
		
		for (String version : versions.keySet()) {
			int version_major = Integer.parseInt(version.split("\\.")[0]);
			int version_minor = Integer.parseInt(version.split("\\.")[1]);
			
			if (version_major > maxMajor) maxMajor = version_major;
			if (version_minor > maxMinor) maxMinor = version_minor;
		}
		
		if (maxMajor > MORECOMMANDSVERSION_MAJOR || (maxMajor == MORECOMMANDSVERSION_MAJOR && maxMinor >= MORECOMMANDSVERSION_MINOR)) 
			upd = maxMajor + "." + maxMinor;
		
		if (upd != null) {
			Triple<Long, String, String> data = versions.get(upd);
			if (new Date(data.getLeft()).after(Reference.BUILD)) this.listener.udpate(upd, data.getMiddle(), data.getRight());
			else MoreCommands.INSTANCE.getLogger().info("No MoreCommands updates for this minecraft version found");
		}
		else MoreCommands.INSTANCE.getLogger().info("No MoreCommands updates for this minecraft version found");
	}
	
	/**
	 * Parses the update file
	 * 
	 * @param manifest the update file
	 * @return returns null if an exception occurred else a map which maps minecraft versions to
	 *         a map which maps MoreCommands versions to a {@link Triple} whose left value is the build
	 *         time of that version in miliseconds, whose middle value is the download website of that 
	 *         version and whose right value is the direct download link of that version
	 */
	public Map<String, Map<String, Triple<Long, String, String>>> parseManifest(File manifest) {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		List<Map<String, Object>> ps = null;
		
		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document d = db.parse(manifest);
			Element e = d.getDocumentElement();
			
			NodeList nl = e.getElementsByTagName("morecommands_versions");
			int index = -1;
			
			if (nl == null || nl.getLength() <= 0) {MoreCommands.INSTANCE.getLogger().info("Invalid update file"); return null;}
			
			for (int i = 0; i < nl.getLength(); i++) {
				Element el = (Element) nl.item(i);
				if (el.getAttribute("version").equals("1.0")) {index = i; break;}
			}
			
			if (index < 0) {MoreCommands.INSTANCE.getLogger().info("Unaccepted update file version"); return null;}
			return parseManifestVersion1_0((Element) nl.item(index));}
		catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Parses an update file of version 1.0 
	 * 
	 * @param root the root {@link Element} of the update file that has version 1.0
	 * @return a map which maps minecraft versions to a map which maps MoreCommands versions to a
	 *         {@link Triple} whose left value is the build time of that version in miliseconds,
	 *         whose middle value is the download website of that version and whose right value is
	 *         the direct download link of that version
	 */
	public Map<String, Map<String, Triple<Long, String, String>>> parseManifestVersion1_0(Element root) {
		NodeList nl = root.getElementsByTagName("mcversion");
		Map<String, Map<String, Triple<Long, String, String>>> map = new HashMap<String, Map<String, Triple<Long, String, String>>>();
		
		if (nl == null || nl.getLength() <= 0) {MoreCommands.INSTANCE.getLogger().info("Invalid update file"); return null;}
		
		for (int i = 0; i < nl.getLength(); i++) {
			Element el = (Element) nl.item(i);
			String version = el.getAttribute("version");
			NodeList ver = el.getElementsByTagName("morecommandsversion");
			Map<String, Triple<Long, String, String>> versions = new HashMap<String, Triple<Long, String, String>>();
			
			for (int k = 0; k < ver.getLength(); k++) {
				Element e = (Element) ver.item(k);
				String v = e.getAttribute("version");
				
				long buildTimeMillis = Long.parseLong(e.getElementsByTagName("buildDateMillis").item(0).getTextContent());
				String website = e.getElementsByTagName("website").item(0).getTextContent();
				String download = e.getElementsByTagName("download").item(0).getTextContent();
				
				versions.put(v, ImmutableTriple.of(buildTimeMillis, website, download));
			}
			
			map.put(version, versions);
		}
		
		return map;
	}
	
	/**
	 * Downloads a string from a URL to a temporary file
	 * 
	 * @param rawurl the raw download URL
	 * @return the downloaded file (is a temporary file)
	 */
	public File downloadFile(String rawurl) {
		File temp = null;
		InputStream is = null;
		FileOutputStream fos = null;
	      
		try {
			URL url = new URL(rawurl);
			is = url.openStream();
			temp = File.createTempFile("morecommands_update", System.currentTimeMillis() + "");
			temp.deleteOnExit();
			fos = new FileOutputStream(temp);
			byte[] buffer = new byte[1024];
			int bytes = 0;

			while ((bytes = is.read(buffer)) > 0) { 
				fos.write(buffer, 0, bytes);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			try {temp.delete();} catch (Exception e2) {}
			temp = null;
		} finally {
			try {fos.close();} catch (Exception e) {}
			try {is.close();} catch (Exception e) {}
		}
		return temp;
	}
}
