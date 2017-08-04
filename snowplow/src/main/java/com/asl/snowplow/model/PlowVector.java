package com.asl.snowplow.model;

import java.awt.Point;

public class PlowVector {
	private Point source;
	private Point destination;
	public Point getSource() {
		return source;
	}
	public void setSource(Point source) {
		this.source = source;
	}
	public Point getDestination() {
		return destination;
	}
	public void setDestination(Point destination) {
		this.destination = destination;
	}
}
