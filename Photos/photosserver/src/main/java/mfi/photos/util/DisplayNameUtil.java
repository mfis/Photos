package mfi.photos.util;

import mfi.photos.shared.GalleryView;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DisplayNameUtil {

	private static final Pattern P0_NOW = Pattern.compile("(9999_99_99)"); // jetzt
	private static final Pattern P1_FULL_DATE = Pattern.compile("[0-9]{4}[_]{1}[0-9]{2}[_]{1}[0-9]{2}"); // 2001_02_04
	private static final Pattern P2_YEAR_MONTH = Pattern.compile("[0-9]{4}[_]{1}[0-9]{2}[_]{1}[xX]{2}"); // 1999_01_XX
	private static final Pattern P3_YEAR = Pattern.compile("[0-9]{4}[_]{1}[Xx]{2}[_]{1}[xX]{2}"); // 1999_XX_XX
	private static final Pattern P4_DECADE = Pattern.compile("[0-9]{3}[Xx]{1}[_]{1}[Xx]{2}[_]{1}[xX]{2}"); // 198X_XX_XX
	private static final Pattern P5_UNDEFINED = Pattern.compile("[0-9Xx]{4}[_]{1}[0-9Xx]{2}[_]{1}[0-9Xx]{2}"); // XXXX_XX_XX

	private static final DateTimeFormatter DATE_PARSE_FULL = DateTimeFormatter.ofPattern("yyyy_MM_dd");
	private static final DateTimeFormatter DATE_FORMAT_FULL = DateTimeFormatter.ofPattern("dd.MM.yyyy")
			.withLocale(Locale.GERMAN);
	private static final DateTimeFormatter DATE_FORMAT_YEAR_MONTH = DateTimeFormatter.ofPattern("MMM yyyy")
			.withLocale(Locale.GERMAN);
	private static final DateTimeFormatter DATE_FORMAT_YEAR = DateTimeFormatter.ofPattern("yyyy")
			.withLocale(Locale.GERMAN);

	public static synchronized void createDisplayName(GalleryView galleryView) {

		String localName = StringUtils.trim(galleryView.getGalleryname());
		String date = StringUtils.substringBefore(localName, " ");
		String caption = StringUtils.trim(StringUtils.removeStart(localName, date));
		String displayDate;
		String sortKey = "";

		String[] dates = StringUtils.split(date, '-');
		String[] displayDates = new String[dates.length];
		for (int d = 0; d < dates.length; d++) {
			displayDates[d] = lookupDisplayDate(dates[d]);
			if (displayDates[d] == null) {
				displayDates[d] = "";
				caption = localName;
				sortKey = localName;
			} else {
				sortKey = dates[d];
			}
		}

		displayDate = StringUtils.join(displayDates, " - ");

		galleryView.setGalleryDisplayNormDate(date);
		galleryView.setGalleryDisplayIdentifier(displayDate);
		galleryView.setGalleryDisplayName(caption);
		galleryView.setSortKey(sortKey);
	}

	private static String lookupDisplayDate(String date) {

		String displayDate;
		Matcher m0Now = P0_NOW.matcher(date);
		if (m0Now.matches()) {
			displayDate = "jetzt";
		} else {
			Matcher m1FullDate = P1_FULL_DATE.matcher(date);
			if (m1FullDate.matches()) {
				LocalDate localDate = LocalDate.parse(date, DATE_PARSE_FULL);
				displayDate = localDate.format(DATE_FORMAT_FULL);
			} else {
				Matcher m2YearMonth = P2_YEAR_MONTH.matcher(date);
				if (m2YearMonth.matches()) {
					LocalDate localDate = LocalDate.parse(date.substring(0, 8) + "01", DATE_PARSE_FULL);
					displayDate = localDate.format(DATE_FORMAT_YEAR_MONTH);
				} else {
					Matcher m3Year = P3_YEAR.matcher(date);
					if (m3Year.matches()) {
						LocalDate localDate = LocalDate.parse(date.substring(0, 5) + "01_01",
								DATE_PARSE_FULL);
						displayDate = localDate.format(DATE_FORMAT_YEAR);
					} else {
						Matcher m4Decade = P4_DECADE.matcher(date);
						if (m4Decade.matches()) {
							displayDate = date.substring(0, 3) + "0'er";
						} else {
							Matcher m54Udefined = P5_UNDEFINED.matcher(date);
							if (m54Udefined.matches()) {
								displayDate = "";
							} else {
								displayDate = null;
							}
						}
					}
				}
			}
		}
		return displayDate;
	}
}
