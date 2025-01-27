package com.mashup.twotoo.presenter.history.model

import com.mashup.twotoo.presenter.util.DateFormatter
import model.challenge.response.ChallengeResponseDomainModel
import java.util.Date

data class ChallengeInfoUiModel(
    val challengeNo: Int = 0,
    val day: Int = 0,
    val name: String = "",
    val detail: String = "",
) {
    companion object {
        val default = ChallengeInfoUiModel(
            0,
            1,
            "30분 이상 운동하기",
            "운동 사진으로 인증하기\n인증 실패하는지 확인",
        )

        fun from(challenge: ChallengeResponseDomainModel): ChallengeInfoUiModel {
            return ChallengeInfoUiModel(
                challengeNo = challenge.challengeNo,
                day = toDday(challenge.endDate, challenge.isFinished),
                name = challenge.name,
                detail = challenge.description,
            )
        }

        // Todo D-day end시간 기준 확인하기
        fun toDday(endDate: String, isFinished: Boolean): Int = if (isFinished) {
            0
        } else {
            val Dday = DateFormatter.getDateTimeByStr(endDate).time - Date().time
            (Dday / (1000 * 60 * 60 * 24)).toInt()
        }
    }
}
