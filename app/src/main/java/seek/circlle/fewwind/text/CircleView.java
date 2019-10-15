package seek.circlle.fewwind.text;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.SweepGradient;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import static android.util.TypedValue.COMPLEX_UNIT_DIP;

public class CircleView extends View {
    Paint mPaint;
    float circleWidth;
    int bgColor;
    int progressColor;
    float arc = 180;
    float offsetAgree;
    boolean contain;
    Path mPath = new Path();
    Paint pathPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    ValueAnimator valueAnimator;

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

        pathPaint.setStyle(Paint.Style.STROKE);
        pathPaint.setColor(Color.BLACK);
        pathPaint.setStrokeWidth(TypedValue.applyDimension(COMPLEX_UNIT_DIP, 4, context.getResources().getDisplayMetrics()));
        valueAnimator = ValueAnimator.ofFloat(0, 1f).setDuration(1600);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                mPath.reset();
                mPath.moveTo(0, getMeasuredHeight() / 2);
                mPath.quadTo(getMeasuredHeight() / 4, value * getMeasuredHeight(), getMeasuredHeight() / 2, getMeasuredHeight() / 2);
                mPath.quadTo(getMeasuredHeight() * 3 / 4, getMeasuredHeight() - value * getMeasuredHeight(), getMeasuredHeight(), getMeasuredHeight() / 2);
                invalidate();
            }
        });
        setProgress(150d);
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

        canvas.drawPath(mPath, pathPaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = Math.min(getMeasuredWidth(), getMeasuredHeight());
        setMeasuredDimension(width, width);
        mSweepGradient = new SweepGradient(getMeasuredHeight() / 2,
                getMeasuredHeight() / 2, //以圆弧中心作为扫描渲染的中心以便实现需要的效果
                new int[]{Color.parseColor("#35D1C9"), Color.parseColor("#e6004a")}, null);
        Matrix matrix = new Matrix();
        matrix.setRotate(-90, getMeasuredHeight() / 2, getMeasuredHeight() / 2);
        mSweepGradient.setLocalMatrix(matrix);
        offsetAgree = (float) (90 - Math.atan2(getMeasuredHeight() / 2 - circleWidth / 2, circleWidth / 2) / (Math.PI / 180));
        valueAnimator.start();
        valueAnimator.setRepeatCount(10);
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
        int radiusBig = getMeasuredHeight() / 2;
        float radiusSamll = radiusBig - circleWidth;
        int radius = (int) Math.sqrt(Math.pow(Math.abs(x - radiusBig), 2) + Math.pow(Math.abs(y - radiusBig), 2));
        return radius >= radiusSamll && radius <= radiusBig;
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
        float v = arc % 30;
        if (v != 0) arc = arc + (30 - v);
        arc = Math.max(arc, 30);
        arc = arc - offsetAgree * 2;
//        arc = (int) Math.min(arc, 360 - offsetAgree * 2);
        invalidate();
    }
}
