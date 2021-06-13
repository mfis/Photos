package mfi.photos.auth;

import lombok.extern.apachecommons.CommonsLog;
import mfi.files.api.DeviceType;
import mfi.files.api.TokenResult;
import mfi.files.api.UserService;
import mfi.photos.util.RequestUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.servlet.ServletRequest;
import java.util.Optional;

@Component
@CommonsLog
public class AuthService {

    @Autowired
    private UserService userService;

    @Value("${technicalUser}")
    private String technicalUser;

    public Optional<UserAuthentication> checkUserWithPassword(String user, String password, String userAgent){

        String loginUser = StringUtils.trimToEmpty(user);
        String loginPass = StringUtils.trimToEmpty(password);
        TokenResult tokenResult =
                userService.createToken(loginUser, loginPass, userAgent, DeviceType.BROWSER);
        if (tokenResult.isCheckOk()) {
            return Optional.of(new UserAuthentication(new UserPrincipal(user, tokenResult.getNewToken()), tokenResult.getNewToken()));
        } else {
            return Optional.empty();
        }
    }

    public Optional<UserAuthentication> checkUserWithToken(String token, String userAgent, boolean isInitialRequest) {

        String user = userService.userNameFromLoginCookie(token);
        TokenResult tokenResult = userService.checkToken(user, token,
                userAgent, DeviceType.BROWSER, isInitialRequest);

        if (tokenResult.isCheckOk()) {
            if(tokenResult.getNewToken()==null){
                return Optional.of(new UserAuthentication(new UserPrincipal(user, token), null));
            }else{
                return Optional.of(new UserAuthentication(new UserPrincipal(user, tokenResult.getNewToken()), tokenResult.getNewToken()));
            }
        } else {
            return Optional.empty();
        }
    }

    public void logout(String token, String userAgent){

        String user = userService.userNameFromLoginCookie(token);
        if(!userService.deleteToken(user, userAgent, DeviceType.BROWSER)){
            log.warn("logout nicht erfolgreich: " + user);
        }
    }

    public Optional<String> requestSecureKey(String token, String userAgent){

        String user = userService.userNameFromLoginCookie(token);

        TokenResult result = userService.readExternalKey(user, token,
                userAgent, DeviceType.BROWSER, technicalUser);
        if(result.isCheckOk()){
            return Optional.of(result.getNewToken());
        }else{
            return Optional.empty();
        }
    }

}
