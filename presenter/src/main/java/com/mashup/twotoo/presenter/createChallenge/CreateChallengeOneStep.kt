package com.mashup.twotoo.presenter.createChallenge

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mashup.twotoo.presenter.R
import com.mashup.twotoo.presenter.createChallenge.model.ChallengeInfoModel
import com.mashup.twotoo.presenter.createChallenge.recommendChallenge.RecommendChallengeBottomSheet
import com.mashup.twotoo.presenter.designsystem.component.button.TwoTooTextButton
import com.mashup.twotoo.presenter.designsystem.component.textfield.TwoTooTextField
import com.mashup.twotoo.presenter.designsystem.theme.TwoTooTheme
import com.mashup.twotoo.presenter.util.DateFormatter
import kotlinx.coroutines.launch

@Composable
fun CreateChallengeOneStep(
    state: ChallengeInfoModel = ChallengeInfoModel(),
    onClickNext: (String, String, String) -> Unit
) {
    Column(
        modifier = Modifier.padding(top = 12.dp),
    ) {
        var challengeName by remember { mutableStateOf(state.challengeName) }
        var startDate by remember { mutableStateOf(state.startDate) }
        var endDate by remember { mutableStateOf(state.endDate) }
        val context = LocalContext.current

        InputChallengeName(challengeName, onTextValueChanged = { challengeName = it })
        RecommendChallengeButton { clickItem ->
            challengeName = context.resources.getString(clickItem)
        }
        SettingChallengeDate(startDate, endDate) { selectedStartDate, selectedEndDate ->
            startDate = selectedStartDate
            endDate = selectedEndDate
        }

        Spacer(Modifier.weight(1f))
        TwoTooTextButton(
            text = stringResource(id = R.string.button_next),
            enabled = challengeName.isNotEmpty(),
            modifier = Modifier
                .fillMaxWidth()
                .height(57.dp),
            onClick = {
                if (challengeName.isNotEmpty()) {
                    onClickNext(challengeName, startDate, endDate)
                }
            },
        )
        Spacer(modifier = Modifier.height(55.dp))
    }
}

@Composable
fun InputChallengeName(text: String, onTextValueChanged: (String) -> Unit) {
    Column {
        Text(
            text = stringResource(id = R.string.challenge_name),
            style = TwoTooTheme.typography.bodyNormal16,
            color = TwoTooTheme.color.mainBrown,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        TwoTooTextField(
            modifier = Modifier.fillMaxWidth(),
            text = text,
            textHint = stringResource(id = R.string.input_challenge_name_placeholder),
            updateText = { updateText -> onTextValueChanged(updateText) },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecommendChallengeButton(
    onClickItemName: (Int) -> Unit
) {
    var isBottomSheetVisible by rememberSaveable { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(true)
    val scope = rememberCoroutineScope()
    val closeSheet: () -> Unit = { scope.launch { sheetState.hide() } }
    val focusManager = LocalFocusManager.current

    if (isBottomSheetVisible) {
        RecommendChallengeBottomSheet(
            onClickItemName = {
                    item ->
                onClickItemName(item)
                closeSheet.invoke()
                isBottomSheetVisible = false
                focusManager.clearFocus()
            },
            sheetState = sheetState,
            onDismiss = {
                closeSheet.invoke()
                isBottomSheetVisible = false
            },
        )
    }

    Button(
        modifier = Modifier.padding(top = 16.dp, bottom = 40.dp),
        shape = TwoTooTheme.shape.medium,
        colors = ButtonDefaults.buttonColors(containerColor = TwoTooTheme.color.mainBrown),
        onClick = { isBottomSheetVisible = true },
    ) {
        Text(
            modifier = Modifier.padding(vertical = 8.dp),
            text = stringResource(id = R.string.recommend_challenge),
            style = TwoTooTheme.typography.bodyNormal14,
            color = TwoTooTheme.color.mainWhite,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingChallengeDate(
    initStartDate: String,
    initEndDate: String,
    period: (String, String) -> Unit,
) {
    var selectedStartDate by remember { mutableStateOf(initStartDate) }
    var endDate by remember { mutableStateOf(initEndDate) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())
    var isShowDatePickerVisible by rememberSaveable { mutableStateOf(false) }

    if (isShowDatePickerVisible) {
        DatePickerDialog(
            shape = RoundedCornerShape(12.dp),
            onDismissRequest = { },
            confirmButton = {
                TextButton(onClick = {
                    val date = datePickerState.selectedDateMillis
                    selectedStartDate = DateFormatter.convertToLongDate(date)
                    endDate = DateFormatter.getDaysAfter(selectedStartDate)
                    isShowDatePickerVisible = !isShowDatePickerVisible
                    period(selectedStartDate, endDate)
                }) {
                    Text(stringResource(id = R.string.button_confirm))
                }
            },
        ) {
            DatePicker(
                state = datePickerState,
            )
        }
    }

    Row {
        Column {
            Text(
                text = stringResource(id = R.string.challenge_start_date),
                style = TwoTooTheme.typography.bodyNormal16,
                color = TwoTooTheme.color.mainBrown,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            Text(
                text = selectedStartDate,
                style = TwoTooTheme.typography.bodyNormal16,
                color = TwoTooTheme.color.gray600,
                modifier = Modifier
                    .drawBehind {
                        drawRoundRect(
                            Color.White,
                            cornerRadius = CornerRadius(8.dp.toPx()),
                        )
                    }
                    .padding(15.dp)
                    .clickable {
                        isShowDatePickerVisible = !isShowDatePickerVisible
                    },
            )
        }
        Spacer(modifier = Modifier.width(32.dp))
        Column {
            Text(
                text = stringResource(id = R.string.challenge_end_date),
                style = TwoTooTheme.typography.bodyNormal16,
                color = TwoTooTheme.color.mainBrown,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            Text(
                text = endDate,
                style = TwoTooTheme.typography.bodyNormal16,
                color = TwoTooTheme.color.gray600,
                modifier = Modifier
                    .drawBehind {
                        drawRoundRect(
                            Color.White,
                            cornerRadius = CornerRadius(8.dp.toPx()),
                        )
                    }
                    .padding(15.dp),
            )
        }
    }
}

@Preview
@Composable
private fun PreviewDate() {
    CreateChallengeOneStep() { name, start, end -> }
}
