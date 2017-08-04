package com.asl.snowplow.controller;

import javax.inject.Inject;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import com.asl.snowplow.model.WorldState;
import com.asl.snowplow.model.ZoneCell;

/*************************************************************************************************************
 * Handles commands that relate to the physical operation of the vehicle
 *************************************************************************************************************/

@Controller
public class ZoneCellMappingController {
	@Inject
	WorldState worldState;
	
	@MessageMapping("/zones/issue")
	@Payload(expression="")
	public void issueCommand(ZoneCell[] updateList){
		for(ZoneCell z: updateList){
			worldState.getZoneCellMap().put(z.getCoordinates(), z);
		}
	}
}
