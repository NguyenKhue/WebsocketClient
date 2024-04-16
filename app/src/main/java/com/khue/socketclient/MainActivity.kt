package com.khue.socketclient

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.khue.socketclient.ui.theme.SocketClientTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity(), WebSocketListener {
    private val webSocketClient by lazy {
        WebSocketClient("wss://10.1.140.124:6868/chat", this)
    }

    val messageList = mutableStateListOf<String>()
    var isConnected by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // connect to websocket server
        setContent {
            SocketClientTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    var text by remember {
                        mutableStateOf("")
                    }
                    var ip by remember {
                        mutableStateOf("")
                    }

                    Box {
                        LazyColumn {
                            items(messageList) {
                                Text(text = it)
                            }
                        }


                        Row(
                            Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                        ) {
                            if (isConnected) {
                                TextField(
                                    modifier = Modifier.weight(1f),
                                    value = text,
                                    onValueChange = { text = it },
                                    placeholder = { Text(text = " Type message") }
                                )
                                Button(onClick = {}) {
                                    Text(text = "Send")
                                }
                            } else {
                                val scope = rememberCoroutineScope()
                                TextField(
                                    modifier = Modifier.weight(1f),
                                    value = ip,
                                    onValueChange = { ip = it },
                                    placeholder = { Text(text = " Type server ip") }
                                )
                                Button(onClick = {
                                    scope.launch {
                                        webSocketClient.connect(this@MainActivity)
                                        isConnected = true
                                    }
                                }) {
                                    Text(text = "connect")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onConnected() {

    }

    override fun onMessage(message: String) {
        Log.d("ChatWebSocketClient", message)
    }

    override fun onDisconnected() {

    }
}
