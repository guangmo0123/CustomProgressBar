package billy.snxi.customprogressbar.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.ProgressBar;

import billy.snxi.customprogressbar.R;

public class MyHorizontalProgressBar extends ProgressBar {
	//属性的默认值
	private static final int DEFAULT_COLOR_REACH = 0xFFFC00D1;
	private static final int DEFAULT_HEIGHT_REACH = 2;    //dp
	private static final int DEFAULT_COLOR_UNREACH = 0xFFD3D6DA;
	private static final int DEFAULT_HEIGHT_UNREACH = 2; //dp
	private static final int DEFAULT_TEXT_SIZE = 9;    //sp
	private static final int DEFAULT_TEXT_COLOR = DEFAULT_COLOR_REACH;
	private static final int DEFAULT_TEXT_OFFSET = 5;    //dp

	//属性字段
	private int mUnReachColor = DEFAULT_COLOR_UNREACH;
	private int mUnReachHeight = dp2Px(DEFAULT_HEIGHT_UNREACH);
	private int mReachColor = DEFAULT_COLOR_REACH;
	private int mReachHeight = dp2Px(DEFAULT_HEIGHT_REACH);
	private int mTextSize = sp2Px(DEFAULT_TEXT_SIZE);
	private int mTextColor = DEFAULT_TEXT_COLOR;
	private int mTextOffset = dp2Px(DEFAULT_TEXT_OFFSET);

	//实际进度条的宽度，不含边距
	private int mRealWidth;
	private Paint mPaint;

	public MyHorizontalProgressBar(Context context) {
		this(context, null);
	}

	public MyHorizontalProgressBar(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public MyHorizontalProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
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
		TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.MyHorizontalProgressBar);
		//获取自定义属性的属性值
		mReachColor = ta.getColor(R.styleable.MyHorizontalProgressBar_progressbar_reach_color, mReachColor);
		mReachHeight = (int) ta.getDimension(R.styleable.MyHorizontalProgressBar_progressbar_reach_height, mReachHeight);
		mUnReachColor = ta.getColor(R.styleable.MyHorizontalProgressBar_progressbar_unreach_color, mUnReachColor);
		mUnReachHeight = (int) ta.getDimension(R.styleable.MyHorizontalProgressBar_progressbar_unreach_height, mUnReachHeight);
		mTextSize = (int) ta.getDimension(R.styleable.MyHorizontalProgressBar_progressbar_text_size, mTextSize);
		mTextColor = ta.getColor(R.styleable.MyHorizontalProgressBar_progressbar_text_color, mTextColor);
		mTextOffset = (int) ta.getDimension(R.styleable.MyHorizontalProgressBar_progressbar_text_offset, mTextOffset);
		//显示回收TypedArray所占用的消耗，最好手动释放
		ta.recycle();
	}

	@Override
	protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		//由此view为横向进度条，所以宽度用户必须指定，不能使用wrap_content，要么是精确值，要么是match_parent
//		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightSize = measureHeigth(heightMeasureSpec);
		//确定view的高和宽
		setMeasuredDimension(widthSize, heightSize);
		//实际进度条的宽度，不含边距
		mRealWidth = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
	}

	/**
	 * 测量view的高度
	 *
	 * @param heightMeasureSpec
	 * @return result
	 */
	private int measureHeigth(int heightMeasureSpec) {
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);
		int result = 0;
		if (heightMode == MeasureSpec.EXACTLY) {    //精确模式
			result = heightSize;
		} else {    //当用户设置的高度不是精确值时
			//进度条中文字的高度
			int textHeight = (int) (mPaint.descent() - mPaint.ascent());
			//取文字高度与进度条高度的最大值+内顶边距+内底边距
			result = getPaddingTop() + getPaddingBottom() + Math.max(Math.abs(textHeight), Math.max(mReachHeight, mUnReachHeight));
			//最大模式，不能超过高度上限
			if (heightMode == MeasureSpec.AT_MOST) {
				result = Math.min(result, heightSize);
			}
		}
		return result;
	}

	@Override
	protected synchronized void onDraw(Canvas canvas) {
		canvas.save();
		//将坐标定位到进度条起始位置
		canvas.translate(getPaddingLeft(), getHeight() / 2);
		//标记是否需要画进度条未加载部分，即：unReach部分
		boolean isNeedDrawUnReachLine = true;
		//进度条占比
		float progressRadio = 1.0f * getProgress() / getMax();
		//进度条显示的进度文本
		String progressText = getProgress() + "%";
		//进度文本的宽度
		int textWidth = (int) mPaint.measureText(progressText);
		//可用来加载进度条的最大长度
		int progressMaxWidth = mRealWidth - textWidth - mTextOffset;
		float progressX = progressMaxWidth * progressRadio;
		if (progressX >= progressMaxWidth) {
			isNeedDrawUnReachLine = false;
			progressX = progressMaxWidth;
		}
		//绘制已经加载的进度条长度，以已经加载的进度条长度作为实际加载长度
		if (progressX > 0) {
			mPaint.setColor(mReachColor);
			mPaint.setStrokeWidth(mReachHeight);
			canvas.drawLine(0, 0, progressX, 0, mPaint);
		}
		//绘制文字，由于坐标点在view的中间，因此需要调整文字的y坐标
		int textY = (int) (-(mPaint.descent() + mPaint.ascent()) / 2);
		mPaint.setColor(mTextColor);
		canvas.drawText(progressText, progressX + mTextOffset, textY, mPaint);
		//绘制还未加载的进度条
		float endX = progressX + textWidth + mTextOffset * 2;
		if (isNeedDrawUnReachLine) {
			mPaint.setColor(mUnReachColor);
			mPaint.setStrokeWidth(mUnReachHeight);
			canvas.drawLine(endX, 0, mRealWidth, 0, mPaint);
		}
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
