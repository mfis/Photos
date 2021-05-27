package mfi.photos.auth;

import lombok.extern.apachecommons.CommonsLog;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@CommonsLog
public class AuthService {

    public Optional<UserAuthentication> checkUserWithPassword(String user, String password){

        boolean checkOK = user !=null && user.equals("test"); // FIXME
        log.info("checkOK=" + checkOK);
        if(checkOK){
            return Optional.of(new UserAuthentication(new UserPrincipal(user)));
        }

        return Optional.empty();
    }

    public Optional<String> lookupUserName(){

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserPrincipal) {
            return Optional.of(((UserPrincipal)principal).getName());
        }
        return Optional.empty();
    }

}
