package com.asl.snowplow.model;

import java.awt.Point;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class ZoneCell {
	private Point coordinates = new Point();
	private int snowVolume = 0;
	private boolean obstruction = false;
	@JsonIgnore
	private boolean dynamicObstruction = false; //true if the obstruction was set based on lidar detectionmail
	private boolean plowZone = false;
	private boolean plowedSnowZone = false;
	private boolean parkZone = false;

	public Point getCoordinates() {
		return coordinates;
	}
	public void setCoordinates(Point coordinates) {
		this.coordinates = coordinates;
	}
	public int getSnowVolume() {
		return snowVolume;
	}
	public void setSnowVolume(int snowVolume) {
		this.snowVolume = snowVolume;
	}
	public boolean isObstruction() {
		return obstruction;
	}
	public void setObstruction(boolean obstruction) {
		this.obstruction = obstruction;
	}
	public boolean isPlowZone() {
		return plowZone;
	}
	public void setPlowZone(boolean plowZone) {
		this.plowZone = plowZone;
	}
	public boolean isPlowedSnowZone() {
		return plowedSnowZone;
	}
	public void setPlowedSnowZone(boolean plowedSnowZone) {
		this.plowedSnowZone = plowedSnowZone;
	}
	public boolean isParkZone() {
		return parkZone;
	}
	public void setParkZone(boolean parkZone) {
		this.parkZone = parkZone;
	}
	public boolean isDynamicObstruction() {
		return dynamicObstruction;
	}
	public void setDynamicObstruction(boolean dynamicObstruction) {
		this.dynamicObstruction = dynamicObstruction;
	}
	
}
