package com.asl.snowplow.service;

public enum MotorDesignator {
	LEFT("L"),
	RIGHT("R");
	String designation;
	private MotorDesignator(String designation) {
		this.designation = designation;
	}
	public String getDesignation() {
		return designation;
	}
}
