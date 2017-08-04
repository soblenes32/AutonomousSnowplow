package com.asl.snowplow.command;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true) 
public class VehicleCommand {
	private long commandID;
	private VehicleCommandType vehicleCommandType;
	private String[] args;
	public VehicleCommand() {}
	public VehicleCommand(VehicleCommandType vehicleCommandType) {
		this.vehicleCommandType = vehicleCommandType;
	}
	public VehicleCommandType getVehicleCommandType() {
		return vehicleCommandType;
	}
	public void setVehicleCommandType(VehicleCommandType vehicleCommandType) {
		this.vehicleCommandType = vehicleCommandType;
	}
	public String[] getArgs() {
		return args;
	}
	public void setArgs(String[] args) {
		this.args = args;
	}
	public long getCommandID() {
		return commandID;
	}
	public void setCommandID(long commandID) {
		this.commandID = commandID;
	}
}
