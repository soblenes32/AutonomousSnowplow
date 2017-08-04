package com.asl.robo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class IngressController {
	
	//@Secured(value={"ROLE_OBSERVER", "ROLE_STUDENT","ROLE_PRECEPTOR", "ROLE_INSTITUTION_COORDINATOR", "ROLE_SITE_ADMINISTRATOR", "ROLE_OPHE", "ROLE_ADMINISTRATOR"})
	@RequestMapping(value={"/"}, method=RequestMethod.GET)
	public String index() {
		return "forward:/app/app.html";
	}
}