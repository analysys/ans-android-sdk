package com.analysys.utils;

import static android.content.Context.SENSOR_SERVICE;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import com.analysys.AnalysysAgent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class AnalysysSSManager {

    public enum RATE {
        SENSOR_DELAY_FASTEST,
        SENSOR_DELAY_GAME,
        SENSOR_DELAY_UI,
        SENSOR_DELAY_NORMAL,
    }

    private final String TAG = "AnalysysSSManager";

    private static volatile AnalysysSSManager instance;

    private Context mContext;

    private int cacheDataLength = 100;

    private int listenDuration = 0;

    private boolean collectReverse = false;

    private boolean useGravity = false;

    private RATE rate = RATE.SENSOR_DELAY_NORMAL;

    private SensorManager manager;

    private Sensor mAccelerometerGravity;

    private Timer timer = new Timer();

    private TimerTask task = new TimerTask() {
        @Override
        public void run() {
            stopListen();
        }
    };

    private SensorEventListener sensorListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            try {
                String value = "";
                if (sensorEvent.values.length >= 2) {
                    value += sensorEvent.values[0];
                    value += ",";
                    value += sensorEvent.values[1];
                    value += ",";
                    value += sensorEvent.values[2];
                }
                switch (sensorEvent.sensor.getType()) {
                    case Sensor.TYPE_ACCELEROMETER:
                    case Sensor.TYPE_LINEAR_ACCELERATION:
                        synchronized (AnalysysSSManager.class) {
                            addStringToContainer(value,accDataList);
                        }
                        break;
                    case Sensor.TYPE_GYROSCOPE:
                    case Sensor.TYPE_ROTATION_VECTOR:
                        synchronized (AnalysysSSManager.class) {
                            addStringToContainer(value,gyroDataList);
                        }
                        break;
                }
            }catch (Throwable t) {
                t.printStackTrace();
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    };



    private Sensor mGyroSensorGravity;

    private Sensor mAccelerometerProcessed;

    private Sensor mGyroSensorProcessed;

    private List<String> gyroDataList = new ArrayList();

    private List<String> accDataList = new ArrayList();

    public static AnalysysSSManager getInstance(Context context) {
        if (context == null) {
            return null;
        }
        if (instance == null) {
            synchronized (AnalysysSSManager.class) {
                if (instance == null) {
                    instance = new AnalysysSSManager(context);
                }
            }
        }
        return instance;
    }

    public AnalysysSSManager (Context context) {
        mContext = context;
        try {
            manager = (SensorManager) mContext.getSystemService(SENSOR_SERVICE);
        }catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public void onAppResume () {
        synchronized (AnalysysSSManager.class) {

        }
    }

    public void onAppPause () {
        synchronized (AnalysysSSManager.class) {

        }
    }

    public void startListen () {
        Log.d(TAG,"startListen in");
        synchronized (AnalysysSSManager.class) {
            if (manager == null) {
                return;
            }
            try {
                unregisterListeners ();
            }catch (Throwable t) {
                t.printStackTrace();
            }
            try{
                this.accDataList.clear();
                this.gyroDataList.clear();
                if (this.isUseGravity()) {
                    if (this.mAccelerometerGravity == null) {
                        this.mAccelerometerGravity = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                    }
                    if (this.mGyroSensorGravity == null) {
                        this.mGyroSensorGravity = manager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
                    }
                    manager.registerListener(sensorListener,mAccelerometerGravity,this.getSystemRate());
                    manager.registerListener(sensorListener,mGyroSensorGravity,this.getSystemRate());
                }else {
                    if (this.mAccelerometerProcessed == null) {
                        this.mAccelerometerProcessed = manager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
                    }
                    if (this.mGyroSensorProcessed == null) {
                        this.mGyroSensorProcessed = manager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
                    }
                    manager.registerListener(sensorListener,mGyroSensorProcessed,this.getSystemRate());
                    manager.registerListener(sensorListener,mAccelerometerProcessed,this.getSystemRate());
                }
            }catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    public void stopListen () {
        Log.d(TAG,"stopListen in time:"+System.currentTimeMillis());
        synchronized (AnalysysSSManager.class) {
            try {
                unregisterListeners ();
            }catch (Throwable t) {
                t.printStackTrace();
            }
            try {
                List<String> accTempSend = new ArrayList<>(this.accDataList);
                List<String> gyroTempSend = new ArrayList<>(this.gyroDataList);
                this.accDataList.clear();
                this.gyroDataList.clear();
                if (accTempSend.size() == 0 || gyroTempSend.size() == 0) {
                    return;
                }

                Map<String, Object> tempMap = new HashMap<>();
                tempMap.put("acc",accTempSend);
                tempMap.put("gyro",gyroTempSend);
                AnalysysAgent.track(mContext,"sensor",tempMap);
            }catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    private void unregisterListeners () {
        if (mAccelerometerGravity != null) {
            manager.unregisterListener(sensorListener,mAccelerometerGravity);
        }
        if (mGyroSensorGravity != null) {
            manager.unregisterListener(sensorListener,mGyroSensorGravity);
        }
        if (mAccelerometerProcessed != null) {
            manager.unregisterListener(sensorListener,mAccelerometerProcessed);
        }
        if (mGyroSensorProcessed != null) {
            manager.unregisterListener(sensorListener,mGyroSensorProcessed);
        }
    }

    private void addStringToContainer (String value,List<String> container) {
        try {
            if (container != null && value != null && value.length() >0) {
                container.add(value);
            }
            if (container != null ) {
                if (container.size() > this.getCacheDataLength()) {
                    if (this.isCollectReverse()) {
                        container.remove(container.size()-1);
                    }else {
                        container.remove(0);
                    }
                }
            }
        }catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public int getCacheDataLength() {
        return cacheDataLength;
    }

    public void setCacheDataLength(int cacheDataLength) {
        this.cacheDataLength = cacheDataLength;
    }

    public boolean isCollectReverse() {
        return collectReverse;
    }

    public void setCollectReverse(boolean collectReverse) {
        this.collectReverse = collectReverse;
    }

    private int getSystemRate() {
        int result = SensorManager.SENSOR_DELAY_NORMAL;
        switch (this.rate){
            case SENSOR_DELAY_UI:
                result = SensorManager.SENSOR_DELAY_UI;
                break;
            case SENSOR_DELAY_GAME:
                result = SensorManager.SENSOR_DELAY_GAME;
                break;
            case SENSOR_DELAY_FASTEST:
                result = SensorManager.SENSOR_DELAY_FASTEST;
                break;
            case SENSOR_DELAY_NORMAL:
                result = SensorManager.SENSOR_DELAY_NORMAL;
                break;
        }
        return result;
    }

    public void setRate(RATE rate) {
        this.rate = rate;
    }

    public boolean isUseGravity() {
        return useGravity;
    }

    public void setUseGravity(boolean useGravity) {
        this.useGravity = useGravity;
    }

    public int getListenDuration() {
        return listenDuration;
    }

    public void setListenDuration(int listenDuration) {
        this.listenDuration = listenDuration;
        timer.schedule(task, listenDuration * 1000);
    }

}
