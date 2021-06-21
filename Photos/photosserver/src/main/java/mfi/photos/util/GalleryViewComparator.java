package mfi.photos.util;

import mfi.photos.shared.GalleryView;

import java.util.Comparator;

public class GalleryViewComparator implements Comparator<GalleryView> {

	@Override
	public int compare(GalleryView o1, GalleryView o2) {

		if (o1 == null && o2 == null) {
			return 0;
		}

		if (o1 == null) {
			return 1;
		}

		if (o2 == null) {
			return -1;
		}

		return o1.getSortKey().compareToIgnoreCase(o2.getSortKey());
	}

}
