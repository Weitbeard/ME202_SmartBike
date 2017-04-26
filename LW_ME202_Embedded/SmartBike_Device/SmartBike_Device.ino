/* 
 * ME202 SmartBike Embedded Program
 * LW - 1.01 - 04/26/17 - implemented bluetooth and state machine logic
 *                        revised sensor reading methods for modularity
 * LW - 1.00 - 04/18/17 - Initial prototype for testing sensors
 */
 
 //include wire for I2C (SDA -> A4 ; SCL -> A5)
#include <Wire.h>
 //include sparkfun library for interfacing with the accelerometer
#include <SparkFun_MMA8452Q.h>
 //include adafruit library for BLE module
#include <Adafruit_ATParser.h>
#include <Adafruit_BLE.h>
#include <Adafruit_BLEGatt.h>
#include <Adafruit_BluefruitLE_UART.h>
#include <SoftwareSerial.h>
 //include user-defined BLE module configurations
#include "BluefruitConfig.h"

 //debug serial printout defines
//#define LIGHT_DEBUG
//#define ACCEL_DEBUG
//#define STATE_DEBUG

 //device configurations
#define DEVICE_ID 1          //unique device ID for this SmartBike
#define BLINK_TIME_DELAY 10  //blink rate for status LED

 //embedded device pin connections
#define LIGHT_SENSOR A3
#define ACCEL_LED 5
#define LIGHT_LED 2

 //sensor reading array defines
#define NUM_SENSORS 2              //number of sensor readings to track
#define LIGHT_LEVEL_INDEX 0        //light level from photoresistor
#define MOVEMENT_INDEX 1           //movement from accelerometer
//#define ORIENTATION_INDEX 2       //orientation from accelerometer **TODO**
 //array to hold sensor readings
int sensorReadings[NUM_SENSORS];

 //photoresistor configurations
#define LIGHT_LEVEL_SAMPLES 3      //number of light level readings to store (rolling average)
#define LIGHT_LEVEL_THRESHOLD 400  //threshold under which light level is "dark"
#define DARK false
#define BRIGHT true
 //array to hold readings for averaging
int lightLevelSamples[LIGHT_LEVEL_SAMPLES];

 //accelerometer configurations
#define MOVEMENT_SAMPLES 3         //number of move speed readings to store (rolling average)
#define MOVEMENT_THRESHOLD 0.3     //threshold over which device is "moving"
 //array to hold readings for averaging
float movementSamples[MOVEMENT_SAMPLES];
 //create object to hold accelerometer (for Sparkfun library)
MMA8452Q myAccel;

 //create object to hold BLE module using software serial (to allow USB on hw UART)
SoftwareSerial bluefruitSS = SoftwareSerial(BLUEFRUIT_SWUART_TXD_PIN, BLUEFRUIT_SWUART_RXD_PIN);
Adafruit_BluefruitLE_UART ble(bluefruitSS, BLUEFRUIT_UART_MODE_PIN, BLUEFRUIT_UART_CTS_PIN, BLUEFRUIT_UART_RTS_PIN);

 //state machine data
bool ConnectedState = false;
bool LightLevelState = BRIGHT;
bool MovingState  = false;

/***************************** SETUP CODE ********************************/
void setup() {
   //enable serial communications
  Serial.begin(9600);
   //write program log header to monitor
  Serial.println("LW - Starting Test for ME202 SmartBike");
 
   //initialize BLE module
  initializeBluefruit();
  
   //initialize accelerometer @ +-2g range and 800Hz data rate 
  myAccel.init(SCALE_2G, ODR_800);
  
   //initialize pins for LEDs
  pinMode(ACCEL_LED, OUTPUT);
  pinMode(LIGHT_LED, OUTPUT);
}


/***************************** MAIN LOOP ********************************/
void loop() {  
  /************* State Updates **********************/
   //check if any states have changed by updating the sensor readings and connection status
  bool stateChangeDetected = ( updateLightLevel(sensorReadings) || 
                               updateMovementReadings(sensorReadings) ||
                               updateConnectionStatus()
                             );

  /************ BLE Communication *******************/
//  char n, inputs[BUFSIZE+1];
//
//  if (Serial.available())
//  {
//    n = Serial.readBytes(inputs, BUFSIZE);
//    inputs[n] = 0;
//    // Send characters to Bluefruit
//    Serial.print("Sending: ");
//    Serial.println(inputs);
//
//    // Send input data to host via Bluefruit
//    ble.print(inputs);
//  }
//
//  // Echo received data
//  while ( ble.available() )
//  {
//    int c = ble.read();
//
//    Serial.print((char)c);
//
//    // Hex output too, helps w/debugging!
//    Serial.print(" [0x");
//    if (c <= 0xF) Serial.print(F("0"));
//    Serial.print(c, HEX);
//    Serial.print("] ");
//  }

  /************ State Machine *******************/

  
  #ifdef STATE_DEBUG
    Serial.print("State changed? ");
    Serial.print(stateChangeDetected);
    Serial.print("\tConnected: ");
    Serial.print(ConnectedState);
    Serial.print("\tLight Level: ");
    Serial.print(LightLevelState);
    Serial.print("\tMoving: ");
    Serial.print(MovingState);
    Serial.println("");
  #endif

  delay(50);
}

/************ Bluefruit Functions ***************/
bool initializeBluefruit(void){
  Serial.print(F("Initialising the Bluefruit LE module: "));
  if ( !ble.begin(VERBOSE_MODE) ) {
    Serial.println(F("Couldn't find Bluefruit, make sure it's in CoMmanD mode & check wiring?"));
  }
  Serial.println( F("OK!") );
  if ( FACTORYRESET_ENABLE ) {
    /* Perform a factory reset to make sure everything is in a known state */
    Serial.println(F("Performing a factory reset: "));
    if ( ! ble.factoryReset() ){
      Serial.println(F("Couldn't factory reset"));
    }
  }
  /* Disable command echo from Bluefruit */
  ble.echo(false);
  Serial.println("Requesting Bluefruit info:");
  /* Print Bluefruit information */
  ble.info();
   //turn off debug messages from bluefruit
  ble.verbose(false);
   //set bluefruit to data mode
  ble.setMode(BLUEFRUIT_MODE_DATA);
}

bool updateConnectionStatus(void){
  bool stateChangeDetected = false;
   //check if bluefruit is connected
  bool newStatus = ble.isConnected();
   //check if connection state has changed
  if(newStatus != ConnectedState){
    stateChangeDetected = true;
     //store new connected state and return
    ConnectedState = newStatus;
  }
  return stateChangeDetected;
}

/************ Light Level Functions ***************/
bool updateLightLevel(int * storedReadings){
  static int sampleIndex = 0;
  bool stateChangeDetected = false;
  int aveLL = 0;
  
   //get raw light level reading
  int rawLL = analogRead(LIGHT_SENSOR);
   //add raw light level reading to averaging array for smoothing
  lightLevelSamples[sampleIndex] = rawLL;
   //increment sample index
  sampleIndex = (sampleIndex + 1)%LIGHT_LEVEL_SAMPLES;
   //average raw light levels
  for(int i = 0; i < LIGHT_LEVEL_SAMPLES ; i++){ aveLL += lightLevelSamples[i]; }
  aveLL = aveLL/LIGHT_LEVEL_SAMPLES;
   //store average level reading in the sensor reading array
  *(storedReadings + LIGHT_LEVEL_INDEX) = aveLL;

    #ifdef LIGHT_DEBUG
       //print out the latest sample and computed average
      Serial.print("Light Level: ");
      Serial.print(rawLL);
      Serial.print("\t");
      Serial.print(aveLL);
      Serial.println("");
    #endif

   //check if average light level is LIGHT or DARK
  bool newLL = (aveLL >= LIGHT_LEVEL_THRESHOLD);
   //check if light level state has changed
  if(newLL != LightLevelState){
    stateChangeDetected = true;
     //store new light level and return
    LightLevelState = newLL;
  }
  return stateChangeDetected;
}

/************ Accelerometer Functions ***************/
bool updateMovementReadings(int * storedReadings){
  static int sampleIndex = 0;
  static float lastX = 0;
  static float lastY = 0;
  static float lastZ = 0;
  float newX,newY,newZ;
  float shakeVal;
  float shakeAve;
  bool stateChangeDetected = false;
  
   //if data is available from the accelerometer
  if (myAccel.available()) {
     //retrieve and store the data
    myAccel.read();
    newX = myAccel.cx;
    newY = myAccel.cy;
    newZ = myAccel.cz;
     //calculate a shake magnitude value from a dumb summation of acceleration change
    shakeVal = fabs(newX-lastX) + fabs(newY-lastY) + fabs(newZ-lastZ);
     //store new values for next loop
    lastX = newX;
    lastY = newY;
    lastZ = newZ;
     //add shakeVal to averaging array for smoothing
    movementSamples[sampleIndex] = shakeVal;
     //increment sample index
    sampleIndex = (sampleIndex + 1)%MOVEMENT_SAMPLES;
     //average shakeVals
    for(int i = 0; i < MOVEMENT_SAMPLES ; i++){ shakeAve += movementSamples[i]; }  
    shakeAve = shakeAve/MOVEMENT_SAMPLES;
    
    #ifdef ACCEL_DEBUG
       //print out the latest sample and computed average
      Serial.print("Movement Level: ");
      Serial.print(shakeVal, 3);
      Serial.print("\t");
      Serial.print(shakeAve, 3);
      Serial.println("");
    #endif

     //store average movement level reading in the sensor reading array
    *(storedReadings + MOVEMENT_INDEX) = shakeAve;

     //check if device is moving
    bool newMoveState = (shakeAve >= MOVEMENT_THRESHOLD);
     //check if moving state has changed
    if(newMoveState != MovingState){
      stateChangeDetected = true;
       //store new movement state
      MovingState = newMoveState;
    }
    
  } //end if (no new data on accelerometer)
  return stateChangeDetected;
}

