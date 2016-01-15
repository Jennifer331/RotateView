package cn.leixiaoyue.rotateview;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Created by 80119424 on 2016/1/14.
 */
public class MyView extends View {
    public static final String TAG = "MyView";

    public static final int ROTATE_BITMAP = 0;
    public static final int ROTATE_MATRIX = 1;

    public static final int ANIMATION_DURATION = 300;

    private Bitmap mDisplayBitmap = null;
    private Bitmap mImage = null;

    private Bitmap[] mBitmapPool = new Bitmap[2];
    private boolean[] mInUse     = new boolean[]{false, false};

    private Paint mPaint = null;
    private int mCurrentDegree = 0;//"Degreee" description uses in Animation Duration
    private int mCurrentState  = 0;
    private int mDestState     = 0;
    private int mBitmapWidth   = 0;
    private int mBitmapHeight  = 0;
    private int mImageWidth    = 0;
    private int mImageHeight   = 0;
    private ValueAnimator mAnimator = null;
    private MyViewListener mListener = null;
    private int mRotateMode = ROTATE_BITMAP;

    public MyView(Context context) {
        this(context, null);
    }

    public MyView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public MyView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    public interface MyViewListener {
        void animationStarted();

        void animationEnded();
    }

    private void init(Context context) {
        initBitmapPool(context);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mAnimator = ValueAnimator.ofFloat(0, 1);
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float mCurrentAnimatorValue = (float) animation.getAnimatedValue();
                mCurrentDegree = mCurrentState + (int) (mCurrentAnimatorValue * 90);
                if (ROTATE_BITMAP == mRotateMode) {
                    reDrawDisplayingBitmap(mCurrentDegree);
                }
                invalidate();
            }
        });
        mAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mListener.animationStarted();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mCurrentState = mDestState;
                if (360 == mCurrentState) {
                    mCurrentDegree = 0;
                }
                mListener.animationEnded();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    private void initBitmapPool(Context context) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 4;
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(context.getResources(), R.drawable.koala, options);

        mImageWidth = options.outWidth;
        mImageHeight = options.outHeight;
        int diagonal = (int)Math.sqrt(mImageWidth * mImageWidth + mImageHeight * mImageHeight);
        mBitmapWidth = diagonal;
        mBitmapHeight = diagonal;
        for (int i = 0; i < mBitmapPool.length; i++) {
            mBitmapPool[i] = Bitmap.createBitmap(diagonal, diagonal, Bitmap.Config.ARGB_8888);
        }

        options.inJustDecodeBounds = false;
        options.inMutable = true;
        mImage = BitmapFactory.decodeResource(context.getResources(), R.drawable.koala, options);

        Bitmap bitmap = getABitmap();
        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(mImage,
                (mBitmapWidth - mImageWidth) * 0.5f,
                (mBitmapHeight - mImageHeight) * 0.5f,
                mPaint);
        mDisplayBitmap = bitmap;
    }

    public void setListener(MyViewListener listener) {
        this.mListener = listener;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();

        canvas.save();
        if (ROTATE_MATRIX == mRotateMode) {
            canvas.rotate(mCurrentDegree, width * 0.5f, height * 0.5f);
        }
        canvas.drawBitmap(mDisplayBitmap,
                width * 0.5f - mBitmapWidth * 0.5f,
                height * 0.5f - mBitmapHeight * 0.5f,
                mPaint);
        canvas.restore();
    }

    private Bitmap getABitmap() {
        for (int i = 0; i < mBitmapPool.length; i++) {
            if (!mInUse[i]) {
                Canvas canvas = new Canvas(mBitmapPool[i]);
                canvas.drawColor(0x000000, PorterDuff.Mode.CLEAR);
                mInUse[i] = true;
                return mBitmapPool[i];
            }
        }
        return null;
    }

    private void recycleBitmap(Bitmap bitmap) {
        for (int i = 0; i < mBitmapPool.length; i++) {
            if (bitmap.equals(mBitmapPool[i])) {
                mInUse[i] = false;
            }
        }
    }

    public void nextState(int mode) {
        if (mRotateMode != mode) {
            if(ROTATE_BITMAP == mRotateMode) {
                reDrawDisplayingBitmap(0);
            }
        }
        mRotateMode = mode;
        mDestState = mCurrentState + 90;
        if (mDestState > 360) {
            mDestState = mDestState % 360;
        }
        beginAnimationToNextState();
    }

    private void beginAnimationToNextState() {
        mAnimator.cancel();
        mAnimator.setFloatValues(0, 1);
        mAnimator.setDuration(ANIMATION_DURATION);
        mAnimator.start();
    }

    private void reDrawDisplayingBitmap(int degree) {
        Matrix matrix = new Matrix();
        matrix.postTranslate((mBitmapWidth - mImageWidth) * 0.5f,
                (mBitmapHeight - mImageHeight) * 0.5f);
        matrix.postRotate(degree, mBitmapWidth * 0.5f, mBitmapHeight * 0.5f);
        Bitmap bitmap = getABitmap();
        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(mImage, matrix, mPaint);
        recycleBitmap(mDisplayBitmap);
        mDisplayBitmap = bitmap;
    }
}
