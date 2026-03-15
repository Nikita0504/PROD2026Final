package com.fruits.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.fruits.domain.model.user.User
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ProfileRoute(
    viewModel: ProfileViewModel = koinViewModel(),
    onShowSnackbar: (String) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val effect by viewModel.effect.collectAsStateWithLifecycle(initialValue = null)

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.onEvent(ProfileEvent.PickImage(it)) }
    }

    LaunchedEffect(effect) {
        when (val e = effect) {
            is ProfileEffect.ShowErrorSnackbar -> onShowSnackbar(e.message)
            is ProfileEffect.ShowSuccessSnackbar -> onShowSnackbar("Успешно сохранено")
            null -> Unit
        }
    }

    ProfileScreen(
        state = state,
        onEvent = viewModel::onEvent,
        onAddImageClick = { launcher.launch("image/*") }
    )
}

@Composable
private fun ProfileScreen(
    state: ProfileState,
    onEvent: (ProfileEvent) -> Unit,
    onAddImageClick: () -> Unit
) {
    if (state.isLoading) {
        LoadingState()
        return
    }

    if (state.error != null) {
        ErrorState(
            error = state.error,
            onRetry = { onEvent(ProfileEvent.Refresh) }
        )
        return
    }

    if (state.user == null) {
        EmptyState()
        return
    }

    LazyColumn(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            Spacer(Modifier.height(32.dp))
        }

        item {
            AvatarSection()
        }

        item {
            UserInfoSection(user = state.user)
        }

        item {
            DescriptionField(
                value = state.editedDescription,
                onValueChange = { onEvent(ProfileEvent.OnDescriptionChanged(it)) }
            )
        }

        item {
            PhotosSectionHeader()
        }

        item {
            PhotosGridSection(
                images = state.displayedImages + state.pendingImages,
                onRemove = { onEvent(ProfileEvent.RemoveImage(it)) },
                onRetry = { onEvent(ProfileEvent.RetryUpload(it)) },
                onAddImageClick = onAddImageClick
            )
        }

        if (state.hasChanges && !state.isSaving) {
            item {
                SaveButton(
                    onClick = { onEvent(ProfileEvent.SaveChanges) },
                    isEnabled = state.isFormValid
                )
            }
        }

        if (state.isSaving) {
            item {
                SavingIndicator()
            }
        }

        item {
            LogoutButton(onClick = { onEvent(ProfileEvent.Logout) })
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Загрузка профиля...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun ErrorState(
    error: String,
    onRetry: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Icon(
                Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = colorScheme.error,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Ошибка загрузки",
                style = MaterialTheme.typography.titleLarge,
                color = colorScheme.error
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onRetry,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Попробовать снова")
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.PersonOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Данные профиля не найдены",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AvatarSection() {
    Surface(
        modifier = Modifier.size(120.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer,
        tonalElevation = 4.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun UserInfoSection(user: User) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "${user.firstName} ${user.secondName}",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Text(
            text = user.email,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun DescriptionField(
    value: String,
    onValueChange: (String) -> Unit
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
private fun PhotosSectionHeader() {
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
private fun PhotosGridSection(
    images: List<ProfileImage>,
    onRemove: (String) -> Unit,
    onRetry: (String) -> Unit,
    onAddImageClick: () -> Unit
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
private fun SaveButton(
    onClick: () -> Unit,
    isEnabled: Boolean
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
private fun SavingIndicator() {
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
private fun LogoutButton(onClick: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = colorScheme.error
        )
    ) {
        Icon(
            Icons.Default.Logout,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Выйти из аккаунта",
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
private fun ImageItem(
    image: ProfileImage,
    onRemove: () -> Unit,
    onRetry: () -> Unit
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

@Preview
@Composable
private fun ProfileScreenPreview() {
    ProfileScreen(
        state = ProfileState(
            user = User(
                id = "id",
                firstName = "Tester",
                secondName = "Last name",
                email = "email@test.com",
                avatarFileKey = "no_avatar",
                readyToGive = true,
                description = "Description text",
                photoFileKeys = listOf()
            )
        ),
        onEvent = {},
        onAddImageClick = {}
    )
}