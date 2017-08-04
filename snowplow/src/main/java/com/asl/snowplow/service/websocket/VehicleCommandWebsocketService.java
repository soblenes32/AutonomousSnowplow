package com.asl.snowplow.service.websocket;

import java.util.Queue;

import javax.inject.Inject;

import org.springframework.messaging.core.MessageSendingOperations;
import org.springframework.stereotype.Service;

import com.asl.snowplow.command.VehicleCommand;

@Service
public class VehicleCommandWebsocketService {

	@Inject
	private MessageSendingOperations<String> messagingTemplate;

	public synchronized void sendVehicleCommandQueue(Queue<VehicleCommand> vehicleCommandList) {
		synchronized(messagingTemplate) {
			messagingTemplate.convertAndSend("/toclient/vehiclecommand/commands", vehicleCommandList);
		}
	}
	
}