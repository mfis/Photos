package mfi.photos.servlet;

import java.io.File;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import mfi.photos.server.logic.FileDownloadUtil;
import mfi.photos.server.logic.Processor;
import mfi.photos.util.CookieMap;

@Controller
public class PhotosAssetServlet {

	@Autowired
	private Processor processor;

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

		String assetCookie = request.getParameter("ac");
		String user = StringUtils.trimToNull(CookieMap.getInstance().read(assetCookie));

		if (user != null && !file.exists()) {
			response.setStatus(404);
		} else if (user != null) {
			fileDownloadUtil.process(request, response, file, content);
		} else {
			response.setStatus(401);
		}
	}

}
