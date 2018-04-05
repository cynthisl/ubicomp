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
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.List;

public class MainActivity extends AppCompatActivity implements SensorEventListener{

    private SensorManager _sm;
    private Sensor _accel;
    private Sensor _stepCounter;

    private float _x, _y, _z;
    private long timestamp = 0;
    private float[] gravity = new float[3];
    private float _acceleration = 0;

    TextView _tv_x, _tv_y, _tv_z, _tv_stepCount;

    int MAX_GRAPH_POINTS = 500;



    private LineGraphSeries<DataPoint> _graph_x;
    private LineGraphSeries<DataPoint> _graph_y;
    private LineGraphSeries<DataPoint> _graph_z;
    private LineGraphSeries<DataPoint> _graph_mag;

    int avgSize = 10;
    float movingAvgVals[] = new float[avgSize];
    int movingAvgIndex = 0;
    float movingAvgSum = 0;

    double androidCounterBase = 0;
    double lastAndroidStepCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        _tv_x = findViewById(R.id.tv_x);
        _tv_y = findViewById(R.id.tv_y);
        _tv_z = findViewById(R.id.tv_z);
        _tv_stepCount = findViewById(R.id.tv_stepCount);

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
        raw_graph.getLegendRenderer().setVisible(true);
        
        GraphView mag_graph = this.findViewById(R.id.mag_graph);
        _graph_mag = new LineGraphSeries<>();
        mag_graph.addSeries(_graph_mag);
        mag_graph.getViewport().setXAxisBoundsManual(true);
        mag_graph.getViewport().setMinX(0);
        mag_graph.getViewport().setMaxX(MAX_GRAPH_POINTS);
        raw_graph.getViewport().setYAxisBoundsManual(true);
        raw_graph.getViewport().setMinY(-10);
        raw_graph.getViewport().setMaxY(10);



        _sm = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        _accel = _sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        _sm.registerListener(this, _accel, SensorManager.SENSOR_DELAY_GAME);

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

                _graph_x.appendData(new DataPoint(timestamp, _x), true, MAX_GRAPH_POINTS);
                _graph_y.appendData(new DataPoint(timestamp, _y), true, MAX_GRAPH_POINTS);
                _graph_z.appendData(new DataPoint(timestamp, _z), true, MAX_GRAPH_POINTS);

                _acceleration = calcMovingAverage((float) Math.sqrt(_x*_x + _y*_y + _z*_z));
                _graph_mag.appendData(new DataPoint(timestamp, _acceleration), true, MAX_GRAPH_POINTS);

                timestamp++;
                break;

            case Sensor.TYPE_STEP_COUNTER:

                lastAndroidStepCount = event.values[0];

                _tv_stepCount.setText("Step counter: " + (lastAndroidStepCount-androidCounterBase));

                break;

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public float calcMovingAverage(float val) {

        // keep a running sum
        movingAvgSum -= movingAvgVals[movingAvgIndex];
        movingAvgSum += val;
        movingAvgVals[movingAvgIndex] = val;

        if(++movingAvgIndex == avgSize) {
            movingAvgIndex = 0;
        }

        return movingAvgSum/avgSize;

    }

    public void reset() {
        timestamp = 0;
        androidCounterBase = lastAndroidStepCount;
    }
}
