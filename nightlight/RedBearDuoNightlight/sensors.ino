Color cycleColor(Color old) {
  // Cycle through the colors seamlessly
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
  // set the LED color

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

int readBrightness() {
  // Read brightness from photocell
  // https://learn.adafruit.com/photocells?view=all
  int photoMax = 4092;
  int val = analogRead(PHOTOCELL_IN);
  //printInt("Photocell", val);
  val = photoMax - val;
  return map(val, 0, photoMax, 0, 255);
  //TODO: is 1023 right value, higher in class
}


Color invert(Color c) {
  // Return inverse color
  return {abs(255-c.red), abs(255-c.green), abs(255-c.blue)};
}

bool readHallEffect() {
  // bool of whether magnet is present or not
  // https://playground.arduino.cc/Code/HallEffect
  int mag = analogRead(HALL_SENSOR_IN);
  bool magnet = false;
  //printInt("Hall", mag);

  if(mag < 500) {
      magnet = true;
  }
  return magnet;
}

bool readFSR() {
  // bool of whether pressure is on force sensor or not
  // https://learn.adafruit.com/force-sensitive-resistor-fsr/using-an-fsr
  int val = analogRead(FSR_IN);
  //printInt("FSR", val);
  bool pressed = false;

  if(val > 1500) {
    pressed = true;
  }
  return pressed;
}



