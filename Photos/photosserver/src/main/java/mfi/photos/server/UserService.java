package mfi.photos.server;

import mfi.photos.auth.UserPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserService {

    public Optional<String> lookupUserName(){

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserPrincipal) {
            return Optional.of(((UserPrincipal)principal).getName());
        }
        return Optional.empty();
    }

}
