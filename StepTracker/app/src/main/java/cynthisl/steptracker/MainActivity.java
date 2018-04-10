package cynthisl.steptracker;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements SensorEventListener{

    int MAX_GRAPH_POINTS = 250;

    float PEAK_RATIO_THRESHOLD = 0.8f;
    float SAFETY = 0.5f;
    int PEAK_WINDOW = 75;
    int MOVING_AVG_SIZE = 15;

    private SensorManager _sm;
    private Sensor _accel;
    private Sensor _stepCounter;

    private float _x, _y, _z;
    private long timestamp = 0;
    private float[] gravity = new float[3];
    private float _acceleration = 0;

    TextView _tv_x, _tv_y, _tv_z, _tv_androidCount, _tv_stepCount;
    Button _bReset;

    AquariumView aquariumView;

    private LineGraphSeries<DataPoint> _graph_x;
    private LineGraphSeries<DataPoint> _graph_y;
    private LineGraphSeries<DataPoint> _graph_z;
    private LineGraphSeries<DataPoint> _graph_mag;
    private PointsGraphSeries<DataPoint> _graph_peaks;

    float movingAvgVals[][] = new float[3][MOVING_AVG_SIZE];
    int movingAvgIndex = 0;
    float movingAvgSum[] = new float[3];
    boolean movingAvgInitialized = false;


    float peakVals[] = new float[PEAK_WINDOW];
    int peakValIdx = 0;
    long lastTimePeaked = 0;

    int stepsCounted = 0;
    double androidCounterBase = 0;
    double lastAndroidStepCount;

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


        _tv_x = findViewById(R.id.tv_x);
        _tv_y = findViewById(R.id.tv_y);
        _tv_z = findViewById(R.id.tv_z);
        _tv_stepCount = findViewById(R.id.tv_stepCount);
        _tv_androidCount = findViewById(R.id.tv_stepCountAndroid);

        _bReset = findViewById(R.id.button_reset);
        _bReset.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                reset();
            }
        });

        GraphView raw_graph = this.findViewById(R.id.raw_graph);
        _graph_x = new LineGraphSeries<>();
        _graph_x.setColor(Color.RED);
        _graph_y = new LineGraphSeries<>();
        _graph_y.setColor(Color.GREEN);
        _graph_z = new LineGraphSeries<>();
        _graph_z.setColor(Color.BLUE);
        raw_graph.addSeries(_graph_x);
        raw_graph.addSeries(_graph_y);
        raw_graph.addSeries(_graph_z);
        raw_graph.getViewport().setXAxisBoundsManual(true);
        raw_graph.getViewport().setMinX(0);
        raw_graph.getViewport().setMaxX(MAX_GRAPH_POINTS);
        raw_graph.getViewport().setYAxisBoundsManual(true);
        raw_graph.getViewport().setMinY(-10);
        raw_graph.getViewport().setMaxY(10);

        GraphView mag_graph = this.findViewById(R.id.mag_graph);
        _graph_mag = new LineGraphSeries<>();
        mag_graph.addSeries(_graph_mag);
        _graph_peaks = new PointsGraphSeries<>();
        _graph_peaks.setSize(5);
        _graph_peaks.setColor(Color.RED);
        mag_graph.addSeries(_graph_peaks);
        mag_graph.getViewport().setXAxisBoundsManual(true);
        mag_graph.getViewport().setMinX(0);
        mag_graph.getViewport().setMaxX(MAX_GRAPH_POINTS);
        //mag_graph.getViewport().setYAxisBoundsManual(true);
        //mag_graph.getViewport().setMinY(0);
       // mag_graph.getViewport().setMaxY(7);




        _sm = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        _accel = _sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        _sm.registerListener(this, _accel, SensorManager.SENSOR_DELAY_NORMAL);

        _stepCounter = _sm.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        _sm.registerListener(this, _stepCounter, SensorManager.SENSOR_DELAY_NORMAL);

        List<Sensor> deviceSensors = _sm.getSensorList(Sensor.TYPE_ALL);
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

                // Isolate the force of gravity with the low-pass filter.
                gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
                gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
                gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

                // Remove the gravity contribution with the high-pass filter.
                _x = event.values[0] - gravity[0];
                _y = event.values[1] - gravity[1];
                _z = event.values[2] - gravity[2];


                _tv_x.setText("x: " + _x);
                _tv_y.setText("y: " + _y);
                _tv_z.setText("z: " + _z);

                Log.d("accel", "x:" + _x + " y:" + _y + " z:" + _z);

                _graph_x.appendData(new DataPoint(timestamp, _x), true, MAX_GRAPH_POINTS);
                _graph_y.appendData(new DataPoint(timestamp, _y), true, MAX_GRAPH_POINTS);
                _graph_z.appendData(new DataPoint(timestamp, _z), true, MAX_GRAPH_POINTS);

                float avgs[] = calcMovingAverage(new float[]{_x, _y, _z});
                _acceleration = (float) Math.sqrt(avgs[0]*avgs[0] + avgs[1]*avgs[1] + avgs[2]*avgs[2]);
                Log.d("smoothed:", "x:" + avgs[0] + " y:" + avgs[1] + " z:" + avgs[2] + " mag:" + _acceleration);
                _graph_mag.appendData(new DataPoint(timestamp, _acceleration), true, MAX_GRAPH_POINTS);

                timestamp++;

                peakVals[peakValIdx++] = _acceleration;
                if(peakValIdx == PEAK_WINDOW) {
                    Map<Long, Float> peaks = detectPeaks(peakVals, lastTimePeaked);
                    stepsCounted += peaks.size();
                    _tv_stepCount.setText("Steps: " + stepsCounted);
                    for(Map.Entry<Long, Float> p : peaks.entrySet()) {
                        _graph_peaks.appendData(new DataPoint(p.getKey(), p.getValue()), false, MAX_GRAPH_POINTS);
                    }

                    lastTimePeaked = timestamp;
                    peakValIdx = 0;

                    aquariumView.updateStepCount(stepsCounted);
                }

                break;

            case Sensor.TYPE_STEP_COUNTER:

                lastAndroidStepCount = event.values[0];

                _tv_androidCount.setText("Android counter: " + (lastAndroidStepCount-androidCounterBase));

                break;

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public float[] calcMovingAverage(float[] val) {

        /*if(!movingAvgInitialized) {
            for(int i=0; i<3; i++) {
                Arrays.fill(movingAvgVals[i], val[i]);
            }
            movingAvgInitialized = true;
        }*/

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

        Map<Long, Float> peaks = new LinkedHashMap<>();

        int peakCount = 0;
        float peakAccumulate = 0;

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
        }

        return peaks;

    }



    public void reset() {
        //timestamp = 0;
        androidCounterBase = lastAndroidStepCount;
        _tv_androidCount.setText("Android counter: " + (lastAndroidStepCount-androidCounterBase));

        stepsCounted = 0;
        _tv_stepCount.setText("Steps: " + stepsCounted);
    }
}
