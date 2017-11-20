package xyz.imxqd.stopwatch;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.Locale;


public class MainActivity extends Activity implements View.OnClickListener {

    private static final String ARG_START_TIME = "start_time";
    private static final String ARG_IS_RUNNING = "is_running";
    private static final String ARG_RECORDS = "records";
    private static final String ARG_RECORD_COUNT = "record_count";
    private static final String ARG_TIME = "time";

    private ScrollView mScrollView;
    private TextView mTvScreen;
    private TextView mTvStartPause;
    private TextView mTvRecord;
    private TextView mTvClear;
    private TextView mTvDisplay;

    private Handler mHandler = new Handler();
    private long mStartTime = 0;
    private boolean isRunning = false;
    private int mRecordCount = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences p = getPreferences(Context.MODE_PRIVATE);
        isRunning = p.getBoolean(ARG_IS_RUNNING, false);
        mRecordCount = p.getInt(ARG_RECORD_COUNT, 0);
        mStartTime = p.getLong(ARG_START_TIME, 0);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setContentView(R.layout.activity_main_land);
        } else {
            setContentView(R.layout.activity_main);
        }
        initViews();

        if (mRecordCount != 0) {
            mTvDisplay.setText(p.getString(ARG_RECORDS, ""));
        }
        setTimeRunnable.time = p.getLong(ARG_TIME, 0);
        setTime(setTimeRunnable.time);
        if (isRunning) {
            mTvStartPause.setText(R.string.pause);
            mHandler.post(setTimeRunnable);
        } else {
            mTvStartPause.setText(R.string.start);
        }
    }

    private void initViews() {
        mScrollView = findViewById(R.id.scv_records);
        mTvScreen = findViewById(R.id.tv_screen);
        mTvStartPause = findViewById(R.id.tv_start_pause);
        mTvRecord = findViewById(R.id.tv_record);
        mTvClear = findViewById(R.id.tv_clear);
        mTvDisplay = findViewById(R.id.tv_display);

        mTvStartPause.setOnClickListener(this);
        mTvRecord.setOnClickListener(this);
        mTvClear.setOnClickListener(this);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        String screen = mTvScreen.getText().toString();
        String display = mTvDisplay.getText().toString();
        if(newConfig.orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT){
            setContentView(R.layout.activity_main);
            initViews();
        }else{
            setContentView(R.layout.activity_main_land);
            initViews();
        }

        mTvScreen.setText(screen);
        mTvDisplay.setText(display);
        if (isRunning) {
            mTvStartPause.setText(R.string.pause);
        } else {
            mTvStartPause.setText(R.string.start);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_start_pause) {
            if (isRunning) {
                mTvStartPause.setText(R.string.start);
                isRunning = false;
                mHandler.removeCallbacksAndMessages(null);
            } else {
                if (mStartTime == 0) {
                    // 重新开始
                    mStartTime = SystemClock.elapsedRealtime();
                } else {
                    // 继续
                    mStartTime = SystemClock.elapsedRealtime() - setTimeRunnable.time;
                }
                mTvStartPause.setText(R.string.pause);
                isRunning = true;
                mHandler.post(setTimeRunnable);
            }

        } else if (v.getId() == R.id.tv_record) {
            if (isRunning) {
                mRecordCount++;
                String s = mTvScreen.getText().toString();
                mTvDisplay.append("#"+mRecordCount + "    " + s + "\n");
                mScrollView.fullScroll(View.FOCUS_DOWN);
            }

        } else if (v.getId() == R.id.tv_clear) {
            if (!isRunning) {
                setTimeRunnable.time = 0;
                mStartTime = 0;
                mRecordCount = 0;
                mTvScreen.setText(R.string.org);
                mTvDisplay.setText("");
                mHandler.removeCallbacksAndMessages(null);
            }
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onStart() {
        if (isRunning) {
            mHandler.post(setTimeRunnable);
        }
        super.onStart();
    }

    @Override
    protected void onStop() {
        mHandler.removeCallbacksAndMessages(null);
        getPreferences(Context.MODE_PRIVATE)
                .edit()
                .putBoolean(ARG_IS_RUNNING, isRunning)
                .putInt(ARG_RECORD_COUNT, mRecordCount)
                .putLong(ARG_START_TIME, mStartTime)
                .putString(ARG_RECORDS, mTvDisplay.getText().toString())
                .putLong(ARG_TIME, setTimeRunnable.time)
                .apply();

        super.onStop();
    }

    private void setTime(long time) {
        int ms = (int) (time % 1000 / 10);
        int sec = (int) (time / 1000 % 60);
        int min = (int) (time / 1000 / 60);
        mTvScreen.setText(String.format(Locale.getDefault(),"%02d:%02d:%02d", min, sec, ms));
    }

    class SetTimeRunnable implements Runnable {
        long time;
        @Override
        public void run() {
            time = SystemClock.elapsedRealtime() - mStartTime;
            setTime(time);
            if (isRunning) {
                mHandler.postDelayed(setTimeRunnable, 16);
            }
        }
    }
     private SetTimeRunnable setTimeRunnable = new SetTimeRunnable();
}