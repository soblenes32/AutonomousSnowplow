package com.asl.snowplow.controller;

import javax.inject.Inject;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

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
	
	@MessageMapping("/joystick")
	public void greeting(JoystickCommand jc) throws Exception {
		//Joystick commands should only be accepted if the operation mode is paused
		if(worldState.getVehicleState().getVehicleOperationMode() == VehicleOperationMode.PAUSED){
			motorStateService.joystickToMotorCommands(jc);
		}
	}
}
