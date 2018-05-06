
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

const int HUE_SELECT_IN = A0;
const int HALL_SENSOR_IN = A5;
const int PHOTOCELL_IN = A3;
const int FSR_IN = A0;

const int COLOR_SPEED = 1;

struct Color {
    byte red;
    byte green;
    byte blue;
};

Color led_color;


// This routine runs only once upon reset
void setup() {

  Serial.begin(9600);

  pinMode(LED1_RED_OUT, OUTPUT);
  pinMode(LED1_GREEN_OUT, OUTPUT);
  pinMode(LED1_BLUE_OUT, OUTPUT);

  pinMode(HUE_SELECT_IN, INPUT);
  pinMode(PHOTOCELL_IN, INPUT);
  pinMode(FSR_IN, INPUT);
  
  //RGB.control(true);
  led_color = {255, 0, 0};
}

void printInt(String s, int i) {
  Serial.print(s + ": ");
  Serial.println(i);
}

// This routine loops forever
void loop() {

  int brightness = readBrightness();
  
  if(readFSR()) {
    led_color = cycleColor(led_color);
  }
  Color c = led_color;

  if(readHallEffect()) {
    c = invert(c);
  } 
  setLEDColor(c, brightness);

  delay(10);
  
}

Color cycleColor(Color old) {
  // order is this:
  // R -> G -> B
  // R->G, B 0, R 255-0, G 0-255
  // G->B, R 0 G 255-0 B 0 - 255
  // B->R, G 0 B 255-0 R 0-255
  enum ColorChange {
    Off,
    RedToGreen,
    GreenToBlue,
    BlueToRed
  };

  ColorChange cc;

  // recast to accept overflow
  int r = old.red;
  int g = old.green;
  int b = old.blue;

  // find state
  if(b == 0 && g == 0) {
    cc = RedToGreen;
  } else if(r == 0 && b == 0) {
    cc = GreenToBlue;
  } else if(g== 0 && r == 0) {
    cc = BlueToRed;
  } else if(b == 0) {
    cc = RedToGreen;
  } else if(r == 0) {
    cc = GreenToBlue;
  } else if(g == 0) {
    cc = BlueToRed;
  } else {
    cc = Off;
  }

  // find which color is currently 0
  // assume never off
  // TODO this is wrong, what if on the cusp of changing
  switch(cc) {
    case RedToGreen:
      // R -> G
      // increase R, decrease G
      r -= COLOR_SPEED;
      g += COLOR_SPEED;
  
      if(r < 0 || g > 255) {
        // change to g->b
        r = 0;
        g = 255;
        
      }
      break;
    
    case GreenToBlue:
      g -= COLOR_SPEED;
      b += COLOR_SPEED;
  
      if(g < 0 || b > 255) {
        g = 0;
        b = 255;
      }
      break;
      
    case BlueToRed:
      b -= COLOR_SPEED;
      r += COLOR_SPEED;
  
      if(b < 0 || r > 255) {
        b = 0;
        r = 255;
      }
      break;
    case Off:
    default:
      r = 0;
      g = 0;
      b = 0;
  }
  
  return {r, g, b};
}

void setLEDColor(Color c, int brightness)
{
  // brightness setting: http://forum.arduino.cc/index.php?topic=272862.0
  // can also use hex RGB

  //printInt("Raw Red", red);
  //printInt("Raw Green", green);
  //printInt("Raw Blue", blue);

  byte red, green, blue;
  
  
  red = map(c.red, 0, 255, 0, brightness);
  green = map(c.green, 0, 255, 0, brightness);
  blue = map(c.blue, 0, 255, 0, brightness);

  // LEDs are common anode
  red = 255 - red;
  green = 255 - green;
  blue = 255 - blue;
    
  analogWrite(LED1_RED_OUT, red);
  analogWrite(LED1_GREEN_OUT, green);
  analogWrite(LED1_BLUE_OUT, blue);  
}


int getColorFromPot(int pin) {
  int val = analogRead(pin);
  return map(val, 0, 4092, 0, 255);
}

int readBrightness() {

  // https://learn.adafruit.com/photocells?view=all
  // pulldown = sqrt(Rmin * Rmax)
  int photoMax = 4092;
  int val = analogRead(PHOTOCELL_IN);
  printInt("Photocell", val);
  val = photoMax - val;
  return map(val, 0, photoMax, 0, 255);
  //TODO: is 1023 right value, higher in class
}


Color getLEDColor()
{
   //https://gist.github.com/hdznrrd/656996
  int i;
  float f, p, q, t;
  float h  = getColorFromPot(HUE_SELECT_IN);
  byte r, g, b;
  
  h = max(0.0, min(360.0, h));
  float s = 1;
  float v = 1;
  

  h /= 60; // sector 0 to 5
  i = floor(h);
  f = h - i; // factorial part of h
  p = v * (1 - s);
  q = v * (1 - s * f);
  t = v * (1 - s * (1 - f));
  switch(i) {
    case 0:
      r = round(255*v);
      g = round(255*t);
      b = round(255*p);
      break;
    case 1:
      r = round(255*q);
      g = round(255*v);
      b = round(255*p);
      break;
    case 2:
      r = round(255*p);
      g = round(255*v);
      b = round(255*t);
      break;
    case 3:
      r = round(255*p);
      g = round(255*q);
      b = round(255*v);
      break;
    case 4:
      r = round(255*t);
      g = round(255*p);
      b = round(255*v);
      break;
    default: // case 5:
      r = round(255*v);
      g = round(255*p);
      b = round(255*q);
    }
  return {r, g, b};
}

Color invert(Color c) {
    return {abs(255-c.red), abs(255-c.green), abs(255-c.blue)};
}

bool readHallEffect() {
    // https://playground.arduino.cc/Code/HallEffect
    int mag = analogRead(HALL_SENSOR_IN);
    bool magnet = false;
    printInt("Hall", mag);

    if(mag < 500) {
        magnet = true;
    }
    return magnet;
}

bool readFSR() {
  // https://learn.adafruit.com/force-sensitive-resistor-fsr/using-an-fsr
  int val = analogRead(FSR_IN);
  printInt("FSR", val);
  bool pressed = false;

  if(val > 1000) {
    pressed = true;
  }
  return pressed;
}

