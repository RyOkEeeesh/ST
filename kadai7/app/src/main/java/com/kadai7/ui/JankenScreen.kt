package com.kadai7.ui

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import com.kadai7.R
import com.kadai7.domain.JankenJudge
import com.kadai7.domain.RoundEvaluation
import com.kadai7.model.JankenHand
import com.kadai7.model.MatchOutcome
import com.kadai7.ui.theme.Kadai7Theme

// じゃんけんカード画像 (360x500) のアスペクト比
private const val CARD_ASPECT_RATIO = 0.72f

// カイジ風ダークカードと和モダン卓上カラーパレット
private val TableBackgroundTop = Color(0xFFF7F4EE)
private val TableBackgroundBottom = Color(0xFFEBE5D8)
private val HeaderSurfaceColor = Color(0xFFFFFFFF)
private val CardDarkBg = Color(0xFF231815)
private val CardInnerBorder = Color(0xFF8D6E63)
private val AccentCrimson = Color(0xFFC62828)
private val WinnerGold = Color(0xFFFFB300)
private val WinnerGoldLight = Color(0xFFFFF8E1)
private val LoseIndigo = Color(0xFF283593)
private val DrawAmber = Color(0xFFD84315)
private val TextPrimary = Color(0xFF1F1A17)
private val TextSecondary = Color(0xFF6D625B)
private val DividerColor = Color(0xFFE0D9CC)

@Composable
fun JankenScreen(
    gameState: JankenGameState = rememberJankenGameState(),
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier
) {
    val evaluation = gameState.roundEvaluation

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(TableBackgroundTop, TableBackgroundBottom)
                )
            )
    ) {
        JankenHeader(
            selectedPlayerCount = gameState.playerCount,
            onSelectPlayerCount = gameState::selectPlayerCount
        )

        // 2. 中央アリーナ領域 (CPUの手 / 画面中央の勝敗結果 &「もう一度」ボタン / ユーザーの選んだ手)
        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            val cpuCount = gameState.cpuCount
            val heightBasedCardHeight = ((maxHeight - 176.dp) / 2f).coerceIn(64.dp, 124.dp)
            val heightBasedCardWidth = heightBasedCardHeight * CARD_ASPECT_RATIO
            val widthBasedCpuCardWidth =
                ((maxWidth - (8.dp * (cpuCount - 1))) / cpuCount).coerceAtMost(90.dp)
            val cpuCardWidth = min(heightBasedCardWidth, widthBasedCpuCardWidth)
            val userCardWidth = min(heightBasedCardWidth, 88.dp)

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 上段: CPUたちの手（選択前は非表示＝裏面、選択後にそれぞれの手を表示）
                CpuHandsSection(
                    cpuCount = cpuCount,
                    evaluation = evaluation,
                    cardWidth = cpuCardWidth
                )

                // 中央: 勝敗結果（勝ち・負け・あいこ）と「もう一度」ボタン
                CenterResultSection(
                    evaluation = evaluation,
                    cpuCount = cpuCount,
                    onPlayAgain = gameState::playAgain
                )

                // 下段(アリーナ内): ユーザーが選択した手の表示領域
                UserChosenHandSection(
                    evaluation = evaluation,
                    cardWidth = userCardWidth
                )
            }
        }

        // 3. 画面下部: グー・チョキ・パーの選択カード一覧
        BottomHandPickerBar(
            selectedHand = evaluation?.userHand,
            enabled = evaluation == null,
            onSelectHand = gameState::selectHand
        )
    }
}

@Composable
private fun JankenHeader(
    selectedPlayerCount: Int,
    onSelectPlayerCount: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = HeaderSurfaceColor,
        shadowElevation = 2.dp
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Image(
                    painter = painterResource(id = R.drawable.janken),
                    contentDescription = "じゃんけん",
                    modifier = Modifier
                        .height(60.dp)
                        .testTag("header_logo"),
                    contentScale = ContentScale.Fit
                )

                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "対戦人数",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFF2EFE9),
                        border = BorderStroke(1.dp, DividerColor)
                    ) {
                        Row(
                            modifier = Modifier.padding(2.dp),
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            for (count in JankenGameState.PLAYER_COUNT_OPTIONS) {
                                val isSelected = count == selectedPlayerCount
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (isSelected) AccentCrimson else Color.Transparent
                                        )
                                        .semantics { selected = isSelected }
                                        .clickable(
                                            role = Role.RadioButton,
                                            onClick = { onSelectPlayerCount(count) }
                                        )
                                        .testTag("player_count_$count")
                                        .padding(horizontal = 11.dp, vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$count",
                                        fontSize = 14.sp,
                                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                        color = if (isSelected) Color.White else TextPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }
            HorizontalDivider(thickness = 1.dp, color = DividerColor)
        }
    }
}

@Composable
private fun CpuHandsSection(
    cpuCount: Int,
    evaluation: RoundEvaluation?,
    cardWidth: Dp,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (evaluation == null) {
                "CPU選択待機中"
            } else {
                "CPUが選択した手"
            },
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (cpuIndex in 0 until cpuCount) {
                val cpuHand = evaluation?.cpuHands?.getOrNull(cpuIndex)
                val isWinner = evaluation?.isCpuWinner(cpuIndex) == true

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.testTag("cpu_slot_${cpuIndex + 1}")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = "CPU ${cpuIndex + 1}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isWinner) AccentCrimson else TextPrimary
                        )
                        if (isWinner) {
                            WinBadge()
                        }
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    AnimatedContent(
                        targetState = cpuHand,
                        transitionSpec = {
                            (fadeIn() + scaleIn(initialScale = 0.88f))
                                .togetherWith(fadeOut())
                        },
                        label = "cpu_card_${cpuIndex + 1}"
                    ) { revealedHand ->
                        if (revealedHand == null) {
                            UnrevealedCardBack(width = cardWidth)
                        } else {
                            RevealedHandCard(
                                hand = revealedHand,
                                width = cardWidth,
                                isWinner = isWinner,
                                contentDescription = "CPU ${cpuIndex + 1}の手: ${revealedHand.label}"
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = cpuHand?.label ?: "？",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (cpuHand != null) TextPrimary else TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun CenterResultSection(
    evaluation: RoundEvaluation?,
    cpuCount: Int,
    onPlayAgain: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = evaluation,
            contentAlignment = Alignment.Center,
            transitionSpec = {
                if (targetState != null) {
                    fadeIn(
                        animationSpec = tween(durationMillis = 250, delayMillis = 300)
                    ).togetherWith(
                        fadeOut(
                            animationSpec = tween(durationMillis = 200, delayMillis = 50)
                        )
                    ).using(null)
                } else {
                    fadeIn(animationSpec = snap())
                        .togetherWith(fadeOut(animationSpec = snap()))
                        .using(null)
                }
            },
            label = "center_result"
        ) { currentEval ->
            if (currentEval == null) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White.copy(alpha = 0.82f),
                    border = BorderStroke(1.dp, DividerColor)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "下のカードを選んで勝負！",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "あなた vs CPU ${cpuCount}人",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
            } else {
                val outcome = currentEval.userOutcome
                val (badgeColor, bgColor, borderColor) = when (outcome) {
                    MatchOutcome.WIN -> Triple(
                        AccentCrimson,
                        WinnerGoldLight,
                        WinnerGold
                    )
                    MatchOutcome.LOSE -> Triple(
                        LoseIndigo,
                        Color(0xFFEEF1FA),
                        LoseIndigo.copy(alpha = 0.35f)
                    )
                    MatchOutcome.DRAW -> Triple(
                        DrawAmber,
                        Color(0xFFFFF3E8),
                        DrawAmber.copy(alpha = 0.35f)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = bgColor,
                        border = BorderStroke(2.dp, borderColor),
                        shadowElevation = 4.dp
                    ) {
                        Text(
                            text = outcome.label,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = badgeColor,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .testTag("result_text")
                                .padding(horizontal = 36.dp, vertical = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = onPlayAgain,
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CardDarkBg,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 28.dp, vertical = 8.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp),
                        modifier = Modifier.testTag("play_again_button")
                    ) {
                        Text(
                            text = "もう一度",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UserChosenHandSection(
    evaluation: RoundEvaluation?,
    cardWidth: Dp,
    modifier: Modifier = Modifier
) {
    val userHand = evaluation?.userHand
    val isUserWinner = evaluation?.userOutcome == MatchOutcome.WIN

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "あなたの手",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isUserWinner) AccentCrimson else TextPrimary
            )
            if (isUserWinner) {
                WinBadge()
            }
        }

        Spacer(modifier = Modifier.height(3.dp))

        AnimatedContent(
            targetState = userHand,
            transitionSpec = {
                (fadeIn() + scaleIn(initialScale = 0.88f)).togetherWith(fadeOut())
            },
            label = "user_chosen_card"
        ) { chosen ->
            if (chosen == null) {
                EmptyUserSlot(width = cardWidth)
            } else {
                RevealedHandCard(
                    hand = chosen,
                    width = cardWidth,
                    isWinner = isUserWinner,
                    contentDescription = "あなたの選んだ手: ${chosen.label}",
                    modifier = Modifier.testTag("user_played_card")
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = userHand?.label ?: "未選択",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (userHand != null) TextPrimary else TextSecondary
        )
    }
}

@Composable
private fun BottomHandPickerBar(
    selectedHand: JankenHand?,
    enabled: Boolean,
    onSelectHand: (JankenHand) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = HeaderSurfaceColor,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (enabled) {
                    "出すカードをタップして選択"
                } else {
                    "「もう一度」で次の勝負へ"
                },
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (enabled) AccentCrimson else TextSecondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                JankenHand.entries.forEach { hand ->
                    val isThisSelected = selectedHand == hand
                    val cardAlpha = when {
                        selectedHand == null -> 1f
                        isThisSelected -> 1f
                        else -> 0.38f
                    }
                    val cardScale by animateFloatAsState(
                        targetValue = if (isThisSelected) 1.05f else 1f,
                        label = "picker_scale_${hand.name}"
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .width(86.dp)
                            .alpha(cardAlpha)
                            .scale(cardScale)
                            .offset(y = if (isThisSelected) (-3).dp else 0.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(CARD_ASPECT_RATIO)
                                .shadow(
                                    elevation = if (enabled || isThisSelected) 4.dp else 1.dp,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clip(RoundedCornerShape(10.dp))
                                .border(
                                    width = if (isThisSelected) 2.5.dp else 1.dp,
                                    color = if (isThisSelected) AccentCrimson else CardDarkBg,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable(
                                    enabled = enabled,
                                    role = Role.Button,
                                    onClick = { onSelectHand(hand) }
                                )
                                .testTag("hand_card_${hand.name}")
                        ) {
                            Image(
                                painter = painterResource(id = hand.drawableRes),
                                contentDescription = hand.label,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = hand.label,
                            fontSize = 13.sp,
                            fontWeight = if (isThisSelected) FontWeight.ExtraBold else FontWeight.Bold,
                            color = if (isThisSelected) AccentCrimson else TextPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RevealedHandCard(
    hand: JankenHand,
    width: Dp,
    isWinner: Boolean,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(width)
            .aspectRatio(CARD_ASPECT_RATIO)
            .shadow(
                elevation = if (isWinner) 6.dp else 3.dp,
                shape = RoundedCornerShape(8.dp)
            )
            .clip(RoundedCornerShape(8.dp))
            .border(
                width = if (isWinner) 2.5.dp else 1.dp,
                color = if (isWinner) WinnerGold else CardDarkBg,
                shape = RoundedCornerShape(8.dp)
            )
    ) {
        Image(
            painter = painterResource(id = hand.drawableRes),
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
private fun UnrevealedCardBack(
    width: Dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(width)
            .aspectRatio(CARD_ASPECT_RATIO)
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .background(CardDarkBg)
            .border(1.dp, CardInnerBorder, RoundedCornerShape(8.dp))
            .padding(5.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(1.dp, CardInnerBorder.copy(alpha = 0.55f), RoundedCornerShape(5.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "?",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = CardInnerBorder
            )
        }
    }
}

@Composable
private fun EmptyUserSlot(
    width: Dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(width)
            .aspectRatio(CARD_ASPECT_RATIO)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White.copy(alpha = 0.45f))
            .border(1.5.dp, DividerColor, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "CARD",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun WinBadge() {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = WinnerGold
    ) {
        Text(
            text = "WIN",
            fontSize = 9.sp,
            fontWeight = FontWeight.ExtraBold,
            color = CardDarkBg,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
        )
    }
}

@Preview(showBackground = true, widthDp = 393, heightDp = 808)
@Composable
private fun JankenScreenBeforeSelectionPreview() {
    Kadai7Theme {
        JankenScreen(
            gameState = JankenGameState(initialPlayerCount = 4)
        )
    }
}

@Preview(showBackground = true, widthDp = 393, heightDp = 808)
@Composable
private fun JankenScreenAfterSelectionPreview() {
    Kadai7Theme {
        val eval = JankenJudge.evaluateForUser(
            userHand = JankenHand.ROCK,
            cpuHands = listOf(JankenHand.SCISSORS, JankenHand.ROCK, JankenHand.SCISSORS)
        )
        JankenScreen(
            gameState = JankenGameState(
                initialPlayerCount = 4,
                initialEvaluation = eval
            )
        )
    }
}
