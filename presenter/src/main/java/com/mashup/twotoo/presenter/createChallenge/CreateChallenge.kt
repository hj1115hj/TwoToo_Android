package com.mashup.twotoo.presenter.createChallenge

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mashup.twotoo.presenter.R
import com.mashup.twotoo.presenter.designsystem.component.button.TwoTooTextButton
import com.mashup.twotoo.presenter.designsystem.component.toolbar.TwoTooBackToolbar
import com.mashup.twotoo.presenter.designsystem.theme.TwoTooTheme

@Composable
fun CreateChallenge(
    stepNumber: Int,
    currentStepScreen: @Composable () -> Unit,
) {
    Scaffold(
        topBar = { TwoTooBackToolbar(onClickBackIcon = {}) },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .paint(
                    painterResource(id = R.drawable.image_background),
                    contentScale = ContentScale.FillBounds,
                ),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize().padding(horizontal = 24.dp),
            ) {
                Text(
                    text = stringResource(id = R.string.create_challenge_step, stepNumber),
                    textAlign = TextAlign.Left,
                    style = TwoTooTheme.typography.headLineNormal28,
                    color = TwoTooTheme.color.mainBrown,
                )
                Text(
                    text = stringResource(id = R.string.create_challenge_desc_1),
                    style = TwoTooTheme.typography.bodyNormal14,
                    color = TwoTooTheme.color.gray600,
                    modifier = Modifier.padding(top = 12.dp),
                )
                currentStepScreen()

                Spacer(Modifier.weight(1f))
                TwoTooTextButton(
                    text = stringResource(id = R.string.button_next),
                    enabled = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(57.dp),
                ) {}
                Spacer(modifier = Modifier.height(55.dp))
            }
        }
    }
}

@Preview
@Composable
private fun PreviewCreateChallengeOneStep() {
    CreateChallenge(stepNumber = 1) {
        CreateChallengeOneStep()
    }
}

@Preview
@Composable
private fun PreviewCreateChallengeTwoStep() {
    CreateChallenge(stepNumber = 2) {
        CreateChallengeTwoStep()
    }
}

@Preview
@Composable
private fun PreviewCreateChallengeThreeStep() {
    CreateChallenge(stepNumber = 3) {
        CreateChallengeCard(
            "하루 운동 30분 이상 하기",
            "2023/05/01 ~ 5/22",
            "운동사진으로 인증하기\n실패하는 사람은 뷔페 쏘기",
        )
    }
}