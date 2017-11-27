package com.asl.snowplow.service;

import java.awt.Point;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.asl.snowplow.model.VehicleState;
import com.asl.snowplow.model.WorldState;
import com.asl.snowplow.model.ZoneCell;

@Service
public class SnowVolumeSimulationService{
	@Inject
	WorldState worldState;
	
	@Inject
	VehicleInstructionService vehicleInstructionService;
	
	/***************************************************************************************
	 * Updates the snowvolume simulation
	 * @return if any zonecell snow volume was changed, returns true. Otherwise false
	 ***************************************************************************************/
	public Set<ZoneCell> updateSnowVolume(){
		VehicleState vs = worldState.getVehicleState();
		Set<ZoneCell> modifiedZoneCellSet = new HashSet<>();

		//Short circuit if the vehicle initialization is not complete
		if(vs.getLastPositionMeasurement() == null) {
			return modifiedZoneCellSet;
		}

		Point positionZoneCellIdx = VehicleInstructionService.positionToZoneCellIdx(vs.getPosition());
		Point lastPositionZoneCellIdx = VehicleInstructionService.positionToZoneCellIdx(vs.getLastPositionMeasurement());

		//If the vehicle has moved to another zoneCell
		if(!positionZoneCellIdx.equals(lastPositionZoneCellIdx)){
			/****************************************************************************
			* Instead of calculating based on last position of the vehicle, move snow
			* based on the vehicle heading
			****************************************************************************/
			Point vehicleZoneCellIdx = VehicleInstructionService.positionToZoneCellIdx(vs.getPosition());
//			Point lastZoneCellIdx = VehicleInstructionService.positionToZoneCellIdx(vs.getLastPositionMeasurement());
//			int xOffset = vehicleZoneCellIdx.x - lastZoneCellIdx.x;
//			int yOffset = vehicleZoneCellIdx.y - lastZoneCellIdx.y;
//			//confine motion to range [1, 0, -1] in each dimension
//			xOffset = (xOffset > 1)?1:
//				(xOffset < -1)?-1:xOffset;
//			yOffset = (yOffset > 1)?1:
//				(yOffset < -1)?-1:yOffset;
			int xOffset = 0;
			int yOffset = 0;
			
			double heading = vs.getCalibratedHeading();
			if(heading > 337.5 || heading <= 22.5){yOffset = 1;}
			if(heading > 22.5 && heading <= 67.5){yOffset = 1; xOffset = 1;}
			if(heading > 67.5 && heading <= 112.5){xOffset = 1;}
			if(heading > 112.5 && heading <= 157.5){yOffset = -1; xOffset = 1;}
			if(heading > 157.5 && heading <= 202.5){yOffset = -1;}
			if(heading > 202.5 && heading <= 247.5){yOffset = -1; xOffset = -1;}
			if(heading > 247.5 && heading <= 292.5){xOffset = -1;}
			if(heading > 292.5 && heading <= 337.5){yOffset = 1; xOffset = -1;}


			//DEBUG
			//ZoneCell zc = worldState.getZoneCellMap().getOrDefault(vehicleZoneCellIdx, new ZoneCell());
			//System.out.println("Snow under vehicle: " + zc.getSnowVolume());

			//Sum up snow volume over motion vectors //
			List<Point> zoneCellsUnderVehicleList = vehicleInstructionService.findAdjacentZoneCellIndexes(vehicleZoneCellIdx, vs.getObstructionSearchRadius(), true); //vs.getPlowReachRadius()
			for(Point zoneCellIdx: zoneCellsUnderVehicleList) {
				ZoneCell searchCell = worldState.getZoneCellMap().get(zoneCellIdx);
				if(searchCell != null && searchCell.getSnowVolume() > 0){
					moveSnow(zoneCellIdx, xOffset, yOffset, modifiedZoneCellSet);
				}
			}
		}else {
			if(vs.getMotorATarget() != 0 || vs.getMotorBTarget() != 0) {
				System.out.println("Current position: " + positionZoneCellIdx + ", last position: " + lastPositionZoneCellIdx);
			}
		}
		return modifiedZoneCellSet;
	}
	
	public void moveSnow(Point zoneCellIdx, int xDir, int yDir, Set<ZoneCell> modifiedZoneCellSet){
		Point vehicleZoneCellIdx = VehicleInstructionService.positionToZoneCellIdx(worldState.getVehicleState().getPosition());
		ZoneCell sourceCell = worldState.getZoneCellMap().getOrDefault(zoneCellIdx, new ZoneCell());
		Point destinationZoneCellIdx = new Point((zoneCellIdx.x + xDir), (zoneCellIdx.y + yDir));
		ZoneCell destinationCell = worldState.getZoneCellMap().getOrDefault(destinationZoneCellIdx, new ZoneCell());
		
		destinationCell.setSnowVolume(destinationCell.getSnowVolume() + sourceCell.getSnowVolume());
		sourceCell.setSnowVolume(0);
		
		modifiedZoneCellSet.add(sourceCell);
		modifiedZoneCellSet.add(destinationCell);
		
		VehicleState vs = worldState.getVehicleState();
		List<Point> zoneCellsUnderVehicleList = vehicleInstructionService.findAdjacentZoneCellIndexes(vehicleZoneCellIdx, vs.getPlowReachRadius(), true);
		
		//Call recursively if moving snow 
		if(zoneCellsUnderVehicleList.contains(destinationZoneCellIdx)){
			moveSnow(destinationZoneCellIdx, xDir, yDir, modifiedZoneCellSet);
		}
	}
}
