package com.asl.snowplow.model;

import java.awt.Point;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.springframework.stereotype.Service;

import com.asl.snowplow.service.ArduinoRxTxUsbService;
import com.asl.snowplow.service.ClientFetchQueueService;
import com.asl.snowplow.service.MotorDesignator;
import com.asl.snowplow.service.SnowVolumeSimulationService;
import com.asl.snowplow.service.TelemetryFilterService;

@Service
public class VehicleState {
	
	//@Inject
	//WorldState worldState;
	
	@Inject
	ArduinoRxTxUsbService rxTxUsbService;
	
	@Inject
	TelemetryFilterService telemetryFilterService;
	
	@Inject
	SnowVolumeSimulationService snowVolumeSimulationService;
	
	//@Inject
	//ZoneCellWebsocketService zoneCellWebsocketService;
	
	@Inject
	ClientFetchQueueService clientFetchQueueService;
	
	//Instructions sent to arduino reflect targets
	private float motorATarget = 0;
	private float motorBTarget = 0;
	//Speeds received from arduino are stored in values
	private float motorAValue = 0;
	private float motorBValue = 0;
	
	
	private static final int SNOW_VOL_KEYFRAME = 400000;
	private int snowVolFramesSinceKeyframe = 0;
	
	private VehicleOperationMode vehicleOperationMode = VehicleOperationMode.COMMAND_QUEUE;
	
	//Current canonical location of the vehicle in the coordinate system
	//To convert to grid coordinates to cellZoneIdx, see: VehicleInstructionService.positionToZoneCellIdx
	//To convert cellZoneIdx to CellZone, see: worldState.zoneCellMap
	private Point position = new Point();
	
	
	//Map of the last n vehicle positions
	private static final int MAX_HISTORICAL_POSITIONS = 4;

	private Map<Long, Point> historicalPositionMap = new HashMap<>();
	private Map<Long, PositionMeasurement> historicalSensorReadingMap = new HashMap<>();
	
	//Radius of the vehicle measured in decimeters/10cm
	private int obstructionSearchRadius = 6;
	
	//Radius of the vehicle's plow measured in decimeters/10cm
	private int plowReachRadius = 1;
	
	//Euclidian rotation of the vehicle relative to the origin
	private Vector3D orientation = new Vector3D(0,0,0);
	//Offset to apply to the z-axis. User-configured through the UI
	private double headingCalibration = 0;
	
	//Iteration sequence of telemetery packets
	long telemetrySerialPacketSequence = 0;
	
	
	/**********************************************************************
	 * Adds a new telemetry packet reading
	 **********************************************************************/
	public void updatePosition(PositionMeasurement p) {
		
		//if(!telemetryFilterService.isPositionOutlier(p)) return;
		
		//Remove oldest historical position if exists
		if(historicalPositionMap.containsKey(telemetrySerialPacketSequence - MAX_HISTORICAL_POSITIONS)) {
			historicalPositionMap.remove(telemetrySerialPacketSequence - MAX_HISTORICAL_POSITIONS);
		}
		if(historicalSensorReadingMap.containsKey(telemetrySerialPacketSequence - MAX_HISTORICAL_POSITIONS)) {
			historicalSensorReadingMap.remove(telemetrySerialPacketSequence - MAX_HISTORICAL_POSITIONS);
		}
		
		//Add the new telemetry position
		historicalSensorReadingMap.put(telemetrySerialPacketSequence, p);
		
		
		/******* Low-pass filter *******/
		//Update the canonical position as the average of MAX_HISTORICAL_POSITIONS
		double x = historicalSensorReadingMap.values().stream().mapToDouble(i->i.getPosition().getX()).average().getAsDouble();
		double y = historicalSensorReadingMap.values().stream().mapToDouble(i->i.getPosition().getY()).average().getAsDouble();
		historicalPositionMap.put(telemetrySerialPacketSequence, new Point((int)x,(int)y));
		position.setLocation(x, y);
		
		/******* Thresholding limit *******/
		//Reduce frame-to-frame movement to max of 1cm
		//Point filteredPosition = telemetryFilterService.translateToValidPosition(p);
		//position.setLocation(filteredPosition);
		//historicalPositionMap.put(telemetrySerialPacketSequence, new Point(filteredPosition));
		
		/******* Raw *******/
		//position.setLocation(p.getPosition());
		//historicalPositionMap.put(telemetrySerialPacketSequence, new Point(p.getPosition()));
		
		
		
		
		
		/******** Snowvolume simulation service *********/
		//Update the internal snow volume simulation before making any decisions
		//Send the modified zonecells tSet<E>he client if any changes were made
		Set<ZoneCell> modifiedZoneCellSet = snowVolumeSimulationService.updateSnowVolume();
		if(modifiedZoneCellSet.size() > 0) {
			snowVolFramesSinceKeyframe++;
			if(snowVolFramesSinceKeyframe > SNOW_VOL_KEYFRAME) { //Send a keyframe every so ofter to update the whole field
				//System.out.println("Sending complete snowvolume update");
				//zoneCellWebsocketService.sendZoneCellUpdate();
				//clientFetchQueueService.setZoneCellList(worldState.getZoneCellMap().values());
				snowVolFramesSinceKeyframe = 0;
			}else {
				//System.out.println("Sending snowvolume cell updates: " + modifiedZoneCellSet.size());
				//zoneCellWebsocketService.sendZoneCellUpdate(modifiedZoneCellSet);
				clientFetchQueueService.getZoneCellList().addAll(modifiedZoneCellSet);
			}
		}
		
		
		
		
		//Increment the telemetry sequence
		this.telemetrySerialPacketSequence++;
	}
	
//	public boolean isPositionValid(PositionMeasurement p) {
//		boolean isValid = true;
//		PositionMeasurement last = getLastPositionMeasurement();
//		double distance = 0;
//		if(last != null) {
//			distance = last.getPosition().distance(p.getPosition());
//		}
//		//Has the vehicle moved more than 4 cm since the last reading?
//		if(distance > 400) isValid = false;
//		
//		if(!isValid) {
//			sequentialInvalidPositionCount++;
//			invalidReadCount++;
//			if(sequentialInvalidPositionCount > maxSequentialInvalidPositionCount) {
//				sequentialInvalidPositionCount = 0;
//				isValid = true;
//				historicalPositionMap.clear();
//			}
//		}else {
//			sequentialInvalidPositionCount = 0;
//			validReadCount++;
//		}
//		return isValid;
//	}
	
	public float getMotorATarget() {
		return motorATarget;
	}
	public synchronized void setMotorATarget(float motorATarget) {
		this.motorATarget = motorATarget;
		rxTxUsbService.setMotorSpeed(motorATarget, MotorDesignator.RIGHT);
	}
	public float getMotorAValue() {
		return motorAValue;
	}
	public synchronized void setMotorAValue(float motorAValue) {
		this.motorAValue = motorAValue;
	}
	public float getMotorBTarget() {
		return motorBTarget;
	}
	public synchronized void setMotorBTarget(float motorBTarget) {
		this.motorBTarget = motorBTarget;
		rxTxUsbService.setMotorSpeed(motorBTarget, MotorDesignator.LEFT);
	}
	public float getMotorBValue() {
		return motorBValue;
	}
	public synchronized void setMotorBValue(float motorBValue) {
		this.motorBValue = motorBValue;
	}
	public Point getPosition() {
		return position;
	}
	public void setPosition(Point position) {
		this.position = position;
	}
	public Vector3D getOrientation() {
		return orientation;
	}
	public void setOrientation(Vector3D orientation) {
		this.orientation = orientation;
	}
	public double getHeadingCalibration() {
		return headingCalibration;
	}
	public void setHeadingCalibration(double headingCalibration) {
		this.headingCalibration = headingCalibration;
	}
	public int getObstructionSearchRadius() {
		return obstructionSearchRadius;
	}
	public void setObstructionSearchRadius(int obstructionSearchRadius) {
		this.obstructionSearchRadius = obstructionSearchRadius;
	}
	public VehicleOperationMode getVehicleOperationMode() {
		return vehicleOperationMode;
	}
	public void setVehicleOperationMode(VehicleOperationMode vehicleOperationMode) {
		this.vehicleOperationMode = vehicleOperationMode;
	}
	public int getPlowReachRadius() {
		return plowReachRadius;
	}
	public void setPlowReachRadius(int plowReachRadius) {
		this.plowReachRadius = plowReachRadius;
	}
	public long getTelemetrySerialPacketSequence() {
		return telemetrySerialPacketSequence;
	}
	public void setTelemetrySerialPacketSequence(long telemetrySerialPacketSequence) {
		this.telemetrySerialPacketSequence = telemetrySerialPacketSequence;
	}
	public Point getLastPositionMeasurement() {
		return historicalPositionMap.get(telemetrySerialPacketSequence-1);
	}
	public Map<Long, Point> getHistoricalPositionMap() {
		return historicalPositionMap;
	}
	public void setHistoricalPositionMap(Map<Long, Point> historicalPositionMap) {
		this.historicalPositionMap = historicalPositionMap;
	}
	public Map<Long, PositionMeasurement> getHistoricalSensorReadingMap() {
		return historicalSensorReadingMap;
	}
	public void setHistoricalSensorReadingMap(Map<Long, PositionMeasurement> historicalSensorReadingMap) {
		this.historicalSensorReadingMap = historicalSensorReadingMap;
	}
	public PositionMeasurement getLastSensorReadingt() {
		return historicalSensorReadingMap.get(telemetrySerialPacketSequence-1);
	}
}
