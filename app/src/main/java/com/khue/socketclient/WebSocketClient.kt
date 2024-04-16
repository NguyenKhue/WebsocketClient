package com.khue.socketclient

import android.content.Context
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.wss
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.security.KeyStore
import javax.net.ssl.SSLContext
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

class WebSocketClient(private val url: String, private val context: Context) {

    fun getKeyStore(): KeyStore {
        val keyStoreFile = FileInputStream(File(context.filesDir, "keystore.jks"))
        val keyStorePassword = "123456".toCharArray()
        val keyStore: KeyStore = KeyStore.getInstance(KeyStore.getDefaultType())
        keyStore.load(keyStoreFile, keyStorePassword)
        return keyStore
    }

    fun getTrustManagerFactory(): TrustManagerFactory? {
        val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        trustManagerFactory.init(getKeyStore())
        return trustManagerFactory
    }

    fun getTrustManager(): X509TrustManager {
        return getTrustManagerFactory()?.trustManagers?.first { it is X509TrustManager } as X509TrustManager
    }

    fun getSslContext(): SSLContext? {
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, getTrustManagerFactory()?.trustManagers, null)
        return sslContext
    }

    private val client = HttpClient(CIO) {
        install(WebSockets)
        engine {
            https {
                trustManager = getTrustManager()
            }
        }
    }

    fun connect(listener: WebSocketListener) {
        GlobalScope.launch {
            client.wss(url) {
                listener.onConnected()

                try {
                    for (frame in incoming) {
                        if (frame is Frame.Text) {
                            listener.onMessage(frame.readText())
                        }
                    }
                } catch (e: Exception) {
                    listener.onDisconnected()
                }
            }
        }
    }

    fun disconnect() {
        client.close()
    }
}

interface WebSocketListener {
    fun onConnected()
    fun onMessage(message: String)
    fun onDisconnected()
}