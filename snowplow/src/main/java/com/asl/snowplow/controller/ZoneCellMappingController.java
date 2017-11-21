package com.asl.snowplow.controller;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.asl.snowplow.model.WorldState;
import com.asl.snowplow.model.ZoneCell;

/*************************************************************************************************************
 * Handles commands that relate to the physical operation of the vehicle
 *************************************************************************************************************/

@Controller
@RequestMapping(value="/zones")
public class ZoneCellMappingController {
	@Inject
	WorldState worldState;
	
	@RequestMapping(value="/issue", method=RequestMethod.POST)
	public @ResponseBody String issueCommand(@RequestBody List<ZoneCell> updateList){
		for(ZoneCell z: updateList){
			worldState.getZoneCellMap().put(z.getCoordinates(), z);
		}
		return "";
	}
	
//	@MessageMapping("/zones/issue")
//	@Payload(expression="")
//	public void issueCommand(ZoneCell[] updateList){
//		for(ZoneCell z: updateList){
//			worldState.getZoneCellMap().put(z.getCoordinates(), z);
//		}
//	}
}
