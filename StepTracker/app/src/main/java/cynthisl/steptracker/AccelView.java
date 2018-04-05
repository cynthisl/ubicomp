package cynthisl.steptracker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class AccelView extends View implements SensorEventListener{

    private SensorManager _sm;
    private Sensor _accel;
    private Paint _paintText = new Paint();
    private String pString = "";
    TextView _tvDebug;

    public AccelView(Context context) {
        super(context);
        init(context, null, 0, 0);
    }

    public AccelView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0, 0);
    }

    public AccelView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, 0);
    }

    public AccelView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
        _tvDebug = this.findViewById((R.id.tv_debug));
    }

    private void init(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {

        Log.e("init", "ERROR init()");
        Log.w("init", "WARN init()");
        Log.i("init", "INFO init()");
        Log.d("init", "DEBUG init()");
        Log.v("init", "VERBOSE init()");

        _paintText = new Paint();
        pString = "";

        _paintText.setColor(Color.BLACK);
        _sm = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
        _accel = _sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        _sm.registerListener(this, _accel, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.drawText(pString, 100, 100, _paintText);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch(event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                float x[] = event.values;
                pString = "x:" + x[0] + " y:" + x[1] + " z:" + x[2];
                Log.d("accel", pString);

                invalidate();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
