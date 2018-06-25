package mfi.photos.server.logic;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import mfi.photos.shared.AES;
import mfi.photos.shared.GalleryList;
import mfi.photos.shared.GalleryList.Item;
import mfi.photos.shared.GalleryView;
import mfi.photos.util.DisplayNameUtil;
import mfi.photos.util.GalleryViewCache;
import mfi.photos.util.GalleryViewComparator;
import mfi.photos.util.IOUtil;
import mfi.photos.util.KeyAccess;
import mfi.photos.util.ServletUtil;

public class Processor {

	Properties properties;

	public Processor() {
		properties = getApplicationProperties();
		if (KeyAccess.getInstance().getUptime() == null) {
			KeyAccess.getInstance().setUptime(String.valueOf(System.currentTimeMillis()));
		}
	}

	public File lookupAssetFile(String path) {

		String base = properties.getProperty("photosDir");
		if (!base.endsWith("/")) {
			base = base + "/";
		}
		path = StringUtils.removeStart(path, properties.getProperty("assetURI"));
		String filePath = base + path + AES.FILE_SUFFIX;

		return new File(filePath);
	}

	public void saveNewImage(Map<String, String> params) throws UnsupportedEncodingException, IOException {

		String galleryName = params.get("galleryName");
		String imageName = params.get("imageName");
		String base64Data = params.get("saveImage");
		byte[] data = Base64.getDecoder().decode(base64Data);
		String photosDir = properties.getProperty("photosDir");
		if (!photosDir.endsWith("/")) {
			photosDir = photosDir + "/";
		}
		File dir = new File(photosDir + galleryName);
		if (!dir.exists()) {
			FileUtils.forceMkdir(dir);
		}
		File photo = new File(photosDir + galleryName + "/" + imageName + AES.FILE_SUFFIX);
		FileUtils.writeByteArrayToFile(photo, data, Boolean.valueOf(params.get("append")));
	}

	public String checksumFromImage(Map<String, String> params) throws UnsupportedEncodingException, IOException {

		String galleryName = params.get("galleryName");
		String imageName = params.get("imageName");
		String photosDir = properties.getProperty("photosDir");
		if (!photosDir.endsWith("/")) {
			photosDir = photosDir + "/";
		}
		File photo = new File(photosDir + galleryName + "/" + imageName + AES.FILE_SUFFIX);
		try {
			String checksumValue = Long.toString(photo.length());
			return checksumValue;
		} catch (Exception ioe) {
			return "n/a";
		}
	}

	public void saveNewGallery(Map<String, String> params) throws UnsupportedEncodingException, IOException {

		Gson gson = new GsonBuilder().create();
		String jsonDir = lookupJsonDir(properties);
		String base64String = params.get("saveGallery");
		String newJson = new String(Base64.getDecoder().decode(base64String), "utf-8");
		GalleryView galleryView = gson.fromJson(newJson, GalleryView.class);
		FileUtils.writeStringToFile(new File(jsonDir + galleryView.getKey() + ".json"), newJson);
		GalleryViewCache.getInstance().refresh(jsonDir, gson);
	}

	public void galleryHTML(Map<String, String> params, StringBuilder sb, String user, String cookie)
			throws IOException {

		String html = IOUtil.readContentFromFileInClasspath("gallery.html");
		String htmlHead = IOUtil.readContentFromFileInClasspath("htmlhead");

		Gson gson = new GsonBuilder().create();
		String jsonDir = lookupJsonDir(properties);
		String key = StringEscapeUtils.escapeHtml4(params.get("gallery"));
		File file = new File(jsonDir + key + ".json");
		if (file.exists() && file.isFile() && file.canRead()) {
			String json = FileUtils.readFileToString(file, "UTF-8");
			GalleryView galleryView = gson.fromJson(json, GalleryView.class);
			galleryView.truncateHashes();
			String assetCookie = ServletUtil.assetCookieIdFromCookie(cookie);
			galleryView.setAssetCookie(assetCookie);
			DisplayNameUtil.createDisplayName(galleryView);
			json = gson.toJson(galleryView);
			if (galleryView.getUsersAsList().contains(user)) {
				html = StringUtils.replace(html, "<!-- HEAD -->", htmlHead);
				html = StringUtils.replace(html, "/*LISTYPOS*/", params.get("y"));
				html = StringUtils.replace(html, "/*LISTSEARCH*/", params.get("s"));
				html = StringUtils.replace(html, "/*JSONFILE*/", json);
				sb.append(html);
			} else {
				listHTML(params, sb, user);
			}
		} else {
			listHTML(params, sb, user);
		}
	}

	public String galleryJson(String user, String key) throws IOException {

		if (!user.equals(properties.getProperty("technicalUser"))) {
			return null;
		}

		String jsonDir = lookupJsonDir(properties);
		File file = new File(jsonDir + key + ".json");
		String json = FileUtils.readFileToString(file, "UTF-8");
		return json;
	}

	public void cleanUp(String clientGalleryListJson, String jsonHash) throws IOException {

		String ownHash = DigestUtils.md5Hex(clientGalleryListJson);
		if (!StringUtils.equals(jsonHash, ownHash)) {
			throw new IOException("Different cleanUp Hashes: " + jsonHash + " / " + ownHash);
		}

		Gson gson = new GsonBuilder().create();
		String jsonDir = lookupJsonDir(properties);
		GalleryViewCache.getInstance().refresh(jsonDir, gson);

		// Delete unreferenced album jsons
		GalleryList clientGalleryList = gson.fromJson(clientGalleryListJson, GalleryList.class);
		List<Item> clientGalleryItems = Arrays.asList(clientGalleryList.getList());
		List<String> clientGalleryAlbumKeys = new ArrayList<>();
		for (Item clientItem : clientGalleryItems) {
			clientGalleryAlbumKeys.add(clientItem.getKey());
		}
		Set<String> albumKeys = GalleryViewCache.getInstance().keySet();
		for (String albumKey : albumKeys) {
			if (!clientGalleryAlbumKeys.contains(albumKey)) {
				File albumJsonToDelete = new File(lookupJsonDir(properties) + albumKey + ".json");
				if (albumJsonToDelete.exists()) {
					FileUtils.deleteQuietly(albumJsonToDelete);
				}
			}
		}
		GalleryViewCache.getInstance().refresh(jsonDir, gson);

		// Delete unreferenced photos
		String base = properties.getProperty("photosDir");
		if (!base.endsWith("/")) {
			base = base + "/";
		}

		File[] listFiles = new File(base).listFiles((FileFilter) DirectoryFileFilter.DIRECTORY);
		for (File dir : listFiles) {
			List<File> files = (List<File>) FileUtils.listFiles(dir, FileFilterUtils.trueFileFilter(),
					FileFilterUtils.trueFileFilter());
			GalleryView galleryView = GalleryViewCache.getInstance().read(dir.getName());
			List<String> pictureNamesInAlbum = new ArrayList<>();
			if (galleryView != null) {
				for (GalleryView.Picture picture : galleryView.getPictures()) {
					pictureNamesInAlbum.add(picture.getName() + AES.FILE_SUFFIX);
				}
			}
			for (File file : files) {
				if (!file.getName().startsWith("tn_") && !file.getName().startsWith("pre_")) {
					if (!pictureNamesInAlbum.contains(file.getName())) {
						// System.out.println("file to delete: " + dir.getName()
						// + "/" + file.getName());
						FileUtils.deleteQuietly(file);
						FileUtils.deleteQuietly(new File(file.getParent() + "/" + "tn_" + file.getName()));
						File videoPreview = new File(file.getParent() + "/" + "pre_" + file.getName());
						if (videoPreview.exists()) {
							FileUtils.deleteQuietly(videoPreview);
						}
					}
				}
			}
			if (dir.listFiles().length == 0) {
				FileUtils.deleteQuietly(dir);
			}
		}
	}

	public void loginscreenHTML(StringBuilder sb, String message) {

		String html = IOUtil.readContentFromFileInClasspath("login.html");
		String htmlHead = IOUtil.readContentFromFileInClasspath("htmlhead");
		html = StringUtils.replace(html, "<!-- HEAD -->", htmlHead);
		html = StringUtils.replace(html, "/*JSONFILE*/", StringUtils.trimToEmpty(message));
		html = StringUtils.replace(html, "/*LAWLINK*/",
				StringUtils.trimToEmpty(properties.getProperty("linkToLawSite")));
		sb.append(html);
	}

	public void listHTML(Map<String, String> params, StringBuilder sb, String user) throws IOException {

		String y = params.get("y");
		String s = StringUtils.trimToEmpty(params.get("s"));
		int yPos = 0;
		if (StringUtils.isNotBlank(y) && StringUtils.isNumeric(y)) {
			yPos = Integer.parseInt(y);
		}

		String userForList = user;
		if (user.equals(properties.getProperty("technicalUser")) && params.containsKey("viewUser")) {
			userForList = params.get("viewUser");
		}

		String json = listJson(userForList, s, yPos, false);

		String html = IOUtil.readContentFromFileInClasspath("list.html");
		String htmlHead = IOUtil.readContentFromFileInClasspath("htmlhead");
		html = StringUtils.replace(html, "<!-- HEAD -->", htmlHead);
		html = StringUtils.replace(html, "/*JSONFILE*/", json);
		sb.append(html);
	}

	public String listJson(String user, String s, int yPos, boolean withHashesAndUsers) {

		Gson gson = new GsonBuilder().create();
		String jsonDir = lookupJsonDir(properties);
		GalleryViewCache.getInstance().refresh(jsonDir, gson);

		List<GalleryView> galleryViews = new LinkedList<GalleryView>();
		Set<String> keySet = GalleryViewCache.getInstance().keySet();

		for (String string : keySet) {
			GalleryView galleryView = GalleryViewCache.getInstance().read(string);
			if (galleryView.getUsersAsList().contains(user) || user.equals(properties.getProperty("technicalUser"))) {
				galleryViews.add(galleryView);
			}
		}

		Collections.sort(galleryViews, new GalleryViewComparator());
		Collections.reverse(galleryViews);

		GalleryList galleryList = new GalleryList(user, galleryViews.size(), yPos, s);
		for (GalleryView view : galleryViews) {
			galleryList.addItem(view.getKey(), view.getGalleryDisplayName(), view.getGalleryDisplayIdentifier(),
					view.getGalleryDisplayNormDate(), withHashesAndUsers ? view.getGalleryhash() : null,
					withHashesAndUsers ? view.getUsers() : null);
		}
		String json = gson.toJson(galleryList);
		return json;
	}

	public boolean checkAuthentication(String user, String pass) {

		try {
			String url = properties.getProperty("authenticationURL");

			HttpClient client = new HttpClient();
			PostMethod method = new PostMethod(url);

			method.addParameter("user", user);
			method.addParameter("pass", pass);
			if (!KeyAccess.getInstance().isKeySet()) {
				method.addParameter("getSecretForUser", properties.getProperty("technicalUser"));
			}

			boolean ok = client.executeMethod(method) == 200;
			if (ok && !KeyAccess.getInstance().isKeySet()) {
				KeyAccess.getInstance().setKey(method.getResponseBodyAsString());
			}
			return ok;

		} catch (Exception e) {
			System.out.println(e);
			return false;
		}
	}

	public String lookupJsonDir(Properties properties) {
		String dir = properties.getProperty("listDir");
		if (!StringUtils.endsWith(dir, "/") && !StringUtils.endsWith(dir, "\\")) {
			dir = dir + File.separatorChar;
		}
		return dir;
	}

	public Properties getApplicationProperties() {

		Properties properties = new Properties();
		String path = System.getProperty("catalina.base") + "/webapps/webAppProperties/photos.properties";
		if (!(new File(path).exists())) {
			path = System.getProperty("catalina.home") + "/webapps/webAppProperties/photos.properties";
		}
		try {
			properties.load(new FileInputStream(path));
			properties.setProperty("FILE", path);
			return properties;
		} catch (Exception e) {
			throw new RuntimeException("Properties could not be loaded", e);
		}
	}

}