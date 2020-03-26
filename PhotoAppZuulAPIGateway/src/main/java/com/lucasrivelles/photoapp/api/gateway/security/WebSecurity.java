package com.lucasrivelles.photoapp.api.gateway.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

@Configuration
@EnableWebSecurity
public class WebSecurity extends WebSecurityConfigurerAdapter {
	
	private Environment environment;
	
	public WebSecurity(Environment environment) {
		this.environment = environment;
	}
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.csrf().disable();
		http.headers().frameOptions().disable();
		
		http.authorizeRequests()
		.antMatchers(HttpMethod.GET, environment.getProperty("api.users.actuator.url_path")).permitAll()
		.antMatchers(HttpMethod.GET, environment.getProperty("api.zuul.actuator.url_path")).permitAll()
		.antMatchers(HttpMethod.POST, environment.getProperty("api.registration.url_path")).permitAll()
		.antMatchers(HttpMethod.POST, environment.getProperty("api.login.url_path")).permitAll()
		.antMatchers(environment.getProperty("api.h2-console.url_path")).permitAll()
		.anyRequest().authenticated()
		.and()
		.addFilter(new AuthorizationFilter(authenticationManager(), environment));
		
		http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
	}
}
