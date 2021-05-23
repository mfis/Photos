package mfi.photos.servlet;

import lombok.extern.apachecommons.CommonsLog;
import lombok.extern.log4j.Log4j;
import mfi.photos.server.Processor;
import mfi.photos.server.UserService;
import mfi.photos.util.ServletUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
@CommonsLog
public class PhotosServlet {

	private static final String UTF_8 = "UTF-8";

	@Autowired
	private Processor processor;

    @Autowired
    private ServletUtil servletUtil;

	@Autowired
	private UserService userService;

	@RequestMapping("/login")
	public @ResponseBody void login(HttpServletResponse response, @RequestParam(name = "msg", required = false) String msg) throws IOException {

		log.info("/login msg=" + msg);
		response.setContentType("text/html");
		response.setCharacterEncoding(UTF_8);
		response.addHeader("Cache-Control", "no-cache");
		response.getWriter().print(processor.loginscreenHTML(null));
	}

	@GetMapping("/logout")
	public @ResponseBody void logout(HttpServletRequest request, HttpServletResponse response) throws IOException {

		log.info("/logout");
		servletUtil.cookieDelete(request, response);
		response.setContentType("text/html");
		response.setCharacterEncoding(UTF_8);
		response.addHeader("Cache-Control", "no-cache");
		response.getWriter().print(processor.loginscreenHTML("Sie wurden abgemeldet."));
	}

	@RequestMapping("/")
	public @ResponseBody void response(HttpServletRequest request, HttpServletResponse response) throws IOException {

		log.info("/");
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

		StringBuilder sb = new StringBuilder();

		response.setContentType("text/html");
		response.setCharacterEncoding(UTF_8);
		response.addHeader("Cache-Control", "no-cache");

		PrintWriter out = response.getWriter();

		// TODO: write separate methods for each request
		// TODO: if !KeyAccess.getInstance().isKeySet() -> login required!
		// if (!KeyAccess.getInstance().isKeySet()) {
		//	method.addParameter("getSecretForUser", properties.getProperty("technicalUser"));
		//}
		//if (ok && !KeyAccess.getInstance().isKeySet()) {
		//	KeyAccess.getInstance().setKey(method.getResponseBodyAsString());
		//}

		if (params.containsKey("gallery") ) {
			processor.galleryHTML(params, sb);
		} else if (params.containsKey("list")) {
			processor.listHTML(params, sb);
		} else {
			processor.listHTML(params, sb);
		}

		response.setStatus(200);

		out.write(sb.toString());
		out.flush();
		out.close();
	}
}
