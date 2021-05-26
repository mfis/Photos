package mfi.photos.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;
import mfi.photos.auth.UserAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.session.ConcurrentSessionFilter;

@EnableWebSecurity
public class SecurityConfig {

	@RequiredArgsConstructor
	@Configuration
	@CommonsLog
	public static class UserWebSecurity extends WebSecurityConfigurerAdapter {

		private final UserAuthenticationFilter userAuthenticationFilter;

		private final static String RES = "/staticresources/*.";

		@Override
		protected void configure(HttpSecurity http) throws Exception {

			http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

			http.
				csrf().disable().
				authorizeRequests().anyRequest().authenticated().
				and().addFilterBefore(userAuthenticationFilter, ConcurrentSessionFilter.class).
				formLogin().loginPage("/login").permitAll()
			;
		}

		@Override
		public void configure(WebSecurity web) {
			web.ignoring().antMatchers(RES + "js", RES + "css", RES + "png", RES + "ico");
			web.ignoring().mvcMatchers("/login");
		}

		@Bean
		public AuthenticationManager authenticationManagerBean() throws Exception {
			return super.authenticationManagerBean();
		}
	}
}
