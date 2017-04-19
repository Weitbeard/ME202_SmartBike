/* 
 * ME202 SmartBike Embedded Program
 * LW - 1.00 - 04/18/17
 */
 
 //include wire for I2C (SDA -> A4 ; SCL -> A5)
#include <Wire.h>
 //include sparkfun library for interfacing with the accelerometer
#include <SparkFun_MMA8452Q.h>

 //define constants for readability
const int SAMPLE_COUNT = 3;
const int SCALE_FACTOR = 100;
const int LIGHT_SENSE = 6;
const int ACCEL_LED = 5;
const int LIGHT_LED = 2;

 //create object to hold accelerometer (for Sparkfun library)
MMA8452Q myAccel;
 //array to hold readings for averaging
float shakeVals[SAMPLE_COUNT];
 //array index
int valIndex = 0;

void setup() {
   //enable serial communications
  Serial.begin(9600);
   //write program log header to monitor
  Serial.println("LW - Starting Test for ME202 SmartBike");
   //initialze accelerometer
  myAccel.init();
   //intialize light sensor pin
  pinMode(LIGHT_SENSE, INPUT);
   //initialize pins for LEDs
  pinMode(ACCEL_LED, OUTPUT);
  pinMode(LIGHT_LED, OUTPUT);
}

void loop() {
   //initialize variables to store prior reading
  static float lastX = 0;
  static float lastY = 0;
  static float lastZ = 0;
  float newX,newY,newZ;
  float shakeVal;
  float shakeAve;
  
   //if there is new data ready from the accelerometer
  if (myAccel.available()) {
     //retrieve and store the data
    myAccel.read();
    newX = myAccel.cx;
    newY = myAccel.cy;
    newZ = myAccel.cz;
     //calculate a shake magnitude value from a dumb summation of acceleration change
    shakeVal = abs(newX-lastX) + abs(newY-lastY) + abs(newZ-lastZ);
     //store new values for next loop
    lastX = newX;
    lastY = newY;
    lastZ = newZ;
     //add shakeVal to averaging array for smoothing
    shakeVals[valIndex] = shakeVal;
     //increment val index
    valIndex = (valIndex + 1)%SAMPLE_COUNT;
     //average shakeVals
    for(int i = 0; i < SAMPLE_COUNT ; i++){
      shakeAve += shakeVals[i];
     //set the brightness of LED based on the scaled average:
    analogWrite(ACCEL_LED, shakeAve*(SCALE_FACTOR/SAMPLE_COUNT));
     //print out the latest sample and computed average
    Serial.print(shakeVal, 3);
    Serial.print("\t");
    Serial.print(shakeAve, 3);
    Serial.println();
    }
  }

   //if currently in a high-light setting
  if(digitalRead(LIGHT_SENSE) == HIGH){
     //turn the light-level LED off
    digitalWrite(LIGHT_LED,LOW);
  }
  else{
     //otherwise, turn the light-level LED on
    digitalWrite(LIGHT_LED,HIGH);
  }
}
