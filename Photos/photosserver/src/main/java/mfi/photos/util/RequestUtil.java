package mfi.photos.util;

import mfi.photos.auth.AuthService;
import mfi.photos.auth.UserAuthentication;
import mfi.photos.auth.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class RequestUtil {

    @Autowired
    private AuthService authService;

    public static final String HEADER_USER_AGENT = "user-agent";

    public static final String COOKIE_NAME = "PhotosLoginCookie";

    public void assertLoggedInUser(){
        if(lookupUserPrincipal().isEmpty()){
            throw new IllegalCallerException("No known user logged in");
        }
    }

    public void defineUserForRequest(UserAuthentication userAuthentication){
        SecurityContextHolder.getContext().setAuthentication(userAuthentication);
    }

    public Optional<UserPrincipal> lookupUserPrincipal(){

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserPrincipal) {
            return Optional.of(((UserPrincipal)principal));
        }
        return Optional.empty();
    }

}
