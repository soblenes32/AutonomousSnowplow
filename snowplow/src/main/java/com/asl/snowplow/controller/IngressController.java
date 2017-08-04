package com.asl.snowplow.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/")
public class IngressController {
	
	//@Secured(value={"ROLE_BASIC"})
	@RequestMapping(value={"/","/diagnostic","/grid","/camera"}, method=RequestMethod.GET)
	public String index() {
		return "forward:/app/app.html";
	}
}