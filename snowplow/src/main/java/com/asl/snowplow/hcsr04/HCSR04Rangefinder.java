package com.asl.snowplow.hcsr04;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

public class HCSR04Rangefinder {
	private boolean isWaitingForEcho = false;
	private Pin triggerPin = RaspiPin.GPIO_04;
	private Pin echoPin = RaspiPin.GPIO_05;
	
	private final static float SOUND_IN_CM_PER_NS = (343f * 100f) / 1000000000f; // 343(m/s) * 100 (cm/m) / 1,000,000,000 (ns/s)
	
	private GpioPinDigitalOutput rangefinderTrigger = null;
	private GpioPinDigitalInput rangefindEcho = null;
	
	private long finishTime;
	private long startTime;
	
	
	public HCSR04Rangefinder() {
		init();
	}
	public HCSR04Rangefinder(Pin triggerPin, Pin echoPin) {
		this.triggerPin = triggerPin;
		this.echoPin = echoPin;
		init();
	}
	
	private void init(){
		//Setup GPIO Pins 
		GpioController gpio = GpioFactory.getInstance();
		rangefinderTrigger = gpio.provisionDigitalOutputPin(triggerPin, "Range Finder Trigger", PinState.LOW);
		rangefindEcho = gpio.provisionDigitalInputPin(echoPin, "Range Pulse Result", PinPullResistance.PULL_DOWN);
		
		//Setup a trigger to register the echo
//		rangefindEcho.addTrigger(new GpioCallbackTrigger(new Callable<Void>() {
//            public Void call() throws Exception {
//            	System.out.println("start: trigger callback");
//            	if(rangefindEcho.isHigh() && startTime == -1){
//            		startTime = System.nanoTime();
//        		}else if(rangefindEcho.isLow() && finishTime == -1){
//					finishTime = System.nanoTime();
//					double distanceInCm = (finishTime - startTime) * SOUND_IN_CM_PER_NS / 2; //round trip means 2x the time, so divide by 2
//					System.out.println("Start ns: " + startTime + ", finish ns: " + finishTime);
//					System.out.print("Distance: " + distanceInCm + " cm");
//            	}
//            	System.out.println("finish: trigger callback");
//            	return null;
//        	}
//		}));
	}
	
	public float firePulse(){
		System.out.println("start: firing pulse");
		startTime = -1;
		finishTime = -1;
		
		//Fire the ultrasonic pulse
		rangefinderTrigger.high();
		long pulseTime = System.nanoTime();
		long pulseEnd = pulseTime + (10 * 1000); //10 microseconds
		while(pulseTime < pulseEnd){
			pulseTime = System.nanoTime();
		}
		rangefinderTrigger.low();
		System.out.println("finish: firing pulse");
		
		//Wait for echo signal to go high - this should be pretty quick
		while(!rangefindEcho.isHigh()){
			finishTime = System.nanoTime();
		}
		
		
		//Wait for echo signal to go low
		startTime = System.nanoTime();
		while(rangefindEcho.isHigh()){
			finishTime = System.nanoTime();
		}
		finishTime = System.nanoTime();
		float distanceInCm = finishTime-startTime; //round trip means 2x the time, so divide by 2
		distanceInCm = distanceInCm * SOUND_IN_CM_PER_NS / 2;
		return distanceInCm;
	}
}
