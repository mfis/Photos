package mfi.photos.shared;

import lombok.Data;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

@Data
public class GalleryView {

	private String key;
	private String galleryname;
	private String galleryDisplayName;
	private String galleryDisplayIdentifier;
	private String galleryDisplayNormDate;
	private String baseURL;
	private String galleryhash;
	private String sortKey;
	private String assetCookie;
	private Picture[] pictures;
	private String[] users;

	public GalleryView(String key, String galleryname, int size, String[] users, String baseURL, String galleryhash) {
		this.key = key;
		this.galleryname = galleryname;
		this.users = users;
		this.pictures = new Picture[size];
		this.baseURL = baseURL;
		this.galleryhash = galleryhash;
	}

	public void addItem(String name, int tnh, int tnw, int h, int w, String hash, long fileSize) {
		Picture newPicture = new Picture();
		newPicture.setName(name);
		newPicture.setTnh(tnh);
		newPicture.setTnw(tnw);
		newPicture.setH(h);
		newPicture.setW(w);
		newPicture.setHash(hash);
		newPicture.setFileSize(fileSize);
		for (int i = 0; i < this.pictures.length; i++) {
			if (this.pictures[i] == null) {
				this.pictures[i] = newPicture;
				break;
			}
		}
	}

	public void compressItems() {
		List<Picture> picList = new LinkedList<>();
		for (Picture picture : pictures) {
			if (picture != null) {
				picList.add(picture);
			}
		}
		this.pictures = new Picture[picList.size()];
		this.pictures = picList.toArray(this.pictures);
	}

	public void truncateHashes() {
		for (Picture picture : pictures) {
			picture.setHash(null);
		}
	}

	public List<String> getUsersAsList() {
		if (users == null) {
			return new LinkedList<>();
		} else {
			return Arrays.asList(users);
		}
	}
}
