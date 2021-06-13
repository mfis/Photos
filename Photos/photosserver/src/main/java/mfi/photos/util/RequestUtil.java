package mfi.photos.util;

import mfi.photos.auth.UserAuthentication;
import mfi.photos.auth.UserPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@Component
public class RequestUtil {

    public static final String PATH_LOGIN = "/login";

    public static final String PATH_LOGOUT = "/logout";

    public static final String HEADER_USER_AGENT = "user-agent";

    public static final String COOKIE_NAME = "PhotosLoginCookie";

    public void assertLoggedInUser(){
        if(lookupUserPrincipal().isEmpty()){
            throw new IllegalCallerException("No known user logged in");
        }
    }

    public String assertUserAndGetName(){
        Optional<UserPrincipal> principal = lookupUserPrincipal();
        if(principal.isPresent()){
            return principal.get().getName();
        }else{
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
        return PATH_LOGIN;
    }

    public static String logoutRequestPath(){
        return PATH_LOGOUT;
    }

    public static List<String> antRequestPathsWithoutAuthentication(){
        var dirs = List.of("/staticresources/*.", "/photoswipe/*.", "/photoswipe/*/*.");
        var suffixes = List.of("js", "css", "png", "ico", "svg");
        var antPaths = new LinkedList<String>();
        dirs.forEach(dir -> suffixes.forEach(suffix -> antPaths.add(dir + suffix)));
        return antPaths;
    }

    public void setEssentialHeader(HttpServletResponse response) {
        response.setHeader("Referrer-Policy", "no-referrer");
        response.setHeader("content-security-policy", "frame-ancestors 'none';");
        response.setHeader("X-Frame-Options", "deny");
        response.setHeader("X-Content-Type-Options", "nosniff");
    }

}
