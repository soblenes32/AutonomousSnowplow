#include <string.h>
#include <Pozyx.h>
#include <Pozyx_definitions.h>
#include <Wire.h>

/*****************************************************************
 * All sent and received messages are linebreak-terminated
 * Receive:
 * [GM] - Get status of motors. Replies [M,L,R] where L and R indicates an integer in range -255 to 255
 * [SLXXXX] - Set status of left motor where X indicates an integer in range -255 to 255. No reply
 * [SRXXXX] - Set status of right motor where X indicates an integer in range -255 to 255. No reply
 * [SAM,NAME1,X1,Y1,Z1,X2,Y2,Z2 ...] - Set anchor positions manually. ex: [SAM,0x681c,0,0,0,0x6165,1,1,1,0x6879,2,2,2,0x6169,3,3,3]
 * [SAA] - Set anchor positions using automatic settings
 * [GT] - Get telemetry. Replies [T,X,Y,Z,YAW,PITCH,ROLL,ERRX,ERRY,ERRZ,ERRXY,ERRXZ,ERRYZ]
 * [GA] - Get anchor positions. Replies [A,NAME1,X1,Y1,Z1,NAME2,X2,Y2,Z2, ... ]
 * [PTXXXX] - Poll telemetry at specified ms interval. X denotes an integer in range 0 to 9999
 *****************************************************************/

//Pin assignments
/************************************
 * IA IB Result
 * L  L  Off
 * H  L  Forward
 * L  H  Reverse
 * H  H  Off
 ************************************/
int MOTOR_L_A_IA = 6; //Connect to IA1
int MOTOR_L_A_IB = 9; //Connect to IB1
int MOTOR_R_B_IA = 10;//Connect to IA2
int MOTOR_R_B_IB = 11;//Connect to IB2

//Pozyx config
uint16_t remote_id = 0x6000;                            // set this to the ID of the remote device
bool remote = false;                                    // set this to true to use the remote ID

const uint8_t num_anchors = 4;
uint16_t anchors[num_anchors] = {0x681c, 0x6165, 0x6879, 0x6169}; // the network id of the anchors: change these to the network ids of your anchors.

int32_t anchors_x[num_anchors] = {0, 0, 0, 0}; // anchor x-coorindates in mm (used only in manual anchor position override)
int32_t anchors_y[num_anchors] = {0, 0, 0, 0}; // anchor y-coordinates in mm (used only in manual anchor position override)
int32_t heights[num_anchors] = {1, 1, 1, 1}; // anchor z-coordinates in mm

int32_t height = 10;                                  // height of device, required in 2.5D positioning
uint8_t algorithm = POZYX_POS_ALG_TRACKING;             // positioning algorithm to use. try POZYX_POS_ALG_TRACKING for fast moving objects.
uint8_t dimension = POZYX_2_5D;                           // positioning dimension

//Motor control timing variables
unsigned long previousMillis = 0;
float waitInterval = (100/255); //0.1 second for the motor to go from 0 to full

//Telemetry streaming timing variables
unsigned long previousTelemetryMillis = 0;
float streamTelemetryInterval = 0; //0 to shut off

char inData[128];
int idx = 0;

//The intended motor speed
int motorTargetL = 0;
int motorTargetR = 0;

//The current motor speed
int motorValueL = 0;
int motorValueR = 0;

//Buffer for reading raw serial messages
char buffer[6];

void setup() {
  Serial.begin(9600);

  //Init pozyx
  initPozyx(true);
  
  

  //Setup the pin outputs
  pinMode( MOTOR_L_A_IA, OUTPUT );
  pinMode( MOTOR_L_A_IB, OUTPUT );
  pinMode( MOTOR_R_B_IA, OUTPUT );
  pinMode( MOTOR_R_B_IB, OUTPUT );
  digitalWrite( MOTOR_L_A_IA, LOW );
  digitalWrite( MOTOR_L_A_IB, LOW );
  digitalWrite( MOTOR_R_B_IA, LOW );
  digitalWrite( MOTOR_R_B_IB, LOW );
}

void loop() {
  
  /*********************************************************
   * Part 1: Receive new instructions from serial connection
   *********************************************************/
  byte numBytesAvailable = Serial.available();
  if (numBytesAvailable > 0) {
    
    char inChar;
    for (int i=0; i <= numBytesAvailable && inChar != '\n'; i++, idx++) {
      inChar = Serial.read();
      inData[idx] = inChar;
    }
    idx--;
    /*********************************************************
     * Part 2: Upon termination char, interpret the command
     *********************************************************/
    if(inChar == '\n'){
      if(inData[0] == 'G'){
        /************************************************ 
        *  G = Get a status message. e.g. "GR"
        ************************************************/
        int value = 0;
        if(inData[1] == 'M'){ //Left motor value
          value = motorValueL;
           Serial.print("M,");
           Serial.print(motorValueL); Serial.print(",");
           Serial.println(motorValueR);
        }else if(inData[1] == 'T'){ //Telemetry
          printTelemetryToSerial();
        }else if(inData[1] == 'A'){ //Anchor coordinates
          //[A,NAME1,X1,Y1,Z1,NAME2,X2,Y2,Z2, ... ]
          coordinates_t anchor_coor;
          Serial.print("A");
          for(int i = 0; i < num_anchors; i++){
            Pozyx.getDeviceCoordinates(anchors[i], &anchor_coor, remote_id);
            Serial.print(",");
            Serial.print(anchors[i], HEX);
            Serial.print(",");
            Serial.print(anchor_coor.x);
            Serial.print(",");
            Serial.print(anchor_coor.y);
            Serial.print(",");
            Serial.print(anchor_coor.z);
          }
          Serial.println();
        }
      }else if(inData[0] == 'S'){
        /************************************************ 
         *  S = Set motor to value. e.g. "SL-255"
         ************************************************/
        strncpy(buffer, &inData[2], (sizeof(buffer) - 2)); //Get the value substring
        int value = atoi(buffer); //Parse the value substring to an int
        if(value > 255){
          value = 255;
        }
        if(value < -255){
          value = -255;
        }
        if(inData[1] == 'L'){
          /************************************************ 
           *  SL = Set left motor
           ************************************************/
          motorTargetL = value;
        }else if(inData[1] == 'R'){
          /************************************************ 
           *  SR = Set right motor
           ************************************************/
          motorTargetR = value;
        }else if(inData[1] == 'A'){
          if(inData[2] == 'M'){
            /************************************************ 
             *  SAM = Set anchors manually
             ************************************************/
            int anchorIdx = 0;
            int fieldIdx = 0;
            int tokenIdx = 0;
            //Read the "SA" token
            char* msg = strtok(inData,",");
            //Discard the "SA" token, and read token 0
            msg = strtok(NULL,",");
            while (msg != NULL){
              switch (fieldIdx) {
                case 0: anchors[anchorIdx] = strtoul(msg, NULL, 16); break;
                case 1: anchors_x[anchorIdx] = atoi(msg); break;
                case 2: anchors_y[anchorIdx] = atoi(msg); break;
                case 3: heights[anchorIdx] = atoi(msg); break;
              }
              msg = strtok(NULL,",");
              tokenIdx++;
              fieldIdx = tokenIdx%4; //rounds down
              anchorIdx = tokenIdx/4; //rounds down
            }
  
            //Re-initialize the pozyx device with the new configuration
            initPozyx(false);
          }else if(inData[2] == 'A'){
            /************************************************ 
             *  SAA = Set anchors automatically
             ************************************************/
             initPozyx(true);
          }
        }
        memset(buffer, 0, sizeof(buffer)); //Reset shared var

      }else if(inData[0] == 'P'){
        if(inData[1] == 'T'){ //[PTXXXX] - Poll telemetry at specified ms interval. X denotes an integer in range 0 to 9999
          strncpy(buffer, &inData[2], (sizeof(buffer) - 2)); //Get the value substring
          streamTelemetryInterval = atoi(buffer); //Parse the value substring to an int
        }
        memset(buffer, 0, sizeof(buffer)); //Reset shared buffer var
      }else{
        Serial.print("Did not understand command: ");
        Serial.println(inData);
      }

      //Reset the read buffer array and associated index for the next message
      memset(inData, 0, sizeof(inData));
      idx = 0;
    }
  }

  /*********************************************************
   * Part 2: Smooth speed transition to prevent motor damage
   *********************************************************/
  unsigned long currentMillis = millis();
  if (currentMillis - previousMillis >= waitInterval) {
    previousMillis = currentMillis;
    //Increment motorValueA toward its target
    if(motorTargetL > motorValueL){
      motorValueL++;
    }else if (motorTargetL < motorValueL){
      motorValueL--;
    }
    //Increment motorValueB toward its target
    if(motorTargetR > motorValueR){
      motorValueR++;
    }else if (motorTargetR < motorValueR){
      motorValueR--;
    }

    /*********************************************************
     * Part 3: Apply motor values to output pins LEFT
     *********************************************************/
    if(motorValueL == 0){ // stop
      digitalWrite(MOTOR_L_A_IA, LOW );
      digitalWrite(MOTOR_L_A_IB, LOW );
    }else if(motorValueL > 0){ // forward
      analogWrite(MOTOR_L_A_IA, motorValueL); 
      digitalWrite(MOTOR_L_A_IB, LOW); 
    }else if(motorValueL < 0){ // reverse
      digitalWrite(MOTOR_L_A_IA, LOW); 
      analogWrite(MOTOR_L_A_IB, (-1*motorValueL));
    }

    /*********************************************************
     * Part 3: Apply motor values to output pins RIGHT
     *********************************************************/
    if(motorValueR == 0){ // stop
      digitalWrite(MOTOR_R_B_IA, LOW );
      digitalWrite(MOTOR_R_B_IB, LOW );
    }else if(motorValueR > 0){ // forward
      analogWrite(MOTOR_R_B_IA, motorValueR); 
      digitalWrite(MOTOR_R_B_IB, LOW);
    }else if(motorValueR < 0){ // reverse
      digitalWrite(MOTOR_R_B_IA, LOW); 
      analogWrite(MOTOR_R_B_IB, (-1*motorValueR));
    }
  }


  /*********************************************************
   * Part 2: Smooth speed transition to prevent motor damage
   *********************************************************/

  if (streamTelemetryInterval != 0 && (currentMillis - previousTelemetryMillis >= streamTelemetryInterval)) {
    previousTelemetryMillis = currentMillis;
    printTelemetryToSerial();
  }
  

  
}

void initPozyx(boolean isAutoConfigureAnchors){
  if(Pozyx.begin() == POZYX_FAILURE){
    Serial.println(F("ERROR: Unable to connect to POZYX shield"));
    Serial.println(F("Reset required"));
    delay(100);
    abort();
  }

  if(!remote){
    remote_id = NULL;
  }
  
  // clear all previous devices in the device list
  Pozyx.clearDevices(remote_id);
  delay(1000);
  // sets the anchor automatically
  if(isAutoConfigureAnchors){
    Pozyx.doAnchorCalibration(dimension, 10, 4, anchors, heights);
  }else{
    for(int i = 0; i < num_anchors; i++){
      device_coordinates_t anchor;
      anchor.network_id = anchors[i];
      anchor.flag = 0x1;
      anchor.pos.x = anchors_x[i];
      anchor.pos.y = anchors_y[i];
      anchor.pos.z = heights[i];
      Pozyx.addDevice(anchor, remote_id);
    }
  }
  // sets the positioning algorithm
  Pozyx.setPositionAlgorithm(algorithm, dimension, remote_id);
}

void printTelemetryToSerial(){
  coordinates_t position;
  pos_error_t pos_error;
  euler_angles_t orientation;
  Pozyx.doPositioning(&position, dimension, height, algorithm);
  Pozyx.getPositionError(&pos_error);
  Pozyx.getEulerAngles_deg(&orientation, remote_id);
  //[T,X,Y,Z,YAW,PITCH,ROLL]
  Serial.print("T,");
  Serial.print(position.x); Serial.print(",");
  Serial.print(position.y); Serial.print(",");
  Serial.print(position.z); Serial.print(",");
  Serial.print(orientation.heading); Serial.print(",");
  Serial.print(orientation.pitch); Serial.print(",");
  Serial.print(orientation.roll); Serial.print(",");
  Serial.print(pos_error.x); Serial.print(",");
  Serial.print(pos_error.y); Serial.print(",");
  Serial.print(pos_error.z); Serial.print(",");
  Serial.print(pos_error.xy); Serial.print(",");
  Serial.print(pos_error.xz); Serial.print(",");
  Serial.println(pos_error.yz);
}

