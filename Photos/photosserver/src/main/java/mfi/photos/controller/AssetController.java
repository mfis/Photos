package mfi.photos.controller;

import mfi.photos.server.FileDownloadUtil;
import mfi.photos.server.Processor;
import mfi.photos.shared.GalleryView;
import mfi.photos.util.GalleryViewCache;
import mfi.photos.util.RequestUtil;
import org.apache.commons.text.StringEscapeUtils;
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
public class AssetController {

	@Autowired
	private Processor processor;

    @Autowired
	private RequestUtil requestUtil;

	private static final FileDownloadUtil fileDownloadUtil = new FileDownloadUtil();

	@RequestMapping(value = { "/assets/**" }, method = { RequestMethod.HEAD })
	public @ResponseBody void responseHead(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		response.addHeader("Cache-Control", "no-cache");
		responseInternal(request, response, false);
	}

	@GetMapping(value = { "/assets/**" })
	public @ResponseBody void responseGetPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		response.addHeader("Cache-Control", "private, max-age=600");
		responseInternal(request, response, true);
	}

	private void responseInternal(HttpServletRequest request, HttpServletResponse response, boolean content)
			throws IOException {

		requestUtil.assertLoggedInUser();

		File file = processor.lookupAssetFile(request.getRequestURI());
		GalleryView view = GalleryViewCache.getInstance().read(file.getParentFile().getName());
		if (file.exists() && view != null) {
			if(view.getUsersAsList().contains(requestUtil.assertUserAndGetName())){
				fileDownloadUtil.process(request, response, file, content);
			}else{
				response.setStatus(401);
				return;
			}
		}else{
			response.setStatus(404);
			return;
		}
	}
}
