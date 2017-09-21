package com.asl.snowplow.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.springframework.stereotype.Service;

import com.asl.snowplow.model.AnchorState;
import com.asl.snowplow.model.PositionMeasurement;
import com.asl.snowplow.model.VehicleState;
import com.asl.snowplow.model.WorldState;
import com.asl.snowplow.service.websocket.TelemetryWebsocketService;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

@Service
public class ArduinoRxTxUsbService implements SerialPortEventListener{
	@Inject
	WorldState worldState;
	
	@Inject
	TelemetryWebsocketService telemetryWebsocketService;
	
	SerialPort serialPort 					= null;
	private BufferedReader input 			= null;
	private OutputStream output 			= null;
	private static final int TIME_OUT 		= 2000;
	private static final int DATA_RATE 		= 9600;
	private static final String PORT_NAME 	= "/dev/arduino";
	private static final String LINEBREAK_TERMINATOR = "\n";
	
	@PostConstruct
	private void init(){
		System.setProperty("gnu.io.rxtx.SerialPorts", "/dev/arduino" + File.pathSeparator + "/dev/rplidar");
		connectToDevice();
		
		//Initialize polling cycle
		pollTelemetry(100);
		
		//Fetch anchor locations
		getAnchors();
	}
	
	@PreDestroy
	private void close(){
		if (serialPort != null) {
			serialPort.removeEventListener();
			serialPort.close();
			serialPort = null;
		}
		try{if(input != null) input.close();}catch(Exception e){}
		try{if(output != null) output.close();}catch(Exception e){}
	}
	
	
	/***********************************************************************
	 * Establish a connection to the local raspi USB port
	 ***********************************************************************/
	public void connectToDevice(){
		// the next line is for Raspberry Pi and 
        // gets us into the while loop and was suggested here was suggested http://www.raspberrypi.org/phpBB3/viewtopic.php?f=81&t=32186
		
//		CommPortIdentifier portId = null;
//		@SuppressWarnings("rawtypes")
//		Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();
//		
//		//First, Find an instance of serial port as set in PORT_NAMES.
//		while (portEnum.hasMoreElements()) {
//			CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
//			if (currPortId.getName().equals(PORT_NAME)) {
//				portId = currPortId;
//				break;
//			}
//		}
		try {
			CommPortIdentifier portId = CommPortIdentifier.getPortIdentifier(PORT_NAME);

			// open serial port, and use class name for the appName.
			serialPort = (SerialPort) portId.open(this.getClass().getName(), TIME_OUT);
		
			// set port parameters
			serialPort.setSerialPortParams(DATA_RATE,
					SerialPort.DATABITS_8,
					SerialPort.STOPBITS_1,
					SerialPort.PARITY_NONE);
		
			// open the streams
			input = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
			output = serialPort.getOutputStream();
		
			// add event listeners
			serialPort.addEventListener(this);
			serialPort.notifyOnDataAvailable(true);
			
			Thread.sleep(2000);
		} catch (Exception e) {
			System.err.println(e.toString());
		}
	}
	
	/***********************************************************************
	 * Handle an event on the serial port. Read the data and print it.
	 * 
	 * [GM] - Get status of motors. Replies [M,L,R] where L and R indicates an integer in range -255 to 255
	 * [GT] - Get telemetry. Replies [T,X,Y,Z,YAW,PITCH,ROLL]
	 * [GA] - Get anchor positions. Replies [A,NAME1,X1,Y1,Z1,NAME2,X2,Y2,Z2, ... ]
	 ***********************************************************************/
	public synchronized void serialEvent(SerialPortEvent oEvent) {
		if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
			try {
				String inputLine=input.readLine();
				//System.out.println("InputLine: " + inputLine);
				String[] cellArr = inputLine.split(",");
				VehicleState vs = worldState.getVehicleState();
				if(cellArr[0].equals("M")){
					if(cellArr.length == 3){
						vs.setMotorAValue(Float.parseFloat(cellArr[1]));
						vs.setMotorAValue(Float.parseFloat(cellArr[2]));
					}
					telemetryWebsocketService.sendVehicleState();
				}else if(cellArr[0].equals("T")){ //Parse telemetry. Expected form: [T,X,Y,Z,YAW,PITCH,ROLL,ERRX,ERRY,ERRZ,ERRXY,ERRXZ,ERRYZ]
					PositionMeasurement pm = new PositionMeasurement();
					
					pm.getPosition().setLocation((int) Double.parseDouble(cellArr[1]), (int) Double.parseDouble(cellArr[2]));
					pm.setErrorX(Float.parseFloat(cellArr[7]));
					pm.setErrorY(Float.parseFloat(cellArr[8]));
					pm.setErrorXY(Float.parseFloat(cellArr[10]));
					vs.updatePosition(pm);
					vs.setOrientation(new Vector3D(Double.parseDouble(cellArr[6]), Double.parseDouble(cellArr[5]), Double.parseDouble(cellArr[4])));
					telemetryWebsocketService.sendVehicleState();
				}else if(cellArr[0].equals("A")){
					int anchorCount = (cellArr.length - 1) / 4;
					worldState.getAnchorStateList().clear();
					for(int i=0; i<anchorCount;i++){
						AnchorState as = new AnchorState();
						worldState.getAnchorStateList().add(as);
						as.setName("0x"+cellArr[i*4+1]);
						as.setPosition( new Vector3D(Integer.parseInt(cellArr[i*4+2]), Integer.parseInt(cellArr[i*4+3]), Integer.parseInt(cellArr[i*4+4])) );
					}
					worldState.updateAnchorState();
					telemetryWebsocketService.sendAnchorStateList();
				}else{
					System.out.println("Unknown message from microcontroller: " + inputLine);
					System.out.println("Parsed data: ");
					for(String s: cellArr){
						System.out.println(s);
					}
				}
			} catch (Exception e) {
				System.err.println(e.toString());
			}
		}
		// Ignore all the other eventTypes, but you should consider the other ones.
	}
	
	public void write(String message){
		if(serialPort == null){
			connectToDevice();
		}
		try {
			output.write(message.getBytes(StandardCharsets.US_ASCII));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/******************************************************
	 * Set motor speed
	 * @param speed value between -1 and 1
	 ******************************************************/
	public void setMotorSpeed(float speedPercent, MotorDesignator designator){
		//System.out.println("Setting " + designator.designation + " motor to: " + speedPercent);
		int motorSpeed = (int) (255f * speedPercent);
		String message = new StringBuilder("S")
			.append(designator.getDesignation())
			.append(motorSpeed)
			.append(LINEBREAK_TERMINATOR)
			.toString();
		write(message);
	}
	
	/******************************************************
	 * Request motor speed. Arduino will reply with speed
	 ******************************************************/
	public void getMotorSpeed(){
		String message = new StringBuilder("GM")
			.append(LINEBREAK_TERMINATOR)
			.toString();
		write(message);
	}
	
	/******************************************************
	 * Request telemetry data
	 ******************************************************/
	public void getTelemetry(){
		String message = new StringBuilder("GT")
			.append(LINEBREAK_TERMINATOR)
			.toString();
		write(message);
	}
	
	/******************************************************
	 * Request telemetry data at regular intervals
	 * @param intervalms - how frequently to report 
	 * telemetry data. If 0 then stop.
	 ******************************************************/
	public void pollTelemetry(int intervalms){
		String message = new StringBuilder("PT")
			.append(intervalms)
			.append(LINEBREAK_TERMINATOR)
			.toString();
		write(message);
	}
	
	/******************************************************
	 * Request anchor position data
	 ******************************************************/
	public void getAnchors(){
		String message = new StringBuilder("GA")
			.append(LINEBREAK_TERMINATOR)
			.toString();
		write(message);
	}
	
	/****************************************************************
	 * Auto-configure the anchor positions then update anchor positions
	 * from the arduino
	 ****************************************************************/
	public void setAnchorsAutomatic(){
		String message = new StringBuilder("SAA")
			.append(LINEBREAK_TERMINATOR)
			.toString();
		write(message);
		getAnchors();
	}
	
	/****************************************************************
	 * Auto-configure the anchor positions
	 * 
	 * Example message
	 * SAM,0x681c,0,0,0,0x6165,1,1,1,0x6879,2,2,2,0x6169,3,3,3
	 ****************************************************************/
	public void setAnchorsManual(List<AnchorState> anchorStateList){
		StringBuilder sb = new StringBuilder("SAM");
		for(AnchorState as: anchorStateList){
			sb.append(",")
				.append(as.getName())
				.append(",")
				.append((int) as.getPosition().getX())
				.append(",")
				.append((int) as.getPosition().getY())
				.append(",")
				.append((int) as.getPosition().getZ());
		}
		sb.append(LINEBREAK_TERMINATOR);
		write(sb.toString());
		getAnchors();
	}
}
