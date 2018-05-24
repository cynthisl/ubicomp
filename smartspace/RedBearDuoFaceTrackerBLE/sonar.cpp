#include "Arduino.h"
#include "sonar.h"
#include "QuickStats.h"

Sonar::Sonar() {
}
void Sonar::setUp(int trig, int echo) {

  trig_pin = trig;
  echo_pin = echo;
  pinMode(trig_pin, OUTPUT);
  digitalWrite(trig_pin, LOW);

  current_smoothing_idx = 0;
}

/**
 * Take a sonar reading, smooth it, and save it
 **/
void Sonar::takeReading() {
  unsigned long t1;
  unsigned long t2;
  
  // Hold the trigger pin high for at least 10 us
  digitalWrite(trig_pin, HIGH);
  delayMicroseconds(10);
  digitalWrite(trig_pin, LOW);

  // wait for echo
  while(digitalRead(echo_pin) == 0);
  
  // Measure how long the echo pin was held high (pulse width)
  // Note: the micros() counter will overflow after ~70 min
  t1 = micros();
  while ( digitalRead(echo_pin) == 1);
  t2 = micros();
  last_reading = t2 - t1;
  
  last_reading = smooth(last_reading);

}

float Sonar::getCMDistance() {
  return (last_reading / 58.0);
}

float Sonar::getInchDistance() {
  return (last_reading / 148.0);
}


bool Sonar::isTooClose() {
  return (last_reading < (50*58));
}

bool Sonar::isInRange() {
  return (last_reading < MAX_SONAR_DIST);

}

void Sonar::printLastReading() {
  
  if ( !isInRange() ) {
    Serial.println("Out of range");
  } else {
    Serial.print(getCMDistance());
    Serial.print(" cm \t");
    Serial.print(getInchDistance());
    Serial.println(" in");
  }
}


// smoothing median filter
// https://forums.adafruit.com/viewtopic.php?f=25&t=14391
float Sonar::smooth(float val) {
  smoothing_vals[current_smoothing_idx++] = val;
  if(current_smoothing_idx >= SMOOTHING_WINDOW_SIZE) {
    current_smoothing_idx = 0;
  }

  // Using QuickStats to get median
  // https://playground.arduino.cc/Main/QuickStats
  QuickStats qs;
  float med = qs.median(smoothing_vals, SMOOTHING_WINDOW_SIZE);
  
  return med;
  
  
}

