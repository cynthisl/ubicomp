package io.makeabilitylab.facetrackerble;

import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

import io.makeabilitylab.facetrackerble.ble.BLEDevice;

public class ArduinoData {
    boolean isAlarming;
    boolean hasFace;
    float faceLocation; // face location as a percentage of X axis
    String CODEWORD = "lush";
    byte TRUE_BYTE = 0x01;
    byte FALSE_BYTE = 0x00;

    BLEDevice ble;


    public ArduinoData(BLEDevice ble_device) {

        ble = ble_device;

        reset();

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                sendOverBT();
            }
        }, 0, 200);
    }

    private void reset(){
        isAlarming = true;
        hasFace = false;
    }

    public void setFace(float xloc) {
        hasFace = true;
        faceLocation = xloc;
    }

    public void setText(String text) {
        if(text.contains(CODEWORD)) {
            isAlarming = false;
        }
    }

    public void sendOverBT() {

        if(ble.getState() != BLEDevice.State.CONNECTED) {
            return;
        }

        // TODO: So this works, but full range will never be reached
        // (ie, difficult to have center at x=0 because not enough of face is visible to track)
        // Might want to scale this further to get full range
        byte faceCenterXBytes = (byte)(faceLocation * 255);
        Log.i("cam", String.format("x: %02x", faceCenterXBytes));


        byte[] buf = new byte[] { (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00}; // 5-byte initialization


        buf[0] = 0x01;
        buf[1] = hasFace ? TRUE_BYTE : FALSE_BYTE;
        buf[2] = faceCenterXBytes;
        buf[3] = isAlarming ? TRUE_BYTE : FALSE_BYTE;

        Log.i("BLESend", String.format("Sending %s", bytesToHex(buf)));

        // Send the data!
        ble.sendData(buf);

        reset();
    }


    final private static char[] hexArray = { '0', '1', '2', '3', '4', '5', '6',
            '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

    // Convert an array of bytes into Hex format string
    // From sample code
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        int v;
        for (int j = 0; j < bytes.length; j++) {
            v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}
