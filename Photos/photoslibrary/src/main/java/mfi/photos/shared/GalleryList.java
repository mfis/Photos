package mfi.photos.shared;

import lombok.Data;

@Data
public class GalleryList {

	private String user;
	private Long posY;
	private String search;
	private Item[] list;

	public GalleryList(String user, int size, Long posY, String search) {
		this.list = new Item[size];
		this.user = user;
		this.posY = posY;
		this.search = search;
	}

	public void addItem(String key, String name, String identifier, String normDate, String hash, String[] users) {
		Item newItem = new Item();
		newItem.setKey(key);
		newItem.setName(name);
		newItem.setIdentifier(identifier);
		newItem.setNormDate(normDate);
		newItem.setHash(hash);
		newItem.setUsers(users);
		for (int i = 0; i < this.list.length; i++) {
			if (this.list[i] == null) {
				this.list[i] = newItem;
				break;
			}
		}
	}

	public Item[] getList() {
		return list;
	}

}
