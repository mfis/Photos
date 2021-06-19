package mfi.photos.auth;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.apachecommons.CommonsLog;
import mfi.photos.util.KeyAccess;
import mfi.photos.util.RequestUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
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

	@Value("${technicalUser}")
	private String technicalUser;

	private final AntPathMatcher antPathMatcher = new AntPathMatcher();

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
			throws IOException, ServletException {

		if(log.isDebugEnabled()){
			log.debug("doFilter " + ((HttpServletRequest)req).getRequestURI() + " :: " + requestUtil.lookupUserPrincipal().isPresent());
		}

		if(doLogin(req, resp).sentRedirectToLogin) {
			return;
		}

		doReadKey(req);

		chain.doFilter(req, resp);
	}

	private void doReadKey(ServletRequest req) {

		if (!KeyAccess.getInstance().isKeySet() && requestUtil.lookupUserPrincipal().isPresent()) {
			Optional<String> secureKey = authService.requestSecureKey(requestUtil.lookupUserPrincipal().get().getToken(), lookupUserAgent(req));
			secureKey.ifPresent(s -> KeyAccess.getInstance().setKey(s));
		}
	}

	private LoginReturn doLogin(ServletRequest req, ServletResponse resp) throws IOException {

		var uri = ((HttpServletRequest)req).getRequestURI();
		var loginReturn = new LoginReturn();

		//noinspection StatementWithEmptyBody
		if(uri.equals(RequestUtil.loginRequestPath()) || isUriStaticResource(uri) || requestUtil.lookupUserPrincipal().isPresent()) {
			//no auth required
		} else if (uri.equals(RequestUtil.logoutRequestPath())){
			cookieDelete(resp);
			authService.logout(cookieRead(req), lookupUserAgent(req));
		} else if(isLoginWithUserCredentials(req)){
			if(hasUserAcceptedCookies(req)){
				Optional<UserAuthentication> optionalUserAuthentication = tryToLoginWithUserCredentials(req);
				if(optionalUserAuthentication.isPresent()){
					requestUtil.defineUserForRequest(optionalUserAuthentication.get());
					if(sentToken(optionalUserAuthentication.get())){
						cookieWrite(resp, optionalUserAuthentication.get().getNewToken());
					}
				}else{
					loginReturn.setSentRedirectToLogin(sendRedirect(req, resp, "credentials"));
				}
			}else{
				loginReturn.setSentRedirectToLogin(sendRedirect(req, resp, "cookies"));
			}

		} else if(isLoginWithToken(req)){
			Optional<UserAuthentication> optionalUserAuthentication = tryToLoginWithToken(req);
			if(optionalUserAuthentication.isPresent()){
				requestUtil.defineUserForRequest(optionalUserAuthentication.get());
				if(optionalUserAuthentication.get().getNewToken()!=null){
					cookieWrite(resp, optionalUserAuthentication.get().getNewToken());
				}
			}else{
				loginReturn.setSentRedirectToLogin(sendRedirect(req, resp, "token"));
			}

		} else {
			loginReturn.setSentRedirectToLogin(sendRedirect(req, resp, null));
		}

		return loginReturn;
	}

	private boolean sentToken(UserAuthentication userAuthentication) {
		return !isTechnicalUser(userAuthentication.getPrincipal().getName());
	}

	private boolean isTechnicalUser(String name){
		return name.equals(technicalUser);
	}

	private boolean isUriStaticResource(String uri) {
		for (String antPath : RequestUtil.antRequestPathsWithoutAuthentication()){
			if(antPathMatcher.match(antPath, uri)){
				return true;
			}
		}
		return false;
	}

	private boolean isLoginWithToken(ServletRequest req) {
		return StringUtils.isNotBlank(cookieRead(req));
	}

	private boolean sendRedirect(ServletRequest req, ServletResponse resp, String reasonKey) throws IOException{
		if(((HttpServletRequest)req).getRequestURI().contains(".")){
			// assume requesting a file (css, js, jpg) - no redirect, just a 403
			((HttpServletResponse)resp).setStatus(403);
		}else{
			// assume requesting a site (list, gallery) - redirecting to login site
			String reasonParam = reasonKey!=null?"?reason=" + reasonKey:StringUtils.EMPTY;
			((HttpServletResponse)resp).sendRedirect(RequestUtil.loginRequestPath() + reasonParam);
		}
		return true;
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
		boolean isRoot = ((HttpServletRequest)req).getRequestURI().equals("/");
		boolean isList = req.getParameterMap().isEmpty() || req.getParameterMap().containsKey("list");
		return authService.checkUserWithToken(cookieRead(req), lookupUserAgent(req), isRoot && isList);
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

	private void cookieDelete(ServletResponse response) {

		Cookie cookie = new Cookie(COOKIE_NAME, StringUtils.EMPTY);
		cookie.setHttpOnly(true);
		cookie.setMaxAge(0);
		((HttpServletResponse)response).addCookie(cookie);
	}

	private void cookieWrite(ServletResponse response, String value) {

		Cookie cookie = new Cookie(COOKIE_NAME, value);
		cookie.setHttpOnly(true);
		cookie.setMaxAge(60 * 60 * 24 * 92);
		cookie.setSecure(Boolean.parseBoolean(cookieSecure));
		((HttpServletResponse)response).addCookie(cookie);
	}

	private static class LoginReturn{
		@Getter @Setter
		boolean sentRedirectToLogin;
	}
}
