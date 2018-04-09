package cynthisl.steptracker;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

/**
 * TODO: document your custom view class.
 */
public class AquariumView extends View {

    private Paint paint;

    public AquariumView(Context context) {
        super(context);
        init(null, 0);
    }

    public AquariumView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public AquariumView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawFish(canvas);
    }

    private void drawFish(Canvas canvas) {

        paint.setColor(Color.YELLOW);

        canvas.drawCircle(50, 50, 50, paint);
    }
}
