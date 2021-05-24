package mfi.photos.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Optional;

@RequiredArgsConstructor
@CommonsLog
public class UserAuthenticationFilter extends GenericFilterBean {

	private final AuthService authService;

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
			throws IOException, ServletException {

		log.info("doFilter " + ((HttpServletRequest)req).getRequestURI());

		Optional<UserAuthentication> loginWithUserCredentials = tryToLoginWithUserCredentials(req);
		if(loginWithUserCredentials.isPresent()){
			SecurityContextHolder.getContext().setAuthentication(loginWithUserCredentials.get());
		}

		chain.doFilter(req, resp);
	}

	private Optional<UserAuthentication> tryToLoginWithUserCredentials(ServletRequest req){

		String user = req.getParameter("login_user");
		if(StringUtils.isBlank(user)){
			return Optional.empty();
		}

		if(!Boolean.parseBoolean(req.getParameter("cookieok"))){
			throw new CredentialsExpiredException("not ok");
			// return Optional.empty();
		}

		String password = req.getParameter("login_pass");
		return authService.checkUserWithPassword(user, password);
	}
}
