package hr.abysalto.hiring.api.junior.configuration;

import hr.abysalto.hiring.api.junior.security.LoginSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity

public class SecurityConfig {

	private final LoginSuccessHandler loginSuccessHandler;

	public SecurityConfig(LoginSuccessHandler loginSuccessHandler) {
		this.loginSuccessHandler = loginSuccessHandler;
	}

	@Bean
	public WebSecurityCustomizer webSecurityCustomizer() {
		return (web) -> web.ignoring()
				.requestMatchers("/swagger-ui/**", "/v3/api-docs*/**");
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.csrf(CsrfConfigurer::disable)
				.authorizeHttpRequests(authorizeRequests ->
						authorizeRequests.requestMatchers(
								"/swagger-ui/**",
								"/v3/api-docs*/**",
								"/login",
					   			"/data-init/**").permitAll()
								.anyRequest().authenticated()
				)
			.httpBasic(Customizer.withDefaults())
			.formLogin(form -> form
					.loginPage("/login")
					.successHandler(loginSuccessHandler)
					.permitAll()
			);
		return http.build();
	}

	@Bean
	public UserDetailsService userDetailsService() {
		UserDetails userDetails = User.withDefaultPasswordEncoder()
				.username("user").password("password").roles("USER").build();
		return new InMemoryUserDetailsManager(userDetails);
	}

}
