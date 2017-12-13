package com.asl.snowplow.controller;

import javax.inject.Inject;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.asl.snowplow.command.VehicleCommand;
import com.asl.snowplow.command.VehicleOperationCommand;
import com.asl.snowplow.model.VehicleOperationMode;
import com.asl.snowplow.model.WorldState;
import com.asl.snowplow.service.VehicleCommandQueueService;

/*************************************************************************************************************
 * Handles commands that relate to the physical operation of the vehicle
 *************************************************************************************************************/

@Controller
@RequestMapping("/vehiclecommand")
public class VehicleCommandController {
	@Inject
	VehicleCommandQueueService vehicleCommandQueueService;
	
	@Inject
	WorldState worldState;
	
	
	@RequestMapping(value = "/issue", method=RequestMethod.POST)
	public @ResponseBody String issueCommand(@RequestBody VehicleCommand vc) throws Exception {
		//Prevent issuance of commands if the vehicle is in auto mode
		if(worldState.getVehicleState().getVehicleOperationMode() != VehicleOperationMode.AUTONOMOUS){
			vehicleCommandQueueService.issueCommand(vc);
		}
		return ""; //"Size of queue: "+vehicleCommandQueueService.getVehicleCommandQueue().size();
	}
	
	@RequestMapping(value = "/rescend", method=RequestMethod.POST)
	public @ResponseBody String rescendCommand(@RequestBody VehicleCommand vc) throws Exception {
		//Prevent issuance of commands if the vehicle is in auto mode
		if(worldState.getVehicleState().getVehicleOperationMode() != VehicleOperationMode.AUTONOMOUS){
			vehicleCommandQueueService.rescendCommand(vc.getCommandID());
		}
		return "";
	}
	
	@RequestMapping(value = "/purge", method=RequestMethod.POST)
	public @ResponseBody String purgeCommandQueue() throws Exception {
		//Prevent issuance of commands if the vehicle is in auto mode
		if(worldState.getVehicleState().getVehicleOperationMode() != VehicleOperationMode.AUTONOMOUS){
			vehicleCommandQueueService.purgeAllCommands();
		}
		return "";
	}
	
	@RequestMapping(value = "/mode", method=RequestMethod.POST)
	public @ResponseBody String setOperationMode(@RequestBody VehicleOperationCommand vehicleOperationCommand) throws Exception {
		//Prevent issuance of commands if the vehicle is in auto mode
		worldState.getVehicleState().setVehicleOperationMode(vehicleOperationCommand.getVehicleOperationMode());
		if(vehicleOperationCommand.getVehicleOperationMode() == VehicleOperationMode.PAUSED){
			worldState.getVehicleState().setMotorATarget(0);
			worldState.getVehicleState().setMotorBTarget(0);
		}
		return "";
	}
	
//	@MessageMapping("/vehiclecommand/issue")
//	public void issueCommand(VehicleCommand vc) throws Exception {
//		//Prevent issuance of commands if the vehicle is in auto mode
//		if(worldState.getVehicleState().getVehicleOperationMode() != VehicleOperationMode.AUTONOMOUS){
//			vehicleCommandQueueService.issueCommand(vc);
//		}
//	}
//	
//	@MessageMapping("/vehiclecommand/rescend")
//	public void rescendCommand(VehicleCommand vc) throws Exception {
//		//Prevent issuance of commands if the vehicle is in auto mode
//		if(worldState.getVehicleState().getVehicleOperationMode() != VehicleOperationMode.AUTONOMOUS){
//			vehicleCommandQueueService.rescendCommand(vc.getCommandID());
//		}
//	}
//	
//	@MessageMapping("/vehiclecommand/purge")
//	public void purgeCommandQueue() throws Exception {
//		//Prevent issuance of commands if the vehicle is in auto mode
//		if(worldState.getVehicleState().getVehicleOperationMode() != VehicleOperationMode.AUTONOMOUS){
//			vehicleCommandQueueService.purgeAllCommands();
//		}
//	}
//	
//	@MessageMapping("/vehiclecommand/mode")
//	public void setOperationMode(VehicleOperationCommand vehicleOperationCommand) throws Exception {
//		//Prevent issuance of commands if the vehicle is in auto mode
//		worldState.getVehicleState().setVehicleOperationMode(vehicleOperationCommand.getVehicleOperationMode());
//		if(vehicleOperationCommand.getVehicleOperationMode() == VehicleOperationMode.PAUSED){
//			worldState.getVehicleState().setMotorATarget(0);
//			worldState.getVehicleState().setMotorBTarget(0);
//		}
//	}
}
