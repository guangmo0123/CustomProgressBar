package billy.snxi.customprogressbar.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.ProgressBar;

import billy.snxi.customprogressbar.R;

public class MyCircleProgressBar extends ProgressBar {
	//属性的默认值
	private static final int DEFAULT_COLOR_REACH = 0xFFFC00D1;
	private static final int DEFAULT_HEIGHT_REACH = 2;    //dp
	private static final int DEFAULT_COLOR_UNREACH = 0xFFD3D6DA;
	private static final int DEFAULT_HEIGHT_UNREACH = 2; //dp
	private static final int DEFAULT_TEXT_SIZE = 9;    //sp
	private static final int DEFAULT_TEXT_COLOR = DEFAULT_COLOR_REACH;
	private static final int DEFAULT_MIN_SIZE = 50;    //dp

	//属性字段
	private int mUnReachColor = DEFAULT_COLOR_UNREACH;
	private int mUnReachHeight = dp2Px(DEFAULT_HEIGHT_UNREACH);
	private int mReachColor = DEFAULT_COLOR_REACH;
	private int mReachHeight = dp2Px(DEFAULT_HEIGHT_REACH);
	private int mTextSize = sp2Px(DEFAULT_TEXT_SIZE);
	private int mTextColor = DEFAULT_TEXT_COLOR;

	//实际进度条的宽度，不含边距
	private int mRealWidth;
	private Paint mPaint;
	private RectF mRectf;

	public MyCircleProgressBar(Context context) {
		this(context, null);
	}

	public MyCircleProgressBar(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public MyCircleProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		obtainStyledAttrs(attrs);
		mPaint = new Paint();
		mPaint.setTextSize(mTextSize);
		mPaint.setAntiAlias(true);
	}

	/**
	 * 获取xml中定义的自定义属性
	 *
	 * @param attrs
	 */
	private void obtainStyledAttrs(AttributeSet attrs) {
		//获取自定义属性集合
		TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.MyCircleProgressBar);
		//获取自定义属性的属性值
		mReachColor = ta.getColor(R.styleable.MyCircleProgressBar_progressbar_reach_color, mReachColor);
		mReachHeight = (int) ta.getDimension(R.styleable.MyCircleProgressBar_progressbar_reach_height, mReachHeight);
		mUnReachColor = ta.getColor(R.styleable.MyCircleProgressBar_progressbar_unreach_color, mUnReachColor);
		mUnReachHeight = (int) ta.getDimension(R.styleable.MyCircleProgressBar_progressbar_unreach_height, mUnReachHeight);
		mTextSize = (int) ta.getDimension(R.styleable.MyCircleProgressBar_progressbar_text_size, mTextSize);
		mTextColor = ta.getColor(R.styleable.MyCircleProgressBar_progressbar_text_color, mTextColor);
		//显示回收TypedArray所占用的消耗，最好手动释放
		ta.recycle();
	}

	@Override
	protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		//由此view为横向进度条，所以宽度用户必须指定，不能使用wrap_content，要么是精确值，要么是match_parent
		int widthSize = measureSize(heightMeasureSpec, true);
		int heightSize = measureSize(heightMeasureSpec, false);
		//确定view的高和宽
		setMeasuredDimension(widthSize, heightSize);
		//实际圆的直径，不含边距，去view高与宽的最小值
		mRealWidth = Math.min(getMeasuredWidth() - getPaddingLeft() - getPaddingRight(),
				getMeasuredHeight() - getPaddingTop() - getPaddingBottom());
	}

	/**
	 * 测量view的高度
	 *
	 * @param measureSpec
	 * @return result
	 */
	private int measureSize(int measureSpec, boolean isWidth) {
		int mode = MeasureSpec.getMode(measureSpec);
		int size = MeasureSpec.getSize(measureSpec);
		int result = 0;
		if (mode == MeasureSpec.EXACTLY) {    //精确模式
			result = size;
		} else {    //当用户设置的高度不是精确值时
			//进度条中文字的高度
			int textHeight = (int) (mPaint.descent() - mPaint.ascent());
			//取文字高度与最小高度的最大值
			if (isWidth) {
				result = getPaddingLeft() + getPaddingRight() + Math.max(Math.abs(textHeight), DEFAULT_MIN_SIZE);
			} else {
				result = getPaddingTop() + getPaddingBottom() + Math.max(Math.abs(textHeight), DEFAULT_MIN_SIZE);
			}
			//最大模式，不能超过高度上限
			if (mode == MeasureSpec.AT_MOST) {
				result = Math.min(result, size);
			}
		}
		return result;
	}

	@Override
	protected synchronized void onDraw(Canvas canvas) {
		canvas.save();
		//将坐标定位到圆心位置
		canvas.translate(getPaddingLeft() + mRealWidth / 2, getPaddingTop() + mRealWidth / 2);
		mPaint.setStyle(Paint.Style.STROKE);

		//绘制还未加载的进度条
		mPaint.setColor(mUnReachColor);
		mPaint.setStrokeWidth(mUnReachHeight);
		canvas.drawCircle(0, 0, mRealWidth / 2, mPaint);

		//已加载进度条的部分
		float progressRadio = 360f * getProgress() / getMax();
		if (progressRadio >= 360) {
			progressRadio = 360;
		}
		//绘制已经加载的进度条长度，以已经加载的进度条长度作为实际加载长度
		if (progressRadio > 0) {
			mPaint.setColor(mReachColor);
			mPaint.setStrokeWidth(mReachHeight);
			mRectf = new RectF(-mRealWidth / 2, -mRealWidth / 2, mRealWidth / 2, mRealWidth / 2);
			canvas.drawArc(mRectf, -90, progressRadio, false, mPaint);
		}

		//进度条显示的进度文本
		String progressText = getProgress() + "%";
		//绘制文字
		mPaint.setColor(mTextColor);
		mPaint.setStyle(Paint.Style.FILL);
		//文字的宽度
		int textWidth = (int) mPaint.measureText(progressText);
		//绘制文字，由于坐标点在view的中间，因此需要调整文字的y坐标
		int textY = (int) (-(mPaint.descent() + mPaint.ascent()) / 2);
		canvas.drawText(progressText, -textWidth / 2, textY, mPaint);

		canvas.restore();
	}

	/**
	 * 将dp值转化为px
	 *
	 * @param dpValue
	 * @return
	 */
	private int dp2Px(int dpValue) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, getResources().getDisplayMetrics());
	}

	/**
	 * 将sp值转化为px
	 *
	 * @param dpValue
	 * @return
	 */
	private int sp2Px(int dpValue) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, dpValue, getResources().getDisplayMetrics());
	}

}
