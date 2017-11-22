package com.asl.snowplow.controller;

import javax.inject.Inject;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.asl.snowplow.service.ClientFetchQueueService;

/*************************************************************************************************************
 * Handles commands that relate to the physical operation of the vehicle
 *************************************************************************************************************/

@Controller
@RequestMapping("/clientupdate")
public class ClientUpdateFetchController{
	
	@Inject
	ClientFetchQueueService clientFetchQueueService;
	
	@RequestMapping(value = "/fetch", method= {RequestMethod.GET, RequestMethod.POST})
	public @ResponseBody ClientFetchQueueService fetchClientUpdate(){
		ClientFetchQueueService c = clientFetchQueueService.copy();
		clientFetchQueueService.clear();
		return c;
	}
}
