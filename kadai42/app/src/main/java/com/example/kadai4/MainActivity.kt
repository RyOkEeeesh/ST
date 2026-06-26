package com.example.kadai4

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.maxLength
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.then
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
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

@Composable
fun App() {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val weight = rememberTextFieldState(initialText = "")
            val height = rememberTextFieldState(initialText = "")

            val floatInputTransformation = InputTransformation
                .maxLength(5)
                .then {
                    // 小数点1桁までのFloatを許可する正規表現
                    val regex = Regex("""^(\d*\.?\d{0,1})$""")

                    // 条件にマッチしない場合は入力をリバート（無効化）する
                    if (!regex.matches(asCharSequence())) {
                        revertAllChanges()
                    }
                }

            TextField(
                state = weight,
                lineLimits = TextFieldLineLimits.SingleLine,
                label = { Text("体重(kg)") },
                inputTransformation = floatInputTransformation,
                suffix = {
                    Text("kg")
                }
            )

            TextField(
                state = height,
                lineLimits = TextFieldLineLimits.SingleLine,
                label = { Text("身長(cm)") },
                inputTransformation = floatInputTransformation,
                suffix = {
                    Text("cm")
                }
            )
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