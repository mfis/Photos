package mfi.photos.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AuthService {

    public boolean checkUserWithPassword(String user, String password){
        return user !=null && user.equals("test"); // FIXME
    }

}
