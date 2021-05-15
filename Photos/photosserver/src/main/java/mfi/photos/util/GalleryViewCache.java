package mfi.photos.util;

import com.google.gson.Gson;
import mfi.photos.shared.GalleryView;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class GalleryViewCache {

	private static GalleryViewCache instance;

	private static final Object monitor = new Object();

	private static Map<String, GalleryView> map;

	private String hashCode = "";

	static {
		instance = new GalleryViewCache();
	}

	private Map<String, GalleryView> buildNewMap() {
		return Collections.synchronizedMap(new LinkedHashMap<String, GalleryView>());
	}

	private GalleryViewCache() {
		map = buildNewMap();
	}

	public static GalleryViewCache getInstance() {
		if (instance == null) {
			synchronized (monitor) {
				if (instance == null) {
					instance = new GalleryViewCache();
				}
			}
		}
		return instance;
	}

	private void reset() {
		synchronized (monitor) {
			map = buildNewMap();
		}
	}

	private boolean contains(String key) {
		return key != null && map.containsKey(key);
	}

	private String dirHashCode(File[] files) {

		long result = 17;
		for (File f : files) {
			String s = f.getName() + f.length() + "#" + f.lastModified();
			result = 37 * result + s.hashCode();
		}
		return files.length + "*" + String.valueOf(result);
	}

	public synchronized void refresh(String dir, Gson gson) {

		File[] files = new File(dir).listFiles();
		String dirHashCode = dirHashCode(files);

		if (dirHashCode.equals(hashCode)) {
			return;
		}

		reset();

		for (File file : files) {

			if (StringUtils.endsWithIgnoreCase(file.getName(), ".json")) {
				if (file.exists() && file.isFile() && file.canRead()) {
					String fileString = null;
					try {
						fileString = FileUtils.readFileToString(file, "UTF-8");
					} catch (IOException e) {
						throw new RuntimeException("could not read json file: " + file.getName(), e);
					}
					GalleryView galleryView = gson.fromJson(fileString, GalleryView.class);
					DisplayNameUtil.createDisplayName(galleryView);
					map.put(galleryView.getKey(), galleryView);
				}
			}
		}

		hashCode = dirHashCode;
	}

	public GalleryView read(String string) {
		if (contains(string)) {
			return map.get(string);
		} else {
			return null;
		}
	}

	public Set<String> keySet() {
		return map.keySet();
	}

}
