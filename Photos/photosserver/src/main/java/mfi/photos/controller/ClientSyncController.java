package mfi.photos.controller;

import mfi.photos.server.Processor;
import mfi.photos.server.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
public class ClientSyncController {

	private static final String UTF_8 = "UTF-8";

	@Autowired
	private Processor processor;

	@Autowired
	private UserService userService;

	@RequestMapping("/PhotosAddGalleryServlet")
	public void response(HttpServletRequest request, HttpServletResponse response) throws IOException {

		Optional<String> username = userService.lookupUserName();
		if(username.isEmpty()){
			response.setStatus(401);
			return;
		}

		Map<String, String> params = new HashMap<>();
		Enumeration<String> parameterNames = request.getParameterNames();
		while (parameterNames.hasMoreElements()) {
			String key = parameterNames.nextElement();
			params.put(key, request.getParameter(key));
		}

		response.setCharacterEncoding(UTF_8);
		response.addHeader("Cache-Control", "no-cache");

		PrintWriter out = response.getWriter();

		// TODO: write separate methods for each request
		if (params.containsKey("saveGallery")) {
			processor.saveNewGallery(params);
		} else if (params.containsKey("renameGallery")) {
			processor.renameGallery(params);
		} else if (params.containsKey("saveImage")) {
			processor.saveNewImage(params);
		} else if (params.containsKey("checksum")) {
			out.print(processor.checksumFromImage(params));
		} else if (params.containsKey("readlist")) {
			// -> GalleryList
			out.print(processor.listJson(username.get(), null, 0, true));
		} else if (params.containsKey("readalbum")) {
			// -> GalleryView
			out.println(processor.galleryJson(username.get(), params.get("album_key")));
		} else if (params.containsKey("cleanup")) {
			processor.cleanUp(params.get("cleanup"), params.get("cleanupListHash"));
		} else if (params.containsKey("testConnection")) {
			// noop
		}
		response.setStatus(200);

		out.flush();
		out.close();
	}

}
