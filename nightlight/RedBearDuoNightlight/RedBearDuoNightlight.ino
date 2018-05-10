#include "ble_config.h"

/*
 * Simple Bluetooth Demo
 * This code demonstrates how the RedBear Duo board communicates with 
 * the paired Android app. The user can use the Android app to 
 * do digital read & write, analog read & write, and servo control.
 * Created by Liang He, April 27th, 2018
 * 
 * The Library is created based on Bjorn's code for RedBear BLE communication: 
 * https://github.com/bjo3rn/idd-examples/tree/master/redbearduo/examples/ble_led
 * 
 * Our code is created based on the provided example code (Simple Controls) by the RedBear Team:
 * https://github.com/RedBliearLab/Android
 */

#if defined(ARDUINO) 
SYSTEM_MODE(SEMI_AUTOMATIC); 
#endif

#define RECEIVE_MAX_LEN    4
#define SEND_MAX_LEN    4
#define BLE_SHORT_NAME_LEN 0x06 // must be in the range of [0x01, 0x09]
#define BLE_SHORT_NAME 'B','u','n','n','y'  // define each char but the number of char should be BLE_SHORT_NAME_LEN-1


/* Define the pins on the Duo board
 * TODO: change the pins here for your applications
 */
#define DIGITAL_OUT_PIN            D2
#define DIGITAL_IN_PIN             A4
#define PWM_PIN                    D3
#define SERVO_PIN                  D4
#define ANALOG_IN_PIN              A5
// RGB LED pins should be PWM
#define LED1_RED_OUT          D0
#define LED1_GREEN_OUT        D1
#define LED1_BLUE_OUT         D2

#define HUE_SELECT_IN         A0
#define HALL_SENSOR_IN        A5
#define PHOTOCELL_IN          A3
#define FSR_IN                A0

#define COLOR_SPEED 1

struct Color {
    byte red;
    byte green;
    byte blue;
};

Color led_color;

// UUID is used to find the device by other BLE-abled devices
static uint8_t service1_uuid[16]    = { 0x71,0x3d,0x00,0x00,0x50,0x3e,0x4c,0x75,0xba,0x94,0x31,0x48,0xf1,0x8d,0x94,0x1e };
static uint8_t service1_tx_uuid[16] = { 0x71,0x3d,0x00,0x03,0x50,0x3e,0x4c,0x75,0xba,0x94,0x31,0x48,0xf1,0x8d,0x94,0x1e };
static uint8_t service1_rx_uuid[16] = { 0x71,0x3d,0x00,0x02,0x50,0x3e,0x4c,0x75,0xba,0x94,0x31,0x48,0xf1,0x8d,0x94,0x1e };

// Define the receive and send handlers
static uint16_t receive_handle = 0x0000; // recieve
static uint16_t send_handle = 0x0000; // send

static uint8_t receive_data[RECEIVE_MAX_LEN] = { 0x01 };
static uint8_t send_data[SEND_MAX_LEN] = { 0x00 };

// Define the configuration data
static uint8_t adv_data[] = {
  0x02,
  BLE_GAP_AD_TYPE_FLAGS,
  BLE_GAP_ADV_FLAGS_LE_ONLY_GENERAL_DISC_MODE, 
  
  BLE_SHORT_NAME_LEN,
  BLE_GAP_AD_TYPE_SHORT_LOCAL_NAME,
  BLE_SHORT_NAME, 
  
  0x11,
  BLE_GAP_AD_TYPE_128BIT_SERVICE_UUID_COMPLETE,
  0x1e,0x94,0x8d,0xf1,0x48,0x31,0x94,0xba,0x75,0x4c,0x3e,0x50,0x00,0x00,0x3d,0x71 
};

static btstack_timer_source_t send_characteristic;

// Mark whether need to notify analog value to client.
static boolean analog_enabled = false;

// Input pin state.
static byte old_state = LOW;


/**
 * @brief Callback for writing event.
 *
 * @param[in]  value_handle  
 * @param[in]  *buffer       The buffer pointer of writting data.
 * @param[in]  size          The length of writting data.   
 *
 * @retval 
 */
int bleWriteCallback(uint16_t value_handle, uint8_t *buffer, uint16_t size) {
  Serial.print("Write value handler: ");
  Serial.println(value_handle, HEX);

  if (receive_handle == value_handle) {
    memcpy(receive_data, buffer, RECEIVE_MAX_LEN);
    Serial.print("Write value: ");
    for (uint8_t index = 0; index < RECEIVE_MAX_LEN; index++) {
      Serial.print(receive_data[index], HEX);
      Serial.print(" ");
    }
    Serial.println(" ");
    
    /* Process the data
     * TODO: Receive the data sent from other BLE-abled devices (e.g., Android app)
     * and process the data for different purposes (digital write, digital read, analog read, PWM write)
     */
    if(receive_data[0] == 0x01) {
      led_color.red = receive_data[1];
      led_color.green = receive_data[2];
      led_color.blue = receive_data[3];
    }
    /*
    if (receive_data[0] == 0x01) { // Command is to control digital out pin
    
      if (receive_data[1] == 0x01)
        digitalWrite(DIGITAL_OUT_PIN, HIGH);
      else
        digitalWrite(DIGITAL_OUT_PIN, LOW);
    }
    else if (receive_data[0] == 0xA0) { // Command is to enable analog in reading
      if (receive_data[1] == 0x01)
        analog_enabled = true;
      else
        analog_enabled = false;
    }
    else if (receive_data[0] == 0x02) { // Command is to control PWM pin
      analogWrite(PWM_PIN, receive_data[1]);
    }
    else if (receive_data[0] == 0x04) { // Command is to initialize all.
      analog_enabled = false;
      analogWrite(PWM_PIN, 0);
      digitalWrite(DIGITAL_OUT_PIN, LOW);
      old_state = LOW;
    }*/
  }
  return 0;
}

/**
 * @brief Timer task for sending status change to client.
 * @param[in]  *ts   
 * @retval None
 * 
 * TODO: Send the data from either analog read or digital read back to 
 * the other BLE-abled devices
 */
static void  send_notify(btstack_timer_source_t *ts) {
  /*if (analog_enabled) { // if analog reading enabled.
    //Serial.println("characteristic2_notify analog reading ");
    // Read and send out
    uint16_t value = analogRead(ANALOG_IN_PIN);
    send_data[0] = (0x0B);
    send_data[1] = (value >> 8);
    send_data[2] = (value);
    if (ble.attServerCanSendPacket())
      ble.sendNotify(send_handle, send_data, SEND_MAX_LEN);
  }
  // If digital in changes, report the state.
  if (digitalRead(DIGITAL_IN_PIN) != old_state) {
    Serial.println("send_notify digital reading ");
    old_state = digitalRead(DIGITAL_IN_PIN);
    if (digitalRead(DIGITAL_IN_PIN) == HIGH) {
      send_data[0] = (0x0A);
      send_data[1] = (0x01);
      send_data[2] = (0x00);
      ble.sendNotify(send_handle, send_data, SEND_MAX_LEN);
    }
    else {
      send_data[0] = (0x0A);
      send_data[1] = (0x00);
      send_data[2] = (0x00);
      ble.sendNotify(send_handle, send_data, SEND_MAX_LEN);
    }
  }*/
  byte red = led_color.red;
  byte green = led_color.green;
  byte blue = led_color.blue;
  Serial.print("sending color ");
  Serial.print(red);
  Serial.print(" ");
  Serial.print(green);
  Serial.print(" ");
  Serial.println(blue);
  send_data[0] = (0x0A);
  send_data[1] = red;
  send_data[2] = green;
  send_data[3] = blue;
  ble.sendNotify(send_handle, send_data, SEND_MAX_LEN);
  
  // Restart timer.
  ble.setTimer(ts, 200);
  ble.addTimer(ts);
}

void setup() {
  Serial.begin(9600);
  delay(5000);

  // Initialize ble_stack.
  ble.init();
  configureBLE(); //lots of standard initialization hidden in here - see ble_config.cpp
  // Set BLE advertising data
  ble.setAdvertisementData(sizeof(adv_data), adv_data);
  
  // Register BLE callback functions
  ble.onDataWriteCallback(bleWriteCallback);

  // Add user defined service and characteristics
  ble.addService(service1_uuid);
  receive_handle = ble.addCharacteristicDynamic(service1_tx_uuid, ATT_PROPERTY_NOTIFY|ATT_PROPERTY_WRITE|ATT_PROPERTY_WRITE_WITHOUT_RESPONSE, receive_data, RECEIVE_MAX_LEN);
  send_handle = ble.addCharacteristicDynamic(service1_rx_uuid, ATT_PROPERTY_NOTIFY, send_data, SEND_MAX_LEN);

  // BLE peripheral starts advertising now.
  ble.startAdvertising();
  Serial.println("BLE start advertising.");

  /*
   * TODO: This is where you can initialize all peripheral/pin modes
   */
  pinMode(LED1_RED_OUT, OUTPUT);
  pinMode(LED1_GREEN_OUT, OUTPUT);
  pinMode(LED1_BLUE_OUT, OUTPUT);

  pinMode(HUE_SELECT_IN, INPUT);
  pinMode(PHOTOCELL_IN, INPUT);
  pinMode(FSR_IN, INPUT);
  
  //RGB.control(true);
  led_color = {255, 0, 0};
   
  // Start a task to check status.
  send_characteristic.process = &send_notify;
  ble.setTimer(&send_characteristic, 500);//2000ms
  ble.addTimer(&send_characteristic);
}

void printInt(String s, int i) {
  /* Helper function to print an int on one line */
  Serial.print(s + ": ");
  Serial.println(i);
}


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
