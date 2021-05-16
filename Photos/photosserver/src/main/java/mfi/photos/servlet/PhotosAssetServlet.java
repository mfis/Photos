package mfi.photos.servlet;

import mfi.photos.server.logic.FileDownloadUtil;
import mfi.photos.server.logic.Processor;
import mfi.photos.server.logic.UserService;
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
import java.util.Optional;

@Controller
public class PhotosAssetServlet {

	@Autowired
	private Processor processor;

    @Autowired
	private UserService userService;

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

		Optional<String> username = userService.lookupUserName();
		if(username.isEmpty()){
			// TODO: check user rights to read specific album
			response.setStatus(401);
			return;
		}

		if (!file.exists()) {
			response.setStatus(404);
			return;
		}

		fileDownloadUtil.process(request, response, file, content);
	}
}
