package cynthisl.steptracker;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements SensorEventListener{

    int MAX_GRAPH_POINTS = 250;

    float PEAK_RATIO_THRESHOLD = 0.8f;
    float SAFETY = 0.5f;
    int PEAK_WINDOW = 100;
    int MOVING_AVG_SIZE = 25;

    private long timestamp = 0;
    private float[] gravity = new float[3];

    TextView tv_x, tv_y, tv_z, tv_androidCount, tv_stepCount;
    Button bReset;

    AquariumView aquariumView;

    private LineGraphSeries<DataPoint> graph_x;
    private LineGraphSeries<DataPoint> graph_y;
    private LineGraphSeries<DataPoint> graph_z;
    private LineGraphSeries<DataPoint> graph_mag;
    private PointsGraphSeries<DataPoint> graph_peaks;
    private LineGraphSeries<DataPoint> graph_threshold;

    float movingAvgVals[][] = new float[3][MOVING_AVG_SIZE];
    int movingAvgIndex = 0;
    float movingAvgSum[] = new float[3];
    boolean movingAvgInitialized = false;


    float peakVals[] = new float[PEAK_WINDOW];
    int peakValIdx = 0;
    long lastTimePeaked = 0;

    int stepsCounted = 0;
    double androidCounterBase = 0;
    double lastAndroidStepCount = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*
        ViewPager viewPager = findViewById(R.id.viewpager);
        SimpleFragmentPagerAdapter adapter = new SimpleFragmentPagerAdapter(this, getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        TabLayout tabLayout = findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);
        */

        aquariumView = findViewById(R.id.aquarium);


        // initialize textviews
        tv_x = findViewById(R.id.tv_x);
        tv_y = findViewById(R.id.tv_y);
        tv_z = findViewById(R.id.tv_z);
        tv_stepCount = findViewById(R.id.tv_stepCount);
        tv_androidCount = findViewById(R.id.tv_stepCountAndroid);

        bReset = findViewById(R.id.button_reset);
        bReset.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                reset();
            }
        });

        // initialize raw graph
        GraphView raw_graph = this.findViewById(R.id.raw_graph);
        graph_x = new LineGraphSeries<>();
        graph_x.setColor(Color.RED);
        graph_y = new LineGraphSeries<>();
        graph_y.setColor(Color.GREEN);
        graph_z = new LineGraphSeries<>();
        graph_z.setColor(Color.BLUE);
        raw_graph.addSeries(graph_x);
        raw_graph.addSeries(graph_y);
        raw_graph.addSeries(graph_z);
        raw_graph.getViewport().setXAxisBoundsManual(true);
        raw_graph.getViewport().setMinX(0);
        raw_graph.getViewport().setMaxX(MAX_GRAPH_POINTS);
        raw_graph.getViewport().setYAxisBoundsManual(true);
        raw_graph.getViewport().setMinY(-10);
        raw_graph.getViewport().setMaxY(10);

        // initialized magnitude graph
        GraphView mag_graph = this.findViewById(R.id.mag_graph);
        graph_mag = new LineGraphSeries<>();
        mag_graph.addSeries(graph_mag);
        graph_peaks = new PointsGraphSeries<>();
        graph_peaks.setSize(5);
        graph_peaks.setColor(Color.RED);
        mag_graph.addSeries(graph_peaks);
        graph_threshold = new LineGraphSeries<>();
        graph_threshold.setColor(Color.RED);
        mag_graph.addSeries(graph_threshold);
        mag_graph.getViewport().setXAxisBoundsManual(true);
        mag_graph.getViewport().setMinX(0);
        mag_graph.getViewport().setMaxX(MAX_GRAPH_POINTS);


        // set up sensors
        SensorManager sm;
        Sensor accel;
        Sensor stepCounterAndroid;
        sm = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        accel = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sm.registerListener(this, accel, SensorManager.SENSOR_DELAY_NORMAL);

        stepCounterAndroid = sm.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        sm.registerListener(this, stepCounterAndroid, SensorManager.SENSOR_DELAY_NORMAL);

        List<Sensor> deviceSensors = sm.getSensorList(Sensor.TYPE_ALL);
        for(Sensor sensor : deviceSensors) {
            Log.i("onCreate", sensor.getName() + " " + sensor.getVendor() + " " + sensor.getVersion() + " " + sensor.getMaximumRange());
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        switch(event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:

                // Gravity removal code from Android documentation
                // https://developer.android.com/guide/topics/sensors/sensors_motion.html#sensors-motion-accel

                final float alpha = 0.8f;
                float acceleration = 0;
                float x, y, z;

                // Isolate the force of gravity with the low-pass filter.
                gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
                gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
                gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

                // Remove the gravity contribution with the high-pass filter.
                x = event.values[0] - gravity[0];
                y = event.values[1] - gravity[1];
                z = event.values[2] - gravity[2];


                tv_x.setText("x: " + x);
                tv_y.setText("y: " + y);
                tv_z.setText("z: " + z);

                Log.d("accel", "x:" + x + " y:" + y + " z:" + z);

                graph_x.appendData(new DataPoint(timestamp, x), true, MAX_GRAPH_POINTS);
                graph_y.appendData(new DataPoint(timestamp, y), true, MAX_GRAPH_POINTS);
                graph_z.appendData(new DataPoint(timestamp, z), true, MAX_GRAPH_POINTS);

                float avgs[] = calcMovingAverage(new float[]{x, y, z});
                acceleration = (float) Math.sqrt(avgs[0]*avgs[0] + avgs[1]*avgs[1] + avgs[2]*avgs[2]);
                Log.d("smoothed:", "x:" + avgs[0] + " y:" + avgs[1] + " z:" + avgs[2] + " mag:" + acceleration);
                graph_mag.appendData(new DataPoint(timestamp, acceleration), true, MAX_GRAPH_POINTS);

                timestamp++;

                peakVals[peakValIdx++] = acceleration;
                if(peakValIdx == PEAK_WINDOW) {
                    Map<Long, Float> peaks = detectPeaks(peakVals, lastTimePeaked);
                    stepsCounted += peaks.size();
                    tv_stepCount.setText("Steps: " + stepsCounted);
                    for(Map.Entry<Long, Float> p : peaks.entrySet()) {
                        graph_peaks.appendData(new DataPoint(p.getKey(), p.getValue()), false, MAX_GRAPH_POINTS);
                    }

                    lastTimePeaked = timestamp;
                    peakValIdx = 0;

                    aquariumView.updateStepCount(stepsCounted);
                }

                break;

            case Sensor.TYPE_STEP_COUNTER:

                // initialization
                if(lastAndroidStepCount == -1) {
                    androidCounterBase = event.values[0];
                }

                lastAndroidStepCount = event.values[0];

                tv_androidCount.setText("Android counter: " + (lastAndroidStepCount-androidCounterBase));

                break;

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public float[] calcMovingAverage(float[] val) {

        float avg[] = new float[3];

        for(int i=0; i<3; i++) {

            // keep a running sum
            movingAvgSum[i] -= movingAvgVals[i][movingAvgIndex];
            movingAvgSum[i] += val[i];
            movingAvgVals[i][movingAvgIndex] = val[i];

            avg[i] = movingAvgSum[i]/MOVING_AVG_SIZE;

        }
        if (++movingAvgIndex == MOVING_AVG_SIZE) {
            movingAvgIndex = 0;
        }

        return avg;

    }

    public Map<Long, Float> detectPeaks(float[] values, long startTime) {

        // Mladenov’s algorithm from “A Step Counter Service for Java-Enabled Devices Using a Built-In Accelerometer.”

        Map<Long, Float> peaks = new LinkedHashMap<>();

        int peakCount = 0;
        float peakAccumulate = 0;

        // find all the peaks
        for(int i=1; i<PEAK_WINDOW-1; i++){
            float fwd = values[i+1] - values[i];
            float back = values[i] - values[i-1];

            if(fwd < 0 && back > 0) {
                peakCount++;
                peakAccumulate += values[i];
            }
        }

        float peakMean = peakAccumulate/peakCount;

        int stepCount = 0;

        // get all the peaks that are above threshold
        for(int i=1; i<PEAK_WINDOW-1; i++) {
            float fwd = values[i+1] - values[i];
            float back = values[i] - values[i-1];
            if(fwd < 0
                    && back > 0
                    && (values[i] > PEAK_RATIO_THRESHOLD * peakMean)
                    && values[i] > SAFETY) {
                stepCount++;
                peaks.put(startTime+i, values[i]);
            }

            // this really should be where the rest of the graph is drawn, but it's here for debugging
            graph_threshold.appendData(new DataPoint(startTime+i, peakMean*PEAK_RATIO_THRESHOLD), false, MAX_GRAPH_POINTS);
        }

        return peaks;

    }



    public void reset() {
        //timestamp = 0;
        androidCounterBase = lastAndroidStepCount;
        tv_androidCount.setText("Android counter: " + (lastAndroidStepCount-androidCounterBase));

        stepsCounted = 0;
        tv_stepCount.setText("Steps: " + stepsCounted);

        aquariumView.reset();
    }
}
