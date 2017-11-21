package com.asl.snowplow.service;

import java.awt.Point;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.asl.snowplow.command.VehicleCommand;
import com.asl.snowplow.command.VehicleCommandType;
import com.asl.snowplow.model.VehicleState;
import com.asl.snowplow.model.WorldState;
import com.asl.snowplow.model.ZoneCell;
import com.asl.snowplow.rplidar.RpLidarHighLevelDriver;
import com.asl.snowplow.rplidar.RpLidarScan;

@Service
public class RplidarService{
	@Inject
	WorldState worldState;
	
	//@Inject
	//TelemetryWebsocketService telemetryWebsocketService;
	
	@Inject
	ClientFetchQueueService clientFetchQueueService;
	
	//@Inject
	//ZoneCellWebsocketService zoneCellWebsocketService;
	
	@Inject
	VehicleInstructionService vehicleInstructionService;
	
	@Inject
	VehicleCommandQueueService vehicleCommandQueueService;

	private RpLidarHighLevelDriver driver = null;
	private boolean initSuccess = false;
	private double mm[] = new double[ RpLidarScan.N ];
	private Date lastScanThreshold = new Date();
	private long scanNumber = 0;
	
	private int plotMargin = 2000; //The amount of margin in the UI plot
	private int imminentCollisionDetections = 0; //Running tally of numer of detections in imminent path hitbox
	private static final int IMMINENT_PATH_DETECTION_THRESHOLD = 1; //If this number of detections is reached in any reading, then trigger safety halt
	private static final int IMMINENT_TRAVERSAL_ZONE_FORWARD_MM = 850; //40 cm = 4 decimeter
	private static final int IMMINENT_TRAVERSAL_ZONE_SIDE_MM = 600;
	
	@PostConstruct
	private void init(){
		System.setProperty("gnu.io.rxtx.SerialPorts", "/dev/arduino" + File.pathSeparator + "/dev/rplidar");
		driver = new RpLidarHighLevelDriver();
		initSuccess = driver.initialize("/dev/rplidar",500);
		//Let's just pretend it worked...
		initSuccess = true;
	}
	
	@PreDestroy
	private void close(){
		driver.stop();
		initSuccess = false;
		System.out.println("Completing PreDestroy");
	}
	
	@Scheduled(fixedRate=50)
	private void pollScan() {
		VehicleState vehicleState = worldState.getVehicleState();
		imminentCollisionDetections = 0; //Reset the number of imminent collision detections

		if(initSuccess) {
			RpLidarScan scan = new RpLidarScan();
			if (!driver.blockCollectScan(scan, 0)) { //10000
				System.out.println("Scan wasn't ready yet");
			} else {
				scanNumber++;
				List<Point> detectionUIList = new ArrayList<>(); //This list is just to send to the UI
				List<Point> detectionDynObsList = new ArrayList<>(); //This list is just to calculate dynamic obstructions
				
				scan.convertMilliMeters(mm);
				//Vector3D deviceMountedHeading = new Vector3D(0,1,0);
				//double vehicleHeadingDegrees = vehicleInstructionService.calculateRotationToCoordinates(deviceMountedHeading, true);
				//RpLidarScan.N: rplidar data is stored in a sparse array of 360 degrees * 64 degreefractions
				convertScanToGlobalCoordinates(scan, detectionUIList, detectionDynObsList);
				
				//If imminent collision detections exceed the threshold, and the vehicle is trying to move forward, then activate safety stop
				if(imminentCollisionDetections > IMMINENT_PATH_DETECTION_THRESHOLD && vehicleState.getMotorATarget() > 0 && vehicleState.getMotorBTarget() > 0) {
					System.out.println(" Detected " + imminentCollisionDetections + " objects in imminent collision area. Clearing instructions and pausing for 2 sec");
					String[] vcArgs = {""+(new Date().getTime()+2000)}; //Pause for 2 seconds
					VehicleCommand vehicleCommand = new VehicleCommand();
					vehicleCommand.setVehicleCommandType(VehicleCommandType.STOP_UNTIL);
					vehicleCommand.setArgs(vcArgs);
					
					vehicleCommandQueueService.purgeAllCommands();
					vehicleCommandQueueService.issueCommand(vehicleCommand);
					
					String[] vcRArgs = {""+(new Date().getTime()+500)}; //Reverse for 0.5 seconds
					VehicleCommand reverseCommand = new VehicleCommand();
					reverseCommand.setVehicleCommandType(VehicleCommandType.REVERSE_UNTIL);
					reverseCommand.setArgs(vcRArgs);
					vehicleCommandQueueService.issueCommand(reverseCommand);
				}
				
				//Send the lidar detection to the UI
				lastScanThreshold = new Date();
				if(scanNumber%10 == 0) {
					//telemetryWebsocketService.sendLidarDetectionList(detectionUIList);
					clientFetchQueueService.setLidarPointList(detectionUIList);
				}
				
				Set<ZoneCell> updatedZoneCells = worldState.registerLidarScans(detectionDynObsList, scanNumber);
				if(updatedZoneCells.size() > 0) { //Update the zonecell list if an obstruction was registered
					//System.out.println("One or more obstructions were found.");
					//telemetryWebsocketService.sendZoneCellList();
					//zoneCellWebsocketService.sendZoneCellUpdate(updatedZoneCells);
					clientFetchQueueService.getZoneCellList().addAll(updatedZoneCells);
				}
			}
		}else {
			//Lidar not initialized
		}
	}
	
	/********************************************************************************************************
	 * Converts the scan data from polar coordinates centered on the rplidar device into global coordinates
	 * relative to the pozyx anchors. Coordinates are filtered to within anchor bounds + plotMargin and 
	 * populated into detectionUIList. Coordinates are filtered to within anchor bounds and populated into
	 * detectionDynObsList.
	 ********************************************************************************************************/
	private void convertScanToGlobalCoordinates(RpLidarScan scan, List<Point> detectionUIList, List<Point> detectionDynObsList){
		int scanCount = 0;
		int lowAngle = -1;
		int highAngle = -1;
		StringBuilder sb = new StringBuilder();
		VehicleState vehicleState = worldState.getVehicleState();
		for (int j = 0; j < RpLidarScan.N; j++) {
			int rAngle = (j/64) + 180;
			Date scanTime = new Date(scan.time[j]);
			
			if( scan.distance[j] != 0 && scanTime.after(lastScanThreshold)) {
				//DEBUG
				if(lowAngle < 0) {
					lowAngle = rAngle;
				}
				highAngle = rAngle;
				sb.append(rAngle).append(",");
				
				scanCount++;
				
				//Discard any detections on the inside of the wall of the container (ie. < 20cm distant)
				if(mm[j] < 200) {
					continue;
				}
				
				//Check for detections in the vehicle's imminent path
				if(isDetectionInImminentPath(mm[j], (-1 * rAngle))) {
					//System.out.println("collision area detection");
					imminentCollisionDetections++;
				}
				
				//Convert vehicle-relative heading to world-relative heading
				Point position = vehicleState.getPosition();
				double vehicleToGlobalAngle = vehicleInstructionService.calculateRotationToCoordinates(new Vector3D(position.x, position.y + 1,0), true);
				//Calibration turns in same direction as vehicle, but opposite direction from world
				double angle = (-1 * rAngle) + vehicleToGlobalAngle + 90; 
				
				//sin(theta) = opp/hyp == sin(theta) * hyp = opp
				int y = (int) (Math.sin(Math.toRadians(angle)) * mm[j]) + vehicleState.getPosition().y;
				int x = (int) (Math.cos(Math.toRadians(angle)) * mm[j]) + vehicleState.getPosition().x;
				
				//Ignore detections outside of the viewing area
				
				if((x > worldState.getAnchorMinX() - plotMargin) && 
					(x < worldState.getAnchorMaxX() + plotMargin) && 
					(y > worldState.getAnchorMinY() - plotMargin) && 
					(y < worldState.getAnchorMaxY() + plotMargin)) {
					
					Point p = new Point(x,y);
					detectionUIList.add(p);
					
					//Further restrict detections to within anchor boundaries (no margin) for obstruction processing
					if((x > worldState.getAnchorMinX()) && 
							(x < worldState.getAnchorMaxX()) && 
							(y > worldState.getAnchorMinY()) && 
							(y < worldState.getAnchorMaxY())) {
						detectionDynObsList.add(p);
					}
				}
			}
		}
		//System.out.println("scanCount: " + scanCount + ", lowAngle: " + lowAngle + ", highAngle: " + highAngle + ", angles: " + sb.toString());
	}
	
	
	/********************************************************************************************************
	 * 
	 ********************************************************************************************************/
	private boolean isDetectionInImminentPath(double detectionDistance, double angle){
		boolean result = false;
		double normalizedAngle = angle;
		
		//Convert angle to 0 - 359 range
		while(normalizedAngle >= 360) 
			normalizedAngle -= 360;
		while(normalizedAngle < 0) 
			normalizedAngle += 360;
		
		//If the detection is behind the vehicle, just ignore it
		if(normalizedAngle >= 90 && normalizedAngle <= 270)
			return false;
		
		//If the detection is to the left, then treat it the same as to the right for simplicity
		if(normalizedAngle > 270)
			normalizedAngle = Math.abs(normalizedAngle - 360);
		
		//Count number of detections in imminent traversal zone
		//Calc adjacent (forward) distance
		//SOH sin(t) = opp / hyp => opp = sin(t) * hyp
		double opposite = Math.sin(Math.toRadians(normalizedAngle)) * detectionDistance;
		
		//Calc opposite (horizontal) distance
		//CAH cos(t) = opp / hyp => opp = cos(t) * hyp
		double adjacent = Math.cos(Math.toRadians(normalizedAngle)) * detectionDistance;
		
		if(opposite <= IMMINENT_TRAVERSAL_ZONE_SIDE_MM && adjacent <= IMMINENT_TRAVERSAL_ZONE_FORWARD_MM) {
			result = true;
		}
		return result;
	}
}
