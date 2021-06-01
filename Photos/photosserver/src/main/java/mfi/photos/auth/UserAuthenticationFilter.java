package mfi.photos.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;
import mfi.photos.util.KeyAccess;
import mfi.photos.util.RequestUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

import static mfi.photos.util.RequestUtil.COOKIE_NAME;

@RequiredArgsConstructor
@CommonsLog
@Component
public class UserAuthenticationFilter extends GenericFilterBean {

	@Autowired
	private AuthService authService;

	@Autowired
	private RequestUtil requestUtil;

	@Value("${server.servlet.session.cookie.secure}")
	private String cookieSecure;

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
			throws IOException, ServletException {

		log.info("doFilter " + ((HttpServletRequest)req).getRequestURI());

		if(isLoginWithUserCredentials(req)){
			if(hasUserAcceptedCookies(req)){
				Optional<UserAuthentication> optionalUserAuthentication = tryToLoginWithUserCredentials(req);
				if(optionalUserAuthentication.isPresent()){
					requestUtil.defineUserForRequest(optionalUserAuthentication.get());
					cookieWrite(resp, optionalUserAuthentication.get().getNewToken());
				}else{
					sendRedirect(resp, "credentials");
					return;
				}
			}else{
				sendRedirect(resp, "cookies");
				return;
			}

		} else if(isLoginWithToken(req)){
			Optional<UserAuthentication> optionalUserAuthentication = tryToLoginWithToken(req);
			if(optionalUserAuthentication.isPresent()){
				requestUtil.defineUserForRequest(optionalUserAuthentication.get());
				if(optionalUserAuthentication.get().getNewToken()!=null){
					cookieWrite(resp, optionalUserAuthentication.get().getNewToken());
				}
			}else{
				sendRedirect(resp, "token");
				return;
			}
		}

		if (!KeyAccess.getInstance().isKeySet() && requestUtil.lookupUserPrincipal().isPresent()) {
			Optional<String> secureKey = authService.requestSecureKey(requestUtil.lookupUserPrincipal().get().getToken(), lookupUserAgent(req));
			if(secureKey.isPresent()){
				KeyAccess.getInstance().setKey(secureKey.get());
			}
		}

		chain.doFilter(req, resp);
	}

	private boolean isLoginWithToken(ServletRequest req) {
		return StringUtils.isNotBlank(cookieRead(req));
	}

	private void sendRedirect(ServletResponse resp, String reasonKey) throws IOException{
		String reasonParam = reasonKey!=null?"?reason=" + reasonKey:StringUtils.EMPTY;
		((HttpServletResponse)resp).sendRedirect("/login" + reasonParam);
	}

	private boolean isLoginWithUserCredentials(ServletRequest req){
		return StringUtils.isNotBlank(req.getParameter("login_user"));
	}

	private boolean hasUserAcceptedCookies(ServletRequest req){
		return Boolean.parseBoolean(req.getParameter("cookieok"));
	}

	private Optional<UserAuthentication> tryToLoginWithUserCredentials(ServletRequest req){
		return authService.checkUserWithPassword(req.getParameter("login_user"), req.getParameter("login_pass"), lookupUserAgent(req));
	}

	private String lookupUserAgent(ServletRequest req) {
		return ((HttpServletRequest)req).getHeader(RequestUtil.HEADER_USER_AGENT);
	}

	private Optional<UserAuthentication> tryToLoginWithToken(ServletRequest req) {
		boolean isInitialRequest = !req.getParameterNames().hasMoreElements();
		return authService.checkUserWithToken(cookieRead(req), lookupUserAgent(req), isInitialRequest);
	}

	private String cookieRead(ServletRequest request) {

		Cookie[] cookies = ((HttpServletRequest)request).getCookies();
		if (cookies == null) {
			return null;
		}
		for (Cookie cookie : cookies) {
			if (cookie.getName().equals(COOKIE_NAME)) {
				return StringUtils.trimToNull(cookie.getValue());
			}
		}
		return null;
	}

	private void cookieDelete(HttpServletResponse response) {

		Cookie cookie = new Cookie(COOKIE_NAME, StringUtils.EMPTY);
		cookie.setHttpOnly(true);
		cookie.setMaxAge(0);
		response.addCookie(cookie);
	}

	private void cookieWrite(ServletResponse response, String value) {

		Cookie cookie = new Cookie(COOKIE_NAME, value);
		cookie.setHttpOnly(true);
		cookie.setMaxAge(60 * 60 * 24 * 92);
		cookie.setSecure(Boolean.parseBoolean(cookieSecure));
		((HttpServletResponse)response).addCookie(cookie);
	}
}
