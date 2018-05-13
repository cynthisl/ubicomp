// Code to read from the Ultrasonic Range Finder
// Based off example code from RedBearDuoUltrasonicRangeFinder.ino

unsigned long takeSonarReading() {
  
  unsigned long t1;
  unsigned long t2;
  unsigned long pulse_width;
  
  // Hold the trigger pin high for at least 10 us
  digitalWrite(SONAR_TRIG_OUT_PIN, HIGH);
  delayMicroseconds(10);
  digitalWrite(SONAR_TRIG_OUT_PIN, LOW);

  // wait for echo
  while(digitalRead(SONAR_ECHO_IN_PIN) == 0);
  
  // Measure how long the echo pin was held high (pulse width)
  // Note: the micros() counter will overflow after ~70 min
  t1 = micros();
  while ( digitalRead(SONAR_ECHO_IN_PIN) == 1);
  t2 = micros();
  pulse_width = t2 - t1;

  return pulse_width;

}

float sonarToCM(unsigned long reading) {
  return (reading / 58.0);
}

float sonarToInches(unsigned long reading) {
  return (reading / 148.0);
}

