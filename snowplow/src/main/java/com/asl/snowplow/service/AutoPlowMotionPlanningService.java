package com.asl.snowplow.service;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.asl.snowplow.command.VehicleCommand;
import com.asl.snowplow.command.VehicleCommandType;
import com.asl.snowplow.model.PlowVector;
import com.asl.snowplow.model.VehicleState;
import com.asl.snowplow.model.WorldState;
import com.asl.snowplow.model.ZoneCell;

@Service
public class AutoPlowMotionPlanningService{
	@Inject
	WorldState worldState;
	
	@Inject
	VehicleInstructionService vehicleInstructionService;
	
	private static int maxDistanceM = 5;
	
	
	/*************************************************************************************
	* 1) Generates a list of all possible plowvectors 
	* 2) Selects the plowvector with the closest origin point
	* 3) Prepares commands to execute the plowvector
	**************************************************************************************/
	public List<VehicleCommand> generateNextCommands(){
		List<VehicleCommand> vehicleCommandList = new ArrayList<VehicleCommand>();
		List<PlowVector> plowVectorList = listAllPlowVectors();
		
		if(plowVectorList.size() > 0){
			//PLOWING
			//Find the origin closest to the current vehiclePosition
			
			double minDistance = 9999999;
			PlowVector closestpv = null;
			for(PlowVector pv: plowVectorList){
				double distance = worldState.getVehicleState().getPosition().distance(pv.getSource());
				if(distance < minDistance){
					minDistance = distance;
					closestpv = pv;
				}
			}
			//Nav to start of plowvector
			VehicleCommand navToCommand = new VehicleCommand();
			navToCommand.setVehicleCommandType(VehicleCommandType.NAV_TO);
			String[] args = {Integer.toString((int) closestpv.getSource().getX()), Integer.toString((int) closestpv.getSource().getY())};
			navToCommand.setArgs(args);
			//Plow the snow to the end of the plowvector
			VehicleCommand moveToCommand = new VehicleCommand();
			moveToCommand.setVehicleCommandType(VehicleCommandType.MOVE_TO);
			String[] args2 = {Double.toString(closestpv.getDestination().getX()), Double.toString(closestpv.getDestination().getY())};
			moveToCommand.setArgs(args2);
			//Reverse a bit 
			String[] vcArgs = {""+(new Date().getTime()+500)}; //Reverse for 0.5 seconds
			VehicleCommand reverseCommand = new VehicleCommand();
			reverseCommand.setVehicleCommandType(VehicleCommandType.REVERSE_UNTIL);
			reverseCommand.setArgs(vcArgs);
			
			vehicleCommandList.add(navToCommand);
			vehicleCommandList.add(moveToCommand);
			vehicleCommandList.add(reverseCommand);
			
		}else{
			//UNPLOWED SNOWZONES
			//Calculate diagonal plow options?
		
			//PARKING
			Point parkingSpotIdx = findParkZoneCentroidIdx(); 
			Point parkingSpotPosition = VehicleInstructionService.zoneCellIdxToPosition(parkingSpotIdx);
			System.out.println("My parking spot idx is: " + parkingSpotPosition); 
			if(worldState.getVehicleState().getPosition().distance(parkingSpotPosition) < 250){ 
				System.out.println("Arrived at parking spot. Halting for 30 seconds."); 
				VehicleCommand vc = new VehicleCommand(VehicleCommandType.STOP_UNTIL); 
				Calendar calendar = Calendar.getInstance(); 
				calendar.add(Calendar.SECOND, 30); 
				String[] args = {""+calendar.getTime().getTime()};
				vc.setArgs(args); 
				vehicleCommandList.add(vc);
			} else {
				System.out.println("Navigating to destination");
				VehicleCommand vc = new VehicleCommand(VehicleCommandType.NAV_TO);
				String[] args = {""+parkingSpotPosition.getX(), ""+parkingSpotPosition.getY()};
				vc.setArgs(args);
				vehicleCommandList.add(vc);
			}
			
		}
		return vehicleCommandList;
	}



	/*************************************************************************************
	* Generate a list of all possible plowvectors 
	**************************************************************************************/
	public List<PlowVector> listAllPlowVectors(){
		List<PlowVector> plowVectorList = new ArrayList<PlowVector>();
		int[] directions = {1,2,-1,-2};
		
		//Start with all the available plowed zone cells
		List<Point> plowedSnowZoneCellIdxList = worldState.getZoneCellMap().values().stream().filter((zc)->{
			return zc.isPlowedSnowZone() && !zc.isObstruction();
		}).map((zc)->{
			return zc.getCoordinates();
		}).collect(Collectors.toList());
		
		//...and check each direction from them for valid plow vectors
		for(Point origin:plowedSnowZoneCellIdxList){
			for(int direction: directions){
				Point p = findNextZoneCell(origin, direction);
				Point source = findPlowVectorSource(p, origin, null, direction, maxDistanceM);
				if(source != null){
					p = findNextZoneCell(origin, (-1 * direction));
					Point destination = findPlowVectorSource(p, origin, (-1 * direction));
					PlowVector plowVector = new PlowVector();
					plowVector.setSource(VehicleInstructionService.zoneCellIdxToPosition(source)); //Set source grid position, not zonecell index
					plowVector.setDestination(VehicleInstructionService.zoneCellIdxToPosition(destination)); //Convert to mm
					plowVectorList.add(plowVector);
				}
			}
		}
		return plowVectorList;
	}


	/*************************************************************************************
	* Finds the longest valid plow vector leading to the origin plowedSnowZoneIdx
	*
	* currentZoneCellIdx: The zone cell to evaluate
	* plowedSnowZoneOriginIdx: The zone cell where the vector is being calculated from
	* direction: 1=N, 2=E, -1=S, -2=W
	* lastPlowableZoneCellIdx: the zone cell along this vector that is the furthest known from the origin
	* maxDistanceM: how far to search from the origin
	*
	* If the plow vector is valid, returns the zoneCell 
	* If the plow vector is invalid, returns null.
	**************************************************************************************/
	public Point findPlowVectorSource(Point currentZoneCellIdx, Point plowedSnowZoneOriginIdx, Point lastPlowableZoneCellIdx, int direction, int maxDistanceM){
		VehicleState vehicleState = worldState.getVehicleState();
		double distanceFromOriginM = currentZoneCellIdx.distance(plowedSnowZoneOriginIdx) / 10;
		
		ZoneCell currentZoneCell = worldState.getZoneCellMap().get(currentZoneCellIdx);
		boolean isCurrentCellObstructed = vehicleInstructionService.isZoneCellObstructed(currentZoneCellIdx, vehicleState.getObstructionSearchRadius());
		boolean isStopHere = (currentZoneCell != null && (currentZoneCell.isPlowedSnowZone() || isCurrentCellObstructed)) || (distanceFromOriginM > maxDistanceM);

		//if ran into an obstructed zoneCell, and the plowvector is valid then backup the source coordinateIdx by one cell
		if(currentZoneCell != null && isCurrentCellObstructed && lastPlowableZoneCellIdx != null){
			lastPlowableZoneCellIdx = findNextZoneCell(currentZoneCellIdx, (direction * -1));
		}
		
		//Stop
		if(isStopHere){
			//move out RADIUS additional cells if possible
			Point vectorStart = lastPlowableZoneCellIdx;
			if(lastPlowableZoneCellIdx != null) {
				for(int zc = 0; zc < vehicleState.getObstructionSearchRadius(); zc++) {
					Point nextZCIdx = findNextZoneCell(vectorStart, (direction));
					if(vehicleInstructionService.isZoneCellObstructed(nextZCIdx, vehicleState.getObstructionSearchRadius())) {
						break;
					}else{
						vectorStart = nextZCIdx;
					}
				}
			}
			return vectorStart;
		}
		
		//Continue
		//Is there snow to plow along this path? If so then it is a valid plow vector
		if(currentZoneCell != null && currentZoneCell.isPlowZone() && currentZoneCell.getSnowVolume() > 0){
			lastPlowableZoneCellIdx = currentZoneCellIdx;
		}
		
		//Calculate next cell to evaluate
		currentZoneCellIdx = findNextZoneCell(currentZoneCellIdx, direction);
		return findPlowVectorSource(currentZoneCellIdx, plowedSnowZoneOriginIdx, lastPlowableZoneCellIdx, direction, maxDistanceM);
	}

	/********************************************************************************
	* direction: 01=N, 2=E, -1=S, -2=W; multiply by -1 to reverse the direction
	*********************************************************************************/
	public Point findNextZoneCell(Point currentZoneCellIdx, int direction){
		Point nextZoneCellIdx = new Point();
		switch (direction){
			case 1: nextZoneCellIdx.setLocation(currentZoneCellIdx.x, currentZoneCellIdx.y+1); break; //N
			case 2: nextZoneCellIdx.setLocation(currentZoneCellIdx.x+1, currentZoneCellIdx.y); break; //E
			case -1: nextZoneCellIdx.setLocation(currentZoneCellIdx.x,currentZoneCellIdx.y-1); break; //S
			case -2: nextZoneCellIdx.setLocation(currentZoneCellIdx.x-1,currentZoneCellIdx.y); break; //W
		}
		return nextZoneCellIdx;
	}

	/**********************************************************************************
	* Finds the midpoint in the plowed snow zone along the specified direction
	**********************************************************************************/
	public Point findPlowVectorSource(Point currentZoneCellIdx, Point plowedSnowZoneOriginIdx, int direction){
		ZoneCell currentZoneCell = worldState.getZoneCellMap().get(currentZoneCellIdx);
		boolean isStopHere = currentZoneCell == null || !currentZoneCell.isPlowedSnowZone() || currentZoneCell.isObstruction();
		
		if(isStopHere){
			Point plowedSnowZoneLastIdx = findNextZoneCell(currentZoneCellIdx, (direction * -1));
			int midx = (int) (plowedSnowZoneLastIdx.getX() + plowedSnowZoneOriginIdx.getX()) / 2;
			int midy = (int) (plowedSnowZoneLastIdx.getY() + plowedSnowZoneOriginIdx.getY()) / 2;
			return new Point(midx, midy);
		}
		currentZoneCellIdx = findNextZoneCell(currentZoneCellIdx, direction);
		return findPlowVectorSource(currentZoneCellIdx, plowedSnowZoneOriginIdx, direction);
	}
	
	/**********************************************************************************
	* Finds the center coordinate index of the designated parkzone. If no parkzones
	* have been designated, then returns null
	**********************************************************************************/
	public Point findParkZoneCentroidIdx(){ 
	    //Build a list that just contains the valid, unobstructed parkzonecells 
	    ZoneCell bestCandidate = null; 
	    double bestCandidateMeanDistance = 9999999; 
	    List<ZoneCell> validParkZoneCellList = new ArrayList<>(); 
	    for(ZoneCell zc: worldState.getZoneCellMap().values()){ 
	        //is it a parkzone? Is it obstructed? 
	        if(zc.isParkZone() && !vehicleInstructionService.isZoneCellObstructed(zc.getCoordinates(), worldState.getVehicleState().getObstructionSearchRadius())){ 
	            validParkZoneCellList.add(zc); 
	        } 
	    } 
	    //Next find the centroid 
	    for(ZoneCell pz: validParkZoneCellList){ 
	        double candidateMeanDistance = validParkZoneCellList.stream()
	        		.mapToDouble((zc)->pz.getCoordinates().distance(zc.getCoordinates()))
	        		.average()
	        		.getAsDouble();
	        if(candidateMeanDistance < bestCandidateMeanDistance){
	            bestCandidate = pz; 
	            bestCandidateMeanDistance = candidateMeanDistance; 
	        } 
	    } 
	    return (bestCandidate != null)?bestCandidate.getCoordinates():null; 
	} 
}
