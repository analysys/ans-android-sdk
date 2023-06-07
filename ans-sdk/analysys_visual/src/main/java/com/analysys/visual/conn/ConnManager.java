package com.analysys.visual.conn;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Message;

import com.analysys.utils.ANSLog;
import com.analysys.utils.AnalysysUtil;
import com.analysys.utils.ExceptionUtil;
import com.analysys.utils.InternalAgent;
import com.analysys.visual.VisualManager;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.Socket;
import java.net.URI;
import java.security.GeneralSecurityException;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

public class ConnManager {
    private static final String TAG = VisualManager.TAG;
    private Handler mVisualHandler;

    public ConnManager(Handler handler) {
        mVisualHandler = handler;
    }

    public void connectManual() {
        mGestureCallback.onGestureDetected();
    }

    private ConnSensor.IGestureCallback mGestureCallback = new ConnSensor.IGestureCallback() {
        @Override
        public void onGestureDetected() {
            ANSLog.i(TAG, "Gesture detected");
            unregisterSensor();
            sendMessage(VisualManager.MESSAGE_CONNECT_TO_EDITOR, null);
        }
    };

    private ConnSensor mConnSensor = new ConnSensor(mGestureCallback);
    private boolean mIsSensorRegistered;

    private ConnAgent mConnAgent;

    public void doConnect() {
        if (mConnAgent != null && mConnAgent.isValid()) {
            InternalAgent.v(TAG, "already connected");
            return;
        }

        Socket sslSocket = null;
        final String url = VisualManager.getInstance().getUrl();
        if (url.startsWith("wss")) {
            SSLSocketFactory foundSSLFactory = null;
            try {
                final SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, null, null);
                foundSSLFactory = sslContext.getSocketFactory();
            } catch (final GeneralSecurityException e) {
                ANSLog.i(TAG, "ssl support fail", e);
                return;
            } catch (Throwable ignore) {
                ExceptionUtil.exceptionThrow(ignore);
            }
            final SSLSocketFactory socketFactory = foundSSLFactory;
            try {
                sslSocket = socketFactory.createSocket();
            } catch (Throwable ignore) {
                InternalAgent.e(ignore);
                ExceptionUtil.exceptionThrow(ignore);
            }
        } else {
            final SocketFactory socketFactory = SocketFactory.getDefault();
            try {
                sslSocket = socketFactory.createSocket();
            } catch (Throwable ignore) {
                InternalAgent.e(ignore);
                ExceptionUtil.exceptionThrow(ignore);
            }
        }
        try {
            mConnAgent = new ConnAgent(new URI(url), new Editor(), sslSocket);
        } catch (Throwable ignore) {
            InternalAgent.e(ignore);
            ExceptionUtil.exceptionThrow(ignore);
        }
    }

    public void close() {
        if (mConnAgent != null) {
            mConnAgent.close();
        }
    }

    public boolean isConnected() {
        return mConnAgent != null && mConnAgent.isConnected();
    }

    public synchronized void registerSensor() {
        if (!mIsSensorRegistered) {
            ANSLog.i(TAG, "register sensor");
            final SensorManager sensorManager =
                    (SensorManager) AnalysysUtil.getContext().getSystemService(Context.SENSOR_SERVICE);
            final Sensor accelerometer =
                    sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(mConnSensor, accelerometer,
                    SensorManager.SENSOR_DELAY_NORMAL);
            mIsSensorRegistered = true;
        }
    }

    public synchronized void unregisterSensor() {
        if (mIsSensorRegistered) {
            ANSLog.i(TAG, "unregister sensor");
            final SensorManager sensorManager = (SensorManager) AnalysysUtil.getContext().getSystemService
                    (Context.SENSOR_SERVICE);
            sensorManager.unregisterListener(mConnSensor);
            mConnSensor.reset();
            mIsSensorRegistered = false;
        }
    }

    public OutputStream getNewOutputStream() {
        if (mConnAgent == null || !mConnAgent.isValid() || !mConnAgent
                .isConnected()) {
            return null;
        }

        return mConnAgent.getBufferedOutputStream();
    }

    class Editor implements ConnAgent.Editor {

        @Override
        public void sendSnapshot(JSONObject message) {
            sendMessage(VisualManager.MESSAGE_SEND_SNAPSHOT, message);
        }

        @Override
        public void bindEvents(JSONObject message) {
            sendMessage(VisualManager.MESSAGE_HANDLE_EDITOR_BINDINGS_RECEIVED, message);
        }

        @Override
        public void sendDeviceInfo() {
            sendMessage(VisualManager.MESSAGE_SEND_DEVICE_INFO, null);
        }

        @Override
        public void cleanup() {
            sendMessage(VisualManager.MESSAGE_HANDLE_EDITOR_CLOSED, null);
        }
    }

    private void sendMessage(int code, Object obj) {
        mVisualHandler.removeMessages(code);
        final Message msg = mVisualHandler.obtainMessage(code);
        if (obj != null) {
            msg.obj = obj;
        }
        mVisualHandler.sendMessage(msg);
    }
}
