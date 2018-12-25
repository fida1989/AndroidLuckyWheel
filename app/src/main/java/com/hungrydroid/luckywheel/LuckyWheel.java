package com.hungrydroid.luckywheel;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class LuckyWheel extends SurfaceView implements SurfaceHolder.Callback,
        Runnable {

    private SurfaceHolder mHolder;
    private Canvas mCanvas;

    private Thread t;

    private boolean isRunning;

    private String[] mStrs = new String[]{"0", "5", "25", "50", "100",
            "250", "500", "2500", "5000"};

    private int[] mImgs = new int[]{R.mipmap.ic_launcher, R.mipmap.ic_launcher,
            R.mipmap.ic_launcher, R.mipmap.ic_launcher, R.mipmap.ic_launcher,
            R.mipmap.ic_launcher};

    private Bitmap[] mImgsBitmap;

    private Bitmap mBgBitmap = BitmapFactory.decodeResource(getResources(),
            R.drawable.bg2);
    private int[] mColors = new int[] { 0xffdd0244, 0xff6287ec, 0xffc1bdb6,
            0xfff6cfca, 0xffc4b583, 0xff4a570c,0xfff47b67, 0xff9f6408, 0xff49a4aa };
    /*	private int[] mColors = new int[] { 0xffffc300, 0xfff17e01, 0xffffc300,
                0xfff17e01, 0xffffc300, 0xfff17e01,0xffffc300, 0xfff17e01, 0xffffc300 };*/
    /*private int[] mColors = new int[] { 0xffff0000, 0xff0fa300, 0xff003cff,
            0xffff0000, 0xff0fa300, 0xff003cff,0xffff0000, 0xff0fa300, 0xff003cff };*/
    /*private int[] mColors = new int[]{android.R.color.holo_red_dark, android.R.color.holo_blue_dark, android.R.color.holo_green_dark,
            android.R.color.holo_red_dark, android.R.color.holo_blue_dark, android.R.color.holo_green_dark, android.R.color.holo_red_dark, android.R.color.holo_blue_dark, android.R.color.holo_green_dark};*/

    private Paint mArcPaint;

    private Paint mTextPaint;

    private float mTextSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP, 20, getResources().getDisplayMetrics());

    private int mItemCount = 9;
    //private int mItemCount = mStrs.length;

    private RectF mRange = new RectF();

    private int mRadius;

    private int mCenter;

    private int mPadding;

    private double mSpeed;

    private volatile float mStartAngle = 0;

    private boolean isShouldEnd;

    public LuckyWheel(Context context, AttributeSet attrs) {
        super(context, attrs);
        mHolder = getHolder();
        mHolder.addCallback(this);
        setFocusable(true);
        setFocusableInTouchMode(true);
        setKeepScreenOn(true);
    }

    public LuckyWheel(Context context) {
        this(context, null);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = Math.min(getMeasuredHeight(), getMeasuredWidth());
        mPadding = getPaddingLeft();

        mRadius = (width - mPadding * 2) / 2;

        mCenter = width / 2;

        setMeasuredDimension(width, width);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        mArcPaint = new Paint();
        mArcPaint.setAntiAlias(true);
        mArcPaint.setDither(true);

        mTextPaint = new Paint();
        mTextPaint.setColor(0xffffffff);
        mTextPaint.setTextSize(mTextSize);

        mRange = new RectF(mPadding, mPadding, mPadding + mRadius * 2, mPadding
                + mRadius * 2);

        mImgsBitmap = new Bitmap[mItemCount];
		/*for (int i = 0; i < mItemCount; i++) {
			mImgsBitmap[i] = BitmapFactory.decodeResource(getResources(),
					mImgs[i]);
		}*/
        for (int i = 0; i < mItemCount; i++) {
            mImgsBitmap[i] = BitmapFactory.decodeResource(getResources(),
                    R.mipmap.ic_launcher_round);
        }

        isRunning = true;
        t = new Thread(this);
        t.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        // TODO Auto-generated method stub
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        isRunning = false;
    }

    @Override
    public void run() {

        while (isRunning) {
            long start = System.currentTimeMillis();
            draw();
            long end = System.currentTimeMillis();
            if (end - start < 50) {
                try {
                    Thread.sleep(50 - (end - start));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void draw() {
        try {
            mCanvas = mHolder.lockCanvas();
            if (mCanvas != null) {

                drawBackground();

                float tmpAngle = mStartAngle;
                float sweepAngle = 360 / mItemCount;
                for (int i = 0; i < mItemCount; i++) {
                    mArcPaint.setColor(mColors[i]);

                    mCanvas.drawArc(mRange, tmpAngle, sweepAngle, true,
                            mArcPaint);

                    drawText(tmpAngle, sweepAngle, mStrs[i]);

                    //drawIcon(tmpAngle, mImgsBitmap[i]);

                    tmpAngle += sweepAngle;
                }
                mStartAngle += mSpeed;
                if (isShouldEnd) {
                    mSpeed -= 1;
                }
                if (mSpeed <= 0) {
                    mSpeed = 0;
                    isShouldEnd = false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (mCanvas != null) {
                mHolder.unlockCanvasAndPost(mCanvas);
            }
        }
    }


    private void drawIcon(float tmpAngle, Bitmap bitmap) {

        int imgWidth = mRadius / 4;
        float angle = (float) ((tmpAngle + 360 / mItemCount / 2) * Math.PI / 180);
        int x = (int) (mCenter + mRadius / 2 * Math.cos(angle));
        int y = (int) (mCenter + mRadius / 2 * Math.sin(angle));

        Rect rect = new Rect(x - imgWidth / 2, y - imgWidth / 2, x + imgWidth
                / 2, y + imgWidth / 2);
        mCanvas.drawBitmap(bitmap, null, rect, null);
    }


    private void drawText(float tmpAngle, float sweepAngle, String string) {
        Path path = new Path();
        path.addArc(mRange, tmpAngle, sweepAngle);

        float textWidth = mTextPaint.measureText(string);
        int hOffset = (int) (mRadius * Math.PI / mItemCount - textWidth / 2);

        int vOffset = mRadius / 5;
        //int vOffset = mRadius / mItemCount;
        mCanvas.drawTextOnPath(string, path, hOffset, vOffset, mTextPaint);
    }


    private void drawBackground() {
        //mCanvas.drawColor(0xffffffff);
        mCanvas.drawColor(0xff6c6c62);
        mCanvas.drawBitmap(mBgBitmap, null, new Rect(mPadding / 2,
                mPadding / 2, getMeasuredWidth() - mPadding / 2,
                getMeasuredHeight() - mPadding / 2), null);
    }


    public void luckyStart(int index) {
        if (index >= 0 && index < mItemCount) {

            float angle = 360 / mItemCount;

            float from = 270 - (index + 1) * angle;
            float end = from + angle;

            float targetFrom = 5 * 360 + from;
            float targetEnd = 5 * 360 + end;

            float v1 = (float) ((-1 + Math.sqrt(1 + 8 * targetFrom)) / 2);
            float v2 = (float) ((-1 + Math.sqrt(1 + 8 * targetEnd)) / 2);

            mSpeed = v1 + Math.random() * (v2 - v1);
        } else {
            mSpeed = 50;
        }
        isShouldEnd = false;
    }


    public void luckyEnd() {
        isShouldEnd = true;
        mStartAngle = 0;
    }


    public boolean isStart() {
        return mSpeed != 0;
    }

    public boolean isShouldEnd() {
        return isShouldEnd;
    }
}
