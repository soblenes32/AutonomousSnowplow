package com.asl.snowplow.service;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.asl.snowplow.command.JoystickCommand;
import com.asl.snowplow.model.WorldState;

@Service
public class MotorStateService {
	
	@Inject
	ArduinoRxTxUsbService rxTxUsbService;
	
	@Inject
	WorldState worldState;
	
	/******************************************************************
	 * x: -100 = left turn; 100 = right turn
	 * y: -100 = full forward; 100 = full backwards
	 * 
	 * Note: the joystick interprets "up" motion on the Y axis as 
	 * negative values and "down" motion as positive. Here, we reverse
	 * these values because we want "up" to be forwards.
	 * 
	 * forward (0, -100)
	 * backward(0, 100)
	 * left(-100, 0)
	 * right(100, 0)
	 * forward-left(-70,-70)
	 * 
	 * @param jc
	 ******************************************************************/
	public void joystickToMotorCommands(JoystickCommand jc){
		float leftMotor = (jc.getX() + (-1 * jc.getY())) / 100;
		float rightMotor = ((-1 * jc.getX()) + (-1 * jc.getY())) / 100;
		leftMotor = (leftMotor > 1)? 1:leftMotor;
		leftMotor = (leftMotor < -1)? -1:leftMotor;
		rightMotor = (rightMotor > 1)? 1:rightMotor;
		rightMotor = (rightMotor < -1)? -1:rightMotor;
		
		System.out.println("inputs: (" + jc.getX() + ", " + jc.getY() + "), leftMotor: " + leftMotor + ", rightMotor: " + rightMotor);
		
		rxTxUsbService.setMotorSpeed(leftMotor, MotorDesignator.RIGHT);
		rxTxUsbService.setMotorSpeed(leftMotor, MotorDesignator.LEFT);
	}
	
	
	public void updateMotors(){
		rxTxUsbService.setMotorSpeed(worldState.getVehicleState().getMotorATarget(), MotorDesignator.LEFT);
		rxTxUsbService.setMotorSpeed(worldState.getVehicleState().getMotorBTarget(), MotorDesignator.RIGHT);
	}
	
}
