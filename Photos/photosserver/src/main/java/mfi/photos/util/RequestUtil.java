package mfi.photos.util;

import mfi.photos.auth.AuthService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.CharacterPredicates;
import org.apache.commons.text.RandomStringGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

@Component
public class RequestUtil {

    @Autowired
    private AuthService authService;

    public static final String HEADER_USER_AGENT = "user-agent";

    public static final String COOKIE_NAME = "PhotosLoginCookie";

    public void assertLoggedInUser(){
        if(authService.lookupUserName().isEmpty()){
            throw new IllegalCallerException("No known user logged in");
        }
    }

}
