package mfi.photos.auth;

import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@CommonsLog
public class AuthService {

    public boolean checkUserWithPassword(String user, String password){
        boolean checkOK = user !=null && user.equals("test"); // FIXME
        log.info("checkOK=" + checkOK);
        return checkOK;
    }

}
