package io.makeabilitylab.facetrackerble;

import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

import io.makeabilitylab.facetrackerble.ble.BLEDevice;

/**
 * Class to keep track of and send arduino data.
 */
public class ArduinoData  implements Callback{
    boolean isAlarming;
    boolean hasFace;
    float faceLocation; // face location as a percentage of X axis

    static final String CODEWORD = "pear";
    static final byte TRUE_BYTE = 0x01;
    static final byte FALSE_BYTE = 0x00;

    BLEDevice ble;
    long timeLastTextSeen;


    public ArduinoData() {

        reset();

        // timer to continusouly send data
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                sendOverBT();
            }
        }, 0, 200);
    }

    // need to set ble after init because of dependency with ocr processor
    public void setBLE(BLEDevice device) {
        ble = device;
    }

    private void reset(){
        hasFace = false;

        // reset the isAlarming only if there hasn't been a text signal for some time
        if(System.currentTimeMillis() - timeLastTextSeen > 1000) {
            isAlarming = true;
        }
    }

    /**
     * Set position of face as a float percent 0 - 1
     * @param xloc
     */
    public void setFace(float xloc) {
        hasFace = true;
        faceLocation = xloc;
    }

    /**
     * Set text that is seen. Will only work if text matches codeword.
     * @param text
     */
    @Override
    public void setText(String text) {
        // lots of false negatives
        // turning alarms off should be easy. turning them back on should require several consective false.
        // but what about if no text is detected? this won't get called to set it back to false.
        // don't aggresively reset this
        if(text.toLowerCase().contains(CODEWORD)) {
            timeLastTextSeen = System.currentTimeMillis();

            isAlarming = false;
        }
    }

    /**
     * Send the data and reset class
     */
    public void sendOverBT() {

        if(ble == null) {
            Log.i("Arduino", "BLE not set");
            return;
        }

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
