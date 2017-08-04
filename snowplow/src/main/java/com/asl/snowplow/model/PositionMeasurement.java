package com.asl.snowplow.model;

import java.awt.Point;

public class PositionMeasurement {
	private Point position = new Point();
	private float errorX;
	private float errorY;
	private float errorXY;
	
	public Point getPosition() {
		return position;
	}
	public void setPosition(Point position) {
		this.position = position;
	}
	public float getErrorX() {
		return errorX;
	}
	public void setErrorX(float errorX) {
		this.errorX = errorX;
	}
	public float getErrorY() {
		return errorY;
	}
	public void setErrorY(float errorY) {
		this.errorY = errorY;
	}
	public float getErrorXY() {
		return errorXY;
	}
	public void setErrorXY(float errorXY) {
		this.errorXY = errorXY;
	}
}
