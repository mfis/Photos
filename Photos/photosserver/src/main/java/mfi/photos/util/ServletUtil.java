package mfi.photos.util;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

public class ServletUtil {

	private static final String COOKIE_NAME = "PhotosLoginCookie";

	public static String setNewCookie(HttpServletRequest request, HttpServletResponse response, String loginUser) {

		String oldCookieID = cookieRead(request);
		String oldCookieAssetID = oldCookieID == null ? null : assetCookieIdFromCookie(oldCookieID);

		String uuid = UUID.randomUUID().toString();
		String assetID = assetCookieIdFromCookie(uuid);
		ServletUtil.cookieWrite(response, uuid);
		CookieMap.getInstance().write(uuid, loginUser);
		CookieMap.getInstance().write(assetID, loginUser);
		if (StringUtils.isNotBlank(oldCookieID)) {
			if (oldCookieID != null) {
				CookieMap.getInstance().delete(oldCookieID);
			}
			if (oldCookieAssetID != null) {
				CookieMap.getInstance().delete(oldCookieAssetID);
			}
		}

		return uuid;
	}

	public static String assetCookieIdFromCookie(String cookie) {
		return "ac_" + new String(Base64.encodeBase64(DigestUtils.md5(cookie)), StandardCharsets.UTF_8)
				.replaceAll("[^A-Za-z0-9_]", "");
	}

	public static String cookieRead(HttpServletRequest request) {

		Cookie[] cookies = request.getCookies();
		if (cookies == null) {
			return null;
		}
		for (Cookie cookie : cookies) {
			if (cookie.getName().equals(COOKIE_NAME)) {
				return StringUtils.trimToNull(cookie.getValue());
			}
		}
		return null;
	}

	public static void cookieDelete(HttpServletRequest request, HttpServletResponse response) {

		String oldCookie = cookieRead(request);
		if (oldCookie != null) {
			CookieMap.getInstance().delete(oldCookie);
		}

		Cookie cookie = new Cookie(COOKIE_NAME, "");
		cookie.setMaxAge(0);
		response.addCookie(cookie);
	}

	private static void cookieWrite(HttpServletResponse response, String value) {

		Cookie cookie = new Cookie(COOKIE_NAME, value);
		cookie.setMaxAge(60 * 60 * 24 * 92);
		response.addCookie(cookie);
	}
}
