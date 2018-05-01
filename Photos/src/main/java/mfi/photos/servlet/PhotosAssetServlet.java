package mfi.photos.servlet;

import java.io.File;
import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import mfi.photos.server.logic.FileDownloadUtil;
import mfi.photos.server.logic.Processor;
import mfi.photos.util.CookieMap;

/**
 * Servlet implementation class PhotosServlet
 */
public class PhotosAssetServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private Processor processor = null;

	private FileDownloadUtil fileDownloadUtil = null;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public PhotosAssetServlet() {
		super();
	}

	/**
	 * @see Servlet#init(ServletConfig)
	 */
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		processor = new Processor();
		fileDownloadUtil = new FileDownloadUtil();
	}

	/**
	 * @see Servlet#destroy()
	 */
	@Override
	public void destroy() {
		super.destroy();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response(request, response, true);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response(request, response, true);
	}

	@Override
	protected void doHead(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response(request, response, false);
	}

	private void response(HttpServletRequest request, HttpServletResponse response, boolean content)
			throws IOException {

		File file = processor.lookupAssetFile(request.getRequestURI());

		String assetCookie = request.getParameter("ac");
		String user = StringUtils.trimToNull(CookieMap.getInstance().read(assetCookie));

		if (user != null && !file.exists()) {
			response.setStatus(404);
		} else if (user != null) {
			fileDownloadUtil.process(request, response, getServletContext(), file, content);
		} else {
			response.setStatus(401);
		}
	}

}
