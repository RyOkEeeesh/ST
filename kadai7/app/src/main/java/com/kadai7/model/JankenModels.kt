package com.kadai7.model

import androidx.annotation.DrawableRes
import com.kadai7.R
import kotlin.random.Random

/**
 * じゃんけんの手を表す列挙型。
 * - グー: 001 (1)
 * - チョキ: 010 (2)
 * - パー: 100 (4)
 */
enum class JankenHand(
    val bit: Int,
    val label: String,
    @param:DrawableRes val drawableRes: Int
) {
    ROCK(bit = 0b001, label = "グー", drawableRes = R.drawable.g),
    SCISSORS(bit = 0b010, label = "チョキ", drawableRes = R.drawable.c),
    PAPER(bit = 0b100, label = "パー", drawableRes = R.drawable.p);

    companion object {
        fun fromBit(bit: Int): JankenHand = entries.first { it.bit == bit }

        fun random(random: Random = Random.Default): JankenHand {
            val all = entries
            return all[random.nextInt(all.size)]
        }
    }
}

/**
 * ユーザー視点での勝敗結果。
 */
enum class MatchOutcome(val label: String) {
    WIN("勝ち"),
    LOSE("負け"),
    DRAW("あいこ")
}
