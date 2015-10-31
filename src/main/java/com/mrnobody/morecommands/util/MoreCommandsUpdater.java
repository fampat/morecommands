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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.mrnobody.morecommands.core.MoreCommands;

public class MoreCommandsUpdater implements Runnable {
	public static interface UpdateCallback {
		void udpate(String version, String website, String download);
	}
	
	private static final String MANIFEST = "http://bit.ly/morecommands_update";
	private static final int MORECOMMANDSVERSION_MAJOR = Integer.parseInt(Reference.VERSION.split("\\.")[0]);
	private static final int MORECOMMANDSVERSION_MINOR = Integer.parseInt(Reference.VERSION.split("\\.")[1]);
	private final String MCVERSION;
	private final UpdateCallback listener;
	
	public MoreCommandsUpdater(String mcversion, UpdateCallback listener) {
		this.MCVERSION = mcversion;
		this.listener = listener;
	}
	
	@Override
	public void run() {
		File file = downloadFile(MANIFEST);
		if (file == null || !file.exists()) return;
		
		Map<String, Map<String, Object[]>> update = parseManifest(file);
		if (update == null) return;
		
		Map<String, Object[]> versions = update.get(MCVERSION);
		if (versions == null) {MoreCommands.getMoreCommands().getLogger().info("No MoreCommands updates for this minecraft version found"); return;}
		int maxMajor = -1, maxMinor = -1; String upd = null;
		
		for (String version : versions.keySet()) {
			int version_major = Integer.parseInt(version.split("\\.")[0]);
			int version_minor = Integer.parseInt(version.split("\\.")[1]);
			
			if (version_major > maxMajor) maxMajor = version_major;
			if (version_minor > maxMinor) maxMinor = version_minor;
		}
		
		if (maxMajor > MORECOMMANDSVERSION_MAJOR || (maxMajor == MORECOMMANDSVERSION_MAJOR && maxMinor >= MORECOMMANDSVERSION_MAJOR)) 
			upd = maxMajor + "." + maxMinor;
		
		if (upd != null) {
			Object[] data = versions.get(upd);
			long buildTimeMills = (Long) data[0];
			if (new Date(buildTimeMills).after(Reference.BUILD)) this.listener.udpate(upd, (String) data[1], (String) data[2]);
			else MoreCommands.getMoreCommands().getLogger().info("No MoreCommands updates for this minecraft version found");
		}
		else MoreCommands.getMoreCommands().getLogger().info("No MoreCommands updates for this minecraft version found");
	}
	
	public Map<String, Map<String, Object[]>> parseManifest(File manifest) {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		List<Map<String, Object>> ps = null;
		
		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document d = db.parse(manifest);
			Element e = d.getDocumentElement();
			
			NodeList nl = e.getElementsByTagName("morecommands_versions");
			int index = -1;
			
			if (nl == null || nl.getLength() <= 0) {MoreCommands.getMoreCommands().getLogger().info("Invalid update file"); return null;}
			
			for (int i = 0; i < nl.getLength(); i++) {
				Element el = (Element) nl.item(i);
				if (el.getAttribute("version").equals("1.0")) {index = i; break;}
			}
			
			if (index < 0) {MoreCommands.getMoreCommands().getLogger().info("Unaccepted update file version"); return null;}
			return parseManifestVersion1_0((Element) nl.item(index));}
		catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public Map<String, Map<String, Object[]>> parseManifestVersion1_0(Element root) {
		NodeList nl = root.getElementsByTagName("mcversion");
		Map<String, Map<String, Object[]>> map = new HashMap<String, Map<String, Object[]>>();
		
		if (nl == null || nl.getLength() <= 0) {MoreCommands.getMoreCommands().getLogger().info("Invalid update file"); return null;}
		
		for (int i = 0; i < nl.getLength(); i++) {
			Element el = (Element) nl.item(i);
			String version = el.getAttribute("version");
			NodeList ver = el.getElementsByTagName("morecommandsversion");
			Map<String, Object[]> versions = new HashMap<String, Object[]>();
			
			for (int k = 0; k < ver.getLength(); k++) {
				Element e = (Element) ver.item(k);
				String v = e.getAttribute("version");
				
				long buildTimeMillis = Long.parseLong(e.getElementsByTagName("buildDateMillis").item(0).getTextContent());
				String website = e.getElementsByTagName("website").item(0).getTextContent();
				String download = e.getElementsByTagName("download").item(0).getTextContent();
				
				versions.put(v, new Object[] {buildTimeMillis, website, download});
			}
			
			map.put(version, versions);
		}
		
		return map;
	}
	
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
