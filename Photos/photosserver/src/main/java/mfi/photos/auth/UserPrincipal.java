package mfi.photos.auth;

import java.security.Principal;
import lombok.ToString;
import lombok.Value;

@Value
@ToString
public class UserPrincipal implements Principal {

	String name;
}
