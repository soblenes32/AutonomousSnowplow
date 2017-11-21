package com.asl.snowplow.service;

import java.awt.Point;
import java.util.Date;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.inject.Inject;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.asl.snowplow.command.VehicleCommand;
import com.asl.snowplow.command.VehicleCommandType;
import com.asl.snowplow.model.VehicleOperationMode;
import com.asl.snowplow.model.VehicleState;
import com.asl.snowplow.model.WorldState;

/****************************************************************************************
 * High level command queue to enable the vehicle to follow a sequence of instructions
 ****************************************************************************************/
@Service
public class VehicleCommandQueueService {
	@Inject
	VehicleInstructionService vehicleInstructionService;
	
	@Inject
	AutoPlowMotionPlanningService autoPlowMotionPlanningService;
	
	//@Inject
	//VehicleCommandWebsocketService vehicleCommandWebsocketService;
	
	@Inject
	ClientFetchQueueService clientFetchQueueService;
	
	@Inject
	WorldState worldState;
	
	private int nextCommandId = 0;
	private Queue<VehicleCommand> vehicleCommandQueue = new ConcurrentLinkedQueue<>();
	
	/************************************************************************************
	 * Adds the supplied command to the queue, and updates the reference with an ID.
	 * The ID may be used to reference the command. Also returns the id. 
	 ************************************************************************************/
	public int issueCommand(VehicleCommand vehicleCommand){
		//Prevent issuance of commands if the vehicle is in autonomous mode
		int commandId = nextCommandId++;
		vehicleCommand.setCommandID(commandId);
		vehicleCommandQueue.add(vehicleCommand);
		//vehicleCommandWebsocketService.sendVehicleCommandQueue(vehicleCommandQueue);
		clientFetchQueueService.setVehicleCommandList(vehicleCommandQueue);
		return commandId;
	}
	
	
	/************************************************************************************
	 * Removes the command with the specified Id from the queue. If the removed command
	 * was the last command in the queue, then a stop command is issued.
	 * @return true if command with id was found in the queue. false otherwise 
	 ************************************************************************************/
	public boolean rescendCommand(long commandId){
		boolean returnVal = false;
		VehicleCommand command = vehicleCommandQueue.stream()
			.filter((cmd)->{return cmd.getCommandID() == commandId;})
			.findFirst()
			.orElse(null);
		if(command != null){
			vehicleCommandQueue.remove(command);
			returnVal = true;
			//vehicleCommandWebsocketService.sendVehicleCommandQueue(vehicleCommandQueue);
			clientFetchQueueService.setVehicleCommandList(vehicleCommandQueue);
		}
		
		return returnVal;
	}
	
	/************************************************************************************
	 * Removes all commands in the queue and issues an immediate stop command.
	 ************************************************************************************/
	public void purgeAllCommands(){
		vehicleCommandQueue.clear();
	}
	
	/************************************************************************************
	 * Executes the command at the head of the queue at 20hz. When command is complete,
	 * pops it from the queue. 
	 ************************************************************************************/
	@Scheduled(fixedRate=50)
	public void processQueue(){
		VehicleCommand command = vehicleCommandQueue.peek();
		VehicleState vehicleState = worldState.getVehicleState();
		
		
		//If the mode is paused, do not process the command queue
		if(vehicleState.getVehicleOperationMode() == VehicleOperationMode.PAUSED){
			return;
		}
		
		//If the command queue is empty, stop the vehicle or generate new commands
		if(command == null){
			if(vehicleState.getVehicleOperationMode() == VehicleOperationMode.COMMAND_QUEUE){
				if(worldState.getVehicleState().getMotorATarget() != 0 || worldState.getVehicleState().getMotorATarget() != 0){
					vehicleInstructionService.stop();
					//vehicleCommandWebsocketService.sendVehicleCommandQueue(vehicleCommandQueue);
					clientFetchQueueService.setVehicleCommandList(vehicleCommandQueue);
				}
				return;
			}else if(vehicleState.getVehicleOperationMode() == VehicleOperationMode.AUTONOMOUS){
				List<VehicleCommand> plowCommandList = autoPlowMotionPlanningService.generateNextCommands();
				vehicleCommandQueue.addAll(plowCommandList);
				//If no further snow to plow, then idle
				return;
			}
		}
		boolean isDone = false;
		String[] args = command.getArgs();
		switch(command.getVehicleCommandType()){
			case MOVE_TO :
				if(args.length == 2){
					Point p = new Point((int) Float.parseFloat(args[0]), (int) Float.parseFloat(args[1]));
					isDone = vehicleInstructionService.moveDirectlyToCoordinate(p);
					if(isDone){
						vehicleCommandQueue.poll();
						//vehicleCommandWebsocketService.sendVehicleCommandQueue(vehicleCommandQueue);
						clientFetchQueueService.setVehicleCommandList(vehicleCommandQueue);
					}
				}
			break;
			case STOP:
				vehicleInstructionService.stop();
				vehicleCommandQueue.poll();
				//vehicleCommandWebsocketService.sendVehicleCommandQueue(vehicleCommandQueue);
				clientFetchQueueService.setVehicleCommandList(vehicleCommandQueue);
			break;
			case STOP_UNTIL:
				vehicleInstructionService.stop();
				isDone = (new Date().after(new Date(Long.parseLong(args[0]))));
				if(isDone) {
					vehicleCommandQueue.poll();
				}
				//vehicleCommandWebsocketService.sendVehicleCommandQueue(vehicleCommandQueue);
				clientFetchQueueService.setVehicleCommandList(vehicleCommandQueue);
			break;
			case REVERSE_UNTIL:
				worldState.getVehicleState().setMotorATarget(-100);
				worldState.getVehicleState().setMotorBTarget(-100);
				isDone = (new Date().after(new Date(Long.parseLong(args[0]))));
				if(isDone) {
					vehicleCommandQueue.poll();
					vehicleInstructionService.stop();
				}
				//vehicleCommandWebsocketService.sendVehicleCommandQueue(vehicleCommandQueue);
				clientFetchQueueService.setVehicleCommandList(vehicleCommandQueue);
			break;
			case NAV_TO:
				Point position = new Point((int) Float.parseFloat(args[0]), (int) Float.parseFloat(args[1]));
				List<Point> waypointList = vehicleInstructionService.findPath(vehicleState.getPosition(), position, vehicleState.getObstructionSearchRadius(), false);
				vehicleCommandQueue.poll(); //Remove the NAV_TO command
				Queue<VehicleCommand> bufferQueue = new ConcurrentLinkedQueue<>();
				for(Point p: waypointList){
					VehicleCommand vc = new VehicleCommand();
					vc.setVehicleCommandType(VehicleCommandType.MOVE_TO);
					vc.setArgs(new String[]{""+p.getX(),""+p.getY()});
					bufferQueue.add(vc);
				}
				while(!vehicleCommandQueue.isEmpty()){
					bufferQueue.add(vehicleCommandQueue.poll());
				}
				while(!bufferQueue.isEmpty()){
					vehicleCommandQueue.add(bufferQueue.poll());
				}
				//vehicleCommandWebsocketService.sendVehicleCommandQueue(vehicleCommandQueue);
				clientFetchQueueService.setVehicleCommandList(vehicleCommandQueue);
			break;
		}
		//Notify the ardino of the motors to their new speed
	}
}
