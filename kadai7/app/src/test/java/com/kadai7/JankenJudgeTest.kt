package com.kadai7

import com.kadai7.domain.JankenJudge
import com.kadai7.model.JankenHand
import com.kadai7.model.JankenHand.PAPER
import com.kadai7.model.JankenHand.ROCK
import com.kadai7.model.JankenHand.SCISSORS
import com.kadai7.model.MatchOutcome
import com.kadai7.ui.JankenGameState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class JankenJudgeTest {

    @Test
    fun handEncoding_matchesSpecOneHotBits() {
        assertEquals(0b001, ROCK.bit)
        assertEquals(0b010, SCISSORS.bit)
        assertEquals(0b100, PAPER.bit)
    }

    @Test
    fun rotl3_rotatesWithinThreeBits() {
        // グー(001) が勝つ相手 -> チョキ(010)
        assertEquals(0b010, JankenJudge.rotl3(0b001))
        // チョキ(010) が勝つ相手 -> パー(100)
        assertEquals(0b100, JankenJudge.rotl3(0b010))
        // パー(100) が勝つ相手 -> グー(001)
        assertEquals(0b001, JankenJudge.rotl3(0b100))
    }

    @Test
    fun judge_matchesSpecExamples() {
        // [G, G, C, G] -> 勝者: index 0, 1, 3 (グー)
        val r1 = JankenJudge.judge(listOf(ROCK, ROCK, SCISSORS, ROCK))
        assertFalse(r1.isDraw)
        assertEquals(ROCK, r1.winnerHand)
        assertEquals(listOf(0, 1, 3), r1.winners)

        // [G, C, P] -> あいこ
        val r2 = JankenJudge.judge(listOf(ROCK, SCISSORS, PAPER))
        assertTrue(r2.isDraw)
        assertNull(r2.winnerHand)
        assertTrue(r2.winners.isEmpty())

        // [P, P, P] -> あいこ
        val r3 = JankenJudge.judge(listOf(PAPER, PAPER, PAPER))
        assertTrue(r3.isDraw)
        assertNull(r3.winnerHand)
        assertTrue(r3.winners.isEmpty())

        // [C, P, P] -> 勝者: index 0 (チョキ)
        val r4 = JankenJudge.judge(listOf(SCISSORS, PAPER, PAPER))
        assertFalse(r4.isDraw)
        assertEquals(SCISSORS, r4.winnerHand)
        assertEquals(listOf(0), r4.winners)
    }

    @Test
    fun judge_exhaustiveVerificationFor2To5Players() {
        for (playerCount in 2..5) {
            val allCombinations = generateCombinations(playerCount)
            for (hands in allCombinations) {
                val distinct = hands.toSet()
                val actual = JankenJudge.judge(hands)

                if (distinct.size == 1 || distinct.size == 3) {
                    assertTrue("Expected draw for $hands", actual.isDraw)
                    assertTrue(actual.winners.isEmpty())
                } else {
                    // distinct.size == 2
                    val expectedWinnerHand = when {
                        ROCK in distinct && SCISSORS in distinct -> ROCK
                        SCISSORS in distinct && PAPER in distinct -> SCISSORS
                        PAPER in distinct && ROCK in distinct -> PAPER
                        else -> error("Unreachable")
                    }
                    val expectedWinners = hands.mapIndexedNotNull { idx, h ->
                        if (h == expectedWinnerHand) idx else null
                    }
                    assertFalse("Expected non-draw for $hands", actual.isDraw)
                    assertEquals(expectedWinnerHand, actual.winnerHand)
                    assertEquals(expectedWinners, actual.winners)
                }
            }
        }
    }

    @Test
    fun evaluateForUser_returnsWinLoseDrawCorrectly() {
        // ユーザー(G) + CPU(C, G) -> ユーザーも勝ち手に含まれるので「勝ち」
        val winEval = JankenJudge.evaluateForUser(ROCK, listOf(SCISSORS, ROCK))
        assertEquals(MatchOutcome.WIN, winEval.userOutcome)
        assertEquals("勝ち", winEval.userOutcome.label)
        assertFalse(winEval.isCpuWinner(0))
        assertTrue(winEval.isCpuWinner(1))

        // ユーザー(P) + CPU(C, P) -> 勝ち手は C (CPU1のみ) なのでユーザーは「負け」
        val loseEval = JankenJudge.evaluateForUser(PAPER, listOf(SCISSORS, PAPER))
        assertEquals(MatchOutcome.LOSE, loseEval.userOutcome)
        assertEquals("負け", loseEval.userOutcome.label)
        assertTrue(loseEval.isCpuWinner(0))
        assertFalse(loseEval.isCpuWinner(1))

        // ユーザー(G) + CPU(C, P) -> 3すくみで「あいこ」
        val drawEval = JankenJudge.evaluateForUser(ROCK, listOf(SCISSORS, PAPER))
        assertEquals(MatchOutcome.DRAW, drawEval.userOutcome)
        assertEquals("あいこ", drawEval.userOutcome.label)
    }

    @Test
    fun gameState_managesPlayerCountSelectionAndPlayAgainFlow() {
        val state = JankenGameState(initialPlayerCount = 2, random = Random(42))
        assertEquals(2, state.playerCount)
        assertEquals(1, state.cpuCount)
        assertFalse(state.isResultVisible)
        assertNull(state.roundEvaluation)

        // 4人対戦を選択 -> ユーザー1 + CPU3
        state.selectPlayerCount(4)
        assertEquals(4, state.playerCount)
        assertEquals(3, state.cpuCount)
        assertNull(state.roundEvaluation)

        // ユーザーがカードを選択 -> CPU3人分の手が生成され結果算出
        state.selectHand(ROCK)
        assertTrue(state.isResultVisible)
        val firstEval = state.roundEvaluation!!
        assertEquals(ROCK, firstEval.userHand)
        assertEquals(3, firstEval.cpuHands.size)

        // 結果表示中に別のカードを押しても上書きされない
        state.selectHand(PAPER)
        assertEquals(firstEval, state.roundEvaluation)

        // 「もう一度」で選択前の状態に戻る（対戦人数4人は維持）
        state.playAgain()
        assertFalse(state.isResultVisible)
        assertNull(state.roundEvaluation)
        assertEquals(4, state.playerCount)
        assertEquals(3, state.cpuCount)
    }

    private fun generateCombinations(length: Int): List<List<JankenHand>> {
        if (length == 0) return listOf(emptyList())
        val prev = generateCombinations(length - 1)
        return prev.flatMap { prefix ->
            JankenHand.entries.map { hand -> prefix + hand }
        }
    }
}
