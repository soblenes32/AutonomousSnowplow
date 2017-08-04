package com.asl.snowplow.controller;

import javax.inject.Inject;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import com.asl.snowplow.command.VehicleCommand;
import com.asl.snowplow.command.VehicleOperationCommand;
import com.asl.snowplow.model.VehicleOperationMode;
import com.asl.snowplow.model.WorldState;
import com.asl.snowplow.service.VehicleCommandQueueService;

/*************************************************************************************************************
 * Handles commands that relate to the physical operation of the vehicle
 *************************************************************************************************************/

@Controller
public class VehicleCommandController {
	@Inject
	VehicleCommandQueueService vehicleCommandQueueService;
	
	@Inject
	WorldState worldState;
	
	@MessageMapping("/vehiclecommand/issue")
	public void issueCommand(VehicleCommand vc) throws Exception {
		//Prevent issuance of commands if the vehicle is in auto mode
		if(worldState.getVehicleState().getVehicleOperationMode() != VehicleOperationMode.AUTONOMOUS){
			vehicleCommandQueueService.issueCommand(vc);
		}
	}
	
	@MessageMapping("/vehiclecommand/rescend")
	public void rescendCommand(VehicleCommand vc) throws Exception {
		//Prevent issuance of commands if the vehicle is in auto mode
		if(worldState.getVehicleState().getVehicleOperationMode() != VehicleOperationMode.AUTONOMOUS){
			vehicleCommandQueueService.rescendCommand(vc.getCommandID());
		}
	}
	
	@MessageMapping("/vehiclecommand/purge")
	public void purgeCommandQueue() throws Exception {
		//Prevent issuance of commands if the vehicle is in auto mode
		if(worldState.getVehicleState().getVehicleOperationMode() != VehicleOperationMode.AUTONOMOUS){
			vehicleCommandQueueService.purgeAllCommands();
		}
	}
	
	@MessageMapping("/vehiclecommand/mode")
	public void setOperationMode(VehicleOperationCommand vehicleOperationCommand) throws Exception {
		//Prevent issuance of commands if the vehicle is in auto mode
		worldState.getVehicleState().setVehicleOperationMode(vehicleOperationCommand.getVehicleOperationMode());
		if(vehicleOperationCommand.getVehicleOperationMode() == VehicleOperationMode.PAUSED){
			worldState.getVehicleState().setMotorATarget(0);
			worldState.getVehicleState().setMotorBTarget(0);
		}
	}
}
