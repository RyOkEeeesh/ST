package com.example.st31_kadai02_ih13a_kaji_compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.example.st31_kadai02_ih13a_kaji_compose.ui.theme.ST31_kadai02_IH13A_kaji_composeTheme
import kotlinx.coroutines.launch
import kotlin.math.hypot
import kotlin.random.Random
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.toArgb

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ST31_kadai02_IH13A_kaji_composeTheme {
                App()
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun App() {
    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .clipToBounds()
        ) {
            Content(name = "Android")
        }
    }
}

@Composable
fun Content(name: String, modifier: Modifier = Modifier) {
    var currentBgColor by remember { mutableStateOf(Color.White) }
    var nextColor by remember { mutableStateOf(Color.White) }

    val animatedProgress = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(currentBgColor),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerOffset = Offset(size.width / 2, size.height / 2)
            val maxRadius = hypot(size.width, size.height) / 2
            val currentRadius = maxRadius * animatedProgress.value

            drawCircle(
                color = nextColor,
                radius = currentRadius,
                center = centerOffset
            )
        }

        Button(onClick = {
            if (!animatedProgress.isRunning) {
                nextColor = getRandomColor()

                scope.launch {
                    animatedProgress.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(durationMillis = 600)
                    )
                    currentBgColor = nextColor
                    animatedProgress.snapTo(0f)
                }
            }
        }) {
            Text(colorToHexString(nextColor))
        }
    }
}
fun getRandomColor(): Color {
    return Color(
        red = Random.nextFloat(),
        green = Random.nextFloat(),
        blue = Random.nextFloat(),
        alpha = 1.0f
    )
}fun colorToHexString(color: Color): String {
    val argb = color.toArgb()
    // 「and 0xFFFFFF」で透明度(Alpha)の情報を除外して、小文字の6桁（%06x）にします
    return String.format("#%06x", argb and 0xFFFFFF)
}
