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
            // 数字と小数点をまとめて1つの数値トークンとして扱う
            // （以前は "1.5" が "1" と ".5" の2トークンに分割されてしまい、
            //   小数点を含む計算が必ずエラーになっていた）
            ch.isDigit() || ch == '.' -> {
                var num = ""
                var dotCount = 0
                while (i < input.length && (input[i].isDigit() || input[i] == '.')) {
                    if (input[i] == '.') {
                        dotCount++
                        if (dotCount > 1) break // 2個目以降の"."はこの数値に含めない
                    }
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

// 数値を表示用文字列に整形（小数点以下が.0の場合は整数表示、それ以外はそのまま）
fun formatNumber(value: Double): String {
    return if (value == value.toLong().toDouble()) {
        value.toLong().toString()
    } else {
        value.toString()
    }
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
        formatNumber(result)
    } else {
        "エラー"
    }
}

// ============================================================
// フォントサイズ自動調整
// ============================================================

// 文字数に応じてフォントサイズを段階的に縮小し、1行に収まるようにする
fun autoFontSize(text: String, maxSize: Int, minSize: Int): androidx.compose.ui.unit.TextUnit {
    val length = text.length
    val size = when {
        length <= 12 -> maxSize
        length <= 16 -> (maxSize * 0.8).toInt()
        length <= 20 -> (maxSize * 0.65).toInt()
        length <= 24 -> (maxSize * 0.5).toInt()
        else -> minSize
    }
    return size.coerceAtLeast(minSize).sp
}

// ============================================================
// UI
// ============================================================

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun CalculatorScreen() {
    var displayText by remember { mutableStateOf("0") }
    var expression by remember { mutableStateOf("") }
    // "="を押した直後の式表示（"1+2 = 3"のような履歴文字列）
    var historyText by remember { mutableStateOf("") }
    // "="を押した直後かどうか。true の間に数字などを押すと新しい計算として入力し直す
    var justCalculated by remember { mutableStateOf(false) }

    // 式が空なら"AC"（オールクリア）、入力中なら"C"（現在の入力をクリア）に切り替える
    val clearLabel = if (expression.isEmpty()) "AC" else "C"

    val operatorTexts = setOf(
        CalcConstants.PLUS, CalcConstants.MINUS, CalcConstants.MULTIPLY, CalcConstants.DIVIDE
    )

    // 入力中の式をUI記号→計算用記号に変換するヘルパー
    fun toCalcSymbols(expr: String): String =
        expr.replace(CalcConstants.MULTIPLY, CalcConstants.CALC_MULTIPLY)
            .replace(CalcConstants.DIVIDE, CalcConstants.CALC_DIVIDE)

    // 計算中の式に対する結果のライブプレビュー（計算できない途中の式なら空文字）
    val livePreview: String =
        if (!justCalculated && expression.isNotEmpty()) {
            val preview = calculateExpression(toCalcSymbols(expression))
            if (preview == "エラー") "" else preview
        } else {
            ""
        }

    // 上段に出す文字列：="直後は履歴、入力中はライブプレビュー（結果が出せる時だけ）
    val topText = if (justCalculated) historyText else livePreview

    val buttons = listOf(
        "${CalcConstants.LEFT_PAREN}${CalcConstants.RIGHT_PAREN}",
        clearLabel,
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
            // 上段：="の後は履歴（式 = 結果）、入力中は結果が出せる時だけプレビュー表示
            Text(
                text = topText,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = autoFontSize(topText, maxSize = 18, minSize = 12),
                    color = if (justCalculated)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End,
                maxLines = 1
            )
            // 下段：入力中の式、または="の後は結果（長くなったら縮小しつつ1行に収める）
            Text(
                text = displayText,
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = autoFontSize(displayText, maxSize = 48, minSize = 20)
                ),
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
                            clearLabel -> {
                                // AC/Cどちらでも今は同じ全クリア動作（表示上のラベルだけ切り替わる）
                                expression = ""
                                displayText = "0"
                                historyText = ""
                                justCalculated = false
                            }
                            CalcConstants.BACKSPACE -> {
                                if (justCalculated) {
                                    // 結果表示直後のバックスペースは全クリア扱いにする
                                    expression = ""
                                    displayText = "0"
                                    historyText = ""
                                    justCalculated = false
                                } else if (expression.isNotEmpty()) {
                                    expression = expression.dropLast(1)
                                    displayText = if (expression.isEmpty()) "0" else expression
                                }
                            }
                            CalcConstants.EQUALS -> {
                                if (expression.isNotEmpty()) {
                                    // UI記号（×÷）を計算用記号（*/）に変換
                                    val result = calculateExpression(toCalcSymbols(expression))
                                    historyText = expression
                                    displayText = result
                                    justCalculated = true
                                }
                            }
                            CalcConstants.PERCENT -> {
                                if (justCalculated) {
                                    // 結果表示直後に%を押したら、結果を%変換して新しい計算にする
                                    val v = displayText.toDoubleOrNull()
                                    expression = if (v != null) formatNumber(v / 100) else ""
                                    justCalculated = false
                                } else {
                                    // 式の末尾にある数値だけを%（÷100）に変換する
                                    var i = expression.length
                                    while (i > 0 && (expression[i - 1].isDigit() || expression[i - 1] == '.')) i--
                                    val trailingNumber = expression.substring(i)
                                    val v = trailingNumber.toDoubleOrNull()
                                    if (v != null) {
                                        expression = expression.substring(0, i) + formatNumber(v / 100)
                                    }
                                    // 末尾が数値でない（演算子や開き括弧の直後など）場合は何もしない
                                }
                                displayText = expression.ifEmpty { "0" }
                            }
                            "${CalcConstants.LEFT_PAREN}${CalcConstants.RIGHT_PAREN}" -> {
                                // 結果表示直後にかっこを押したら新しい計算として開始
                                if (justCalculated) {
                                    expression = ""
                                    justCalculated = false
                                }
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
                            in operatorTexts -> {
                                if (justCalculated) {
                                    // 結果に続けて演算子を押した場合は、結果の値から計算を続ける
                                    expression = displayText + buttonText
                                } else {
                                    expression += buttonText
                                }
                                displayText = expression
                                justCalculated = false
                            }
                            else -> {
                                // 数字、小数点など
                                if (justCalculated) {
                                    // 結果表示後に数字などを押したら新しい計算として入力し直す
                                    expression = ""
                                    justCalculated = false
                                }
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