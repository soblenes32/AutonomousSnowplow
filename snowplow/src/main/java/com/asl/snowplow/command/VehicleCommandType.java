package com.asl.snowplow.command;

public enum VehicleCommandType {
	MOVE_TO, //Args: [0] = x, [1] = y
	STOP,
	NAV_TO, //Args: [0] = x, [1] = y
	STOP_UNTIL, //Args: [0] = time in ms to resume operation - Date().getTime()
	REVERSE_UNTIL //Args: [0] = time in ms to reverse - Date().getTime()
}
