package com.example.kadai05_calc

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kadai05_calc.ui.theme.Kadai05_calcTheme

// ============================================================
// 定数定義
// ============================================================
object CalcConstants {
    const val PLUS = "+"
    const val MINUS = "-"
    const val MULTIPLY = "×"
    const val DIVIDE = "÷"
    const val EQUALS = "="
    const val CLEAR = "C"
    const val BACKSPACE = "⌫"
    const val DECIMAL = "."
    const val PERCENT = "%"
    const val LEFT_PAREN = "("
    const val RIGHT_PAREN = ")"

    // 計算用演算子記号（内部計算・トークン比較用）
    const val CALC_PLUS = "+"
    const val CALC_MINUS = "-"
    const val CALC_MULTIPLY = "*"
    const val CALC_DIVIDE = "/"

    // UI表示用記号 → 計算用記号 のマッピング
    val operatorMap = mapOf(
        MULTIPLY to CALC_MULTIPLY,
        DIVIDE to CALC_DIVIDE,
        PLUS to CALC_PLUS,
        MINUS to CALC_MINUS
    )

    // 計算エンジンで使う演算子一覧・優先順位
    val calcOperators = listOf(CALC_PLUS, CALC_MINUS, CALC_MULTIPLY, CALC_DIVIDE)
    val precedence = mapOf(
        CALC_PLUS to 1,
        CALC_MINUS to 1,
        CALC_MULTIPLY to 2,
        CALC_DIVIDE to 2
    )
}

// ============================================================
// MainActivity
// ============================================================
class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Kadai05_calcTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { _ ->
                    CalculatorScreen()
                }
            }
        }
    }
}

// ============================================================
// 計算エンジン
// ============================================================

// 1. 文字列をトークンに分解
fun tokenize(input: String): List<String> {
    val result = mutableListOf<String>()
    var i = 0
    while (i < input.length) {
        val ch = input[i]
        when {
            ch.isDigit() -> {
                var num = ""
                while (i < input.length && input[i].isDigit()) {
                    num += input[i]
                    i++
                }
                result.add(num)
                continue
            }
            ch == '.' -> {
                var num = "."
                i++
                while (i < input.length && input[i].isDigit()) {
                    num += input[i]
                    i++
                }
                result.add(num)
                continue
            }
            ch in "${CalcConstants.CALC_PLUS}${CalcConstants.CALC_MINUS}${CalcConstants.CALC_MULTIPLY}${CalcConstants.CALC_DIVIDE}${CalcConstants.LEFT_PAREN}${CalcConstants.RIGHT_PAREN}" -> {
                result.add(ch.toString())
            }
            ch.isWhitespace() -> { /* 無視 */ }
            else -> {
                // 想定外の文字はとりあえず無視（またはエラーとして扱う）
                result.add("?")
            }
        }
        i++
    }
    return result
}

// 2. 中置記法 → RPN変換（Shunting Yard）
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
fun infixToRPN(tokens: List<String>): List<String> {
    val output = mutableListOf<String>()
    val operators = mutableListOf<String>()

    val precedence = CalcConstants.precedence

    for (token in tokens) {
        when {
            token.toDoubleOrNull() != null -> {
                output.add(token)
            }
            token == "(" -> {
                operators.add(token)
            }
            token == ")" -> {
                while (operators.isNotEmpty() && operators.last() != "(") {
                    output.add(operators.removeLast())
                }
                if (operators.isNotEmpty() && operators.last() == "(") {
                    operators.removeLast()
                }
            }
            token in precedence -> {
                while (
                    operators.isNotEmpty() &&
                    operators.last() != "(" &&
                    precedence[operators.last()]!! >= precedence[token]!!
                ) {
                    output.add(operators.removeLast())
                }
                operators.add(token)
            }
            else -> {
                // 未知のトークンは無視（またはエラー）
            }
        }
    }

    while (operators.isNotEmpty()) {
        output.add(operators.removeLast())
    }

    return output
}

// 3. RPN計算
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
fun calcRPN(tokens: List<String>): Double? {
    val stack = mutableListOf<Double>()

    for (token in tokens) {
        when {
            token.toDoubleOrNull() != null -> {
                stack.add(token.toDouble())
            }
            token in CalcConstants.calcOperators -> {
                if (stack.size < 2) return null
                val b = stack.removeLast()
                val a = stack.removeLast()
                val result = when (token) {
                    CalcConstants.CALC_PLUS -> a + b
                    CalcConstants.CALC_MINUS -> a - b
                    CalcConstants.CALC_MULTIPLY -> a * b
                    CalcConstants.CALC_DIVIDE -> {
                        if (b == 0.0) return null
                        a / b
                    }
                    else -> return null
                }
                stack.add(result)
            }
            else -> return null
        }
    }

    return if (stack.size == 1) stack.last() else null
}

// 4. メイン計算関数（UIで使う）
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
fun calculateExpression(expression: String): String {
    if (expression.isEmpty()) return "0"

    // 1. トークン化
    val tokens = tokenize(expression)
    // 2. RPN変換
    val rpn = infixToRPN(tokens)
    // 3. 計算
    val result = calcRPN(rpn)

    return if (result != null) {
        // 小数点以下が.0の場合は整数表示、それ以外はそのまま
        if (result == result.toLong().toDouble()) {
            result.toLong().toString()
        } else {
            result.toString()
        }
    } else {
        "エラー"
    }
}

// ============================================================
// UI
// ============================================================

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun CalculatorScreen() {
    var displayText by remember { mutableStateOf("0") }
    var expression by remember { mutableStateOf("") }

    val buttons = listOf(
        "${CalcConstants.LEFT_PAREN}${CalcConstants.RIGHT_PAREN}",
        CalcConstants.CLEAR,
        CalcConstants.BACKSPACE,
        CalcConstants.DIVIDE,
        "7", "8", "9", CalcConstants.MULTIPLY,
        "4", "5", "6", CalcConstants.MINUS,
        "1", "2", "3", CalcConstants.PLUS,
        CalcConstants.PERCENT, "0", CalcConstants.DECIMAL, CalcConstants.EQUALS
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Bottom
    ) {
        // --- ディスプレイエリア ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            // 入力中の式（小さく表示）
            Text(
                text = expression,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End,
                maxLines = 2
            )
            // 結果（大きく表示）
            Text(
                text = displayText,
                style = MaterialTheme.typography.headlineLarge.copy(fontSize = 48.sp),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End,
                maxLines = 1
            )
        }

        // --- キーパッドエリア ---
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(buttons) { buttonText ->
                CalculatorButton(
                    text = buttonText,
                    onClick = {
                        when (buttonText) {
                            CalcConstants.CLEAR -> {
                                expression = ""
                                displayText = "0"
                            }
                            CalcConstants.BACKSPACE -> {
                                if (expression.isNotEmpty()) {
                                    expression = expression.dropLast(1)
                                    displayText = if (expression.isEmpty()) "0" else expression
                                }
                            }
                            CalcConstants.EQUALS -> {
                                if (expression.isNotEmpty()) {
                                    // UI記号（×÷）を計算用記号（*/）に変換
                                    val calcExpression = expression
                                        .replace(CalcConstants.MULTIPLY, CalcConstants.CALC_MULTIPLY)
                                        .replace(CalcConstants.DIVIDE, CalcConstants.CALC_DIVIDE)
                                    val result = calculateExpression(calcExpression)
                                    displayText = result
                                    expression = "$expression = $result"
                                }
                            }
                            "${CalcConstants.LEFT_PAREN}${CalcConstants.RIGHT_PAREN}" -> {
                                // かっこボタン：文脈に応じて ( か ) を判断して追加
                                val openCount = expression.count { it == CalcConstants.LEFT_PAREN[0] }
                                val closeCount = expression.count { it == CalcConstants.RIGHT_PAREN[0] }
                                val lastChar = expression.lastOrNull()
                                val endsWithOperand = lastChar != null &&
                                        (lastChar.isDigit() || lastChar == CalcConstants.RIGHT_PAREN[0])

                                val shouldCloseParen = openCount > closeCount && endsWithOperand

                                if (shouldCloseParen) {
                                    // 開きかっこが閉じられていない状態で、直前が数値や)なら閉じかっこを追加
                                    expression += CalcConstants.RIGHT_PAREN
                                } else {
                                    // 暗黙の乗算をサポート（直前が数値や)なら*を自動挿入）
                                    if (endsWithOperand) {
                                        expression += CalcConstants.CALC_MULTIPLY
                                    }
                                    expression += CalcConstants.LEFT_PAREN
                                }
                                displayText = expression
                            }
                            else -> {
                                // 数字、演算子、小数点など
                                expression += buttonText
                                displayText = expression
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun CalculatorButton(
    text: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .aspectRatio(1f)
            .fillMaxWidth(),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 24.sp)
        )
    }
}

// ============================================================
// Preview
// ============================================================

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Kadai05_calcTheme {
        CalculatorScreen()
    }
}