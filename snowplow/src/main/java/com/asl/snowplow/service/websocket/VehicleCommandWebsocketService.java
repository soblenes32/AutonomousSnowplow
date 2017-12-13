package com.asl.snowplow.service.websocket;

import java.util.Queue;

import javax.inject.Inject;

import org.springframework.messaging.core.MessageSendingOperations;
import org.springframework.stereotype.Service;

import com.asl.snowplow.command.VehicleCommand;
import com.asl.snowplow.service.ClientFetchQueueService;

/*****************************************************************************
 * Hooking up to clientUpdateFetchController to use ajax workaround because
 * websocket is not working without internet connection
 *****************************************************************************/
//@Service
public class VehicleCommandWebsocketService {
	
	@Inject
	ClientFetchQueueService clientFetchQueueService;

//	@Inject
//	private MessageSendingOperations<String> messagingTemplate;
//
//	public synchronized void sendVehicleCommandQueue(Queue<VehicleCommand> vehicleCommandList) {
//		synchronized(messagingTemplate) {
//			messagingTemplate.convertAndSend("/toclient/vehiclecommand/commands", vehicleCommandList);
//		}
//	}
	
//	public synchronized void sendVehicleCommandQueue(Queue<VehicleCommand> vehicleCommandList) {
//		clientFetchQueueService.setVehicleCommandList(vehicleCommandList);
//	}
	
}