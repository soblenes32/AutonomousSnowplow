package com.asl.robo.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;


@Service
public class AuthenticationService {
	
	/**
	 * Interrogate the security context for logged-in user's name
	 * @return userName if the user is currently logged in. Returns null if user is not logged in.
	 */
	public static String getLoggedInUserName(){
		return getLoggedInUserPrincipal().getUsername();
	}
	
	/**
	 * Interrogate the security context for logged-in user's principal object
	 * @return userName if the user is currently logged in. Returns null if user is not logged in.
	 */
	public static UserDetails getLoggedInUserPrincipal(){
		UserDetails userPrincipal = null;
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (!(auth instanceof AnonymousAuthenticationToken)) {
			userPrincipal = (UserDetails) auth.getPrincipal();
		}
		return userPrincipal;
	}
	
	/**
	 * Determine whether the user has an authority
	 * @param role is the string representation of the user role to check for.
	 * @return true if the user retains the indicated authority. False otherwise.
	 */
	@SuppressWarnings("unchecked")
	public static boolean hasRole(String role){
		Collection<GrantedAuthority> authorities = (Collection<GrantedAuthority>) SecurityContextHolder.getContext().getAuthentication().getAuthorities();
		boolean hasRole = false;
		for (GrantedAuthority authority : authorities) {
			hasRole = authority.getAuthority().equals(role);
			if (hasRole) { break; }
		}
		return hasRole;
	}
	
	@SuppressWarnings("unchecked")
	public static List<String> roles(){
		Collection<GrantedAuthority> authorities = (Collection<GrantedAuthority>) SecurityContextHolder.getContext().getAuthentication().getAuthorities();
		List<String> roleList = new ArrayList<String>();
		for (GrantedAuthority authority : authorities) {
			roleList.add(authority.getAuthority());
		}
		return roleList;
	}
}
