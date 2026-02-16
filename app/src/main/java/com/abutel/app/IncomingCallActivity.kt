package com.abutel.app

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abutel.app.ui.theme.AbutelTheme
import kotlinx.coroutines.delay

class IncomingCallActivity : ComponentActivity() {

    private var callService: CallService? = null
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as CallService.LocalBinder
            callService = binder.getService()
        }
        override fun onServiceDisconnected(name: ComponentName?) {
            callService = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        Intent(this, CallService::class.java).also { intent ->
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }

        val callerName = intent.getStringExtra("CALLER_NAME") ?: "Desconocido"

        setContent {
            AbutelTheme {
                IncomingCallScreen(
                    callerName = callerName,
                    onAccept = { acceptCall() },
                    onReject = { rejectCall() }
                )
            }
        }
    }

    private fun acceptCall() {
        callService?.answerCall()
        finish()
    }

    private fun rejectCall() {
        callService?.hangUp()
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(serviceConnection)
    }
}

@Composable
fun IncomingCallScreen(
    callerName: String,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    var countdown by remember { mutableStateOf(5) }

    LaunchedEffect(Unit) {
        while (countdown > 0) {
            delay(1000)
            countdown--
        }
        onAccept()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = callerName.first().toString(),
                    color = Color(0xFF0F172A),
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = callerName,
                color = Color.White,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Llamada entrante...",
                color = Color(0xFF94A3B8),
                fontSize = 24.sp
            )

            if (countdown > 0) {
                Text(
                    text = "Auto-contestando en $countdown",
                    color = Color(0xFF2DD4BF),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(48.dp),
                modifier = Modifier.padding(top = 32.dp)
            ) {
                FloatingActionButton(
                    onClick = onReject,
                    modifier = Modifier.size(80.dp),
                    containerColor = Color.Red,
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Default.CallEnd,
                        contentDescription = "Colgar",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }

                FloatingActionButton(
                    onClick = onAccept,
                    modifier = Modifier.size(80.dp),
                    containerColor = Color.Green,
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Contestar",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        }
    }
}
