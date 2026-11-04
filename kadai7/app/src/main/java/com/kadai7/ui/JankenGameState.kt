package com.kadai7.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.kadai7.domain.JankenJudge
import com.kadai7.domain.RoundEvaluation
import com.kadai7.model.JankenHand
import kotlin.random.Random

/**
 * じゃんけん画面の状態管理クラス。
 * 対戦人数（2〜5人）の選択、ユーザーの手選択とCPUのランダム手生成、再戦リセットを管理する。
 */
@Stable
class JankenGameState(
    initialPlayerCount: Int = MIN_PLAYERS,
    initialEvaluation: RoundEvaluation? = null,
    private val random: Random = Random.Default
) {
    var playerCount: Int by mutableIntStateOf(initialPlayerCount.coerceIn(MIN_PLAYERS, MAX_PLAYERS))
        private set

    var roundEvaluation: RoundEvaluation? by mutableStateOf(initialEvaluation)
        private set

    val cpuCount: Int
        get() = playerCount - 1

    val isResultVisible: Boolean
        get() = roundEvaluation != null

    /**
     * 対戦人数を変更する（2〜5人）。人数変更時は進行中のラウンドをリセットする。
     */
    fun selectPlayerCount(count: Int) {
        val coerced = count.coerceIn(MIN_PLAYERS, MAX_PLAYERS)
        if (playerCount != coerced || roundEvaluation != null) {
            playerCount = coerced
            roundEvaluation = null
        }
    }

    /**
     * ユーザーがカードを選択したときの処理。
     * CPU全員の手をランダムに生成し、ビットマスク判定で勝敗を算出する。
     */
    fun selectHand(userHand: JankenHand) {
        if (roundEvaluation != null) return
        val cpuHands = List(cpuCount) { JankenHand.random(random) }
        roundEvaluation = JankenJudge.evaluateForUser(
            userHand = userHand,
            cpuHands = cpuHands
        )
    }

    /**
     * 「もう一度」ボタン押下時に選択前の状態へ戻す。
     */
    fun playAgain() {
        roundEvaluation = null
    }

    companion object {
        const val MIN_PLAYERS = 2
        const val MAX_PLAYERS = 5
        val PLAYER_COUNT_OPTIONS: IntRange = MIN_PLAYERS..MAX_PLAYERS

        val Saver: Saver<JankenGameState, Any> = listSaver(
            save = { state ->
                val eval = state.roundEvaluation
                if (eval == null) {
                    listOf(state.playerCount, -1)
                } else {
                    buildList {
                        add(state.playerCount)
                        add(eval.userHand.ordinal)
                        eval.cpuHands.forEach { add(it.ordinal) }
                    }
                }
            },
            restore = { saved ->
                val playerCount = saved[0]
                val userHandOrdinal = saved[1]
                val restoredEval = if (userHandOrdinal >= 0) {
                    val hands = JankenHand.entries
                    val userHand = hands[userHandOrdinal]
                    val cpuHands = saved.drop(2).map { hands[it] }
                    JankenJudge.evaluateForUser(userHand, cpuHands)
                } else {
                    null
                }
                JankenGameState(
                    initialPlayerCount = playerCount,
                    initialEvaluation = restoredEval
                )
            }
        )
    }
}

@Composable
fun rememberJankenGameState(
    initialPlayerCount: Int = JankenGameState.MIN_PLAYERS
): JankenGameState {
    return rememberSaveable(saver = JankenGameState.Saver) {
        JankenGameState(initialPlayerCount = initialPlayerCount)
    }
}
