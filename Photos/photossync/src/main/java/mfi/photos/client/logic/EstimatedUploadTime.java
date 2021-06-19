package mfi.photos.client.logic;

import java.util.LinkedList;
import java.util.List;

public class EstimatedUploadTime {

	private final static long UPLOAD_TIME_GALLERY_JSON = 1000L;
	private final static long CLEANUP_TIME = 1000L;

	private final int albumCount;
	private final long sumFileSize;
	List<Long> megaByteProcessingDuration;
	private long bytesProcessed;
	private int albumCompletedCount;
	private long actualPhotoUploadStartTime;
	private boolean completed;

	public EstimatedUploadTime(int albumCount, long sumFileSize) {
		this.albumCount = albumCount;
		this.sumFileSize = sumFileSize;
		megaByteProcessingDuration = new LinkedList<>();
		albumCompletedCount = 0;
		actualPhotoUploadStartTime = 0;
		bytesProcessed = 0;
		completed = false;
	}

	public void startUpload() {
		actualPhotoUploadStartTime = System.currentTimeMillis();
	}

	public void completedPhotoUpload(long fileSize) {
		long duration = System.currentTimeMillis() - actualPhotoUploadStartTime;
		double durMB = (1024d * 1024d / fileSize) * (duration);
		megaByteProcessingDuration.add((long) durMB);
		actualPhotoUploadStartTime = System.currentTimeMillis();
		bytesProcessed += fileSize;
	}

	public void completedAlbumUpload() {
		albumCompletedCount++;
	}

	public void completed() {
		completed = true;
	}

	public String estimatedTime() {

		if (megaByteProcessingDuration.size() < 5) {
			return null;
		}

		if (completed) {
			return null;
		}

		long sum = 0;
		for (long l : megaByteProcessingDuration) {
			sum += l;
		}
		long average = sum / megaByteProcessingDuration.size();

		long estimated = average * (sumFileSize - bytesProcessed) / 1024 / 1024;
		estimated += ((albumCount + albumCompletedCount) * UPLOAD_TIME_GALLERY_JSON);
		estimated += CLEANUP_TIME;
		int seconds = (int) (estimated / 1000L);
		if (seconds > 60 * 60) {
			System.out.println(seconds);
		}

		return formatSeconds(seconds);
	}

	public static String formatSeconds(int seconds) {

		if (seconds < 50) {
			return "Weniger als 1 Minute";
		} else if (seconds < (60 * 60)) { // 1h
			int m = (seconds / 60);
			if (seconds - (m * 60) > 30) {
				m++;
			}
			return (m == 0 ? 1 : m) + (" Minute" + (m == 1 ? "" : "n"));
		} else {
			int h = seconds / (60 * 60);
			int m = (seconds / 60) - (h * 60);
			return h + " Stunde" + (h == 1 ? "" : "n") + (m == 0 ? "" : (", " + m + " Minute" + (m == 1 ? "" : "n")));
		}
	}
}
