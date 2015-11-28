package me.dm7.barcodescanner.core;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.View;

public class ViewFinderView extends View implements IViewFinder {
    private static final String TAG = "ViewFinderView";

    private Rect mFramingRect;

    private int mTopOffset = 0;

    private static final int MIN_FRAME_WIDTH = 240;
    private static final int MIN_FRAME_HEIGHT = 240;

    private static final float LANDSCAPE_WIDTH_RATIO = 5f / 8;
    private static final float LANDSCAPE_HEIGHT_RATIO = 5f / 8;
    private static final int LANDSCAPE_MAX_FRAME_WIDTH = (int) (1920 * LANDSCAPE_WIDTH_RATIO); // = 5/8 * 1920
    private static final int LANDSCAPE_MAX_FRAME_HEIGHT = (int) (1080 * LANDSCAPE_HEIGHT_RATIO); // = 5/8 * 1080

    private static final float PORTRAIT_WIDTH_RATIO = 7f / 8;
    private static final float PORTRAIT_HEIGHT_RATIO = 3f / 8;
    private static final int PORTRAIT_MAX_FRAME_WIDTH = (int) (1080 * PORTRAIT_WIDTH_RATIO); // = 7/8 * 1080
    private static final int PORTRAIT_MAX_FRAME_HEIGHT = (int) (1920 * PORTRAIT_HEIGHT_RATIO); // = 3/8 * 1920

    private static final int[] SCANNER_ALPHA = {0, 64, 128, 192, 255, 192, 128, 64};
    private int scannerAlpha;
    private static final int POINT_SIZE = 10;
    private static final long ANIMATION_DELAY = 80l;

    private final int mDefaultLaserColor = getResources().getColor(R.color.viewfinder_laser);
    private final int mDefaultMaskColor = getResources().getColor(R.color.viewfinder_mask);
    private final int mDefaultBorderColor = getResources().getColor(R.color.viewfinder_border);
    private final int mDefaultBorderStrokeWidth = getResources().getInteger(R.integer.viewfinder_border_width);
    private final int mDefaultBorderLineLength = getResources().getInteger(R.integer.viewfinder_border_length);

    protected Paint mLaserPaint;
    protected Paint mFinderMaskPaint;
    protected Paint mBorderPaint;
    protected int mBorderLineLength;

    private Camera.Size optimalSize;
    private static Canvas currentCanvas;

    public ViewFinderView(Context context) {
        super(context);
        init();
    }

    public ViewFinderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ViewFinderView(Context context, Camera.Size size) {
        super(context);
        optimalSize = size;
        init();
    }

    private void init() {
        //set up laser paint
        mLaserPaint = new Paint();
        mLaserPaint.setColor(mDefaultLaserColor);
        mLaserPaint.setStyle(Paint.Style.FILL);

        //finder mask paint
        mFinderMaskPaint = new Paint();
        mFinderMaskPaint.setColor(mDefaultMaskColor);

        //border paint
        mBorderPaint = new Paint();
        mBorderPaint.setColor(mDefaultBorderColor);
        mBorderPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint.setStrokeWidth(mDefaultBorderStrokeWidth);

        mBorderLineLength = mDefaultBorderLineLength;

    }

    public void setLaserColor(int laserColor) {
        mLaserPaint.setColor(laserColor);
    }

    public void setMaskColor(int maskColor) {
        mFinderMaskPaint.setColor(maskColor);
    }

    public void setBorderColor(int borderColor) {
        mBorderPaint.setColor(borderColor);
    }

    public void setBorderStrokeWidth(int borderStrokeWidth) {
        mBorderPaint.setStrokeWidth(borderStrokeWidth);
    }

    public void setBorderLineLength(int borderLineLength) {
        mBorderLineLength = borderLineLength;
    }

    public void setupViewFinder() {
        updateFramingRect();
        invalidate();
    }

    public Rect getFramingRect() {
        return mFramingRect;
    }

    @Override
    public void setTopOffset(int margin) {
        mTopOffset = margin;
    }

    @Override
    public int getTopOffset() {
        return mTopOffset;
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (mFramingRect == null) {
            return;
        }


        drawViewFinderMask(canvas);
        drawViewFinderBorder(canvas);
   //     drawLaser(canvas);

        currentCanvas = canvas;
    }

    public void drawViewFinderMask(Canvas canvas) {
        int width = canvas.getWidth();
        int height = canvas.getHeight();

        if (optimalSize != null && false) {

            int leftOfSet = (width - optimalSize.width);

            canvas.drawRect(leftOfSet, 0, width - leftOfSet, mFramingRect.top, mFinderMaskPaint);
            canvas.drawRect(leftOfSet, mFramingRect.top, mFramingRect.left, mFramingRect.bottom + 1, mFinderMaskPaint);
            canvas.drawRect(mFramingRect.right + 1, mFramingRect.top, width - leftOfSet, mFramingRect.bottom + 1, mFinderMaskPaint);
            canvas.drawRect(leftOfSet, mFramingRect.bottom + 1, width - leftOfSet, height , mFinderMaskPaint);

        }else{

            canvas.drawRect(0, 0, width, mFramingRect.top, mFinderMaskPaint);
            canvas.drawRect(0, mFramingRect.top, mFramingRect.left, mFramingRect.bottom + 1, mFinderMaskPaint);
            canvas.drawRect(mFramingRect.right + 1, mFramingRect.top, width, mFramingRect.bottom + 1, mFinderMaskPaint);
            canvas.drawRect(0, mFramingRect.bottom + 1, width, height , mFinderMaskPaint);
        }

    }


    public void drawViewFinderBorder(Canvas canvas) {
        canvas.drawLine(mFramingRect.left - 1, mFramingRect.top - 1, mFramingRect.left - 1, mFramingRect.top - 1 + mBorderLineLength, mBorderPaint);
        canvas.drawLine(mFramingRect.left - 1, mFramingRect.top - 1, mFramingRect.left - 1 + mBorderLineLength, mFramingRect.top - 1, mBorderPaint);

        canvas.drawLine(mFramingRect.left - 1, mFramingRect.bottom + 1, mFramingRect.left - 1, mFramingRect.bottom + 1 - mBorderLineLength, mBorderPaint);
        canvas.drawLine(mFramingRect.left - 1, mFramingRect.bottom + 1, mFramingRect.left - 1 + mBorderLineLength, mFramingRect.bottom + 1, mBorderPaint);

        canvas.drawLine(mFramingRect.right + 1, mFramingRect.top - 1, mFramingRect.right + 1, mFramingRect.top - 1 + mBorderLineLength, mBorderPaint);
        canvas.drawLine(mFramingRect.right + 1, mFramingRect.top - 1, mFramingRect.right + 1 - mBorderLineLength, mFramingRect.top - 1, mBorderPaint);

        canvas.drawLine(mFramingRect.right + 1, mFramingRect.bottom + 1, mFramingRect.right + 1, mFramingRect.bottom + 1 - mBorderLineLength, mBorderPaint);
        canvas.drawLine(mFramingRect.right + 1, mFramingRect.bottom + 1, mFramingRect.right + 1 - mBorderLineLength, mFramingRect.bottom + 1, mBorderPaint);
    }

    public void drawLaser(Canvas canvas) {
        // Draw a red "laser scanner" line through the middle to show decoding is active
        mLaserPaint.setAlpha(SCANNER_ALPHA[scannerAlpha]);
        scannerAlpha = (scannerAlpha + 1) % SCANNER_ALPHA.length;
        int middle = mFramingRect.height() / 2 + mFramingRect.top;
        canvas.drawRect(mFramingRect.left + 2, middle - 1, mFramingRect.right - 1, middle + 2, mLaserPaint);

        postInvalidateDelayed(ANIMATION_DELAY,
                mFramingRect.left - POINT_SIZE,
                mFramingRect.top - POINT_SIZE,
                mFramingRect.right + POINT_SIZE,
                mFramingRect.bottom + POINT_SIZE);
    }

    @Override
    protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld) {
        updateFramingRect();
    }

    public synchronized void updateFramingRect() {
        Point viewResolution = new Point(getWidth(), getHeight() - mTopOffset);
        int width;
        int height;
        int orientation = DisplayUtils.getScreenOrientation(getContext());

        if (orientation != Configuration.ORIENTATION_PORTRAIT) {
            height = findDesiredDimensionInRange(0.95f, viewResolution.y, MIN_FRAME_HEIGHT, LANDSCAPE_MAX_FRAME_HEIGHT);
            width = height;
        } else {
            width = findDesiredDimensionInRange(0.95f, viewResolution.x, MIN_FRAME_WIDTH, PORTRAIT_MAX_FRAME_WIDTH);
            height = width;
        }

        int leftOffset = (viewResolution.x - width) / 2;
        int topOffset = mTopOffset + (viewResolution.y - height) / 2;
        mFramingRect = new Rect(leftOffset, topOffset, leftOffset + width, topOffset + height);
    }

    private static int findDesiredDimensionInRange(float ratio, int resolution, int hardMin, int hardMax) {
        int dim = (int) (ratio * resolution);
        if (dim < hardMin) {
            return hardMin;
        }
        if (dim > hardMax) {
            return hardMax;
        }
        return dim;
    }

    public void setOptimalSize(Camera.Size optimalSize) {
        this.optimalSize = optimalSize;
    }

}
