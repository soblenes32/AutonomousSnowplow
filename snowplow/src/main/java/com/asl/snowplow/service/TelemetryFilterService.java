package com.asl.snowplow.service;

import java.awt.Point;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.asl.snowplow.model.PositionMeasurement;
import com.asl.snowplow.model.WorldState;

@Service
public class TelemetryFilterService{
	@Inject
	private WorldState worldState;
	
	private FileWriter fw = null;
	private BufferedWriter bw = null;
	
	//Number of invalid readouts in a row
	private int sequentialInvalidPositionCount = 0;
	//Number of invalid readouts before the position "snaps" to the latest readout
	private int maxSequentialInvalidPositionCount = 20;
	
	//If a new point is farther than threshold distance, then it is considered an outlier
	private static final int OUTLIER_THRESHOLD_MM = 400; 
	
	private long validReadCount = 0;
	private long invalidReadCount = 0;
	
	@PostConstruct
	private void init(){
		try{
			fw = new FileWriter("/home/pi/snowplow/positions.txt", true);
			bw = new BufferedWriter(fw);
		}catch (IOException e){
			e.printStackTrace();
		}
	}
	
	@PreDestroy
	private void close(){
		if(fw != null)
			try {fw.close();}catch(Exception e) {}
		if(bw != null)
			try {bw.close();}catch(Exception e) {}
	}
	
	
	/*********************************************************************************
	 * Writes the raw and processed telemetry data to system file for analysis
	 *********************************************************************************/
	private void logTelemetryData(PositionMeasurement p) {
		//TODO
//		if(this.telemetrySerialPacketSequence % 60 == 0) {
//			System.out.println("Outlier telemetry readings removed: "+invalidReadCount+", readings read: " + validReadCount);
//		}
//		StringBuilder sb = new StringBuilder();
//		
//		try {
//			bw.append(sb.toString());
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}
	
	/*********************************************************************************************************
	 * Evaluates whether the position is an outlier reading based on distance from last known position
	 *********************************************************************************************************/
	public boolean isPositionOutlier(PositionMeasurement p) {
		boolean isValid = true;
		Point last = worldState.getVehicleState().getLastPositionMeasurement();
		double distance = 0;
		if(last != null) {
			distance = last.distance(p.getPosition());
		}
		//Has the vehicle moved more than 4 cm since the last reading?
		if(distance > OUTLIER_THRESHOLD_MM) isValid = false;
		
		if(!isValid) {
			sequentialInvalidPositionCount++;
			invalidReadCount++;
			if(sequentialInvalidPositionCount > maxSequentialInvalidPositionCount) {
				sequentialInvalidPositionCount = 0;
				isValid = true;
				worldState.getVehicleState().getHistoricalPositionMap().clear();
			}
		}else {
			sequentialInvalidPositionCount = 0;
			validReadCount++;
		}
		return isValid;
	}
	
	/*********************************************************************************************************
	 * Limits translation distance between any two frames to 100mm
	 *********************************************************************************************************/
	public Point translateToValidPosition(PositionMeasurement p) {
		int MAX_DISTANCE_PER_FRAME_MM = 50;
		Point destination = p.getPosition();
		Point source = worldState.getVehicleState().getLastPositionMeasurement();
		if(source == null) {
			source = destination; //Assume no movement on the first iteration
		}
		double distance = destination.distance(source);
		Point translatedPosition = p.getPosition();
		if(distance > MAX_DISTANCE_PER_FRAME_MM) {
			double proportion = MAX_DISTANCE_PER_FRAME_MM/distance;
			double x = ((destination.getX() - source.getX()) * proportion) + source.getX();
			double y = ((destination.getY() - source.getY()) * proportion) + source.getY();
			translatedPosition.setLocation(x,y);
		}
		return translatedPosition;
	}
}
