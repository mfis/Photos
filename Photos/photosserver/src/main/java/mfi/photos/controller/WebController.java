package mfi.photos.controller;

import lombok.extern.apachecommons.CommonsLog;
import mfi.photos.server.Processor;
import mfi.photos.shared.GalleryView;
import mfi.photos.util.GalleryViewCache;
import mfi.photos.util.RequestUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
@CommonsLog
public class WebController {

	@Autowired
	private Processor processor;

	@Autowired
	private RequestUtil requestUtil;

	@Autowired
	private Environment env;

	@RequestMapping(RequestUtil.PATH_LOGIN)
	public String login(Model model, HttpServletResponse response, @RequestParam(name = "reason", required = false) String reason) {

		requestUtil.setEssentialHtmlRequestHeader(response);
		processor.loginscreenHTML(loginReasonText(reason), model);
		return "login";
	}

	@GetMapping(RequestUtil.PATH_LOGOUT)
	public String logout(Model model, HttpServletResponse response) {

		requestUtil.setEssentialHtmlRequestHeader(response);
		processor.loginscreenHTML("Sie wurden abgemeldet.", model);
		return "login";
	}

	@RequestMapping("/")
	public String response(Model model, HttpServletResponse response,
									   @RequestParam(name = "y", required = false) Long yPos,
									   @RequestParam(name = "s", required = false) String searchString,
									   @RequestParam(name = "gallery", required = false) String galleryName
									   ) throws IOException {

		requestUtil.assertLoggedInUser();
		requestUtil.setEssentialHtmlRequestHeader(response);

		if (galleryName!=null) {
			GalleryView view = GalleryViewCache.getInstance().read(StringEscapeUtils.escapeHtml4(galleryName));
			String user = requestUtil.assertUserAndGetName();
			if (view != null && view.getUsersAsList().contains(user)) {
				processor.galleryHTML(yPos, searchString, galleryName, model);
				return "gallery";
			}
		}

		processor.listHTML(yPos, searchString, model);
		return "list";
	}

	private String loginReasonText(String key){

		if(StringUtils.isBlank(key)){
			return null;
		}
		return env.getProperty("login.failure.reason." + key);
	}
}
