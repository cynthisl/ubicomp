
/* 
 * Defaultly disabled. More details: https://docs.particle.io/reference/firmware/photon/#system-thread 
 */
//SYSTEM_THREAD(ENABLED);

/*
 * Defaultly disabled. If BLE setup is enabled, when the Duo is in the Listening Mode, it will de-initialize and re-initialize the BT stack.
 * Then it broadcasts as a BLE peripheral, which enables you to set up the Duo via BLE using the RedBear Duo App or customized
 * App by following the BLE setup protocol: https://github.com/redbear/Duo/blob/master/docs/listening_mode_setup_protocol.md#ble-peripheral 
 * 
 * NOTE: If enabled and upon/after the Duo enters/leaves the Listening Mode, the BLE functionality in your application will not work properly.
 */
//BLE_SETUP(ENABLED);

/*
 * SYSTEM_MODE:
 *     - AUTOMATIC: Automatically try to connect to Wi-Fi and the Particle Cloud and handle the cloud messages.
 *     - SEMI_AUTOMATIC: Manually connect to Wi-Fi and the Particle Cloud, but automatically handle the cloud messages.
 *     - MANUAL: Manually connect to Wi-Fi and the Particle Cloud and handle the cloud messages.
 *     
 * SYSTEM_MODE(AUTOMATIC) does not need to be called, because it is the default state. 
 * However the user can invoke this method to make the mode explicit.
 * Learn more about system modes: https://docs.particle.io/reference/firmware/photon/#system-modes .
 */
#if defined(ARDUINO) 
SYSTEM_MODE(SEMI_AUTOMATIC); 
#endif

// RGB LED pins should be PWM
const int LED1_RED_OUT = D0;
const int LED1_GREEN_OUT = D1;
const int LED1_BLUE_OUT = D2;

const int TRIM_RED = A0;
const int TRIM_GREEN = A1;
const int TRIM_BLUE = A2;


// This routine runs only once upon reset
void setup() {

  Serial.begin(9600);

  pinMode(LED1_RED_OUT, OUTPUT);
  pinMode(LED1_GREEN_OUT, OUTPUT);
  pinMode(LED1_BLUE_OUT, OUTPUT);

  pinMode(TRIM_RED, INPUT);
  pinMode(TRIM_GREEN, INPUT);
  pinMode(TRIM_BLUE, INPUT);
  
  //RGB.control(true);
}

// This routine loops forever
void loop() {
  /*RGB.color(255, 0, 0);                 // set LED to RED
  delay(500);
  
  RGB.color(0, 255 ,0);                 // set LED to GREEN
  delay(500);
  
  RGB.color(0, 0,255);                 // set LED to BLUE
  delay(500);*/

  /*
  setColor(255, 0, 0, 255);  // red
  delay(1000);
  setColor(0, 255, 0, 255);  // green
  delay(1000);
  setColor(0, 0, 255, 255);  // blue
  delay(1000);
  setColor(255, 255, 0, 255);  // yellow
  delay(1000);  
  setColor(80, 0, 80, 255);  // purple
  delay(1000);
  setColor(0, 255, 255, 255);  // aqua
  delay(1000);
  */

  updateColors();
  delay(100);
  
}

void setColor(int red, int green, int blue, int brightness)
{
  // brightness setting: http://forum.arduino.cc/index.php?topic=272862.0
  // can also use hex RGB

  // LEDs are common anode
    red = 255 - red;
    green = 255 - green;
    blue = 255 - blue;

  /*
  red = map(red, 0, 255, 0, brightness);
  */
    
  analogWrite(LED1_RED_OUT, red);
  analogWrite(LED1_GREEN_OUT, green);
  analogWrite(LED1_BLUE_OUT, blue);  
}

void updateColors() {
  int red = getColorFromPot(TRIM_RED);
  int green = getColorFromPot(TRIM_GREEN);
  int blue = getColorFromPot(TRIM_BLUE);
  int brightness = 255;

  setColor(red, green, blue, brightness);
}

int getColorFromPot(int pin) {
  int val = analogRead(pin);
  return map(val, 0, 1023, 0, 255);
}

void calcPhotocell() {
  // https://learn.adafruit.com/photocells?view=all
  // pulldown = sqrt(Rmin * Rmax)
}

int readBrightness() {
  // https://www.arduino.cc/en/tutorial/potentiometer

  //int val = analogRead(potPin);
  //return map(val, 0, 1023, 0, 255);
  //TODO: is 1023 right value, higher in class
  return 0;
  
}

