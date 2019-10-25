/*
 * Copyright (c) 2010-2018 Nathan Rajlich
 *
 *  Permission is hereby granted, free of charge, to any person
 *  obtaining a copy of this software and associated documentation
 *  files (the "Software"), to deal in the Software without
 *  restriction, including without limitation the rights to use,
 *  copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the
 *  Software is furnished to do so, subject to the following
 *  conditions:
 *
 *  The above copyright notice and this permission notice shall be
 *  included in all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 *  OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *  NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 *  HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 *  WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 *  FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 *  OTHER DEALINGS IN THE SOFTWARE.
 */

package com.analysys.visual.websocket;

import com.analysys.visual.websocket.drafts.BaseDraft;
import com.analysys.visual.websocket.exceptions.InvalidDataException;
import com.analysys.visual.websocket.framing.Framedata;
import com.analysys.visual.websocket.framing.PingFrame;
import com.analysys.visual.websocket.framing.PongFrame;
import com.analysys.visual.websocket.handshake.ClientHandshake;
import com.analysys.visual.websocket.handshake.HandshakeImpl1Server;
import com.analysys.visual.websocket.handshake.ServerHandshake;
import com.analysys.visual.websocket.handshake.ServerHandshakeBuilder;

/**
 * This class default implements all methods of the WebSocketListener that can be overridden
 * optionally when advances functionalities is needed.<br>
 **/
public abstract class BaseWebSocketAdapter implements WebSocketListener {

    /**
     * This default implementation does not do anything. Go ahead and overwrite it.
     *
     * @see com.analysys.visual.websocket.WebSocketListener#onWebsocketHandshakeReceivedAsServer(WebSocket, BaseDraft, ClientHandshake)
     */
    @Override
    public ServerHandshakeBuilder onWebsocketHandshakeReceivedAsServer(WebSocket conn,
                                                                       BaseDraft draft,
                                                                       ClientHandshake request) throws InvalidDataException {
        return new HandshakeImpl1Server();
    }

    @Override
    public void onWebsocketHandshakeReceivedAsClient(WebSocket conn, ClientHandshake request,
                                                     ServerHandshake response) throws InvalidDataException {
        //To overwrite
    }

    /**
     * This default implementation does not do anything which will cause the connections to
     * always progress.
     *
     * @see com.analysys.visual.websocket.WebSocketListener#onWebsocketHandshakeSentAsClient(WebSocket, ClientHandshake)
     */
    @Override
    public void onWebsocketHandshakeSentAsClient(WebSocket conn, ClientHandshake request) throws InvalidDataException {
        //To overwrite
    }

    /**
     * This default implementation does not do anything. Go ahead and overwrite it
     *
     * @see com.analysys.visual.websocket.WebSocketListener#onWebsocketMessageFragment(WebSocket, Framedata)
     */
    @Override
    @Deprecated
    public void onWebsocketMessageFragment(WebSocket conn, Framedata frame) {
        //To overwrite
    }

    /**
     * This default implementation will send a pong in response to the received ping.
     * The pong frame will have the same payload as the ping frame.
     *
     * @see com.analysys.visual.websocket.WebSocketListener#onWebsocketPing(WebSocket, Framedata)
     */
    @Override
    public void onWebsocketPing(WebSocket conn, Framedata f) {
        conn.sendFrame(new PongFrame((PingFrame) f));
    }

    /**
     * This default implementation does not do anything. Go ahead and overwrite it.
     *
     * @see com.analysys.visual.websocket.WebSocketListener#onWebsocketPong(WebSocket, Framedata)
     */
    @Override
    public void onWebsocketPong(WebSocket conn, Framedata f) {
        //To overwrite
    }
}
