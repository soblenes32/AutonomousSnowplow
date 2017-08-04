package com.asl.snowplow.model;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

public class AnchorState {
	private String name = null;
	//private Point position = new Point();
	private Vector3D position = null;
	
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Vector3D getPosition() {
		return position;
	}

	public void setPosition(Vector3D position) {
		this.position = position;
	}
}
