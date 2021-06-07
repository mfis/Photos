package mfi.photos.controller;

import lombok.extern.apachecommons.CommonsLog;
import mfi.photos.server.Processor;
import mfi.photos.util.RequestUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Controller
@CommonsLog
public class WebController {

	private static final String UTF_8 = "UTF-8";

	@Autowired
	private Processor processor;

	@Autowired
	private RequestUtil requestUtil;

	@Autowired
	private Environment env;

	@RequestMapping("/login")
	public @ResponseBody void login(HttpServletResponse response, @RequestParam(name = "reason", required = false) String reason) throws IOException {

		log.debug("/login reason=" + reason);
		response.setContentType("text/html");
		response.setCharacterEncoding(UTF_8);
		response.addHeader("Cache-Control", "no-cache");
		response.getWriter().print(processor.loginscreenHTML(loginReasonText(reason)));
	}

	private String loginReasonText(String key){
		if(StringUtils.isBlank(key)){
			return null;
		}
		return env.getProperty("login.failure.reason." + key);
	}

	@GetMapping("/logout")
	public @ResponseBody void logout(HttpServletResponse response) throws IOException {

		log.debug("/logout");
		// TODO: servletUtil.cookieDelete(request, response);
		response.setContentType("text/html");
		response.setCharacterEncoding(UTF_8);
		response.addHeader("Cache-Control", "no-cache");
		response.getWriter().print(processor.loginscreenHTML("Sie wurden abgemeldet."));
	}

	@RequestMapping("/")
	public @ResponseBody void response(HttpServletResponse response,
									   @RequestParam(name = "y", required = false) Long yPos,
									   @RequestParam(name = "s", required = false) String searchString,
									   @RequestParam(name = "gallery", required = false) String galleryName
									   ) throws IOException {

		log.debug("/");
		requestUtil.assertLoggedInUser();

		StringBuilder sb = new StringBuilder();

		response.setContentType("text/html");
		response.setCharacterEncoding(UTF_8);
		response.addHeader("Cache-Control", "no-cache");

		PrintWriter out = response.getWriter();

		// TODO: write separate methods for each request

		if (galleryName!=null) {
			processor.galleryHTML(yPos, searchString, galleryName, sb);
		} else {
			processor.listHTML(yPos, searchString, sb);
		}

		response.setStatus(200);

		out.write(sb.toString());
		out.flush();
		out.close();
	}
}
