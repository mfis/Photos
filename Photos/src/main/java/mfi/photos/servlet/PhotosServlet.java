package mfi.photos.servlet;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import mfi.photos.server.logic.Processor;
import mfi.photos.util.CookieMap;
import mfi.photos.util.KeyAccess;
import mfi.photos.util.ServletUtil;

@Controller
public class PhotosServlet {

	private static final String UTF_8 = "UTF-8";

	private static final Processor processor = new Processor();

	@RequestMapping("/PhotosServlet")
	public @ResponseBody void response(HttpServletRequest request, HttpServletResponse response)
			throws IOException {

		Map<String, String> params = new HashMap<>();
		Enumeration<String> parameterNames = request.getParameterNames();
		while (parameterNames.hasMoreElements()) {
			String key = parameterNames.nextElement();
			params.put(key, request.getParameter(key));
		}

		StringBuilder sb = new StringBuilder();

		response.setContentType("text/html");
		response.setCharacterEncoding(UTF_8);
		response.addHeader("Content-Encoding", "gzip");
		response.addHeader("Cache-Control", "no-cache");

		String user = StringUtils.trimToNull(CookieMap.getInstance().read(ServletUtil.cookieRead(request)));
		String newCookie = null;

		if (params.containsKey("login_user")) {
			newCookie = checkLogin(params, request, response, sb);
		} else if (!KeyAccess.getInstance().isKeySet()) {
			processor.loginscreenHTML(sb, null);
		} else if (params.containsKey("gallery") && user != null) {
			newCookie = ServletUtil.setNewCookie(request, response, user);
			processor.galleryHTML(params, sb, user, newCookie);
		} else if (params.containsKey("list") && user != null) {
			newCookie = ServletUtil.setNewCookie(request, response, user);
			processor.listHTML(params, sb, user);
		} else if (params.containsKey("logoff")) {
			ServletUtil.cookieDelete(request, response);
			processor.loginscreenHTML(sb, null);
		} else if (user != null) {
			newCookie = ServletUtil.setNewCookie(request, response, user);
			processor.listHTML(params, sb, user);
		} else {
			processor.loginscreenHTML(sb, null);
		}

		response.setStatus(200);

		OutputStream out = new GZIPOutputStream(response.getOutputStream());
		out.write(sb.toString().getBytes(UTF_8));
		out.flush();
		out.close();
	}

	private String checkLogin(Map<String, String> params, HttpServletRequest request,
			HttpServletResponse response, StringBuilder sb) throws IOException {

		if (!StringUtils.trimToEmpty(params.get("cookieok")).equals("true")) {
			processor.loginscreenHTML(sb,
					"Sie m&uuml;ssen zur Anmeldung zun&auml;chst der Datenschutzerkl&auml;rung zustimmen.");
			return null;
		}
		String newCookie = null;
		String loginUser = StringUtils.trimToEmpty(params.get("login_user"));
		String loginPass = StringUtils.trimToEmpty(params.get("login_pass"));
		boolean loginSuccessful = processor.checkAuthentication(loginUser, loginPass);
		if (loginSuccessful) {
			if (KeyAccess.getInstance().isKeySet()) {
				newCookie = ServletUtil.setNewCookie(request, response, loginUser);
				processor.listHTML(params, sb, loginUser);
			} else {
				processor.loginscreenHTML(sb, "Anmeldung zur Zeit nicht möglich");
			}
		} else {
			processor.loginscreenHTML(sb, "Anmeldung nicht erfolgreich");
		}
		return newCookie;
	}

}
