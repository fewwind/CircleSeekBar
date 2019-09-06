package seek.circlle.fewwind.text;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.SweepGradient;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import static android.util.TypedValue.COMPLEX_UNIT_DIP;

public class CircleView extends View {
    Paint mPaint;
    float circleWidth;
    int bgColor;
    int progressColor;
    int arc = 30;
    float offsetAgree;
    boolean contain;

    public CircleView(Context context) {
        this(context, null);
    }

    public CircleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circleWidth = TypedValue.applyDimension(COMPLEX_UNIT_DIP, 20, context.getResources().getDisplayMetrics());
        mPaint.setStrokeWidth(circleWidth);
        mPaint.setStyle(Paint.Style.STROKE);
        bgColor = Color.parseColor("#DCF7F3");
        progressColor = Color.parseColor("#35D1C9");
    }

    SweepGradient mSweepGradient;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mPaint.setColor(bgColor);
        mPaint.setShader(null);
        canvas.drawCircle(getWidth() / 2, getMeasuredHeight() / 2, getMeasuredHeight() / 2 - circleWidth / 2, mPaint);
        mPaint.setColor(progressColor);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setShader(mSweepGradient);
        canvas.drawArc(circleWidth / 2, circleWidth / 2, getMeasuredWidth() - circleWidth / 2, getMeasuredHeight() - circleWidth / 2, 270 + offsetAgree, arc, false, mPaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Log.w("Fewwind", getMeasuredHeight() + "<>");
        int width = Math.min(getMeasuredWidth(), getMeasuredHeight());
        setMeasuredDimension(width, width);
        mSweepGradient = new SweepGradient(getMeasuredHeight() / 2,
                getMeasuredHeight() / 2, //以圆弧中心作为扫描渲染的中心以便实现需要的效果
                new int[]{Color.parseColor("#35D1C9"), Color.parseColor("#e6004a")}, null);
        Matrix matrix = new Matrix();
        matrix.setRotate(-90, getMeasuredHeight() / 2, getMeasuredHeight() / 2);
        mSweepGradient.setLocalMatrix(matrix);
        offsetAgree = (float) (90 - Math.atan2(getMeasuredHeight() / 2 - circleWidth / 2, circleWidth / 2) / (Math.PI / 180));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        getParent().requestDisallowInterceptTouchEvent(true);
        int x = (int) event.getX();
        int y = (int) event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                contain = isContain(x, y);
                if (contain) setProgress(getArc(x, y));
                break;
            case MotionEvent.ACTION_MOVE:
                if (contain) setProgress(getArc(x, y));
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        return true;
    }

    boolean isContain(int x, int y) {
        int rudioBig = getMeasuredHeight() / 2;
        float rudioSamll = rudioBig - circleWidth;
        int rudio = (int) Math.sqrt(Math.pow(Math.abs(x - rudioBig), 2) + Math.pow(Math.abs(y - rudioBig), 2));
        return rudio >= rudioSamll && rudio <= rudioBig;
    }

    float thumbX;
    float thumbY;

    double getArc(int x, int y) {
        int radius = getMeasuredHeight() / 2;
        double radX = Math.abs(x - radius);
        double radY = Math.abs(y - radius);
        double arc = Math.atan2(radY, radX) / (Math.PI / 180);
        thumbX = (float) ((radius - circleWidth / 2) * Math.cos(arc));
        thumbY = (float) ((radius - circleWidth / 2) * Math.sin(arc));
        if (x >= radius && y <= radius) {
            thumbX += radius;
            thumbY = radius - thumbY;
            arc = 90 - arc;
        } else if (x >= radius && y > radius) {
            arc = 90 + arc;
            thumbX += radius;
            thumbY += radius;
        } else if (x <= radius && y <= radius) {
            arc = 270 + arc;
            thumbX = radius - thumbX;
            thumbY += radius;
        } else if (x <= radius && y > radius) {
            arc = 270 - arc;
            thumbX = radius - thumbX;
            thumbY = radius - thumbY;
        }
        return arc;
    }


    public void setProgress(float progress) {
        arc = (int) (progress * 360);
        invalidate();
    }

    public void setProgress(double angle) {
        arc = (int) angle;
        arc = arc + (30 - arc % 30);
        arc = Math.max(arc, 30);
        arc = (int) Math.min(arc, 360 - offsetAgree * 2);
        invalidate();
    }
}
