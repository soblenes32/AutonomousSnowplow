package com.asl.snowplow.controller;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.asl.snowplow.model.AnchorState;
import com.asl.snowplow.model.WorldState;
import com.asl.snowplow.service.ArduinoRxTxUsbService;
import com.asl.snowplow.service.VehicleInstructionService;

/*************************************************************************************************************
 * Issues command to asynchronously read device sensor data
 * 
 * Method URLs
 * 
 * /update/vehicle - Update the vehicle position/orientation
 * /update/anchors - Update the anchor positions
 * /poll/{rate} - Change the vehicle position/orientation polling rate in ms. Set to 0 to disable polling.
 * 
 *************************************************************************************************************/

@Controller
@RequestMapping("/telemetry")
public class TelemetryController {
	@Inject
	ArduinoRxTxUsbService rxTxUsbService;
	
	@Inject
	WorldState worldState;
	
	@Inject
	VehicleInstructionService vehicleLowLevelInstructionService;

	@RequestMapping(value="/update/vehicle", method=RequestMethod.GET)
	public @ResponseBody String getTelemetry() {
		rxTxUsbService.getTelemetry();
		return "{}";
	}
	
	@RequestMapping(value="/poll/{rate}", method=RequestMethod.GET)
	public @ResponseBody String polltelemetry(@PathVariable int rate) {
		rxTxUsbService.pollTelemetry(rate);
		return "{}";
	}
	
	@RequestMapping(value="/update/anchors", method=RequestMethod.GET)
	public @ResponseBody String getAnchors() {
		rxTxUsbService.getAnchors();
		return "{}";
	}
	
	@RequestMapping(value="/heading/calibrate/{headingCalibration}", method=RequestMethod.GET)
	public @ResponseBody String calibrateHeading(@PathVariable double headingCalibration) {
		worldState.getVehicleState().setHeadingCalibration(headingCalibration);
		return "{}";
	}
	
	@RequestMapping(value="/update/anchors/automatic", method=RequestMethod.POST)
	public @ResponseBody String setAnchorsAutomatic() {
		rxTxUsbService.setAnchorsAutomatic();
		return "{}";
	}
	
	@RequestMapping(value="/update/anchors/manual", method=RequestMethod.POST)
	public @ResponseBody List<AnchorState> setAnchorsAutomatic(@RequestBody List<AnchorState> anchorStateList) {
		rxTxUsbService.setAnchorsManual(anchorStateList);
		return anchorStateList;
	}
}