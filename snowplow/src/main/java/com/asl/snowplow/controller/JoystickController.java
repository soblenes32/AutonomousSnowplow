package com.asl.snowplow.controller;

import javax.inject.Inject;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.asl.snowplow.command.JoystickCommand;
import com.asl.snowplow.model.VehicleOperationMode;
import com.asl.snowplow.model.WorldState;
import com.asl.snowplow.service.MotorStateService;

@Controller
public class JoystickController {
	@Inject
	MotorStateService motorStateService;
	
	@Inject
	WorldState worldState;
	
//	@MessageMapping("/joystick")
//	public void greeting(JoystickCommand jc) throws Exception {
//		//Joystick commands should only be accepted if the operation mode is paused
//		if(worldState.getVehicleState().getVehicleOperationMode() == VehicleOperationMode.PAUSED){
//			motorStateService.joystickToMotorCommands(jc);
//		}
//	}
	
	@RequestMapping(value = "/joystick", method=RequestMethod.POST)
	public @ResponseBody String greeting(@RequestBody JoystickCommand jc) throws Exception {
		//Joystick commands should only be accepted if the operation mode is paused
		if(worldState.getVehicleState().getVehicleOperationMode() == VehicleOperationMode.PAUSED){
			motorStateService.joystickToMotorCommands(jc);
		}
		return "";
	}
}
