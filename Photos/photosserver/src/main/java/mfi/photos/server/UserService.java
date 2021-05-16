package mfi.photos.server;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserService {

    public Optional<String> lookupUserName(){

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(principal ==null){
            return Optional.empty();
        }

        String username;
        if (principal instanceof UserDetails) {
            username = ((UserDetails)principal).getUsername();
        } else {
            username = principal.toString();
        }
        return Optional.of(username);
    }

}
