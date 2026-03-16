package com.fruits.onboarding

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun OnboardingRoute(
    viewModel: OnboardingViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val effect by viewModel.effect.collectAsStateWithLifecycle(initialValue = null)
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(effect) {
        when (val e = effect) {
            is OnboardingEffect.ShowError -> {
                snackbarHostState.showSnackbar(e.message)
            }

            null -> Unit
        }
    }

    OnboardingScreen(
        state = state,
        onEvent = viewModel::onEvent,
        onNavigateBack = viewModel::goBack,
        snackbarHostState = snackbarHostState
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OnboardingScreen(
    state: OnboardingState,
    onEvent: (OnboardingEvent) -> Unit,
    onNavigateBack: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val colorScheme = MaterialTheme.colorScheme
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onEvent(OnboardingEvent.PickImage(it)) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Завершение профиля",
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Назад",
                            tint = colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorScheme.surface,
                    titleContentColor = colorScheme.onSurface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = colorScheme.surface
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            DescriptionSection(
                value = state.description,
                onValueChange = { onEvent(OnboardingEvent.OnDescriptionChanged(it)) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            PhotosSection(
                selectedImages = state.selectedImages,
                onRemove = { onEvent(OnboardingEvent.RemoveImage(it)) },
                onRetry = { onEvent(OnboardingEvent.RetryUpload(it)) },
                onAddPhoto = { launcher.launch("image/*") }
            )

            Spacer(modifier = Modifier.height(32.dp))

            state.error?.let { error ->
                ErrorCard(error = error)
                Spacer(modifier = Modifier.height(16.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            SubmitButton(
                isLoading = state.isLoading,
                isEnabled = state.isFormValid && !state.isLoading,
                onClick = { onEvent(OnboardingEvent.SubmitForm) }
            )
        }
    }
}

@Composable
private fun ErrorCard(error: String) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colorScheme.errorContainer),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = colorScheme.onErrorContainer
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = error,
                color = colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun DescriptionSection(
    value: String,
    onValueChange: (String) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Text(
        text = "О себе",
        style = MaterialTheme.typography.titleMedium,
        color = colorScheme.onSurface,
    )
    Spacer(modifier = Modifier.height(8.dp))
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Краткое описание") },
        placeholder = { Text("Расскажите немного о себе...") },
        supportingText = {
            Text("${value.length} / 256")
        },
        isError = value.length > 256,
        modifier = Modifier.fillMaxWidth(),
        maxLines = 4,
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
private fun PhotosSection(
    selectedImages: List<OnboardingImage>,
    onRemove: (Uri) -> Unit,
    onRetry: (Uri) -> Unit,
    onAddPhoto: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Text(
        text = "Фотографии",
        style = MaterialTheme.typography.titleMedium,
        color = colorScheme.onSurface,
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = "Добавьте фото (от 256x256 до 2048x2048)",
        style = MaterialTheme.typography.bodySmall,
        color = colorScheme.onSurfaceVariant,
    )
    Spacer(modifier = Modifier.height(16.dp))

    if (selectedImages.isNotEmpty()) {
        FlowRow(
            horizontalArrangement = Arrangement.SpaceAround,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            selectedImages.forEach { img ->
                ImageCard(
                    uri = img.uri,
                    isLoading = img.isLoading,
                    onError = img.errorMessage,
                    onRemove = { onRemove(img.uri) },
                    onRetry = { onRetry(img.uri) },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(end = 12.dp)
                )
            }
        }
    }

    Surface(
        onClick = onAddPhoto,
        shape = RoundedCornerShape(16.dp),
        color = colorScheme.secondaryContainer,
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .padding(top = 12.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AddPhotoAlternate,
                    contentDescription = "Добавить фото",
                    tint = colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Добавить фото",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

@Composable
private fun SubmitButton(
    isLoading: Boolean,
    isEnabled: Boolean,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        enabled = isEnabled,
        shape = RoundedCornerShape(16.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = colorScheme.onPrimary,
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text("Сохранение...")
        } else {
            Text(
                text = "Завершить профиль",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
private fun ImageCard(
    uri: Uri,
    isLoading: Boolean,
    onError: String?,
    onRemove: () -> Unit,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(uri)
                .crossfade(true)
                .build(),
            contentDescription = "Preview",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            alpha = if (isLoading) 0.5f else 1f
        )

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (onError != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .size(20.dp)
                                .clickable { onRetry?.invoke() }
                        )
                    }
                }
            }
        }


        Box(
            contentAlignment = Alignment.TopEnd,
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            IconButton(
                onClick = onRemove,
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Удалить",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(22.dp)
                )
            }

        }

        if (onError != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f))
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Ошибка",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
