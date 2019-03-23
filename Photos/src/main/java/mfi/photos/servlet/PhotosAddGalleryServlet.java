package mfi.photos.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import mfi.photos.server.logic.Processor;

@Controller
public class PhotosAddGalleryServlet {

	private static final String UTF_8 = "UTF-8";

	private static final Processor processor = new Processor();

	@RequestMapping("/PhotosAddGalleryServlet")
	public void response(HttpServletRequest request, HttpServletResponse response) throws IOException {

		Map<String, String> params = new HashMap<>();
		Enumeration<String> parameterNames = request.getParameterNames();
		while (parameterNames.hasMoreElements()) {
			String key = parameterNames.nextElement();
			params.put(key, request.getParameter(key));
		}

		response.setContentType("text/html");
		response.setCharacterEncoding(UTF_8);
		response.addHeader("Content-Encoding", "gzip");
		response.addHeader("Cache-Control", "no-cache");

		String loginUser = StringUtils.trimToEmpty(params.get("login_user"));
		String loginPass = StringUtils.trimToEmpty(params.get("login_pass"));
		boolean loginSuccessful = processor.checkAuthentication(loginUser, loginPass);
		String responseContent = "";
		if (loginSuccessful) {
			if (params.containsKey("saveGallery")) {
				processor.saveNewGallery(params);
			} else if (params.containsKey("renameGallery")) {
				processor.renameGallery(params);
			} else if (params.containsKey("saveImage")) {
				processor.saveNewImage(params);
			} else if (params.containsKey("checksum")) {
				responseContent = processor.checksumFromImage(params);
			} else if (params.containsKey("readlist")) {
				// -> GalleryList
				responseContent = processor.listJson(loginUser, null, 0, true);
			} else if (params.containsKey("readalbum")) {
				// -> GalleryView
				responseContent = processor.galleryJson(loginUser, params.get("album_key"));
			} else if (params.containsKey("cleanup")) {
				processor.cleanUp(params.get("cleanup"), params.get("cleanupListHash"));
			} else if (params.containsKey("testConnection")) {
				// noop
			}
			response.setStatus(200);
		} else {
			response.setStatus(401);
		}

		PrintWriter out = response.getWriter();
		out.println(responseContent);
		out.flush();
		out.close();
	}

}
