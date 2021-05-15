package mfi.photos.servlet;

import mfi.photos.server.logic.FileDownloadUtil;
import mfi.photos.server.logic.Processor;
import mfi.photos.util.ServletUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

@Controller
public class PhotosAssetServlet {

	@Autowired
	private Processor processor;

    @Autowired
    private ServletUtil servletUtil;

	private static final FileDownloadUtil fileDownloadUtil = new FileDownloadUtil();

	@RequestMapping(value = { "/assets/**" }, method = { RequestMethod.HEAD })
	public @ResponseBody void responseHead(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		responseInternal(request, response, false);
	}

	@GetMapping(value = { "/assets/**" })
	public @ResponseBody void responseGetPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		responseInternal(request, response, true);
	}

	private void responseInternal(HttpServletRequest request, HttpServletResponse response, boolean content)
			throws IOException {
		File file = processor.lookupAssetFile(request.getRequestURI());

        String user = servletUtil.userFromCookie(request, response);

		if (user != null && !file.exists()) {
			response.setStatus(404);
		} else if (user != null) {
			fileDownloadUtil.process(request, response, file, content);
		} else {
			response.setStatus(401);
		}
	}

}
