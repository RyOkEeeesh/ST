package com.kadai7

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.kadai7.ui.JankenGameState
import com.kadai7.ui.JankenScreen
import com.kadai7.ui.theme.Kadai7Theme
import org.junit.Rule
import org.junit.Test
import kotlin.random.Random

class JankenScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun fullJankenFlow_selectPlayers_selectCard_showResult_andPlayAgain() {
        val state = JankenGameState(initialPlayerCount = 2, random = Random(7))
        composeRule.setContent {
            Kadai7Theme(darkTheme = false, dynamicColor = false) {
                JankenScreen(gameState = state)
            }
        }

        // Header logo と対戦人数 2〜5 が表示されている
        composeRule.onNodeWithTag("header_logo").assertIsDisplayed()
        composeRule.onNodeWithTag("player_count_2").assertIsSelected()
        composeRule.onNodeWithTag("cpu_slot_1").assertIsDisplayed()

        // 対戦人数を 4 に変更 -> CPU 1〜3 が表示される
        composeRule.onNodeWithTag("player_count_4").performClick()
        composeRule.onNodeWithTag("player_count_4").assertIsSelected()
        composeRule.onNodeWithTag("cpu_slot_1").assertIsDisplayed()
        composeRule.onNodeWithTag("cpu_slot_2").assertIsDisplayed()
        composeRule.onNodeWithTag("cpu_slot_3").assertIsDisplayed()

        // 画面下部のグーのカードを選択
        composeRule.onNodeWithTag("hand_card_ROCK").performClick()

        // ユーザーの選んだ手、勝敗結果、「もう一度」ボタンが表示される
        composeRule.onNodeWithTag("user_played_card").assertIsDisplayed()
        composeRule.onNodeWithTag("result_text")
            .assertIsDisplayed()
            .assertTextEquals(state.roundEvaluation!!.userOutcome.label)
        composeRule.onNodeWithTag("play_again_button").assertIsDisplayed()

        // 「もう一度」ボタンを押すと選択前の状態に戻る
        composeRule.onNodeWithTag("play_again_button").performClick()
        composeRule.onNodeWithTag("play_again_button").assertDoesNotExist()
    }
}
