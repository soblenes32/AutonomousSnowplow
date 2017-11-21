package com.asl.snowplow.controller;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.asl.snowplow.service.ClientFetchQueueService;

/*************************************************************************************************************
 * Handles commands that relate to the physical operation of the vehicle
 *************************************************************************************************************/

@Controller
@RequestMapping("/clientupdate")
public class ClientUpdateFetchController extends HandlerInterceptorAdapter{
	
	@Inject
	ClientFetchQueueService clientFetchQueueService;
	
	@RequestMapping(value = "/fetch", method= {RequestMethod.GET, RequestMethod.POST})
	public @ResponseBody ClientFetchQueueService fetchClientUpdate(){
		return clientFetchQueueService;
	}
	
	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
	    super.postHandle(request, response, handler, modelAndView);
	    clientFetchQueueService.clear();
	}
}
