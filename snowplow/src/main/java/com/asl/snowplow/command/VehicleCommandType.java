package com.asl.snowplow.command;

public enum VehicleCommandType {
	MOVE_TO, //Args: [0] = x, [1] = y
	STOP,
	NAV_TO //Args: [0] = x, [1] = y
}
