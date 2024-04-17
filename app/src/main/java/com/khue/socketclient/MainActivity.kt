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
import java.io.ByteArrayInputStream
import java.net.URI
import java.security.KeyStore
import java.security.cert.CertificateFactory
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLHandshakeException
import javax.net.ssl.SSLSocket
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager


val crt = "-----BEGIN CERTIFICATE-----\n" +
        "MIID2TCCAsGgAwIBAgIUY34cAQzIs2ZFtMC9H8xkHkypw40wDQYJKoZIhvcNAQEL\n" +
        "BQAwfDELMAkGA1UEBhMCVk4xDDAKBgNVBAgMA0hDTTEMMAoGA1UEBwwDSENNMQsw\n" +
        "CQYDVQQKDAJTUzELMAkGA1UECwwCc2ExDzANBgNVBAMMBmVwYXBlcjEmMCQGCSqG\n" +
        "SIb3DQEJARYXMDkwMzIwMDFraHVuZ0BnbWFpbC5jb20wHhcNMjQwNDE1MTczNTIw\n" +
        "WhcNMjkwNDE0MTczNTIwWjB8MQswCQYDVQQGEwJWTjEMMAoGA1UECAwDSENNMQww\n" +
        "CgYDVQQHDANIQ00xCzAJBgNVBAoMAlNTMQswCQYDVQQLDAJzYTEPMA0GA1UEAwwG\n" +
        "ZXBhcGVyMSYwJAYJKoZIhvcNAQkBFhcwOTAzMjAwMWtodW5nQGdtYWlsLmNvbTCC\n" +
        "ASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAJbVni9kt04YUxtUO1u+cbbh\n" +
        "/mYwbA9vVnHZRqp8bUCiTHR8OkVHo/ShiagUt2XfGyUVLUtCwoFdIoMRb5PBxY1X\n" +
        "1zshZI5zKWU6CrgF4fb2xWjvm0tqLcrviffMT51vnohUGuUO6rMsZHPbToXpOY3R\n" +
        "Hwh/7Z+NeeRPq8J6tMQM3Yq1y1SyyBlTogj6EWXPuud8M8BcvSNYQH26lQTsOJxo\n" +
        "4unISol1UOumFEylAUXZWPt8PYvyOXEcK3fQfsA8HHI/wmxB5EieEi8s2/fAAuam\n" +
        "6vuLERZcGeakcsfkwOkqFeGxzzyaC0Xt/NDxuTAyLMMNTr9R4dbvebcuKB9Ud+0C\n" +
        "AwEAAaNTMFEwHQYDVR0OBBYEFCryj4bm1Y3/8/CblVEls+mbHJXcMB8GA1UdIwQY\n" +
        "MBaAFCryj4bm1Y3/8/CblVEls+mbHJXcMA8GA1UdEwEB/wQFMAMBAf8wDQYJKoZI\n" +
        "hvcNAQELBQADggEBAF4b+1PwU42fWBFo+ACnWVd0HQq7V92kX44LCKdjaZM71XIt\n" +
        "vcptTg1aEolyVSH2j4Ni+3uxiOzXVxY2c75o0C1w/N5Y3jRBzqnMTiw6l7AUzbdw\n" +
        "eM6veos4UCw5l1ALhquuLzCT7acGYD0iWk6QRQpn5cGuTQlAAFNNMGzm2l3pxvCW\n" +
        "UWSM2uE665fhqDQET0lLs/FGmGK3V9DSIWo7LB/cr4cp0tFFyOk6Qm3yu+bm4jLF\n" +
        "kIJG9rhcxvl28Y20ZPZadsWbKLlVv413su8ThmnekM8yXDPxp8Eh504r7miamGf7\n" +
        "dF0faRatl0DlAITKv0NMjVe5/QRKnZXLB8MpLmU=\n" +
        "-----END CERTIFICATE-----"

class MainActivity : ComponentActivity() {
    private lateinit var webSocketClient: ChatWebSocketClient

    val messageList = mutableStateListOf<String>()
    var isConnected by mutableStateOf(false)

    fun connectSocket(ip: String) {
        val serverUri = URI("wss://$ip:8002/chat")
        webSocketClient = ChatWebSocketClient(serverUri, onSocketClose = {
            isConnected = false
        }, messageListener = { message ->
            messageList.add(message)
        })
        webSocketClient.setSocketFactory(getSslContext()?.socketFactory)
        webSocketClient.connectBlocking()
    }

    fun getKeyStore(): KeyStore {
        val certificateFactory = CertificateFactory.getInstance("X509")
        val epaperCA = certificateFactory.generateCertificate(ByteArrayInputStream(crt.toByteArray()))
        val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
        keyStore.load(null, null)
        keyStore.setCertificateEntry("khue", epaperCA)
        return keyStore
    }

    fun getTrustManagerFactory(): TrustManagerFactory? {
        val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        trustManagerFactory.init(getKeyStore())
        return trustManagerFactory
    }

    fun getSslContext(): SSLContext? {
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, getTrustManagerFactory()?.trustManagers, null)
        return sslContext
    }

    fun getTrustManager(): X509TrustManager {
        return getTrustManagerFactory()?.trustManagers?.first { it is X509TrustManager } as X509TrustManager
    }

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
                        mutableStateOf("10.1.141.187")
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
                                Button(onClick = { webSocketClient.sendMessage(text) }) {
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
                                        connectSocket(ip)
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

    override fun onDestroy() {
        super.onDestroy()
        // close websocket connection
        webSocketClient.close()
    }
}
