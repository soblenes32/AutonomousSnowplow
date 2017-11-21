package com.asl.snowplow.service.websocket;

import java.awt.Point;
import java.util.List;

import javax.inject.Inject;

import org.springframework.messaging.core.MessageSendingOperations;
import org.springframework.stereotype.Service;

import com.asl.snowplow.model.WorldState;
import com.asl.snowplow.service.ClientFetchQueueService;

/*****************************************************************************
 * Hooking up to clientUpdateFetchController to use ajax workaround because
 * websocket is not working without internet connection
 *****************************************************************************/

//@Service
public class TelemetryWebsocketService {
	
	@Inject
	WorldState worldState;
	
	@Inject
	ClientFetchQueueService clientFetchQueueService;
	
//	@Inject
//	private MessageSendingOperations<String> messagingTemplate;

//	public synchronized void sendVehicleState() {
//		synchronized(messagingTemplate) {
//			messagingTemplate.convertAndSend("/toclient/telemetry/vehicle", worldState.getVehicleState());
//		}
//	}
//	
//	public synchronized void sendAnchorStateList() {
//		synchronized(messagingTemplate) {
//			messagingTemplate.convertAndSend("/toclient/telemetry/anchors", worldState.getAnchorStateList());
//		}
//	}
//	
//	public synchronized void sendLidarDetectionList(List<Point> lidarPointList) {
//		synchronized(messagingTemplate) {
//			messagingTemplate.convertAndSend("/toclient/telemetry/lidar", lidarPointList);
//		}
//	}

	public synchronized void sendVehicleState() {
		clientFetchQueueService.setVehicleState(worldState.getVehicleState());
	}
	
	public synchronized void sendAnchorStateList() {
		clientFetchQueueService.setAnchorStateList(worldState.getAnchorStateList());
	}
	
	public synchronized void sendLidarDetectionList(List<Point> lidarPointList) {
		clientFetchQueueService.setLidarPointList(lidarPointList);
	}
	
}