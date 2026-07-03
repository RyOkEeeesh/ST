package com.example.kadai4

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.maxLength
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.then
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kadai4.ui.theme.Kadai4Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Kadai4Theme {
                App()
            }
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun App() {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp), // 全体にゆとりのある余白を追加
            verticalArrangement = Arrangement.spacedBy(16.dp) // 各要素の間に一律で隙間を空ける
        ) {
            Text(
                text = "BMI 計算",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            val height = rememberTextFieldState(initialText = "")
            val weight = rememberTextFieldState(initialText = "")

            val h: Float = (height.text.toString().toFloatOrNull() ?: 0.0f) / 100
            val w: Float = weight.text.toString().toFloatOrNull() ?: 0.0f

            val bmi = if (h > 0 && w > 0) w / (h * h) else Float.NaN

            val imgs = listOf(
                R.drawable.f0,
                R.drawable.f1,
                R.drawable.f2,
            )

            val floatInputTransformation = InputTransformation
                .maxLength(5)
                .then {
                    val regex = Regex("""^(\d*\.?\d{0,1})$""")
                    if (!regex.matches(asCharSequence())) {
                        revertAllChanges()
                    }
                }

            val keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal
            )

            OutlinedTextField(
                state = height,
                lineLimits = TextFieldLineLimits.SingleLine,
                label = { Text("身長") },
                inputTransformation = floatInputTransformation,
                suffix = { Text("cm") },
                keyboardOptions = keyboardOptions,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                state = weight,
                lineLimits = TextFieldLineLimits.SingleLine,
                label = { Text("体重") },
                inputTransformation = floatInputTransformation,
                suffix = { Text("kg") },
                keyboardOptions = keyboardOptions,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            val (img, result) = when {
                bmi.isNaN() -> null to ""
                bmi < 18.5 -> imgs[0] to "低体重"
                bmi < 25.0 -> imgs[1] to "普通体重"
                bmi < 30.0 -> imgs[2] to "肥満（1度）"
                bmi < 35.0 -> imgs[2] to "肥満（2度）"
                bmi < 40.0 -> imgs[2] to "肥満（3度）"
                else       -> imgs[2] to "肥満（4度）"
            }

            if (!bmi.isNaN()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "あなたのBMI",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = String.format("%.1f", bmi),
                                fontSize = 42.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = result,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // 右側：画像表示（正方形にクリップして綺麗に配置）
                        if (img != null) {
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            ) {
                                Image(
                                    painter = painterResource(id = img),
                                    contentDescription = "結果のイラスト",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AppPreview() {
    Kadai4Theme {
        App()
    }
}