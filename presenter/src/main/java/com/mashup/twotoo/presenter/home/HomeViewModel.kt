package com.mashup.twotoo.presenter.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mashup.twotoo.presenter.designsystem.component.bottomsheet.BottomSheetData
import com.mashup.twotoo.presenter.home.di.HomeScope
import com.mashup.twotoo.presenter.home.mapper.toUiModel
import com.mashup.twotoo.presenter.home.model.AuthType
import com.mashup.twotoo.presenter.home.model.BeforeChallengeState
import com.mashup.twotoo.presenter.home.model.ChallengeState
import com.mashup.twotoo.presenter.home.model.HomeChallengeInfoModel
import com.mashup.twotoo.presenter.home.model.HomeCheerUiModel
import com.mashup.twotoo.presenter.home.model.HomeDialogType
import com.mashup.twotoo.presenter.home.model.HomeFlowerPartnerAndMeUiModel
import com.mashup.twotoo.presenter.home.model.HomeSideEffect
import com.mashup.twotoo.presenter.home.model.HomeStateUiModel
import com.mashup.twotoo.presenter.home.model.OngoingChallengeUiModel
import com.mashup.twotoo.presenter.home.model.ToastText
import com.mashup.twotoo.presenter.home.model.toUiModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import model.challenge.request.ChallengeNoRequestDomainModel
import model.commit.request.CheerRequestDomainModel
import model.commit.request.CommitNoRequestDomainModel
import model.commit.request.CommitRequestDomainModel
import model.notification.request.NotificationRequestDomainModel
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import usecase.challenge.FinishChallengeWithNoUseCase
import usecase.commit.CreateCheerUseCase
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
    private val createCheerUseCase: CreateCheerUseCase,
    private val stingUseCase: StingUseCase,
) : ViewModel(), ContainerHost<HomeStateUiModel, HomeSideEffect> {

    override val container: Container<HomeStateUiModel, HomeSideEffect> = container(
        initialState = HomeStateUiModel.init,
    )

    fun getHomeViewChallenge() = intent {
        getHomeViewUseCase().onSuccess { homeViewResponseDomainModel ->
            reduce {
                state.copy(
                    homeChallengeInfoModel = homeViewResponseDomainModel.onGoingChallenge.toUiModel(),
                    challengeStateUiModel = homeViewResponseDomainModel.toUiModel(),
                )
            }

            if (state.challengeStateUiModel is OngoingChallengeUiModel) { // todo 인덴트 줄이기
                with(state.challengeStateUiModel as OngoingChallengeUiModel) {
                    when (homeChallengeStateUiModel.challengeState) {
                        ChallengeState.Cheer -> {
                            val myCheerText = (homeChallengeStateUiModel.challengeStateUiModel as HomeCheerUiModel).partner.cheerText
                            if (!getVisibilityCheerDialogUseCase() && myCheerText.isBlank()) {
                                // 응원텍스트가 있다면 표시하지 않습니다.
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
                            if (this.authType == AuthType.AuthBoth) {
                                postSideEffect(
                                    HomeSideEffect.DismissBottomSheet,
                                )
                                delay(100)
                                postSideEffect(
                                    HomeSideEffect.OpenHomeDialog(
                                        type = HomeDialogType.Cheer,
                                    ),
                                )
                            } else {
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

    fun setInvisibleCheerDialogSideEffect() {
        viewModelScope.launch {
            setVisibilityCheerDialogUseCase(true)
        }
    }

    fun setInvisibleCompleteDialogSideEffect() {
        viewModelScope.launch {
            setVisibilityCompleteDialogUseCase(true)
        }
    }

    fun navigateToHistory(challengeNo: Int) = intent {
        postSideEffect(HomeSideEffect.NavigateToChallengeDetail(challengeNo))
    }

    fun openToShotBottomSheet() = intent {
        postSideEffect(HomeSideEffect.DismissBottomSheet)
        delay(100)
        postSideEffect(HomeSideEffect.OpenToShotBottomSheet)
    }

    fun openToAuthBottomSheet() = intent {
        postSideEffect(HomeSideEffect.DismissBottomSheet)
        delay(100)
        postSideEffect(HomeSideEffect.OpenToAuthBottomSheet)
    }

    fun openToCheerBottomSheet() = intent {
        postSideEffect(HomeSideEffect.DismissBottomSheet)
        delay(100)
        postSideEffect(HomeSideEffect.OpenToCheerBottomSheet)
    }

    fun onClickBeforeChallengeTextButton(beforeChallengeState: BeforeChallengeState) = intent {
        postSideEffect(HomeSideEffect.NavigationToCreateChallenge(beforeChallengeState, state.homeChallengeInfoModel))
    }

    fun onClickSendBottomSheetDataButton(bottomSheetData: BottomSheetData) = intent {
        when (bottomSheetData) {
            is BottomSheetData.AuthenticateData -> {
                createCommitUseCase(
                    commitRequestDomainModel = CommitRequestDomainModel(
                        text = bottomSheetData.text,
                        challengeNo = (this.state.challengeStateUiModel as OngoingChallengeUiModel).challengeNo.toString(),
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
                    postSideEffect(
                        HomeSideEffect.CallViewHomeApi,
                    )
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
                if ((state.challengeStateUiModel as OngoingChallengeUiModel).homeShotCountTextUiModel.count == 0) {
                    postSideEffect(
                        HomeSideEffect.DismissBottomSheet,
                    )
                    delay(100)
                    postSideEffect(
                        HomeSideEffect.Toast(
                            ToastText.ShotInvalid,
                        ),
                    )
                    return@intent
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

                    postSideEffect(
                        HomeSideEffect.CallViewHomeApi,
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
                val cheerText = bottomSheetData.text
                if (cheerText.isBlank()) {
                    postSideEffect(
                        HomeSideEffect.DismissBottomSheet,
                    )
                    postSideEffect(
                        HomeSideEffect.Toast(
                            ToastText.CheerFail,
                        ),
                    )
                    return@intent
                }
                val commitNo = (
                    (state.challengeStateUiModel as OngoingChallengeUiModel)
                        .homeChallengeStateUiModel.challengeStateUiModel as HomeCheerUiModel
                    ).partner.commitNo

                createCheerUseCase(
                    commitNoRequestDomainModel = CommitNoRequestDomainModel(commitNo = commitNo),
                    cheerRequestDomainModel = CheerRequestDomainModel(cheerText = cheerText),
                ).onSuccess {
                    postSideEffect(
                        HomeSideEffect.Toast(
                            ToastText.CheerSuccess,
                        ),
                    )
                    postSideEffect(
                        HomeSideEffect.RemoveVisibilityCheerDialog,
                    )
                    delay(100)
                    postSideEffect(
                        HomeSideEffect.CallViewHomeApi,
                    )
                }.onFailure {
                    postSideEffect(
                        HomeSideEffect.SetInVisibleCheerDialog,
                    )

                    postSideEffect(
                        HomeSideEffect.DismissBottomSheet,
                    )
                    postSideEffect(
                        HomeSideEffect.Toast(
                            ToastText.CheerFail,
                        ),
                    )
                }
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
            postSideEffect(HomeSideEffect.NavigationToCreateChallenge(BeforeChallengeState.TERMINATION, HomeChallengeInfoModel()))
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
