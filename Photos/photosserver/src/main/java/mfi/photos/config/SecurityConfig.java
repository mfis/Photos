package mfi.photos.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mfi.photos.auth.AuthService;
import mfi.photos.auth.UserAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.session.ConcurrentSessionFilter;

@EnableWebSecurity
public class SecurityConfig {

	@RequiredArgsConstructor
	@Configuration
	@Slf4j
	public static class UserWebSecurity extends WebSecurityConfigurerAdapter {

		private final AuthService authService;

		private final static String RES = "/staticresources/*.";

		@Override
		protected void configure(HttpSecurity http) throws Exception {
			http.addFilterAfter(new UserAuthenticationFilter(authService), ConcurrentSessionFilter.class);

			http.
				authorizeRequests().antMatchers(RES + "js", RES + "css", RES + "png", RES + "ico").permitAll().
					and().
				formLogin().loginPage("/login").failureUrl("/login?msg=error").defaultSuccessUrl("/").permitAll().
					and().
				logout().logoutUrl("/logout").deleteCookies("photosLoginCookie").logoutSuccessUrl("/login?msg=logout").permitAll().
					and().
				authorizeRequests().anyRequest().authenticated()
				;
		}

		@Bean
		public AuthenticationManager authenticationManagerBean() throws Exception {
			return super.authenticationManagerBean();
		}
	}
}
