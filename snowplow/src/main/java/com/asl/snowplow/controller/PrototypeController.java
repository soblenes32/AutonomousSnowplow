package com.asl.snowplow.controller;

import java.awt.Point;

import javax.inject.Inject;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.asl.snowplow.model.AnchorState;
import com.asl.snowplow.model.WorldState;
import com.asl.snowplow.service.MotorDesignator;
import com.asl.snowplow.service.ArduinoRxTxUsbService;
import com.asl.snowplow.service.VehicleInstructionService;

@Controller
@RequestMapping("/prototype")
public class PrototypeController {
	@Inject
	ArduinoRxTxUsbService rxTxUsbService;
	
	@Inject
	WorldState worldState;
	
	@Inject
	VehicleInstructionService vehicleInstructionService;
	
	@RequestMapping(value="usb/connect", method=RequestMethod.GET)
	public @ResponseBody String usbConnect() {
		//return loUsbService.connectToDevice();
		//return linuxOSUsbFileService.connectToDevice();
		rxTxUsbService.connectToDevice();
		return "done";
	}
	
	
	@RequestMapping(value="usb/read/{designation}", method=RequestMethod.GET)
	public @ResponseBody String usbRead(@PathVariable MotorDesignator designation) {
		rxTxUsbService.getMotorSpeed();
		return "done";
	}
	
	@RequestMapping(value="usb/write/forward", method=RequestMethod.GET)
	public @ResponseBody String usbWriteForward() {
		rxTxUsbService.setMotorSpeed(1.0f, MotorDesignator.LEFT);
		rxTxUsbService.setMotorSpeed(1, MotorDesignator.RIGHT);
		return "done";
	}
	@RequestMapping(value="usb/write/backward", method=RequestMethod.GET)
	public @ResponseBody String usbWriteBackward() {
		rxTxUsbService.setMotorSpeed(-1.0f, MotorDesignator.LEFT);
		rxTxUsbService.setMotorSpeed(1, MotorDesignator.RIGHT);
		return "done";
	}
	@RequestMapping(value="usb/write/stop", method=RequestMethod.GET)
	public @ResponseBody String usbWriteStop() {
		rxTxUsbService.setMotorSpeed(-0.0f, MotorDesignator.LEFT);
		rxTxUsbService.setMotorSpeed(1, MotorDesignator.RIGHT);
		return "done";
	}
	
	@RequestMapping(value="getanchors", method=RequestMethod.GET)
	public @ResponseBody String getAnchors() {
		rxTxUsbService.getAnchors();
		return "done";
	}
	
	@RequestMapping(value="getangle", method=RequestMethod.GET)
	public @ResponseBody String getAngle() {
		AnchorState a0 = worldState.getAnchorStateList().get(0);
		return ""+vehicleInstructionService.calculateRotationToCoordinates(a0.getPosition(), true);
	}
	
	
}