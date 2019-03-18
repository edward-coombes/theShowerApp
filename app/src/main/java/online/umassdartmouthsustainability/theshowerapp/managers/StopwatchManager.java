package online.umassdartmouthsustainability.theshowerapp.managers;

import android.media.MediaPlayer;
import android.os.Handler;

import android.util.Log;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import online.umassdartmouthsustainability.theshowerapp.fragments.StopwatchFragment;

@SuppressWarnings("")
public class StopwatchManager {
    private static StopwatchManager manager;
    private String tag = "theShowerApp.Stopwatch";

    private boolean started = false;
    private boolean timerInitialized = false;

    private int min;
    private int sec;

    private MediaPlayer successMP;
    private MediaPlayer alarmMP;

    public static final char QUICK_CODE = 'Q';
    public static final char RELAX_CODE = 'R';
    public static final char SHAVE_CODE = 'S';
    private static final long startDelay = 3;
    private Handler uiHandler;

    private StopwatchFragment f;

    private Time watch;
    private Timer t = new Timer();
    private DecrementTask decrementer;

    public static StopwatchManager getInstance(MediaPlayer alarmMP, MediaPlayer successMP) {
        if (manager == null) {
            manager = new StopwatchManager();
            manager.alarmMP = alarmMP;
            manager.successMP = successMP;
        }

        return manager;
    }

    public static StopwatchManager getInstance() {
        return StopwatchManager.manager;
    }

    public void setUiHandler(Handler uiHandler) {
        manager.uiHandler = uiHandler;
    }

    public boolean getStarted() {
        return manager.started;
    }

    public void initializeTimer(char timeCode) {
        manager.timerInitialized = true;
        switch (timeCode) {
            case QUICK_CODE:
                manager.min = 5;
                manager.sec = 0;
                break;
            default:
            case RELAX_CODE:
                manager.min = 7;
                manager.sec = 30;
                break;
            case SHAVE_CODE:
                manager.min = 10;
                manager.sec = 0;
                break;
        }
        manager.uiHandler.obtainMessage(1).sendToTarget();
        manager.watch = new Time(min, sec, manager);
        manager.decrementer = new DecrementTask(manager.watch, manager.uiHandler);
    }

    public String getTimeString() {
        return manager.watch.getDisplay();
    }

    public int getGoalTime() {
        return min * 60 + sec;
    }

    public int getElapsedTime() {
        return manager.watch.getElapsedTime();
    }

    private void successTone() {
        manager.successMP.start();
    }

    void startAlarm() {
        manager.alarmMP.start();
    }

    public void startTimer() {
        manager.started = true;
        t.scheduleAtFixedRate(decrementer,
                startDelay * 1000, 1000);
    }

    public void stopTimer() {
        //stop the timer running
        manager.started = false;
        manager.timerInitialized = false;
        manager.t.cancel();
        manager.t = new Timer();

        //stop the alarm if it's playing
        if (manager.alarmMP.isPlaying()) {
            manager.alarmMP.stop();
            manager.alarmMP.prepareAsync();
        } else {
            //otherwise play success sound
            manager.successTone();
        }

    }

}

class DecrementTask extends TimerTask {
    private Time t;
    private boolean alarm = false;
    private boolean prev = false;
    private Handler uiHandler;

    DecrementTask(Time t, Handler h) {
        this.t = t;
        this.uiHandler = h;
        this.uiHandler.obtainMessage(1).sendToTarget();
    }

    public void run() {
        if (t.decrementTimer()) {
            this.alarm = true;
        }
        try {
            this.uiHandler.obtainMessage(1).sendToTarget();
        } finally {
            if (this.alarm && !this.prev) {
                //start playing alarm sound
                this.prev = true;
            }
        }

    }

}

class Time {
    //private String tag = "theShowerApp.TimeKeeper";
    private int sec;
    private int min;
    private int elapsedTime;
    private boolean alarmStarted = false;
    private String display;
    private StopwatchManager m;

    Time(int m, int s, StopwatchManager manager) {
        this.setTime(m, s);
        this.m = manager;
    }

    private void setTime(int m, int s) {
        this.min = m;
        this.sec = s;
        this.updateDisplay();
    }

    private void updateDisplay() {
        if (this.min >= 0 && this.sec >= 0)
            this.display = String.format(Locale.US, "%d:%02d", this.min, this.sec);
        else
            this.display = "0:00";
    }

    public String getDisplay() {
        //Log.d(tag,this.display);
        return this.display;
    }

    boolean decrementTimer() {
        if (this.min >= 0 && this.sec >= 0) {
            this.sec--;
            if (this.sec < 0) {
                this.sec = 59;
                this.min--;
            }
            this.updateDisplay();
        }
        this.elapsedTime++;

        return this.timeout();
    }

    private boolean timeout() {
        boolean timeout = (this.min <= 0 && this.sec <= 0);
        if (timeout && !this.alarmStarted) {
            this.alarmStarted = true;
            this.m.startAlarm();
        }
        return timeout;
    }

    int getElapsedTime() {
        return this.elapsedTime;
    }

}