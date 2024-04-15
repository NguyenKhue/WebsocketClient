package com.khue.socketclient

import android.util.Log
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI

class ChatWebSocketClient(serverUri: URI, private val messageListener: (String) -> Unit,private val onSocketClose: () -> Unit) : WebSocketClient(serverUri) {

    override fun onOpen(handshakedata: ServerHandshake?) {
        // When WebSocket connection opened
        Log.d("ChatWebSocketClient", "onOpen")
    }

    override fun onClose(code: Int, reason: String?, remote: Boolean) {
        // When WebSocket connection closed
        Log.d("ChatWebSocketClient", "onClose")
        onSocketClose()
    }

    override fun onMessage(message: String?) {
        // When Receive a message we handle it at MainActivity
        messageListener.invoke(message ?: "")
    }

    override fun onError(ex: Exception?) {
        Log.e("ChatWebSocketClient", "err: ${ex?.message}")
    }

    fun sendMessage(message: String) {
        send(message)
    }
}