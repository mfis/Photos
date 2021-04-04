package mfi.photos.servlet;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import mfi.photos.util.IOUtil;

@Controller
public class PhotosServlet {

    private static final String UTF_8 = "UTF-8";

    private static final String COOKIE_NAME = "PhotosLoginCookie";

    @RequestMapping("/")
    public @ResponseBody void response(HttpServletRequest request, HttpServletResponse response) throws IOException {

        StringBuilder sb = new StringBuilder();

        response.setContentType("text/html");
        response.setCharacterEncoding(UTF_8);
        response.addHeader("Content-Encoding", "gzip");
        response.addHeader("Cache-Control", "no-cache");
        response.setHeader("Referrer-Policy", "no-referrer");
        response.setHeader("content-security-policy", "frame-ancestors 'none';");
        response.setHeader("X-Frame-Options", "deny");
        response.setHeader("X-Content-Type-Options", "nosniff");

        loginscreenHTML(sb, "Aufgrund von Wartungsarbeiten steht diese Anwendung zur Zeit nicht zur Verfügung.");
        cookieDelete(request, response);

        response.setStatus(200);

        OutputStream out = new GZIPOutputStream(response.getOutputStream());
        out.write(sb.toString().getBytes(UTF_8));
        out.flush();
        out.close();
    }

    private void loginscreenHTML(StringBuilder sb, String message) {

        String html = IOUtil.readContentFromFileInClasspath("login.html");
        String htmlHead = IOUtil.readContentFromFileInClasspath("htmlhead");
        html = StringUtils.replace(html, "<!-- HEAD -->", htmlHead);
        html = StringUtils.replace(html, "/*JSONFILE*/", StringUtils.trimToEmpty(message));
        html = StringUtils.replace(html, "/*LAWLINK*/", "https://fimatas.de/impressum/");
        sb.append(html);
    }

    private void cookieDelete(HttpServletRequest request, HttpServletResponse response) {

        Cookie cookie = new Cookie(COOKIE_NAME, "");
        cookie.setMaxAge(0);
        cookie.setHttpOnly(true);
        response.addCookie(cookie);
    }

}
