package com.asl.snowplow.model;

public enum VehicleOperationMode {
	PAUSED, //Queue does not get processed. Vehicle can be controlled via joystick
	COMMAND_QUEUE, //Accepts new user commands in the command queue. Processes items in the command queue
	AUTONOMOUS //Processes items in the command queue. Does not accept new user commands. Generates new command queue commands
}
