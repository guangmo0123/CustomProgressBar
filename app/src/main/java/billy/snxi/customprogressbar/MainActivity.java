package billy.snxi.customprogressbar;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.View;

import billy.snxi.customprogressbar.view.MyCircleProgressBar;
import billy.snxi.customprogressbar.view.MyHorizontalProgressBar;

public class MainActivity extends Activity {
	private static final int HANDLER_MSG_UPDATE = 0x110;
	private MyHorizontalProgressBar mMyProgressBar1;
	private MyCircleProgressBar mMyProgressBar2;
	private Handler mHandler;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initView();
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				int progress1 = mMyProgressBar1.getProgress();
				if (progress1 <= 100) {
					mMyProgressBar1.setProgress(++progress1);
				}
				int progress2 = mMyProgressBar2.getProgress();
				if (progress2 <= 100) {
					mMyProgressBar2.setProgress(++progress2);
				}
				if (progress1 >= 100 && progress2 >= 100) {
					mHandler.removeMessages(HANDLER_MSG_UPDATE);
					return;
				}
				mHandler.sendEmptyMessageDelayed(HANDLER_MSG_UPDATE, 100);
			}
		};
		mHandler.sendEmptyMessage(HANDLER_MSG_UPDATE);
	}

	private void initView() {
		mMyProgressBar1 = findViewById(R.id.myProgressBar1);
		mMyProgressBar2 = findViewById(R.id.myProgressBar2);
	}

	public void onStartProgressBar(View view) {
		if (mMyProgressBar1.getProgress() >= 100) {
			mMyProgressBar1.setProgress(0);
		}
		if (mMyProgressBar2.getProgress() >= 100) {
			mMyProgressBar2.setProgress(0);
		}
		mHandler.sendEmptyMessage(HANDLER_MSG_UPDATE);
	}
}
