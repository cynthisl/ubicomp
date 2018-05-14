#ifndef SONAR_H
#define SONAR_H

#include "Arduino.h"



// Anything over 400 cm (23200 us pulse) is "out of range"
#define MAX_SONAR_DIST 23200

class Sonar {
  public:
    Sonar();
    void setUp(int trig, int echo);
    void takeReading();
    float getCMDistance();
    float getInchDistance();
    bool isTooClose();
    bool isInRange();
    void printLastReading();
    
    
  private:
    int trig_pin;
    int echo_pin;
    unsigned long last_reading;

  
};


#endif
