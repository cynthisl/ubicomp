package cynthisl.steptracker;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import java.util.Vector;

/**
 * TODO: document your custom view class.
 */
public class AquariumView extends View {

    private Paint paint;
    int canvasWidth;
    int canvasHeight;
    long stepCount;
    long lastFishUpdate;

    Vector<Fish> fishes;

    class Fish {
        int left, top, length, height, color;
        boolean swimLeft;
        public Fish(int l, int t, int len, int h, int c, boolean swimDir) {
            left = l;
            top = t;
            length = len;
            height = h;
            color = c;
            swimLeft = swimDir;
        }
    }

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
        stepCount = 0;
        lastFishUpdate = 0;
        fishes = new Vector<>();

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        canvasWidth = w;
        canvasHeight = h;
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for(Fish f : fishes) {
            drawFish(f, canvas);
        }

        paint.setColor(Color.BLACK);
        paint.setTextSize(20);
        canvas.drawText("Steps: " + stepCount, 20, 30, paint);
    }

    public void addRandomFish() {

        int left = (int)(Math.random() * canvasWidth);
        int top = (int)(Math.random() * canvasHeight);
        int len = (int)(Math.random() * 250 + 20);
        int height = (int)(Math.random() * 100 + 10);
        int col = Color.argb(255, (int)(Math.random()*256), (int)(Math.random()*256), (int)(Math.random()*256));
        boolean dir = (Math.random() < 0.5);

        Fish f = new Fish(left, top, len, height, col, dir);
        fishes.add(f);
    }

    public void drawFish(Fish f, Canvas canvas) {

        int left = f.left;
        int top = f.top;
        int len = f.length;
        int height = f.height;
        int color = f.color;


        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);

        if(f.swimLeft) {
            Path tail = new Path();
            tail.setFillType(Path.FillType.EVEN_ODD);
            tail.moveTo((int) (left + len * .75), (int) (top + height * .5));
            tail.lineTo((int) (left + len), (int) (top));
            tail.lineTo(left + len, top + height);
            tail.lineTo((int) (left + len * .75), (int) (top + height * .5));
            tail.close();

            canvas.drawOval(left, top, (int) (left + len * .8), top + height, paint);
            canvas.drawPath(tail, paint);

            paint.setColor(Color.BLACK);
            canvas.drawCircle((int) (left + len * .1), (int) (top + height * .5), 5, paint);
        } else {
            Path tail = new Path();
            tail.setFillType(Path.FillType.EVEN_ODD);
            tail.moveTo((int) (left + len * .25), (int) (top + height * .5));
            tail.lineTo(left, top);
            tail.lineTo(left, top + height);
            tail.lineTo((int) (left + len * .25), (int) (top + height * .5));
            tail.close();

            canvas.drawOval((int)(left +len*.2), top, (left + len), top + height, paint);
            canvas.drawPath(tail, paint);

            paint.setColor(Color.BLACK);
            canvas.drawCircle((int) (left + len * .9), (int) (top + height * .5), 5, paint);

        }

    }

    public void updateStepCount(long steps) {

        stepCount = steps;

        if(lastFishUpdate + 10 <= stepCount) {
            addRandomFish();
            lastFishUpdate = stepCount;
        }

        invalidate();
    }
}
