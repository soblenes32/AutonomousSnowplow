package com.asl.snowplow.service.websocket;

import java.util.Collection;

import javax.inject.Inject;

import org.springframework.messaging.core.MessageSendingOperations;
import org.springframework.stereotype.Service;

import com.asl.snowplow.model.WorldState;
import com.asl.snowplow.model.ZoneCell;

@Service
public class ZoneCellWebsocketService {

	@Inject
	private MessageSendingOperations<String> messagingTemplate;
	
	@Inject
	WorldState worldState;

	public synchronized void sendZoneCellUpdate(Collection<ZoneCell> updateCells) {
		synchronized(messagingTemplate) {
			messagingTemplate.convertAndSend("/toclient/zones/update", updateCells);
		}
	}
	
	public synchronized void sendZoneCellUpdate() {
		synchronized(messagingTemplate) {
			messagingTemplate.convertAndSend("/toclient/zones/update", worldState.getZoneCellMap().values());
		}
	}
	
}