package mfi.photos.util;

import mfi.photos.auth.AuthService;
import mfi.photos.auth.UserAuthentication;
import mfi.photos.auth.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;
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

        if(SecurityContextHolder.getContext().getAuthentication()!=null){
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof UserPrincipal) {
                return Optional.of(((UserPrincipal)principal));
            }
        }

        return Optional.empty();
    }

    public static String loginRequestPath(){
        return "/login";
    }

    public static List<String> antRequestPathsWithoutAuthentication(){
        var dirs = List.of("/staticresources/*.", "/photoswipe/*.", "/photoswipe/*/*.");
        var suffixes = List.of("js", "css", "png", "ico");
        var antPaths = new LinkedList<String>();
        dirs.forEach(dir -> suffixes.forEach(suffix -> antPaths.add(dir + suffix)));
        return antPaths;
    }

}
