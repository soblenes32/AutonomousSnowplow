package com.asl.snowplow;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.servlet.configuration.EnableWebMvcSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

//@Configuration
@EnableWebMvcSecurity
@EnableGlobalMethodSecurity(securedEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	
	@Override
    protected void configure(HttpSecurity http) throws Exception {
		http.csrf().disable();
        http.authorizeRequests()
        	.antMatchers("/html/**", "/libs/**", "/app/**", "/static/**").permitAll() //Some things to exclude from the filter chain...
	        .antMatchers("/signout","/signin","/signin_fail").permitAll()
	            .and()
	        // **************** Form configuration ***************** //
	        .formLogin()
	            .loginPage("/signin")
	            .usernameParameter("j_username")
	            .passwordParameter("j_password")
	            .loginProcessingUrl("/j_spring_security_check")
	            .failureUrl("/signin_fail")
	            .defaultSuccessUrl("/", true)
	        .and()
				.logout()
				.deleteCookies("JSESSIONID")
				.logoutUrl("/signout")
				.logoutSuccessUrl("/")
				.permitAll()
			.and()
            	.rememberMe();
    }
	/***************************************************************************************************
	 * Authentication configuration #1: interrogate USER_T for username, password, and role
	 ***************************************************************************************************/
//	@Override
//	@Order(1)
//	public void configure(AuthenticationManagerBuilder auth) throws Exception {
//		auth.jdbcAuthentication()
//			.dataSource(dataSource)
//			.usersByUsernameQuery("select login_id as username, password, true from USER_T where login_id = ? and is_active_directory_auth='f'")
//			.authoritiesByUsernameQuery("select email as username, ROLE from USER_T where login_id = ?")
//			.passwordEncoder(bCryptPasswordEncoder());
//
//			//.userDetailsService(userDetailsService());
//    }
	
	@Bean
	public BCryptPasswordEncoder bCryptPasswordEncoder(){
		return new BCryptPasswordEncoder();
	}
}
