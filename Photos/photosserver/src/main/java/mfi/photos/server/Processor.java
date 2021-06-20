package mfi.photos.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import mfi.photos.shared.AES;
import mfi.photos.shared.GalleryList;
import mfi.photos.shared.Item;
import mfi.photos.shared.GalleryView;
import mfi.photos.shared.Picture;
import mfi.photos.util.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
public class Processor {

	public static final String JSON_FILE_SUFFIX = ".json";

	private final Gson gson = new GsonBuilder().create();

	@Autowired
	private RequestUtil requestUtil;

	@Value("${photosDir}")
	private String photosDir;

	@Value("${listDir}")
	private String listDir;

	@Value("${assets.uri}")
	private String assetsUri;

	@Value("${technicalUser}")
	private String technicalUser;

	@Value("${linkToLawSite}")
	private String linkToLawSite;

	public File lookupAssetFile(String path) {
		return new File(lookupPhotosDir() + StringUtils.removeStart(path, assetsUri.trim()) + AES.FILE_SUFFIX);
	}

	public void saveNewImage(String galleryName, String imageName, String base64Data, String append) throws IOException {

		byte[] data = Base64.getDecoder().decode(base64Data);
		String photosDir = lookupPhotosDir();
		File dir = new File(photosDir + galleryName);
		if (!dir.exists()) {
			FileUtils.forceMkdir(dir);
		}
		File photo = new File(photosDir + galleryName + "/" + imageName + AES.FILE_SUFFIX);
		FileUtils.writeByteArrayToFile(photo, data, Boolean.parseBoolean(append));
	}

	public String lookupPhotosDir() {
		return photosDir;
	}

	public String checksumFromImage(String galleryName, String imageName) {

		String photosDir = lookupPhotosDir();
		File photo = new File(photosDir + galleryName + "/" + imageName + AES.FILE_SUFFIX);
		try {
			return Long.toString(photo.length());
		} catch (Exception ioe) {
			return "n/a";
		}
	}

	public void saveNewGallery(String base64StringGalleryName) throws IOException {

		String jsonDir = lookupListDir();
		String newJson = new String(Base64.getDecoder().decode(base64StringGalleryName), StandardCharsets.UTF_8);
		GalleryView galleryView = gson.fromJson(newJson, GalleryView.class);
		FileUtils.writeStringToFile(new File(jsonDir + galleryView.getKey() + ".json"), newJson);
		GalleryViewCache.getInstance().refresh(jsonDir, gson);
	}

	public void renameGallery(String keyOld, String base64StringGalleryName) throws IOException {

		String jsonDir = lookupListDir();
		String newJson = new String(Base64.getDecoder().decode(base64StringGalleryName), StandardCharsets.UTF_8);
		GalleryView galleryView = gson.fromJson(newJson, GalleryView.class);

		// rename directory
		String photosDir = lookupPhotosDir();
		File dir = new File(photosDir + keyOld);
		boolean ok = dir.renameTo(new File(photosDir + galleryView.getKey()));
		if (!ok) {
			throw new RuntimeException("renaming not successful");
		}

		// save new gallery
		FileUtils.writeStringToFile(new File(jsonDir + galleryView.getKey() + ".json"), newJson);

		// delete old gallery
		File oldJsonFile = new File(jsonDir + keyOld + ".json");
		ok = oldJsonFile.delete();
		if (!ok) {
			throw new RuntimeException("renaming not successful");
		}

		GalleryViewCache.getInstance().refresh(jsonDir, gson);
	}

	public void galleryHTML(Long yPos, String searchString, String galleryName, StringBuilder sb)
			throws IOException {

		String html = IOUtil.readContentFromFileInClasspath("gallery.html");
		String htmlHead = IOUtil.readContentFromFileInClasspath("htmlhead");

		GalleryView galleryView = deepCopyGalleryView(GalleryViewCache.getInstance().read(StringEscapeUtils.escapeHtml4(galleryName)));
		galleryView.truncateHashes();
		URL baseUrl = new URL(galleryView.getBaseURL());
		galleryView.setBaseURL(StringUtils.removeStart(baseUrl.getPath(), StringUtils.substringBefore(baseUrl.getPath(), "/assets/")));
		DisplayNameUtil.createDisplayName(galleryView);
		String json = gson.toJson(galleryView);

		html = StringUtils.replace(html, "<!-- HEAD -->", htmlHead);
		html = StringUtils.replace(html, "/*LISTYPOS*/", Long.toString(yPos));
		html = StringUtils.replace(html, "/*LISTSEARCH*/", searchString);
		html = StringUtils.replace(html, "/*JSONFILE*/", json);

		sb.append(html);
	}

	public String galleryJson(String key) throws IOException {

		String user = requestUtil.assertUserAndGetName();
		if (!user.equals(technicalUser)) {
			return null;
		}

		String jsonDir = lookupListDir();
		File file = new File(jsonDir + key + ".json");
		return FileUtils.readFileToString(file, StandardCharsets.UTF_8);
	}

	public void cleanUp(String clientGalleryListJson, String jsonHash) throws IOException {

		String ownHash = DigestUtils.md5Hex(clientGalleryListJson);
		if (!StringUtils.equals(jsonHash, ownHash)) {
			throw new IOException("Different cleanUp Hashes: " + jsonHash + " / " + ownHash);
		}

		String jsonDir = lookupListDir();
		GalleryViewCache.getInstance().refresh(jsonDir, gson);

		// Delete unreferenced album jsons
		GalleryList clientGalleryList = gson.fromJson(clientGalleryListJson, GalleryList.class);
		List<String> clientGalleryAlbumKeys = new ArrayList<>();
		for (Item clientItem : clientGalleryList.getList()) {
			clientGalleryAlbumKeys.add(clientItem.getKey());
		}
		Set<String> albumKeys = GalleryViewCache.getInstance().keySet();
		for (String albumKey : albumKeys) {
			if (!clientGalleryAlbumKeys.contains(albumKey)) {
				File albumJsonToDelete = new File(lookupListDir() + albumKey + JSON_FILE_SUFFIX);
				if (albumJsonToDelete.exists()) {
					FileUtils.deleteQuietly(albumJsonToDelete);
				}
			}
		}
		GalleryViewCache.getInstance().refresh(jsonDir, gson);

		String base = lookupPhotosDir();

		File[] listFiles = new File(base).listFiles((FileFilter) DirectoryFileFilter.DIRECTORY);
		for (File dir : Objects.requireNonNull(listFiles)) {
			List<File> files = (List<File>) FileUtils.listFiles(dir, FileFilterUtils.trueFileFilter(),
					FileFilterUtils.trueFileFilter());
			GalleryView galleryView = GalleryViewCache.getInstance().read(dir.getName());
			List<String> pictureNamesInAlbum = new ArrayList<>();
			if (galleryView != null) {
				for (Picture picture : galleryView.getPictures()) {
					pictureNamesInAlbum.add(picture.getName() + AES.FILE_SUFFIX);
				}
			}
			for (File file : files) {
				if (!file.getName().startsWith("tn_") && !file.getName().startsWith("pre_")) {
					if (!pictureNamesInAlbum.contains(file.getName())) {
						FileUtils.deleteQuietly(file);
						FileUtils.deleteQuietly(new File(file.getParent() + "/" + "tn_" + file.getName()));
						File videoPreview = new File(file.getParent() + "/" + "pre_" + file.getName());
						if (videoPreview.exists()) {
							FileUtils.deleteQuietly(videoPreview);
						}
					}
				}
			}
			if (Objects.requireNonNull(dir.listFiles()).length == 0) {
				FileUtils.deleteQuietly(dir);
			}
		}
	}

	public void loginscreenHTML(String message, Model model) {

		model.addAttribute("message", StringUtils.trimToEmpty(message));
		model.addAttribute("lawlink", StringUtils.trimToEmpty(linkToLawSite));
	}

	public void listHTML(Long yPos, String searchString, StringBuilder sb) {

		String json = listJson(searchString, yPos, false);

		String html = IOUtil.readContentFromFileInClasspath("list.html");
		String htmlHead = IOUtil.readContentFromFileInClasspath("htmlhead");
		html = StringUtils.replace(html, "<!-- HEAD -->", htmlHead);
		html = StringUtils.replace(html, "/*JSONFILE*/", json);
		sb.append(html);
	}

	public String listJson(String s, Long yPos, boolean withHashesAndUsers) {

		String user = requestUtil.assertUserAndGetName();
		String jsonDir = lookupListDir();
		GalleryViewCache.getInstance().refresh(jsonDir, gson);

		List<GalleryView> galleryViews = new LinkedList<>();
		Set<String> keySet = GalleryViewCache.getInstance().keySet();

		for (String string : keySet) {
			GalleryView galleryView = GalleryViewCache.getInstance().read(string);
			if (galleryView.getUsersAsList().contains(user) || user.equals(technicalUser)) {
				galleryViews.add(galleryView);
			}
		}

		galleryViews.sort(new GalleryViewComparator());
		Collections.reverse(galleryViews);

		GalleryList galleryList = new GalleryList(user, galleryViews.size(), yPos, StringUtils.trimToEmpty(s));
		for (GalleryView view : galleryViews) {
			galleryList.addItem(view.getKey(), view.getGalleryDisplayName(),
					view.getGalleryDisplayIdentifier(), view.getGalleryDisplayNormDate(),
					withHashesAndUsers ? view.getGalleryhash() : null,
					withHashesAndUsers ? view.getUsers() : null);
		}
		return gson.toJson(galleryList);
	}

	public String lookupListDir() {
		return listDir;

	}

	public String lookupLinkToLawSite(){
		return linkToLawSite;
	}

	private GalleryView deepCopyGalleryView(GalleryView galleryView){
		return gson.fromJson(gson.toJson(galleryView), GalleryView.class);
	}
}
