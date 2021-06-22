package mfi.photos.controller;

import mfi.photos.server.FileDownload;
import mfi.photos.server.Processor;
import mfi.photos.shared.GalleryView;
import mfi.photos.util.GalleryViewCache;
import mfi.photos.util.RequestUtil;
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

    @Autowired
	private FileDownload fileDownload;

	@RequestMapping(value = { RequestUtil.ASSETS_ANT_PATH }, method = { RequestMethod.HEAD })
	public @ResponseBody void responseHead(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		response.addHeader("Cache-Control", "no-cache");
		responseInternal(request, response, false);
	}

	@GetMapping(value = { RequestUtil.ASSETS_ANT_PATH })
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
				fileDownload.process(request, response, file, content);
			}else{
				response.setStatus(401);
			}
		}else{
			response.setStatus(404);
		}
	}
}
