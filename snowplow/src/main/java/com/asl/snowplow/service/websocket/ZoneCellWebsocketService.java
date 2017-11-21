package com.asl.snowplow.service.websocket;

import java.util.Collection;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.asl.snowplow.model.WorldState;
import com.asl.snowplow.model.ZoneCell;
import com.asl.snowplow.service.ClientFetchQueueService;

/*****************************************************************************
 * Hooking up to clientUpdateFetchController to use ajax workaround because
 * websocket is not working without internet connection
 *****************************************************************************/
//@Service
public class ZoneCellWebsocketService {

//	@Inject
//	private MessageSendingOperations<String> messagingTemplate;
	
	@Inject
	WorldState worldState;
	
	@Inject
	ClientFetchQueueService clientFetchQueueService;

//	public synchronized void sendZoneCellUpdate(Collection<ZoneCell> updateCells) {
//		synchronized(messagingTemplate) {
//			messagingTemplate.convertAndSend("/toclient/zones/update", updateCells);
//		}
//	}
//	
//	public synchronized void sendZoneCellUpdate() {
//		synchronized(messagingTemplate) {
//			messagingTemplate.convertAndSend("/toclient/zones/update", worldState.getZoneCellMap().values());
//		}
//	}
	
	public synchronized void sendZoneCellUpdate(Collection<ZoneCell> updateCells) {
		clientFetchQueueService.getZoneCellList().addAll(updateCells);
	}
	
	public synchronized void sendZoneCellUpdate() {
		sendZoneCellUpdate(worldState.getZoneCellMap().values());
	}
	
	
}