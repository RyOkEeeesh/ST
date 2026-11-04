package com.kadai7.domain

import com.kadai7.model.JankenHand
import com.kadai7.model.MatchOutcome

/**
 * ビットマスクによる多人数じゃんけん勝敗判定結果。
 *
 * @property isDraw あいこの場合は true
 * @property winnerHand 勝ち手（あいこの場合は null）
 * @property winners 勝者のインデックス一覧（あいこの場合は空リスト）
 */
data class JudgeResult(
    val isDraw: Boolean,
    val winnerHand: JankenHand?,
    val winners: List<Int>
)

/**
 * 1回のじゃんけん勝負の評価結果（ユーザー: index 0、CPU: index 1..N）。
 */
data class RoundEvaluation(
    val userHand: JankenHand,
    val cpuHands: List<JankenHand>,
    val judgeResult: JudgeResult,
    val userOutcome: MatchOutcome
) {
    fun isCpuWinner(cpuIndex: Int): Boolean {
        return !judgeResult.isDraw && (cpuIndex + 1) in judgeResult.winners
    }
}

object JankenJudge {
    private const val ALL_HANDS_MASK = 0b111

    fun rotl3(x: Int): Int = ((x shl 1) or (x shr 2)) and ALL_HANDS_MASK

    fun judge(hands: List<JankenHand>): JudgeResult {
        if (hands.isEmpty()) {
            return JudgeResult(isDraw = true, winnerHand = null, winners = emptyList())
        }

        val mask = hands.fold(0) { acc, hand -> acc or hand.bit }

        val isOneBit = (mask and (mask - 1)) == 0
        if (isOneBit || mask == ALL_HANDS_MASK) {
            return JudgeResult(isDraw = true, winnerHand = null, winners = emptyList())
        }

        val missing = ALL_HANDS_MASK xor mask
        val winnerBit = rotl3(missing)
        val winnerHand = JankenHand.fromBit(winnerBit)

        val winners = hands.mapIndexedNotNull { index, hand ->
            if (hand.bit == winnerBit) index else null
        }

        return JudgeResult(
            isDraw = false,
            winnerHand = winnerHand,
            winners = winners
        )
    }

    fun evaluateForUser(userHand: JankenHand, cpuHands: List<JankenHand>): RoundEvaluation {
        val allHands = buildList(cpuHands.size + 1) {
            add(userHand)
            addAll(cpuHands)
        }
        val result = judge(allHands)
        val outcome = when {
            result.isDraw -> MatchOutcome.DRAW
            0 in result.winners -> MatchOutcome.WIN
            else -> MatchOutcome.LOSE
        }
        return RoundEvaluation(
            userHand = userHand,
            cpuHands = cpuHands,
            judgeResult = result,
            userOutcome = outcome
        )
    }
}
