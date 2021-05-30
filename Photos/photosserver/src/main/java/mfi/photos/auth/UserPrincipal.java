package mfi.photos.auth;

import java.security.Principal;
import lombok.Value;

@Value
public class UserPrincipal implements Principal {

	String name;

	String token;
}
