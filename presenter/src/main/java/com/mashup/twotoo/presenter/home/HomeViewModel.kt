package com.mashup.twotoo.presenter.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mashup.twotoo.presenter.designsystem.component.bottomsheet.BottomSheetData
import com.mashup.twotoo.presenter.home.di.HomeScope
import com.mashup.twotoo.presenter.home.mapper.toUiModel
import com.mashup.twotoo.presenter.home.model.AuthType
import com.mashup.twotoo.presenter.home.model.BeforeChallengeState
import com.mashup.twotoo.presenter.home.model.ChallengeState
import com.mashup.twotoo.presenter.home.model.HomeDialogType
import com.mashup.twotoo.presenter.home.model.HomeFlowerPartnerAndMeUiModel
import com.mashup.twotoo.presenter.home.model.HomeSideEffect
import com.mashup.twotoo.presenter.home.model.HomeStateUiModel
import com.mashup.twotoo.presenter.home.model.OngoingChallengeUiModel
import com.mashup.twotoo.presenter.home.model.ToastText
import kotlinx.coroutines.launch
import model.challenge.request.ChallengeNoRequestDomainModel
import model.commit.request.CommitRequestDomainModel
import model.notification.request.NotificationRequestDomainModel
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import usecase.challenge.FinishChallengeWithNoUseCase
import usecase.commit.CreateCommitUseCase
import usecase.notification.StingUseCase
import usecase.user.GetVisibilityCheerDialogUseCase
import usecase.user.GetVisibilityCompleteDialogUseCase
import usecase.user.RemoveVisibilityCheerDialogUseCase
import usecase.user.RemoveVisibilityCompleteDialogUseCase
import usecase.user.SetVisibilityCheerDialogUseCase
import usecase.user.SetVisibilityCompleteDialogUseCase
import usecase.view.GetViewHomeUseCase
import javax.inject.Inject

/**
 * @Created by 김현국 2023/06/23
 */

@HomeScope
class HomeViewModel @Inject constructor(
    private val getHomeViewUseCase: GetViewHomeUseCase,
    private val createCommitUseCase: CreateCommitUseCase,
    private val getVisibilityCheerDialogUseCase: GetVisibilityCheerDialogUseCase,
    private val getVisibilityCompleteDialogUseCase: GetVisibilityCompleteDialogUseCase,
    private val setVisibilityCheerDialogUseCase: SetVisibilityCheerDialogUseCase,
    private val setVisibilityCompleteDialogUseCase: SetVisibilityCompleteDialogUseCase,
    private val finishChallengeWithNoUseCase: FinishChallengeWithNoUseCase,
    private val removeVisibilityCheerDialogUseCase: RemoveVisibilityCheerDialogUseCase,
    private val removeVisibilityCompleteDialogUseCase: RemoveVisibilityCompleteDialogUseCase,
    private val stingUseCase: StingUseCase,
) : ViewModel(), ContainerHost<HomeStateUiModel, HomeSideEffect> {

    override val container: Container<HomeStateUiModel, HomeSideEffect> = container(HomeStateUiModel.before)

    fun getHomeViewChallenge() = intent {
        getHomeViewUseCase().onSuccess { homeViewResponseDomainModel ->
            reduce {
                state.copy(
                    challengeStateUiModel = homeViewResponseDomainModel.toUiModel(0),
                )
            }

            if (state.challengeStateUiModel is OngoingChallengeUiModel) { // todo 인덴트 줄이기
                with(state.challengeStateUiModel as OngoingChallengeUiModel) {
                    when (homeChallengeStateUiModel.challengeState) {
                        ChallengeState.Cheer -> {
                            if (!getVisibilityCheerDialogUseCase()) {
                                postSideEffect(HomeSideEffect.OpenHomeDialog(HomeDialogType.Cheer))
                            }
                        }

                        ChallengeState.Complete -> {
                            if (!getVisibilityCompleteDialogUseCase()) {
                                if (isBothBloom(this)) {
                                    postSideEffect(HomeSideEffect.OpenHomeDialog(HomeDialogType.Bloom))
                                } else {
                                    postSideEffect(HomeSideEffect.OpenHomeDialog(HomeDialogType.DoNotBloom))
                                }
                            }
                        }

                        ChallengeState.Auth -> with(homeChallengeStateUiModel.challengeStateUiModel as HomeFlowerPartnerAndMeUiModel) {
                            if (this.authType != AuthType.AuthBoth) {
                                postSideEffect(HomeSideEffect.RemoveVisibilityCheerDialog)
                            }
                        }
                    }
                }
            }
        }.onFailure {
            postSideEffect(HomeSideEffect.Toast(ToastText.LoadHomeFail))
        }
    }

    fun onClickCheerDialogNegativeButton() = intent {
        setVisibilityCheerDialogUseCase(true)
    }

    fun onClickCompleteDialogConfirmButton() = intent {
        setVisibilityCompleteDialogUseCase(true)
    }

    fun removeVisibilityCompleteDialogSideEffect() {
        viewModelScope.launch {
            removeVisibilityCompleteDialogUseCase()
        }
    }

    fun removeVisibilityCheerDialogSideEffect() {
        viewModelScope.launch {
            removeVisibilityCheerDialogUseCase()
        }
    }

    fun navigateToHistory() = intent {
        postSideEffect(HomeSideEffect.NavigateToChallengeDetail)
    }

    fun openToShotBottomSheet() = intent {
        postSideEffect(HomeSideEffect.OpenToShotBottomSheet)
    }

    fun openToAuthBottomSheet() = intent {
        postSideEffect(HomeSideEffect.OpenToAuthBottomSheet)
    }

    fun openToCheerBottomSheet() = intent {
        postSideEffect(HomeSideEffect.OpenToCheerBottomSheet)
    }

    fun onClickBeforeChallengeTextButton(beforeChallengeState: BeforeChallengeState) = intent {
        // TODO create Challenge navigation 연결
        when (beforeChallengeState) {
            BeforeChallengeState.EMPTY -> {
                postSideEffect(HomeSideEffect.NavigationToCreateChallenge)
            }
            BeforeChallengeState.REQUEST -> {
                // Todo 챌린지 확인 페이지 이동
            }
            BeforeChallengeState.RESPONSE -> {
                // Todo 챌린지 확인 페이지 이동 Step3
            }
            BeforeChallengeState.WAIT -> {
                // Todo 챌린지 확인 페이지 이동
            }
            BeforeChallengeState.TERMINATION -> {
                postSideEffect(HomeSideEffect.NavigationToCreateChallenge)
            }
        }
    }

    fun onClickSendBottomSheetDataButton(bottomSheetData: BottomSheetData) = intent {
        when (bottomSheetData) {
            is BottomSheetData.AuthenticateData -> {
                createCommitUseCase(
                    commitRequestDomainModel = CommitRequestDomainModel(
                        text = bottomSheetData.text,
                        img = bottomSheetData.image.toString(),
                    ),
                ).onSuccess { domainModel ->
                    postSideEffect(
                        HomeSideEffect.DismissBottomSheet,
                    )
                    postSideEffect(
                        HomeSideEffect.Toast(
                            ToastText.CommitSuccess,
                        ),
                    )
                    reduce {
                        val currentState = state.challengeStateUiModel as OngoingChallengeUiModel
                        currentState.let {
                            state.copy(
                                challengeStateUiModel = it.copy(
                                    shotInteractionState = true,
                                ),
                            )
                        }
                    }
                    // TODO GET VIEW API 재호출
                }
                    .onFailure {
                        postSideEffect(
                            HomeSideEffect.DismissBottomSheet,
                        )
                        postSideEffect(
                            HomeSideEffect.Toast(
                                ToastText.CommitFail,
                            ),
                        )
                    }
            }
            is BottomSheetData.ShotData -> {
                with((state.challengeStateUiModel as? OngoingChallengeUiModel)?.homeShotCountTextUiModel?.count) {
                    if ((this == null) || (this <= 0)) {
                        postSideEffect(
                            HomeSideEffect.Toast(
                                ToastText.ShotInvalid,
                            ),
                        )
                        return@intent
                    }
                }
                // 서버 데이터 전송
                stingUseCase(
                    notificationRequestDomainModel = NotificationRequestDomainModel(
                        message = bottomSheetData.text,
                    ),
                ).onSuccess {
                    postSideEffect(
                        HomeSideEffect.DismissBottomSheet,
                    )
                    postSideEffect(
                        HomeSideEffect.Toast(
                            ToastText.ShotSuccess,
                        ),
                    )
                }
                    .onFailure {
                        postSideEffect(
                            HomeSideEffect.Toast(
                                ToastText.ShotFail,
                            ),
                        )
                    }
            }
            is BottomSheetData.CheerData -> {
                // 서버 데이터 전송

                // toast sideEffect
                postSideEffect(
                    HomeSideEffect.Toast(
                        ToastText.CheerSuccess,
                    ),
                )
            }
        }
    }

    fun onClickCompleteButton(
        challengeNo: Int,
    ) = intent {
        finishChallengeWithNoUseCase(
            challengeNoRequestDomainModel = ChallengeNoRequestDomainModel(
                challengeNo = challengeNo,
            ),
        ).onSuccess {
            postSideEffect(HomeSideEffect.RemoveVisibilityCompleteDialog)
            postSideEffect(HomeSideEffect.NavigationToCreateChallenge) // todo 변경
        }.onFailure {
            postSideEffect(HomeSideEffect.Toast(ToastText.FinishFail))
        }
    }

    fun onWiggleAnimationEnd() = intent {
        reduce {
            val currentState = state.challengeStateUiModel as OngoingChallengeUiModel
            currentState.let {
                state.copy(
                    challengeStateUiModel = it.copy(
                        shotInteractionState = false,
                    ),
                )
            }
        }
    }

    private fun isBothBloom(state: OngoingChallengeUiModel): Boolean {
        val meProgress = state.homeGoalAchievePartnerAndMeUiModel.me.progress
        val partnerProgress = state.homeGoalAchievePartnerAndMeUiModel.partner.progress
        return meProgress >= 0.8f && partnerProgress >= 0.8f
    }
}
