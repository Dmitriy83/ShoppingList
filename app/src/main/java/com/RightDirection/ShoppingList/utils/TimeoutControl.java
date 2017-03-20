package com.RightDirection.ShoppingList.utils;


import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Контроль таймаута попытки соединения с Firebase
 */
public class TimeoutControl {

    private ArrayList<IOnTimeoutListener> mListeners;
    private long mTimeout;
    private Timer mTimer;

    public TimeoutControl(long timeout){
        mTimeout = timeout;
        mListeners = new ArrayList<>();
    }

    public void start(){
        startTimer();
    }

    private void startTimer(){
        mTimer = new Timer();
        mTimer.schedule(new Timeout(), mTimeout);
    }

    private void stopTimer(){
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    public void stop() {
        stopTimer();
    }

    private class Timeout extends TimerTask {
        @Override
        public void run() {
            stopTimer();
            notifyListeners();
        }
    }

    private void notifyListeners() {
        for (IOnTimeoutListener listener: mListeners) {
            listener.onTimeout();
        }
    }

    public void addListener(IOnTimeoutListener listener){
        mListeners.add(listener);
    }

    public interface IOnTimeoutListener{void onTimeout();}
}

