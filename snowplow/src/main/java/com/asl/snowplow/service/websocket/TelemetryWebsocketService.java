package com.asl.snowplow.service.websocket;

import java.awt.Point;
import java.util.List;

import javax.inject.Inject;

import org.springframework.messaging.core.MessageSendingOperations;
import org.springframework.stereotype.Service;

import com.asl.snowplow.model.WorldState;

@Service
public class TelemetryWebsocketService {
	
	@Inject
	WorldState worldState;
	
	@Inject
	private MessageSendingOperations<String> messagingTemplate;

	public synchronized void sendVehicleState() {
		synchronized(messagingTemplate) {
			messagingTemplate.convertAndSend("/toclient/telemetry/vehicle", worldState.getVehicleState());
		}
	}
	
	public synchronized void sendAnchorStateList() {
		synchronized(messagingTemplate) {
			messagingTemplate.convertAndSend("/toclient/telemetry/anchors", worldState.getAnchorStateList());
		}
	}
	
	public synchronized void sendZoneCellList() {
		synchronized(messagingTemplate) {
			messagingTemplate.convertAndSend("/toclient/telemetry/zones", worldState.getZoneCellMap().values());
		}
	}
	
	public synchronized void sendLidarDetectionList(List<Point> lidarPointList) {
		synchronized(messagingTemplate) {
			messagingTemplate.convertAndSend("/toclient/telemetry/lidar", lidarPointList);
		}
	}
	
}