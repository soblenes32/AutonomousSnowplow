package com.asl.snowplow.service;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Queue;

import org.springframework.stereotype.Service;

import com.asl.snowplow.command.VehicleCommand;
import com.asl.snowplow.model.AnchorState;
import com.asl.snowplow.model.VehicleState;
import com.asl.snowplow.model.ZoneCell;

/*******************************************************************************
 * This service gets returned when the client requests a fetch of current data
 *
 *******************************************************************************/
@Service
public class ClientFetchQueueService {
	private Collection<ZoneCell> zoneCellList 			= Collections.synchronizedList(new ArrayList<>());
	private Collection<VehicleCommand> vehicleCommandList = Collections.synchronizedList(new ArrayList<>());
	private VehicleState vehicleState 					= null;
	private List<AnchorState> anchorStateList 			= null;
	private List<Point> lidarPointList 					= null;
	
	public ClientFetchQueueService copy() {
		synchronized(this) {
			ClientFetchQueueService c = new ClientFetchQueueService();
			c.zoneCellList = new ArrayList<ZoneCell>(zoneCellList);
			c.vehicleCommandList = new ArrayList<VehicleCommand>(this.vehicleCommandList);
			c.vehicleState = this.vehicleState;
			c.anchorStateList = this.anchorStateList;
			c.lidarPointList = this.lidarPointList;
			return c;
		}
	}
	
	public void clear() {
		zoneCellList.clear();
		//vehicleCommandList.clear();
		vehicleState = null;
		anchorStateList = null;
		lidarPointList = null;
	}
	
	public Collection<ZoneCell> getZoneCellList() {
		return zoneCellList;
	}
	public void setZoneCellList(Collection<ZoneCell> zoneCellList) {
		this.zoneCellList.clear();
		this.zoneCellList.addAll(zoneCellList);
	}
	public Collection<VehicleCommand> getVehicleCommandList() {
		synchronized(this) {
			return vehicleCommandList;
		}
	}
	public void setVehicleCommandList(Queue<VehicleCommand> vehicleCommandList) {
		System.out.println("Setting commandlist to size: " + vehicleCommandList.size());
		synchronized(this) {
			this.vehicleCommandList.clear();
			this.vehicleCommandList.addAll(vehicleCommandList);
		}
	}
	public VehicleState getVehicleState() {
		synchronized(this) {
			return vehicleState;
		}
	}
	public void setVehicleState(VehicleState vehicleState) {
		this.vehicleState = vehicleState;
	}
	public List<AnchorState> getAnchorStateList() {
		synchronized(this) {
			return anchorStateList;
		}
	}
	public void setAnchorStateList(List<AnchorState> anchorStateList) {
		synchronized(this) {
			this.anchorStateList = anchorStateList;
		}
	}
	public List<Point> getLidarPointList() {
		synchronized(this) {
			return lidarPointList;
		}
	}
	public void setLidarPointList(List<Point> lidarPointList) {
		synchronized(this) {
			this.lidarPointList = lidarPointList;
		}
	}
}
