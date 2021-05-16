package mfi.photos.util;

import mfi.photos.server.ContextListener;
import mfi.photos.server.Processor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.CharacterPredicates;
import org.apache.commons.text.RandomStringGenerator;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

@Component
public class ServletUtil {

    private static final String COOKIE_NAME = "PhotosLoginCookie";

    public String setNewCookie(HttpServletRequest request, HttpServletResponse response, String loginUser) {

        String uuid = UUID.randomUUID().toString() + "_" + new RandomStringGenerator.Builder().withinRange('0', 'z')
            .filteredBy(CharacterPredicates.LETTERS, CharacterPredicates.DIGITS).build().generate(3600);
        cookieWrite(response, uuid);

        return uuid;
    }

    public String cookieRead(HttpServletRequest request) {

        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(COOKIE_NAME)) {
                return StringUtils.trimToNull(cookie.getValue());
            }
        }
        return null;
    }

    public void cookieDelete(HttpServletRequest request, HttpServletResponse response) {

        Cookie cookie = new Cookie(COOKIE_NAME, "");
        cookie.setMaxAge(0);
        cookie.setHttpOnly(true);
        response.addCookie(cookie);
    }

    private void cookieWrite(HttpServletResponse response, String value) {

        Cookie cookie = new Cookie(COOKIE_NAME, value);
        cookie.setMaxAge(60 * 60 * 24 * 92);
        cookie.setHttpOnly(true);
        response.addCookie(cookie);
    }
}
