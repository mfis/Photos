package mfi.photos.util;

import mfi.photos.server.logic.ContextListener;
import mfi.photos.server.logic.Processor;
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

    @Autowired
    private Processor processor;

    private static final String COOKIE_NAME = "PhotosLoginCookie";

    private int cookieNotFoundCounter = 0;

    public String setNewCookie(HttpServletRequest request, HttpServletResponse response, String loginUser) {

        String oldCookieID = cookieRead(request);

        String uuid = UUID.randomUUID().toString() + "_" + new RandomStringGenerator.Builder().withinRange('0', 'z')
            .filteredBy(CharacterPredicates.LETTERS, CharacterPredicates.DIGITS).build().generate(3600);
        cookieWrite(response, uuid);
        CookieMap.getInstance().write(uuid, loginUser);
        if (StringUtils.isNotBlank(oldCookieID)) {
            CookieMap.getInstance().delete(oldCookieID);
        }

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

    public String userFromCookie(HttpServletRequest request, HttpServletResponse response) {
        String cookie = cookieRead(request);
        String user = null;
        if (StringUtils.isNotBlank(cookie)) {
            user = CookieMap.getInstance().read(StringUtils.trimToEmpty(cookie));
        }
        if (StringUtils.isNotBlank(cookie) && StringUtils.isBlank(user)) {
            LoggerFactory.getLogger(ContextListener.class).error("FALSE LOGIN ATTEMPT");
            cookieNotFoundCounter++;
            if (cookieNotFoundCounter > 6) {
                LoggerFactory.getLogger(ContextListener.class).error("DELETING ALL LOGIN COOKIES");
                CookieMap.getInstance().reset();
                try {
                    CookieMap.getInstance().saveTo(processor.getApplicationProperties());
                    cookieNotFoundCounter = 0;
                } catch (Exception e) {
                    LoggerFactory.getLogger(ContextListener.class).error("error saving cookiemap", e);
                }
            }
            cookieDelete(request, response);
        }
        return user;
    }

    public void cookieDelete(HttpServletRequest request, HttpServletResponse response) {

        String oldCookie = cookieRead(request);
        if (oldCookie != null) {
            CookieMap.getInstance().delete(oldCookie);
        }

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
