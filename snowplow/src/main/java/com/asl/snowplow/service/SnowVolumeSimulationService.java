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
		boolean isZoneCellModified = false;
		Set<ZoneCell> modifiedZoneCellSet = new HashSet<>();
		
		//Short circuit if this if the vehicle initialization
		if(vs.getLastPositionMeasurement() == null) 
			return modifiedZoneCellSet;
		
		Point positionZoneCellIdx = VehicleInstructionService.positionToZoneCellIdx(vs.getPosition());
		Point lastPositionZoneCellIdx = VehicleInstructionService.positionToZoneCellIdx(vs.getLastPositionMeasurement().getPosition());
		
		//If the vehicle has moved to another zoneCell
		if(!positionZoneCellIdx.equals(lastPositionZoneCellIdx)){
			//Determine the direction of motion
			Point vehicleZoneCellIdx = VehicleInstructionService.positionToZoneCellIdx(vs.getPosition());
			Point lastZoneCellIdx = VehicleInstructionService.positionToZoneCellIdx(vs.getLastPositionMeasurement().getPosition());
			int xOffset = lastZoneCellIdx.x - vehicleZoneCellIdx.x;
			int yOffset = lastZoneCellIdx.y - vehicleZoneCellIdx.y;
			//confine motion to range [1, 0, -1] in each dimension
			xOffset = (xOffset > 1)?1:
				(xOffset < -1)?-1:xOffset;
			yOffset = (yOffset > 1)?1:
				(yOffset < -1)?-1:yOffset;
			
			//DEBUG
			//ZoneCell zc = worldState.getZoneCellMap().getOrDefault(vehicleZoneCellIdx, new ZoneCell());
			//System.out.println("Snow under vehicle: " + zc.getSnowVolume());

			//Sum up snow volume over motion vectors //
			List<Point> zoneCellsUnderVehicleList = vehicleInstructionService.findAdjacentZoneCellIndexes(vehicleZoneCellIdx, vs.getObstructionSearchRadius(), true); //vs.getPlowReachRadius()
			for(Point zoneCellIdx: zoneCellsUnderVehicleList) {
				ZoneCell searchCell = worldState.getZoneCellMap().get(zoneCellIdx);
				if(searchCell != null && searchCell.getSnowVolume() > 0){
					isZoneCellModified = true;
					moveSnow(zoneCellIdx, xOffset, yOffset, modifiedZoneCellSet);
				}
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
