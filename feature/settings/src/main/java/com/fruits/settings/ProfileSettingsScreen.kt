package com.fruits.settings

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSettingsRoute(
    viewModel: ProfileSettingsViewModel = koinViewModel(),
    onShowSnackbar: (String) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val effect by viewModel.effect.collectAsStateWithLifecycle(initialValue = null)

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.onEvent(ProfileSettingsEvent.PickImage(it)) }
    }

    LaunchedEffect(effect) {
        when (val e = effect) {
            is ProfileSettingsEffect.ShowErrorSnackbar -> onShowSnackbar(e.message)
            is ProfileSettingsEffect.ShowSuccessSnackbar -> onShowSnackbar("Успешно сохранено")
            is ProfileSettingsEffect.NavigateBack -> onNavigateBack()
            null -> Unit
        }
    }

    ProfileSettingsScreen(
        state = state,
        onEvent = viewModel::onEvent,
        onAddImageClick = { launcher.launch("image/*") },
        onNavigateBack = onNavigateBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ProfileSettingsScreen(
    state: ProfileSettingsState,
    onEvent: (ProfileSettingsEvent) -> Unit,
    onAddImageClick: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Редактирование профиля") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = null,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { padding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        if (state.error != null && state.user == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = state.error,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                )
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            DescriptionField(
                value = state.editedDescription,
                onValueChange = { onEvent(ProfileSettingsEvent.OnDescriptionChanged(it)) },
            )
            Spacer(modifier = Modifier.height(16.dp))
            PhotosSectionHeader()
            Spacer(modifier = Modifier.height(8.dp))
            PhotosGridSection(
                images = state.displayedImages + state.pendingImages,
                onRemove = { onEvent(ProfileSettingsEvent.RemoveImage(it)) },
                onRetry = { onEvent(ProfileSettingsEvent.RetryUpload(it)) },
                onAddImageClick = onAddImageClick,
            )
            Spacer(modifier = Modifier.height(16.dp))
            if (state.hasChanges && !state.isSaving) {
                SaveButton(
                    onClick = { onEvent(ProfileSettingsEvent.SaveChanges) },
                    isEnabled = state.isFormValid,
                )
            }
            if (state.isSaving) {
                SavingIndicator()
            }
        }
    }
}

@Composable
internal fun DescriptionField(
    value: String,
    onValueChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("О себе") },
        placeholder = { Text("Расскажите немного о себе...") },
        modifier = Modifier.fillMaxWidth(),
        maxLines = 4,
        supportingText = {
            Text(
                text = "${value.length} / 256",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End
            )
        },
        shape = RoundedCornerShape(12.dp),
        isError = value.isBlank()
    )
}

@Composable
internal fun PhotosSectionHeader() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Фотографии",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Минимум 1 фото, максимум 5",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
internal fun PhotosGridSection(
    images: List<ProfileImage>,
    onRemove: (String) -> Unit,
    onRetry: (String) -> Unit,
    onAddImageClick: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme

    if (images.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Добавьте минимум 1 фотографию",
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.error
            )
        }
    } else {
        FlowRow(
            horizontalArrangement = Arrangement.Center,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            images.forEach { image ->
                ImageItem(
                    image = image,
                    onRemove = { onRemove(image.id) },
                    onRetry = { onRetry(image.id) }
                )
            }

            if (images.count { !it.isPending } < 5) {
                Surface(
                    onClick = onAddImageClick,
                    modifier = Modifier
                        .width(200.dp)
                        .aspectRatio(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = colorScheme.secondaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Добавить фото",
                                tint = colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Добавить",
                                style = MaterialTheme.typography.labelSmall,
                                color = colorScheme.onSecondaryContainer,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun SaveButton(
    onClick: () -> Unit,
    isEnabled: Boolean,
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(12.dp),
        enabled = isEnabled
    ) {
        Text(
            text = "Сохранить изменения",
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
internal fun SavingIndicator() {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = colorScheme.onPrimaryContainer,
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Сохранение...",
                style = MaterialTheme.typography.bodyLarge,
                color = colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
internal fun ImageItem(
    image: ProfileImage,
    onRemove: () -> Unit,
    onRetry: () -> Unit,
) {
    Box(
        modifier = Modifier
            .width(200.dp)
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(end = 8.dp)
    ) {
        if (image.uri != null) {
            AsyncImage(
                model = image.uri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                alpha = if (image.isLoading) 0.6f else 1f
            )
        }

        if (image.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { image.progress },
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        if (image.errorMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = "Retry",
                    modifier = Modifier
                        .clickable { onRetry() }
                        .size(24.dp),
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }

        IconButton(
            onClick = onRemove,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .background(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                    shape = CircleShape
                )
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Remove",
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

