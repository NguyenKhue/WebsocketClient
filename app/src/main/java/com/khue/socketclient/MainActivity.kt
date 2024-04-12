package com.khue.socketclient

import android.os.Bundle
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
import androidx.compose.runtime.LaunchedEffect
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
import java.net.URI

class MainActivity : ComponentActivity() {
    private lateinit var webSocketClient: ChatWebSocketClient

    val messageList = mutableStateListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val serverUri = URI("ws://10.1.141.187:6868/chat")
        webSocketClient = ChatWebSocketClient(serverUri) { message ->
            messageList.add(message)
        }
        // connect to websocket server
        setContent {
            SocketClientTheme {
                LaunchedEffect(key1 = Unit) {
                    webSocketClient.connect()
                }
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    var text by remember {
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
                            TextField(value = text, onValueChange = { text = it })
                            Button(onClick = { webSocketClient.sendMessage(text) }) {
                                Text(text = "Send Message")
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // close websocket connection
        webSocketClient.close()
    }
}
