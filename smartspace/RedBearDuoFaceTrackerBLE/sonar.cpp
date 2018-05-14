#include "Arduino.h"
#include "sonar.h"

Sonar::Sonar() {
}
void Sonar::setUp(int trig, int echo) {

  trig_pin = trig;
  echo_pin = echo;
  pinMode(trig_pin, OUTPUT);
  digitalWrite(trig_pin, LOW);
}

void Sonar::takeReading() {
  unsigned long t1;
  unsigned long t2;
  unsigned long pulse_width;
  
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
  pulse_width = t2 - t1;

  last_reading = pulse_width;
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

