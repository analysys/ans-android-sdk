package com.analysys.visual.viewcrawler;

import com.analysys.utils.ANSThreadPool;
import com.analysys.utils.InternalAgent;
import com.analysys.visual.websocket.client.BaseWebSocketClient;
import com.analysys.visual.websocket.drafts.Draft_6455;
import com.analysys.visual.websocket.exceptions.NotSendableException;
import com.analysys.visual.websocket.exceptions.WebsocketNotConnectedException;
import com.analysys.visual.websocket.framing.Framedata;
import com.analysys.visual.websocket.handshake.ServerHandshake;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URI;
import java.nio.ByteBuffer;

/**
 * EditorClient should handle all communication to and from the socket. It should be fairly naive
 * and
 * only know how to delegate messages to the ABHandler class.
 */
class EditorConnection {

    private static final int CONNECT_TIMEOUT = 10000;
    private static final ByteBuffer EMPTY_BYTE_BUFFER = ByteBuffer.allocate(0);
    private static final String TAG = "VisualEditorConnection";
    private final Editor mService;
    private final EditorClient mClient;
    private final URI mURI;

    public EditorConnection(URI uri, Editor service, Socket sslSocket)
            throws EditorConnectionException {
        mService = service;
        mURI = uri;
        try {
            mClient = new EditorClient(uri, CONNECT_TIMEOUT, sslSocket);
            mClient.connectBlocking();
        } catch (final InterruptedException e) {
            throw new EditorConnectionException(e);
        }
    }

    public boolean isValid() {
        return !mClient.isClosed() && !mClient.isClosing() && !mClient.isFlushAndClose();
    }

    public boolean isConnected() {
        return mClient.isOpen();
    }

    /**
     * add new Method to provide client active disconnect socket
     */
    public void close() {
        mClient.close();
    }

    public BufferedOutputStream getBufferedOutputStream() {
        return new BufferedOutputStream(new WebSocketOutputStream());
    }
    public interface Editor {
        void sendSnapshot(JSONObject message);

        void bindEvents(JSONObject message);

        void sendDeviceInfo();

        void cleanup();
    }

    public class EditorConnectionException extends IOException {
        private static final long serialVersionUID = -1884953175346045636L;

        public EditorConnectionException(Throwable cause) {
            // IOException(cause) is only available in API level 9!
            super(cause.getMessage());
        }
    }

    private class EditorClient extends BaseWebSocketClient {

        private boolean mMessageReceived;
        private long mOpenTime;

        public EditorClient(URI uri, int connectTimeout, Socket sslSocket) {
            super(uri, new Draft_6455(), null, connectTimeout);
            setSocket(sslSocket);
        }

        @Override
        public void onOpen(ServerHandshake handshakedata) {
            InternalAgent.d(TAG, "Websocket connected");
            mOpenTime = System.currentTimeMillis();
            ANSThreadPool.execute(mCheckConnRunnable);
        }

        private Runnable mCheckConnRunnable = new Runnable() {
            @Override
            public void run() {
                while (System.currentTimeMillis() - mOpenTime < 3000) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (mMessageReceived) {
                        break;
                    }
                }
                if (!mMessageReceived) {
                    close();
                }
            }
        };

        @Override
        public void onMessage(String message) {
            InternalAgent.d(TAG, "message sent to the client by the server: " + message);
            mMessageReceived = true;
            try {
                final JSONObject messageJson = new JSONObject(message);
                final String type = messageJson.getString("type");
                //请求设备的基本信息
                if ("device_info_request".equals(type)) {
                    mService.sendDeviceInfo();
                } else if ("snapshot_request".equals(type)) {
                    //请求快照信息
                    // (具体为获取现在屏幕截图和详细组件信息)
                    mService.sendSnapshot(messageJson);
                    //可视化埋点绑定信息
                } else if ("event_binding_request".equals(type)) {
                    mService.bindEvents(messageJson);
                }
            } catch (final JSONException e) {
                InternalAgent.e(TAG, "JSON parsing failure:" + message, e);
            }
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
            InternalAgent.d(TAG, "Disconnect the socket connection between the server and the " +
                    "client。" + "Code: " + code + ", reason: " +
                    reason + "URI: " + mURI);


            mService.cleanup();
        }

        @Override
        public void onError(Exception ex) {
            if (ex != null && ex.getMessage() != null) {
                InternalAgent.i(TAG, "The connection between the server and the client is " +
                        "wrong，Resource: " + ex.getMessage());
            } else {
                InternalAgent.e(TAG, "The connection between the server and the client is wrong");

            }
        }
    }

    private class WebSocketOutputStream extends OutputStream {
        @Override
        public void write(int b) {
            try {
                // This should never be called.
                final byte[] oneByte = new byte[1];
                oneByte[0] = (byte) b;
                write(oneByte, 0, 1);
            } catch (Exception e) {
            }
        }

        @Override
        public void write(byte[] b) {
            try {
                write(b, 0, b.length);
            } catch (Exception e) {
            }
        }

        @Override
        public void write(byte[] b, int off, int len)
                throws EditorConnectionException {
            final ByteBuffer message = ByteBuffer.wrap(b, off, len);
            try {
                mClient.sendFragmentedFrame(Framedata.Opcode.TEXT, message, false);
            } catch (final WebsocketNotConnectedException e) {
                InternalAgent.e(e);
                throw new EditorConnectionException(e);
            } catch (final NotSendableException e) {
                throw new EditorConnectionException(e);
            }
        }

        @Override
        public void close() {
            try {
                mClient.sendFragmentedFrame(Framedata.Opcode.TEXT, EMPTY_BYTE_BUFFER, true);
            } catch (Exception e) {
            }
        }
    }
}
