package com.s2b.hayagriva

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import java.util.Locale

class MainActivity : ComponentActivity() {
    private var speechRecognizer: SpeechRecognizer? = null
    private var tts: TextToSpeech? = null

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.ENGLISH
            }
        }

        setContent {
            HayagrivaApp(
                onSpeak = { startListening() },
                onCamera = {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA))
                    }
                }
            )
        }
    }

    private fun startListening() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(arrayOf(Manifest.permission.RECORD_AUDIO))
            return
        }
        if (!SpeechRecognizer.isRecognitionAvailable(this)) return

        speechRecognizer?.destroy()
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this).also { recognizer ->
            recognizer.setRecognitionListener(object : android.speech.RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {}
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {}
                override fun onError(error: Int) {}
                override fun onResults(results: Bundle?) {
                    val text = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
                    if (!text.isNullOrBlank()) {
                        tts?.speak("I heard: $text", TextToSpeech.QUEUE_FLUSH, null, "hayagriva")
                    }
                }
                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
        speechRecognizer?.startListening(intent)
    }

    override fun onDestroy() {
        speechRecognizer?.destroy()
        tts?.shutdown()
        super.onDestroy()
    }
}

private val Background = Color(0xFF03050D)
private val Panel = Color(0xFF0B0F1B)
private val TextPrimary = Color(0xFFE9F0FF)
private val TextSecondary = Color(0xFF9DA7BC)
private val Cyan = Color(0xFF00D9FF)
private val Purple = Color(0xFFB45CFF)
private val Pink = Color(0xFFFF4F9A)

@Composable
fun HayagrivaApp(onSpeak: () -> Unit, onCamera: () -> Unit) {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = Background) {
            val transition = rememberInfiniteTransition(label = "orb")
            val pulse by transition.animateFloat(
                initialValue = 0.94f,
                targetValue = 1.06f,
                animationSpec = infiniteRepeatable(tween(1400, easing = LinearEasing), RepeatMode.Reverse),
                label = "pulse"
            )
            val glow by transition.animateFloat(
                initialValue = 0.45f,
                targetValue = 0.9f,
                animationSpec = infiniteRepeatable(tween(1100), RepeatMode.Reverse),
                label = "glow"
            )

            Column(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF02030A), Background)))) {
                Header()
                Row(modifier = Modifier.fillMaxSize()) {
                    SideBar()
                    Box(modifier = Modifier.weight(1f).fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            StatusChip("I'm listening...")
                            Spacer(Modifier.height(24.dp))
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(300.dp)) {
                                Box(Modifier.size(300.dp).scale(pulse).alpha(glow * 0.22f).background(Brush.radialGradient(listOf(Cyan, Purple, Color.Transparent)), RoundedCornerShape(150.dp)))
                                Image(
                                    painter = painterResource(R.drawable.hayagriva_orb),
                                    contentDescription = "S2B Hayagriva",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.size(230.dp).scale(pulse)
                                )
                            }
                            Spacer(Modifier.height(22.dp))
                            Waveform()
                            Spacer(Modifier.height(14.dp))
                            Text("Speak now... I'm here to help!", color = TextPrimary, fontSize = 17.sp)
                            Spacer(Modifier.height(28.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RoundButton(Icons.Default.Keyboard, "Keyboard") {}
                                Spacer(Modifier.size(22.dp))
                                SpeakButton(onSpeak)
                                Spacer(Modifier.size(22.dp))
                                RoundButton(Icons.Default.CameraAlt, "Camera", onCamera)
                            }
                        }
                    }
                    SuggestionPanel()
                }
            }
        }
    }
}

@Composable
private fun Header() {
    Row(Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 18.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Menu, null, tint = TextSecondary, modifier = Modifier.size(28.dp))
        Spacer(Modifier.size(16.dp))
        Surface(shape = RoundedCornerShape(30.dp), color = Panel) {
            Row(Modifier.padding(horizontal = 16.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(10.dp).background(Color(0xFF22DD77), RoundedCornerShape(10.dp)))
                Spacer(Modifier.size(8.dp))
                Text("Online", color = TextPrimary)
            }
        }
        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("S2B HAYAGRIVA", color = Cyan, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Text("AI Assistant", color = TextSecondary, fontSize = 14.sp)
        }
        Surface(shape = RoundedCornerShape(30.dp), color = Panel) {
            Row(Modifier.padding(horizontal = 14.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Language, null, tint = TextPrimary)
                Spacer(Modifier.size(7.dp))
                Text("English", color = TextPrimary)
            }
        }
        IconButton(onClick = {}) { Icon(Icons.Default.Person, null, tint = TextSecondary) }
    }
}

@Composable
private fun SideBar() {
    Column(Modifier.padding(start = 22.dp, top = 22.dp).size(width = 190.dp, height = 620.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        NavItem(Icons.Default.Home, "Home", true)
        NavItem(Icons.Default.ChatBubbleOutline, "Chat")
        NavItem(Icons.Default.Mic, "Voice")
        NavItem(Icons.Default.CameraAlt, "Vision")
        NavItem(Icons.Default.Build, "Tools")
        NavItem(Icons.Default.History, "History")
        NavItem(Icons.Default.Settings, "Settings")
    }
}

@Composable
private fun NavItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, selected: Boolean = false) {
    Surface(shape = RoundedCornerShape(20.dp), color = if (selected) Color(0xFF071927) else Color.Transparent) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 13.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = if (selected) Cyan else TextSecondary)
            Spacer(Modifier.size(16.dp))
            Text(label, color = if (selected) TextPrimary else TextSecondary, fontSize = 16.sp)
        }
    }
}

@Composable
private fun SuggestionPanel() {
    Surface(Modifier.padding(end = 22.dp).size(width = 270.dp, height = 510.dp), shape = RoundedCornerShape(25.dp), color = Panel) {
        Column(Modifier.padding(22.dp)) {
            Text("Status", color = TextSecondary)
            Spacer(Modifier.height(8.dp))
            Text("Listening", color = Cyan, fontSize = 20.sp)
            Spacer(Modifier.height(22.dp))
            Text("You can say", color = TextSecondary)
            Spacer(Modifier.height(12.dp))
            listOf("Hey Hayagriva", "Tell me a joke", "What's the weather?", "Solve this math problem", "Open YouTube").forEachIndexed { i, text ->
                Surface(Modifier.fillMaxWidth().padding(vertical = 4.dp), shape = RoundedCornerShape(15.dp), color = Color(0xFF101522)) {
                    Column(Modifier.padding(12.dp)) {
                        Text(text, color = TextPrimary, fontSize = 14.sp)
                        Text(if (i == 0) "Wake Word" else "Suggestion", color = TextSecondary, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusChip(text: String) {
    Surface(shape = RoundedCornerShape(30.dp), color = Panel) {
        Text(text, color = Cyan, fontSize = 18.sp, modifier = Modifier.padding(horizontal = 24.dp, vertical = 11.dp))
    }
}

@Composable
private fun Waveform() {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
        repeat(19) { index ->
            val h = listOf(5, 10, 7, 14, 8, 20, 10, 26, 12, 30, 13, 22, 9, 16, 7, 12, 5, 9, 4)[index]
            Box(Modifier.padding(horizontal = 2.dp).size(width = 3.dp, height = h.dp).background(Cyan, RoundedCornerShape(4.dp)))
        }
    }
}

@Composable
private fun RoundButton(icon: androidx.compose.ui.graphics.vector.ImageVector, description: String, onClick: () -> Unit) {
    Surface(shape = RoundedCornerShape(50.dp), color = Panel) {
        IconButton(onClick = onClick, modifier = Modifier.size(72.dp)) { Icon(icon, description, tint = TextPrimary, modifier = Modifier.size(28.dp)) }
    }
}

@Composable
private fun SpeakButton(onClick: () -> Unit) {
    Surface(shape = RoundedCornerShape(50.dp), color = Color(0xFF111321)) {
        Row(Modifier.padding(horizontal = 30.dp, vertical = 18.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onClick) { Icon(Icons.Default.Mic, "Speak", tint = Pink, modifier = Modifier.size(28.dp)) }
            Text("Tap to Speak", color = TextPrimary, fontSize = 17.sp)
        }
    }
}
