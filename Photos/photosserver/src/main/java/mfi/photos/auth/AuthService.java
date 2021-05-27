package mfi.photos.auth;

import lombok.extern.apachecommons.CommonsLog;
import mfi.files.api.DeviceType;
import mfi.files.api.TokenResult;
import mfi.files.api.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@CommonsLog
public class AuthService {

    @Autowired
    private UserService userService;

    public Optional<UserAuthentication> checkUserWithPassword(String user, String password, String userAgent){

        String loginUser = StringUtils.trimToEmpty(user);
        String loginPass = StringUtils.trimToEmpty(password);
        TokenResult tokenResult =
                userService.createToken(loginUser, loginPass, userAgent, DeviceType.BROWSER);
        if (tokenResult.isCheckOk()) {
            return Optional.of(new UserAuthentication(new UserPrincipal(user), tokenResult.getNewToken()));
        } else {
            return Optional.empty();
        }
    }

    public Optional<String> lookupUserName(){

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserPrincipal) {
            return Optional.of(((UserPrincipal)principal).getName());
        }
        return Optional.empty();
    }

}
