package com.mashup.twotoo.presenter.designsystem.component.bottomsheet

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.holix.android.bottomsheetdialog.compose.BottomSheetBehaviorProperties
import com.holix.android.bottomsheetdialog.compose.BottomSheetDialog
import com.holix.android.bottomsheetdialog.compose.BottomSheetDialogProperties
import com.mashup.twotoo.presenter.designsystem.component.bottomsheet.BottomSheetType.Authenticate
import com.mashup.twotoo.presenter.designsystem.component.bottomsheet.BottomSheetType.SendType
import com.mashup.twotoo.presenter.designsystem.component.bottomsheet.BottomSheetType.SendType.Cheer
import com.mashup.twotoo.presenter.designsystem.component.bottomsheet.BottomSheetType.SendType.Shot
import com.mashup.twotoo.presenter.designsystem.theme.TwoTooTheme
import kotlinx.coroutines.launch
import tech.thdev.compose.extensions.keyboard.state.MutableExKeyboardStateSource
import tech.thdev.compose.extensions.keyboard.state.foundation.removeFocusWhenKeyboardIsHidden
import tech.thdev.compose.extensions.keyboard.state.localowners.LocalMutableExKeyboardStateSourceOwner
import java.io.File
import java.util.Objects

@Composable
fun TwoTooBottomSheet(
    type: BottomSheetType,
    onDismiss: () -> Unit,
    onClickButton: (BottomSheetData) -> Unit = {},
) {
    when (type) {
        is Authenticate -> TwoTooAuthBottomSheet(
            type = type,
            onClickButton = onClickButton,
            onDismiss = onDismiss,
        )

        is SendType -> TwoTooSendMsgBottomSheet(
            type = type,
            onClickButton = onClickButton,
            onDismiss = onDismiss,
        )
    }
}

@Composable
fun TwoTooAuthBottomSheet(
    type: Authenticate,
    onClickButton: (BottomSheetData) -> Unit,
    onDismiss: () -> Unit,
) {
    var imageUri by remember {
        mutableStateOf<Uri?>(null)
    }
    val context = LocalContext.current
    val file by remember {
        mutableStateOf(File.createTempFile("IMG_", ".jpg", context.cacheDir))
    }
    LaunchedEffect(key1 = Unit) {
        file.deleteOnExit()
    }
    val uri = FileProvider.getUriForFile(
        Objects.requireNonNull(context),
        "com.mashup.twotoo.provider",
        file,
    )

    val imageCropLauncher = rememberLauncherForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            imageUri = result.uriContent
        } else {
            val exception = result.error
            // 후에 토스트 추가
        }
    }

    var setImageDialogVisible by remember { mutableStateOf(false) }

    val takePhotoFromCameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
    ) { success ->
        if (success) {
            val cropOptions = CropImageContractOptions(
                uri,
                CropImageOptions(),
            ).apply {
                setFixAspectRatio(true)
                setOutputUri(uri)
            }
            imageCropLauncher.launch(cropOptions)
            setImageDialogVisible = false
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { success ->
        if (success) {
            Toast.makeText(context, "Permission Granted", Toast.LENGTH_SHORT).show()
            setImageDialogVisible = true
        } else {
            Toast.makeText(context, "Permission Denied", Toast.LENGTH_SHORT).show()
        }
    }

    val takePhotoFromAlbumLauncher = rememberLauncherForActivityResult(
        contract =
        ActivityResultContracts.PickVisualMedia(),
    ) { photoUri: Uri? ->
        val cropOptions = CropImageContractOptions(
            photoUri,
            CropImageOptions(),
        ).apply {
            setFixAspectRatio(true)
            setOutputUri(uri)
        }
        if (photoUri != null) {
            imageCropLauncher.launch(cropOptions)
            setImageDialogVisible = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        TwoTooBottomSheetImpl(
            onDismiss = onDismiss,
        ) {
            AuthenticateContent(
                type = type,
                imageUri = imageUri,
                onClickPlusButton = {
                    val permissionCheckResult =
                        ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                    if (permissionCheckResult == PackageManager.PERMISSION_GRANTED) {
                        setImageDialogVisible = true
                    } else {
                        // Request a permission
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                },
                onClickButton = onClickButton,
                modifier = Modifier.removeFocusWhenKeyboardIsHidden().background(
                    color = TwoTooTheme.color.backgroundYellow,
                    shape = RoundedCornerShape(topStart = 60f, topEnd = 60f),
                ),
            )
        }
        if (setImageDialogVisible) {
            SetImageOptionDialog(
                onDismissRequest = { setImageDialogVisible = false },
                onClickCameraButton = {
                    takePhotoFromCameraLauncher.launch(uri)
                },
                onClickAlbumButton = {
                    takePhotoFromAlbumLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
                onClickDismissButton = { setImageDialogVisible = false },
            )
        }
    }
}

@Composable
fun TwoTooSendMsgBottomSheet(
    type: SendType,
    onDismiss: () -> Unit,
    onClickButton: (BottomSheetData) -> Unit = {},
) {
    TwoTooBottomSheetImpl(
        onDismiss = onDismiss,
    ) {
        SendMsgBottomSheetContent(
            modifier = Modifier.removeFocusWhenKeyboardIsHidden().background(
                color = TwoTooTheme.color.backgroundYellow,
                shape = RoundedCornerShape(topStart = 60f, topEnd = 60f),
            ),
            type = type,
            onClickButton = onClickButton,
        )
    }
}

@Composable
fun TwoTooBottomSheetImpl(
    onDismiss: () -> Unit,
    bottomSheetContent: @Composable () -> Unit,
) {
    BottomSheetDialog(
        onDismissRequest = onDismiss,
        properties = BottomSheetDialogProperties(
            behaviorProperties = BottomSheetBehaviorProperties(
                state = BottomSheetBehaviorProperties.State.Expanded,
            ),
        ),
    ) {
        CompositionLocalProvider(
            LocalMutableExKeyboardStateSourceOwner provides MutableExKeyboardStateSource(),
        ) {
            bottomSheetContent()
        }
    }
}

/**
 * 직접 실행하거나, Interactive Mode에서 확인이 가능합니다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "인증하기")
@Composable
fun OpenAuthenticate() {
    TwoTooTheme {
        Box(
            modifier = Modifier.fillMaxSize(),
        ) {
            var isBottomSheetVisible by rememberSaveable {
                mutableStateOf(false)
            }
            val bottomSheetState = rememberModalBottomSheetState(true)
            val coroutineScope = rememberCoroutineScope()
            Button(
                onClick = {
                    isBottomSheetVisible = !isBottomSheetVisible
                },
            ) {
                Text("열기 / 닫기")
            }
            if (isBottomSheetVisible) {
                TwoTooBottomSheet(
                    type = Authenticate(),
                    onDismiss = {
                        coroutineScope.launch {
                            bottomSheetState.hide()
                        }.invokeOnCompletion {
                            if (!bottomSheetState.isVisible) {
                                isBottomSheetVisible = !isBottomSheetVisible
                            }
                        }
                    },
                )
            }
        }
    }
}

/**
 * 직접 실행하거나, Interactive Mode에서 확인이 가능합니다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "벌로 콕 찌르기")
@Composable
private fun OpenShot() {
    TwoTooTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            var isBottomSheetVisible by rememberSaveable {
                mutableStateOf(false)
            }
            val bottomSheetState = rememberModalBottomSheetState(true)
            val coroutineScope = rememberCoroutineScope()
            Button(
                onClick = {
                    isBottomSheetVisible = !isBottomSheetVisible
                },
            ) {
                Text("열기 / 닫기")
            }
            if (isBottomSheetVisible) {
                TwoTooBottomSheet(
                    type = Shot(),
                    onDismiss = {
                        coroutineScope.launch {
                            bottomSheetState.hide()
                        }.invokeOnCompletion {
                            if (!bottomSheetState.isVisible) {
                                isBottomSheetVisible = !isBottomSheetVisible
                            }
                        }
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "오늘의 응원 한마디")
@Composable
private fun OpenCheer() {
    TwoTooTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            var isBottomSheetVisible by rememberSaveable {
                mutableStateOf(false)
            }
            val bottomSheetState = rememberModalBottomSheetState(true)
            val coroutineScope = rememberCoroutineScope()
            Button(
                onClick = {
                    isBottomSheetVisible = !isBottomSheetVisible
                },
            ) {
                Text("열기 / 닫기")
            }
            if (isBottomSheetVisible) {
                TwoTooBottomSheet(
                    type = Cheer(),
                    onDismiss = {
                        coroutineScope.launch {
                            bottomSheetState.hide()
                        }.invokeOnCompletion {
                            if (!bottomSheetState.isVisible) {
                                isBottomSheetVisible = !isBottomSheetVisible
                            }
                        }
                    },
                )
            }
        }
    }
}
